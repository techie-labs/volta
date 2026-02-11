package io.techie.volta

import java.util.Locale

/**
 * Internal helper to read battery information from the underlying OS (Windows, macOS, Linux).
 *
 * Uses command-line tools (wmic, pmset, sysfs) to fetch data.
 */
internal object DesktopBatteryReader {

    data class Info(
        val level: Int? = null,
        val isCharging: Boolean? = null,
        val isPlugged: Boolean? = null,
        val isPowerSaving: Boolean = false,
        val isSafeMode: Boolean = false,
        val technology: String? = null,
        val cycleCount: Int? = null,
        val voltageMv: Int? = null,
        val chargeCounterUah: Long? = null,
        val remainingTimeMillis: Long? = null
    )

    fun read(): Info {
        return when {
            isWindows -> readWindows()
            isMac -> readMac()
            isLinux -> readLinux()
            else -> Info()
        }
    }

    // --- Windows Implementation ---

    private fun readWindows(): Info {
        var level: Int? = null
        var status: Int? = null
        var voltage: Int? = null
        var technology: String? = null
        var isSafeMode = false
        var isPowerSaving = false

        // 1. Battery Info (WMIC)
        runCatching {
            val output = executeCommand("wmic Path Win32_Battery Get EstimatedChargeRemaining,BatteryStatus,Voltage,Chemistry /FORMAT:LIST")
            level = parseValue(output, "EstimatedChargeRemaining")?.toIntOrNull()
            status = parseValue(output, "BatteryStatus")?.toIntOrNull()
            voltage = parseValue(output, "Voltage")?.toIntOrNull() // Usually in mV
            technology = parseValue(output, "Chemistry") // e.g., "LION"
        }

        val isCharging = status in 6..9
        val isPlugged = status != 1

        // 2. Safe Mode
        runCatching {
            val output = executeCommand("wmic computersystem get bootupstate /FORMAT:LIST")
            isSafeMode = output.contains("Fail-safe", ignoreCase = true)
        }

        // 3. Power Saving
        runCatching {
            val output = executeCommand("powercfg /getactivescheme")
            isPowerSaving = output.contains("saver", ignoreCase = true)
        }

        return Info(
            level = level,
            isCharging = isCharging,
            isPlugged = isPlugged,
            isPowerSaving = isPowerSaving,
            isSafeMode = isSafeMode,
            technology = technology,
            voltageMv = voltage
        )
    }

    // --- macOS Implementation ---

    private fun readMac(): Info {
        var level: Int? = null
        var isCharging = false
        var isPlugged = false
        var isPowerSaving = false
        var isSafeMode = false
        var cycleCount: Int? = null
        var remainingTime: Long? = null

        // 1. Battery & Power Source (pmset)
        runCatching {
            val output = executeCommand("pmset -g batt")
            level = Regex("(\\d+)%").find(output)?.groupValues?.get(1)?.toInt()
            isCharging = output.contains("charging;", ignoreCase = true)
            isPlugged = output.contains("AC Power", ignoreCase = true) || output.contains("AC attached", ignoreCase = true)

            // Parse remaining time (e.g., "2:30 remaining")
            val timeMatch = Regex("(\\d+):(\\d+) remaining").find(output)
            if (timeMatch != null) {
                val hours = timeMatch.groupValues[1].toLong()
                val minutes = timeMatch.groupValues[2].toLong()
                remainingTime = (hours * 60 + minutes) * 60 * 1000
            }
        }

        // 2. System Profiler (SPPowerDataType) for Cycle Count
        // This is heavy, so maybe don't run it every poll if performance is an issue.
        // For now, we include it for completeness.
        runCatching {
            val output = executeCommand("system_profiler SPPowerDataType")
            val cycleMatch = Regex("Cycle Count:\\s*(\\d+)").find(output)
            cycleCount = cycleMatch?.groupValues?.get(1)?.toInt()
        }

        // 3. Power Saving
        runCatching {
            val output = executeCommand("pmset -g")
            isPowerSaving = Regex("lowpowermode\\s+1").containsMatchIn(output)
        }

        // 4. Safe Mode
        runCatching {
            val output = executeCommand("sysctl -n kern.safe_boot")
            isSafeMode = output.trim() == "1"
        }

        return Info(
            level = level,
            isCharging = isCharging,
            isPlugged = isPlugged,
            isPowerSaving = isPowerSaving,
            isSafeMode = isSafeMode,
            cycleCount = cycleCount,
            remainingTimeMillis = remainingTime
        )
    }

    // --- Linux Implementation ---

    private fun readLinux(): Info {
        var level: Int? = null
        var isCharging = false
        var isPlugged = false
        var isSafeMode = false
        var technology: String? = null
        var voltage: Int? = null
        var chargeCounter: Long? = null
        var cycleCount: Int? = null

        // 1. Battery Info (sysfs)
        runCatching {
            level = readFile("/sys/class/power_supply/BAT0/capacity")?.toIntOrNull()
            val status = readFile("/sys/class/power_supply/BAT0/status") ?: ""
            isCharging = status.equals("Charging", ignoreCase = true)

            val acOnline = readFile("/sys/class/power_supply/AC/online")?.trim()
            isPlugged = acOnline == "1" || status.equals("Full", ignoreCase = true) || isCharging

            technology = readFile("/sys/class/power_supply/BAT0/technology")
            
            // Voltage is usually in microvolts, convert to millivolts
            val voltageUv = readFile("/sys/class/power_supply/BAT0/voltage_now")?.toIntOrNull()
            voltage = voltageUv?.div(1000)

            // Charge counter (charge_now) in microampere-hours
            chargeCounter = readFile("/sys/class/power_supply/BAT0/charge_now")?.toLongOrNull()

            cycleCount = readFile("/sys/class/power_supply/BAT0/cycle_count")?.toIntOrNull()
        }

        // 2. Safe Mode
        runCatching {
            val cmdline = readFile("/proc/cmdline") ?: ""
            isSafeMode = cmdline.contains("rescue") || cmdline.contains("single") || cmdline.contains("emergency")
        }

        return Info(
            level = level,
            isCharging = isCharging,
            isPlugged = isPlugged,
            isPowerSaving = false,
            isSafeMode = isSafeMode,
            technology = technology,
            voltageMv = voltage,
            chargeCounterUah = chargeCounter,
            cycleCount = cycleCount
        )
    }

    // --- Utilities ---

    private val isWindows get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")
    private val isMac get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")
    private val isLinux get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("nux")

    private fun executeCommand(command: String): String {
        val parts = command.split(" ")
        return ProcessBuilder(parts)
            .start()
            .inputStream
            .bufferedReader()
            .readText()
    }

    private fun readFile(path: String): String? {
        return try {
            val process = ProcessBuilder("cat", path).start()
            process.inputStream.bufferedReader().readText().trim()
        } catch (e: Exception) {
            null
        }
    }

    private fun parseValue(output: String, key: String): String? {
        return Regex("$key=(\\d+|\\w+)").find(output)?.groupValues?.get(1)
    }
}

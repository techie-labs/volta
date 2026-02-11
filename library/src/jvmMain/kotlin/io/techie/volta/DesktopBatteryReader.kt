package io.techie.volta

import java.util.Locale

/**
 * Internal helper to read battery information from the underlying OS (Windows, macOS, Linux).
 *
 * Uses command-line tools (wmic, pmset, sysfs, ioreg) to fetch data.
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
        val remainingTimeMillis: Long? = null,
        val chargingSource: String? = null,
        val temperatureC: Float? = null,
        val currentNowMa: Long? = null,
        val designCapacityMah: Long? = null,
        val maxCapacityMah: Long? = null
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
        var isCharging: Boolean? = null
        var isPlugged: Boolean? = null
        var isPowerSaving = false
        var isSafeMode = false
        var cycleCount: Int? = null
        var remainingTime: Long? = null
        var chargingSource: String? = null
        var voltageMv: Int? = null
        var currentNowMa: Long? = null
        var temperatureC: Float? = null
        var designCapacityMah: Long? = null
        var maxCapacityMah: Long? = null
        var technology: String? = null

        // 1. Basic Info via pmset (Preferred for Level & Status)
        // pmset is generally more reliable for user-facing status than raw ioreg
        runCatching {
            val output = executeCommand("pmset -g batt")
            level = Regex("(\\d+)%").find(output)?.groupValues?.get(1)?.toInt()
            
            val isDischarging = output.contains("discharging;", ignoreCase = true)
            val isChargingStr = output.contains("charging;", ignoreCase = true)
            
            isCharging = isChargingStr && !isDischarging
            isPlugged = output.contains("AC Power", ignoreCase = true) || output.contains("AC attached", ignoreCase = true)

            chargingSource = if (isPlugged) {
                "AC"
            } else {
                "Battery"
            }

            // Parse remaining time (e.g., "2:30 remaining")
            val timeMatch = Regex("(\\d+):(\\d+) remaining").find(output)
            if (timeMatch != null) {
                val hours = timeMatch.groupValues[1].toLong()
                val minutes = timeMatch.groupValues[2].toLong()
                remainingTime = (hours * 60 + minutes) * 60 * 1000
            }
        }

        // 2. Advanced Info via IOKit (ioreg)
        runCatching {
            val output = executeCommand("ioreg -r -n AppleSmartBattery -d 1")
            
            // Voltage (mV)
            voltageMv = Regex("\"Voltage\" = (\\d+)").find(output)?.groupValues?.get(1)?.toInt()
            
            // Current (mA)
            val amperageStr = Regex("\"Amperage\" = (-?\\d+)").find(output)?.groupValues?.get(1)
            if (amperageStr != null) {
                currentNowMa = amperageStr.toLongOrNull()
                if (currentNowMa != null && currentNowMa > 20000) {
                     currentNowMa = currentNowMa - 18446744073709551615UL.toLong() - 1
                }
            }

            // Temperature (Celsius)
            val tempRaw = Regex("\"Temperature\" = (\\d+)").find(output)?.groupValues?.get(1)?.toFloat()
            if (tempRaw != null) {
                temperatureC = tempRaw / 100f
            }

            // Cycle Count
            cycleCount = Regex("\"CycleCount\" = (\\d+)").find(output)?.groupValues?.get(1)?.toInt()

            // Capacity
            maxCapacityMah = Regex("\"MaxCapacity\" = (\\d+)").find(output)?.groupValues?.get(1)?.toLong()
            designCapacityMah = Regex("\"DesignCapacity\" = (\\d+)").find(output)?.groupValues?.get(1)?.toLong()
            
            // Fallback: Try AppleRawMaxCapacity if MaxCapacity is weird or missing
            if (maxCapacityMah == null || maxCapacityMah < 1000) {
                 val rawMax = Regex("\"AppleRawMaxCapacity\" = (\\d+)").find(output)?.groupValues?.get(1)?.toLong()
                 if (rawMax != null && rawMax > 0) {
                     maxCapacityMah = rawMax
                 }
            }

            // Only use ioreg for level/status if pmset failed (level is null)
            if (level == null) {
                val currentCap = Regex("\"CurrentCapacity\" = (\\d+)").find(output)?.groupValues?.get(1)?.toDouble()
                if (currentCap != null && maxCapacityMah != null && maxCapacityMah > 0) {
                    level = ((currentCap / maxCapacityMah) * 100).toInt()
                }
                
                val isChargingRaw = Regex("\"IsCharging\" = (Yes|No)").find(output)?.groupValues?.get(1)
                if (isChargingRaw != null) {
                    isCharging = isChargingRaw == "Yes"
                }
                
                val externalConnected = Regex("\"ExternalConnected\" = (Yes|No)").find(output)?.groupValues?.get(1)
                if (externalConnected != null) {
                    isPlugged = externalConnected == "Yes"
                    chargingSource = if (isPlugged) "AC" else "Battery"
                }
            }
            
            technology = "Li-ion" 
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
        
        // Filter suspicious capacity
        val finalMaxCapacity = if (maxCapacityMah != null && maxCapacityMah < 1000) null else maxCapacityMah

        return Info(
            level = level,
            isCharging = isCharging,
            isPlugged = isPlugged,
            isPowerSaving = isPowerSaving,
            isSafeMode = isSafeMode,
            cycleCount = cycleCount,
            remainingTimeMillis = remainingTime,
            chargingSource = chargingSource,
            voltageMv = voltageMv,
            currentNowMa = currentNowMa,
            temperatureC = temperatureC,
            technology = technology,
            chargeCounterUah = finalMaxCapacity?.times(1000),
            designCapacityMah = designCapacityMah,
            maxCapacityMah = finalMaxCapacity
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
        } catch (_: Exception) {
            null
        }
    }

    private fun parseValue(output: String, key: String): String? {
        return Regex("$key=(\\d+|\\w+)").find(output)?.groupValues?.get(1)
    }
}

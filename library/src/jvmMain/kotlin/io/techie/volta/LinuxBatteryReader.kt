package io.techie.volta

/**
 * Linux implementation of [PlatformBatteryReader].
 *
 * This class retrieves battery information on Linux systems by reading from the standard
 * `sysfs` power supply interface located at `/sys/class/power_supply/`.
 *
 * It assumes the primary battery is named `BAT0` and the AC adapter is named `AC`.
 * Note: Battery names might vary (e.g., `BAT1`) on some devices, which is a known limitation.
 */
internal class LinuxBatteryReader : PlatformBatteryReader {

    override fun read(): DesktopBatteryReader.Info {
        var level: Int? = null
        var isCharging = false
        var isPlugged = false
        var isSafeMode = false
        var technology: String? = null
        var voltage: Int? = null
        var chargeCounter: Long? = null
        var cycleCount: Int? = null

        // 1. Battery Info (sysfs)
        // Reads standard files exposed by the kernel power supply subsystem.
        runCatching {
            // Capacity: 0-100%
            level = ShellUtils.readFile("/sys/class/power_supply/BAT0/capacity")?.toIntOrNull()
            
            // Status: "Charging", "Discharging", "Full", "Not charging", "Unknown"
            val status = ShellUtils.readFile("/sys/class/power_supply/BAT0/status") ?: ""
            isCharging = status.equals("Charging", ignoreCase = true)

            // AC Online: 1 = Connected, 0 = Disconnected
            val acOnline = ShellUtils.readFile("/sys/class/power_supply/AC/online")?.trim()
            isPlugged = acOnline == "1" || status.equals("Full", ignoreCase = true) || isCharging

            // Technology: e.g., "Li-ion"
            technology = ShellUtils.readFile("/sys/class/power_supply/BAT0/technology")
            
            // Voltage: usually in microvolts (µV), convert to millivolts (mV)
            val voltageUv = ShellUtils.readFile("/sys/class/power_supply/BAT0/voltage_now")?.toIntOrNull()
            voltage = voltageUv?.div(1000)

            // Charge Counter (charge_now): in microampere-hours (µAh)
            chargeCounter = ShellUtils.readFile("/sys/class/power_supply/BAT0/charge_now")?.toLongOrNull()

            // Cycle Count: Number of charge cycles
            cycleCount = ShellUtils.readFile("/sys/class/power_supply/BAT0/cycle_count")?.toIntOrNull()
        }

        // 2. Safe Mode Detection
        // Checks the kernel command line arguments in /proc/cmdline.
        // Keywords like "rescue", "single", or "emergency" indicate single-user/safe mode.
        runCatching {
            val cmdline = ShellUtils.readFile("/proc/cmdline") ?: ""
            isSafeMode = cmdline.contains("rescue") || cmdline.contains("single") || cmdline.contains("emergency")
        }

        return DesktopBatteryReader.Info(
            level = level,
            isCharging = isCharging,
            isPlugged = isPlugged,
            isPowerSaving = false, // Power saving detection varies wildly on Linux (TLP, power-profiles-daemon, etc.)
            isSafeMode = isSafeMode,
            technology = technology,
            voltageMv = voltage,
            chargeCounterUah = chargeCounter,
            cycleCount = cycleCount
        )
    }
}

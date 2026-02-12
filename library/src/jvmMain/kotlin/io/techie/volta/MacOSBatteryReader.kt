package io.techie.volta

/**
 * macOS implementation of [PlatformBatteryReader].
 *
 * This class retrieves battery information on macOS systems using two primary tools:
 * 1. `pmset`: Used for basic, user-facing status (Level, Charging State, Time Remaining).
 *    This is prioritized because it aligns with what the user sees in the menu bar.
 * 2. `ioreg`: Used for advanced diagnostics (Voltage, Current, Temperature, Cycle Count).
 *    This provides raw hardware data from the AppleSmartBattery driver.
 */
internal class MacOSBatteryReader : PlatformBatteryReader {

    override fun read(): DesktopBatteryReader.Info {
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
        // We prioritize pmset for status because ioreg can sometimes report conflicting
        // or raw states that don't match the UI (e.g., "Not Charging" vs "Discharging").
        runCatching {
            val output = ShellUtils.execute("pmset -g batt")
            level = Regex("(\\d+)%").find(output)?.groupValues?.get(1)?.toInt()
            
            // Determine charging status
            // "charging;" -> Charging
            // "discharging;" -> Discharging
            // "AC attached" -> Plugged in
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
        // Queries the AppleSmartBattery registry entry for detailed hardware stats.
        runCatching {
            val output = ShellUtils.execute("ioreg -r -n AppleSmartBattery -d 1")
            
            // Voltage (mV)
            voltageMv = Regex("\"Voltage\" = (\\d+)").find(output)?.groupValues?.get(1)?.toInt()
            
            // Current (mA)
            // Can be negative (discharging) or positive (charging).
            // Sometimes returned as a large unsigned integer for negative values.
            val amperageStr = Regex("\"Amperage\" = (-?\\d+)").find(output)?.groupValues?.get(1)
            if (amperageStr != null) {
                currentNowMa = amperageStr.toLongOrNull()
                // Handle unsigned 64-bit wrap-around for negative values
                if (currentNowMa != null && currentNowMa > 20000) {
                     currentNowMa = currentNowMa - 18446744073709551615UL.toLong() - 1
                }
            }

            // Temperature (Celsius * 100)
            val tempRaw = Regex("\"Temperature\" = (\\d+)").find(output)?.groupValues?.get(1)?.toFloat()
            if (tempRaw != null) {
                temperatureC = tempRaw / 100f
            }

            // Cycle Count
            cycleCount = Regex("\"CycleCount\" = (\\d+)").find(output)?.groupValues?.get(1)?.toInt()

            // Capacity (mAh)
            maxCapacityMah = Regex("\"MaxCapacity\" = (\\d+)").find(output)?.groupValues?.get(1)?.toLong()
            designCapacityMah = Regex("\"DesignCapacity\" = (\\d+)").find(output)?.groupValues?.get(1)?.toLong()
            
            // Fallback: Try AppleRawMaxCapacity if MaxCapacity is weird or missing (common on Apple Silicon)
            if (maxCapacityMah == null || maxCapacityMah < 1000) {
                 val rawMax = Regex("\"AppleRawMaxCapacity\" = (\\d+)").find(output)?.groupValues?.get(1)?.toLong()
                 if (rawMax != null && rawMax > 0) {
                     maxCapacityMah = rawMax
                 }
            }

            // Fallback for Level & Status (only if pmset failed)
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
            
            // Apple batteries are generally Li-ion / Li-poly
            technology = "Li-ion" 
        }

        // 3. Power Saving Mode
        // Checks if 'lowpowermode' is set to 1 in pmset settings.
        runCatching {
            val output = ShellUtils.execute("pmset -g")
            isPowerSaving = Regex("lowpowermode\\s+1").containsMatchIn(output)
        }

        // 4. Safe Mode
        // Checks the kernel boot arguments via sysctl.
        runCatching {
            val output = ShellUtils.execute("sysctl -n kern.safe_boot")
            isSafeMode = output.trim() == "1"
        }
        
        // Filter suspicious capacity values (e.g., < 1000 mAh on a laptop)
        // This avoids showing incorrect data when ioreg returns raw/unscaled units.
        val finalMaxCapacity = if (maxCapacityMah != null && maxCapacityMah < 1000) null else maxCapacityMah

        return DesktopBatteryReader.Info(
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
}

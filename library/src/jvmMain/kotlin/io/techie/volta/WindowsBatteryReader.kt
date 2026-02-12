/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.techie.volta

/**
 * Windows implementation of [PlatformBatteryReader].
 *
 * This class retrieves battery information on Windows systems using standard command-line tools:
 * - `wmic`: Used to query `Win32_Battery` for level, status, voltage, and chemistry.
 * - `wmic`: Used to query `Win32_ComputerSystem` for boot state (Safe Mode detection).
 * - `powercfg`: Used to detect the active power scheme (Power Saver Mode).
 *
 * Note: `wmic` is deprecated in newer Windows versions but remains the most reliable method
 * for backward compatibility without JNI/JNA. Future implementations might consider PowerShell
 * or native Win32 APIs.
 */
internal class WindowsBatteryReader : PlatformBatteryReader {

    override fun read(): DesktopBatteryReader.Info {
        var level: Int? = null
        var status: Int? = null
        var voltage: Int? = null
        var technology: String? = null
        var isSafeMode = false
        var isPowerSaving = false

        // 1. Battery Info (WMIC)
        // Query Win32_Battery for essential battery properties.
        // EstimatedChargeRemaining: Percentage (0-100)
        // BatteryStatus: 1=Discharging, 2=AC, 6-9=Charging
        // Voltage: Battery voltage in millivolts
        // Chemistry: Battery technology (e.g., LION, NiMH)
        runCatching {
            val output = ShellUtils.execute("wmic Path Win32_Battery Get EstimatedChargeRemaining,BatteryStatus,Voltage,Chemistry /FORMAT:LIST")
            level = ShellUtils.parseValue(output, "EstimatedChargeRemaining")?.toIntOrNull()
            status = ShellUtils.parseValue(output, "BatteryStatus")?.toIntOrNull()
            voltage = ShellUtils.parseValue(output, "Voltage")?.toIntOrNull()
            technology = ShellUtils.parseValue(output, "Chemistry")
        }

        // Interpret BatteryStatus codes
        // Status 6, 7, 8, 9 indicate various charging states.
        // Status 1 indicates "Discharging" (on battery).
        // Other statuses (2, 3, 4, 5) are less common or indicate errors/unknown.
        val isCharging = status in 6..9
        val isPlugged = status != 1

        // 2. Safe Mode Detection
        // Checks the 'BootupState' property of Win32_ComputerSystem.
        // "Fail-safe" indicates Safe Mode.
        runCatching {
            val output = ShellUtils.execute("wmic computersystem get bootupstate /FORMAT:LIST")
            isSafeMode = output.contains("Fail-safe", ignoreCase = true)
        }

        // 3. Power Saving Mode Detection
        // Checks the active power scheme using powercfg.
        // If the active scheme name contains "saver", we assume Power Saver mode is active.
        runCatching {
            val output = ShellUtils.execute("powercfg /getactivescheme")
            isPowerSaving = output.contains("saver", ignoreCase = true)
        }

        return DesktopBatteryReader.Info(
            level = level,
            isCharging = isCharging,
            isPlugged = isPlugged,
            isPowerSaving = isPowerSaving,
            isSafeMode = isSafeMode,
            technology = technology,
            voltageMv = voltage,
        )
    }
}

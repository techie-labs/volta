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
package io.techie.volta.diagnostics
import io.techie.volta.core.orNull
import io.techie.volta.core.BatteryStateProvider
import io.techie.volta.core.BatteryState

/**
 * Constants used as keys for the diagnostic dump map.
 */
object DiagnosticKeys {
    const val BATTERY_LEVEL = "volta_battery_level"
    const val IS_CHARGING = "volta_is_charging"
    const val CHARGING_STATUS = "volta_charging_status"
    const val CHARGING_SOURCE = "volta_charging_source"
    const val IS_POWER_SAVING_MODE = "volta_is_power_saving_mode"
    const val CYCLE_COUNT = "volta_cycle_count"
    const val VOLTAGE_MV = "volta_voltage_mv"
    const val TEMPERATURE_C = "volta_temperature_c"
    const val HEALTH = "volta_health"
}

/**
 * Synchronously retrieves a snapshot of the current power and thermal states.
 * Useful for injecting hardware context into crash reports or logging tools.
 *
 * This is designed as an extension function to adhere to the Open/Closed Principle,
 * extending the capabilities of [BatteryState] without modifying the core data class.
 */
fun BatteryState.getDiagnosticDump(): Map<String, String> {
    return mapOf(
        DiagnosticKeys.BATTERY_LEVEL to (level?.toString() ?: "UNKNOWN"),
        DiagnosticKeys.IS_CHARGING to (isCharging?.toString() ?: "UNKNOWN"),
        DiagnosticKeys.CHARGING_STATUS to chargingStatus.name,
        DiagnosticKeys.CHARGING_SOURCE to chargingSource.name,
        DiagnosticKeys.IS_POWER_SAVING_MODE to isPowerSavingMode.toString(),
        DiagnosticKeys.CYCLE_COUNT to (cycleCount.orNull()?.toString() ?: "UNKNOWN"),
        DiagnosticKeys.VOLTAGE_MV to (voltageMv.orNull()?.toString() ?: "UNKNOWN"),
        DiagnosticKeys.TEMPERATURE_C to (temperatureC.orNull()?.toString() ?: "UNKNOWN"),
        DiagnosticKeys.HEALTH to (health.orNull()?.name ?: "UNKNOWN")
    )
}

/**
 * Convenience extension for the [BatteryStateProvider] to easily retrieve a diagnostic dump of the current state.
 */
fun BatteryStateProvider.getDiagnosticDump(): Map<String, String> {
    return this.battery.value.getDiagnosticDump()
}

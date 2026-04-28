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
import io.techie.volta.core.Availability
import io.techie.volta.core.BatteryState
import io.techie.volta.enums.BatteryHealth
import io.techie.volta.enums.ChargingSource
import io.techie.volta.enums.ChargingStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class BatteryDiagnosticsTest {

    @Test
    fun testGetDiagnosticDumpWithFullData() {
        val state = BatteryState(
            isCharging = true,
            level = 85,
            chargingStatus = ChargingStatus.CHARGING,
            chargingSource = ChargingSource.USB,
            isPowerSavingMode = false,
            cycleCount = Availability.Available(150),
            voltageMv = Availability.Available(4200),
            temperatureC = Availability.Available(32.5f),
            health = Availability.Available(BatteryHealth.GOOD),
        )

        val dump = state.getDiagnosticDump()

        assertEquals("85", dump[DiagnosticKeys.BATTERY_LEVEL])
        assertEquals("true", dump[DiagnosticKeys.IS_CHARGING])
        assertEquals("CHARGING", dump[DiagnosticKeys.CHARGING_STATUS])
        assertEquals("USB", dump[DiagnosticKeys.CHARGING_SOURCE])
        assertEquals("false", dump[DiagnosticKeys.IS_POWER_SAVING_MODE])
        assertEquals("150", dump[DiagnosticKeys.CYCLE_COUNT])
        assertEquals("4200", dump[DiagnosticKeys.VOLTAGE_MV])
        assertEquals("32.5", dump[DiagnosticKeys.TEMPERATURE_C])
        assertEquals("GOOD", dump[DiagnosticKeys.HEALTH])
    }

    @Test
    fun testGetDiagnosticDumpWithMissingData() {
        val state = BatteryState(
            isCharging = null,
            level = null,
            chargingStatus = ChargingStatus.UNKNOWN,
            chargingSource = ChargingSource.UNKNOWN,
            isPowerSavingMode = true,
            cycleCount = Availability.NotSupported,
            voltageMv = Availability.Unknown,
            temperatureC = Availability.NotSupported,
            health = Availability.Unknown,
        )

        val dump = state.getDiagnosticDump()

        assertEquals("UNKNOWN", dump[DiagnosticKeys.BATTERY_LEVEL])
        assertEquals("UNKNOWN", dump[DiagnosticKeys.IS_CHARGING])
        assertEquals("UNKNOWN", dump[DiagnosticKeys.CHARGING_STATUS])
        assertEquals("UNKNOWN", dump[DiagnosticKeys.CHARGING_SOURCE])
        assertEquals("true", dump[DiagnosticKeys.IS_POWER_SAVING_MODE])
        assertEquals("UNKNOWN", dump[DiagnosticKeys.CYCLE_COUNT])
        assertEquals("UNKNOWN", dump[DiagnosticKeys.VOLTAGE_MV])
        assertEquals("UNKNOWN", dump[DiagnosticKeys.TEMPERATURE_C])
        assertEquals("UNKNOWN", dump[DiagnosticKeys.HEALTH])
    }
}

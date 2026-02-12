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

import io.techie.volta.enums.ChargingSource
import io.techie.volta.enums.ChargingStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

class BatteryStateTest {

    @Test
    fun testDefaultValues() {
        val state = BatteryState()

        assertNull(state.isCharging)
        assertNull(state.isLow)
        assertNull(state.level)
        assertEquals(ChargingStatus.UNKNOWN, state.chargingStatus)
        assertEquals(ChargingSource.UNKNOWN, state.chargingSource)
        assertEquals(Availability.Unknown, state.voltageMv)
        assertEquals(Availability.Unknown, state.temperatureC)
        assertEquals(Availability.Unknown, state.health)
        assertNull(state.technology)
        assertEquals(Availability.Unknown, state.cycleCount)
        assertEquals(Availability.Unknown, state.currentNowMa)
        assertEquals(Availability.Unknown, state.currentAverageMa)
        assertEquals(Availability.Unknown, state.chargeCounterUah)
        assertEquals(Availability.Unknown, state.remainingEnergyTimeMillis)
        assertFalse(state.isPowerSavingMode)
        assertFalse(state.isSafeMode)
        assertFalse(state.isProtected)
    }

    @Test
    fun testCustomValues() {
        val state = BatteryState(
            isCharging = true,
            level = 85,
            chargingStatus = ChargingStatus.CHARGING,
            chargingSource = ChargingSource.AC,
            voltageMv = Availability.Available(4200),
            temperatureC = Availability.Available(35.5f),
            technology = "Li-ion"
        )

        assertEquals(true, state.isCharging)
        assertEquals(85, state.level)
        assertEquals(ChargingStatus.CHARGING, state.chargingStatus)
        assertEquals(ChargingSource.AC, state.chargingSource)
        assertEquals(Availability.Available(4200), state.voltageMv)
        assertEquals(Availability.Available(35.5f), state.temperatureC)
        assertEquals("Li-ion", state.technology)
    }
}

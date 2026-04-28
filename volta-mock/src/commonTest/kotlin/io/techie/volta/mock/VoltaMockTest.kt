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
package io.techie.volta.mock

import io.techie.volta.VoltaSensorState
import io.techie.volta.core.BatteryState
import io.techie.volta.enums.ChargingStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VoltaMockTest {

    private fun getBattery(mock: VoltaMock): BatteryState {
        val state = mock.batteryState.value
        assertTrue(state is VoltaSensorState.Available, "Expected state to be Available")
        return state.data
    }

    @Test
    fun testVoltaMockLevelUpdate() {
        val mock = VoltaMock()
        mock.setBatteryLevel(50)
        assertEquals(50, getBattery(mock).level)

        mock.setBatteryLevel(10)
        assertEquals(10, getBattery(mock).level)
    }

    @Test
    fun testVoltaMockChargingUpdate() {
        val mock = VoltaMock()
        mock.setCharging(true)
        assertEquals(true, getBattery(mock).isCharging)
        assertEquals(ChargingStatus.CHARGING, getBattery(mock).chargingStatus)

        mock.setCharging(false)
        assertEquals(false, getBattery(mock).isCharging)
        assertEquals(ChargingStatus.DISCHARGING, getBattery(mock).chargingStatus)
    }

    @Test
    fun testVoltaMockPowerSavingUpdate() {
        val mock = VoltaMock()
        mock.setPowerSavingMode(true)
        assertEquals(true, getBattery(mock).isPowerSavingMode)

        mock.setPowerSavingMode(false)
        assertEquals(false, getBattery(mock).isPowerSavingMode)
    }
}

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
package io.techie.volta.devtools

import io.techie.volta.core.Availability
import io.techie.volta.core.BatteryState
import io.techie.volta.core.BatteryStateProvider
import io.techie.volta.core.ChargingStatusChange
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VoltaSmartSyncTest {

    private class MockBatteryStateProvider(initialState: BatteryState) : BatteryStateProvider {
        val mutableState = MutableStateFlow(initialState)
        override val battery: StateFlow<BatteryState> = mutableState
        override val chargingEvents: Flow<ChargingStatusChange> = emptyFlow()
        override fun observe() {}
        override fun stop() {}
    }

    @Test
    fun testObserveSafeExecution() = runTest {
        val provider = MockBatteryStateProvider(
            BatteryState(
                level = 15,
                isCharging = false,
                temperatureC = Availability.Available(45.0f),
                isPowerSavingMode = true,
            ),
        )

        val condition = ExecutionCondition(
            minBatteryLevel = 20,
            requiresCharging = true,
            maxTemperatureC = 40.0f,
            ignorePowerSavingMode = false,
        )

        val flow = provider.observeSafeExecution(condition)

        // Initial state does not meet conditions
        assertFalse(flow.first())

        // Update state to meet all conditions
        provider.mutableState.value = BatteryState(
            level = 25,
            isCharging = true,
            temperatureC = Availability.Available(35.0f),
            isPowerSavingMode = false,
        )

        assertTrue(flow.first())
    }

    @Test
    fun testWhenOptimal() = runTest {
        // Not optimal initially
        val provider = MockBatteryStateProvider(BatteryState(level = 15))
        val condition = ExecutionCondition(minBatteryLevel = 20)

        var executed = false

        // Launch in background
        val job = launch {
            provider.whenOptimal(condition) {
                executed = true
            }
        }

        // Wait a bit, should not have executed
        delay(100)
        assertFalse(executed)

        // Make optimal
        provider.mutableState.value = BatteryState(level = 25)

        // Wait for it to process
        job.join()

        assertTrue(executed)
    }
}

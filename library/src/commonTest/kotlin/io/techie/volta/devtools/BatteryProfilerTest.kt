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
import io.techie.volta.core.ChargingStatusChange
import io.techie.volta.core.BatteryStateProvider
import io.techie.volta.core.BatteryState

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BatteryProfilerTest {

    private class MockBatteryStateProvider(initialState: BatteryState) : BatteryStateProvider {
        val mutableState = MutableStateFlow(initialState)
        override val battery: StateFlow<BatteryState> = mutableState
        override val chargingEvents: Flow<ChargingStatusChange> = emptyFlow()
        override fun observe() {}
        override fun stop() {}
    }

    @Test
    fun testBatteryProfilerSession() {
        val provider = MockBatteryStateProvider(BatteryState(level = 100))
        val profiler = BatteryProfiler(provider)

        profiler.startSession("TestSession")

        // Simulate a battery drop
        provider.mutableState.value = BatteryState(level = 95)
        
        val report = profiler.stopSession("TestSession")

        assertNotNull(report)
        assertEquals("TestSession", report.sessionName)
        assertEquals(100, report.startBatteryPercent)
        assertEquals(95, report.endBatteryPercent)
        assertTrue(report.duration.inWholeMilliseconds >= 0)
    }
}

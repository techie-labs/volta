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
package io.techie.volta.internal

import io.techie.volta.Volta
import io.techie.volta.VoltaSensorState
import io.techie.volta.core.BatteryState
import io.techie.volta.core.BatteryStateProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class VoltaImpl(
    private val provider: BatteryStateProvider,
    dispatcher: CoroutineDispatcher,
) : Volta {
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private val _batteryState = MutableStateFlow<VoltaSensorState<BatteryState>>(VoltaSensorState.Unknown)
    override val batteryState: StateFlow<VoltaSensorState<BatteryState>> = _batteryState.asStateFlow()

    override fun startMonitoring() {
        provider.observe()
        scope.launch {
            try {
                provider.battery.collect { state ->
                    _batteryState.value = VoltaSensorState.Available(state)
                }
            } catch (e: Exception) {
                // If provider throws (e.g., SecurityException on Android)
                _batteryState.value = VoltaSensorState.Error(e)
            }
        }
    }

    override fun stopMonitoring() {
        provider.stop()
    }
}

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

import io.techie.volta.Volta
import io.techie.volta.VoltaSensorState
import io.techie.volta.core.Availability
import io.techie.volta.core.BatteryState
import io.techie.volta.enums.ChargingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A mock implementation of [Volta] for Compose Previews and UI Testing.
 */
class VoltaMock(
    initialState: VoltaSensorState<BatteryState> = VoltaSensorState.Available(BatteryState(level = 100, isCharging = false)),
) : Volta {

    private val _batteryState = MutableStateFlow(initialState)
    override val batteryState: StateFlow<VoltaSensorState<BatteryState>> = _batteryState.asStateFlow()

    override fun startMonitoring() {
        // No-op for mock
    }

    override fun stopMonitoring() {
        // No-op for mock
    }

    // --- Mock Controls ---

    fun emitState(state: VoltaSensorState<BatteryState>) {
        _batteryState.value = state
    }

    fun setBatteryLevel(level: Int) {
        updateAvailableState { it.copy(level = level.coerceIn(0, 100)) }
    }

    fun setCharging(isCharging: Boolean) {
        val toStatus = if (isCharging) ChargingStatus.CHARGING else ChargingStatus.DISCHARGING
        updateAvailableState { it.copy(isCharging = isCharging, chargingStatus = toStatus) }
    }

    fun setThermalState(temperatureC: Float) {
        updateAvailableState { it.copy(temperatureC = Availability.Available(temperatureC)) }
    }

    fun setPowerSavingMode(enabled: Boolean) {
        updateAvailableState { it.copy(isPowerSavingMode = enabled) }
    }

    private inline fun updateAvailableState(update: (BatteryState) -> BatteryState) {
        val current = _batteryState.value
        if (current is VoltaSensorState.Available) {
            _batteryState.value = VoltaSensorState.Available(update(current.data))
        }
    }
}

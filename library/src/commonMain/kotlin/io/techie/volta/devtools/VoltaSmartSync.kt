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
import io.techie.volta.core.BatteryStateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Defines the optimal hardware conditions required to safely execute a heavy task.
 */
data class ExecutionCondition(
    val minBatteryLevel: Int = 20,
    val requiresCharging: Boolean = false,
    val maxTemperatureC: Float = 40.0f,
    val ignorePowerSavingMode: Boolean = false,
)

/**
 * Returns a [Flow] that emits `true` whenever the device's hardware conditions meet the specified [ExecutionCondition].
 *
 * This allows developers to observe the device hardware and only trigger background processes (like ML models or syncing)
 * when the conditions are right, preserving battery health and device performance.
 */
fun BatteryStateProvider.observeSafeExecution(condition: ExecutionCondition): Flow<Boolean> {
    return this.battery.map { state ->
        val levelOk = state.level?.let { it >= condition.minBatteryLevel } ?: true
        val chargingOk = !condition.requiresCharging || (state.isCharging == true)

        val tempOk = when (val temp = state.temperatureC) {
            is Availability.Available -> temp.value <= condition.maxTemperatureC
            else -> true // Optimistic fallback if temperature is not supported on the platform
        }

        val powerModeOk = condition.ignorePowerSavingMode || !state.isPowerSavingMode

        levelOk && chargingOk && tempOk && powerModeOk
    }.distinctUntilChanged()
}

/**
 * Suspends the coroutine until the hardware conditions are optimal according to the [ExecutionCondition],
 * then executes the [block].
 */
suspend fun <T> BatteryStateProvider.whenOptimal(
    condition: ExecutionCondition,
    block: suspend () -> T,
): T {
    observeSafeExecution(condition).first { isOptimal -> isOptimal }
    return block()
}

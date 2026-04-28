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
import io.techie.volta.core.BatteryStateProvider
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * A report generated at the end of a profiling session detailing the battery consumption.
 */
data class VoltaSessionReport(
    val sessionName: String,
    val duration: Duration,
    val startBatteryPercent: Int?,
    val endBatteryPercent: Int?,
    val averageDischargeRatePercentPerHour: Float?,
)

/**
 * A tool to profile battery consumption over a specific session or feature execution.
 *
 * session-based battery consumption metrics without intertwining with core provider logic.
 */
class BatteryProfiler(private val provider: BatteryStateProvider) {
    private val activeSessions = mutableMapOf<String, SessionData>()

    /**
     * Starts tracking battery metrics for a session with the given [name].
     */
    fun startSession(name: String) {
        val startLevel = provider.battery.value.level
        activeSessions[name] = SessionData(
            startTimeMark = TimeSource.Monotonic.markNow(),
            startLevel = startLevel,
        )
    }

    /**
     * Stops tracking the session and returns a [VoltaSessionReport], or null if the session wasn't started.
     */
    fun stopSession(name: String): VoltaSessionReport? {
        val session = activeSessions.remove(name) ?: return null

        val duration = session.startTimeMark.elapsedNow()
        val endLevel = provider.battery.value.level

        val rate = if (duration.inWholeSeconds > 0 && session.startLevel != null && endLevel != null) {
            val drop = session.startLevel - endLevel
            if (drop > 0) {
                (drop.toFloat() / (duration.inWholeSeconds.toFloat() / 3600f))
            } else {
                0f
            }
        } else {
            null
        }

        return VoltaSessionReport(
            sessionName = name,
            duration = duration,
            startBatteryPercent = session.startLevel,
            endBatteryPercent = endLevel,
            averageDischargeRatePercentPerHour = rate,
        )
    }

    private data class SessionData(
        val startTimeMark: TimeMark,
        val startLevel: Int?,
    )
}

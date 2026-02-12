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

import java.util.Locale

/**
 * Facade for reading battery information on Desktop (JVM).
 * Delegates to platform-specific implementations.
 */
internal object DesktopBatteryReader {

    data class Info(
        val level: Int? = null,
        val isCharging: Boolean? = null,
        val isPlugged: Boolean? = null,
        val isPowerSaving: Boolean = false,
        val isSafeMode: Boolean = false,
        val technology: String? = null,
        val cycleCount: Int? = null,
        val voltageMv: Int? = null,
        val chargeCounterUah: Long? = null,
        val remainingTimeMillis: Long? = null,
        val chargingSource: String? = null,
        val temperatureC: Float? = null,
        val currentNowMa: Long? = null,
        val designCapacityMah: Long? = null,
        val maxCapacityMah: Long? = null,
    )

    private val reader: PlatformBatteryReader by lazy {
        when {
            isWindows -> WindowsBatteryReader()
            isMac -> MacOSBatteryReader()
            isLinux -> LinuxBatteryReader()
            else -> object : PlatformBatteryReader {
                override fun read() = Info()
            }
        }
    }

    fun read(): Info = reader.read()

    private val isWindows get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")
    private val isMac get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")
    private val isLinux get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("nux")
}

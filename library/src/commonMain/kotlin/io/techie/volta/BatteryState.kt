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

import io.techie.volta.enums.BatteryHealth
import io.techie.volta.enums.ChargingSource
import io.techie.volta.enums.ChargingStatus

/**
 * Represents a snapshot of the battery state at a specific point in time.
 *
 * This class provides a unified view of battery properties across different platforms (Android, iOS, JVM).
 * Properties that are not supported or available on a specific platform are represented by `null`
 * or [Availability.NotSupported].
 */
data class BatteryState(
    /**
     * Indicates if the device is currently connected to a power source and charging.
     *
     * - `true`: The device is plugged in and charging, or fully charged while plugged in.
     * - `false`: The device is discharging (running on battery).
     * - `null`: The state is unknown or not yet resolved.
     */
    val isCharging: Boolean? = null,

    /**
     * Indicates if the battery level is considered low.
     *
     * - `true`: Battery level is low (typically ≤ 15% or system triggered).
     * - `false`: Battery level is normal.
     * - `null`: Unknown.
     */
    val isLow: Boolean? = null,

    /**
     * The current battery level as a percentage (0-100).
     *
     * - `null`: Unknown or not yet resolved.
     */
    val level: Int? = null,

    /**
     * The detailed status of the battery.
     *
     * Examples: [ChargingStatus.CHARGING], [ChargingStatus.DISCHARGING], [ChargingStatus.FULL].
     */
    val chargingStatus: ChargingStatus = ChargingStatus.UNKNOWN,

    /**
     * The source of power, if available.
     *
     * Examples: [ChargingSource.USB], [ChargingSource.AC], [ChargingSource.WIRELESS].
     * Note: Often [ChargingSource.UNKNOWN] on iOS.
     */
    val chargingSource: ChargingSource = ChargingSource.UNKNOWN,

    /**
     * The battery voltage in millivolts (mV).
     *
     * Wrapped in [Availability] to handle platform support (e.g., supported on Android, not on iOS).
     */
    val voltageMv: Availability<Int> = Availability.Unknown,

    /**
     * The battery temperature in Celsius (°C).
     *
     * Wrapped in [Availability] to handle platform support.
     */
    val temperatureC: Availability<Float> = Availability.Unknown,

    /**
     * The health condition of the battery.
     *
     * Wrapped in [Availability].
     */
    val health: Availability<BatteryHealth> = Availability.Unknown,

    /**
     * The battery technology (chemistry).
     *
     * Examples: "Li-ion", "Li-poly".
     * - Android: Supported.
     * - JVM: Supported on Linux/Windows.
     * - iOS: Not supported.
     */
    val technology: String? = null,

    /**
     * The number of charge cycles the battery has completed.
     *
     * A cycle is defined as using 100% of the battery's capacity (not necessarily in one go).
     *
     * - Android: Supported on API 34+ (Android 14).
     * - JVM: Supported on macOS and Linux.
     * - iOS: Not supported.
     */
    val cycleCount: Availability<Int> = Availability.Unknown,

    /**
     * The instantaneous battery current in milliamperes (mA).
     *
     * - Positive values indicate charging.
     * - Negative values indicate discharging.
     *
     * - Android: Supported.
     * - JVM: Supported on macOS and Linux.
     * - iOS: Not supported.
     */
    val currentNowMa: Availability<Long> = Availability.Unknown,

    /**
     * The average battery current in milliamperes (mA).
     *
     * - Android: Supported.
     * - JVM: Limited support.
     * - iOS: Not supported.
     */
    val currentAverageMa: Availability<Long> = Availability.Unknown,

    /**
     * The remaining battery capacity in microampere-hours (µAh).
     *
     * - Android: Supported.
     * - JVM: Supported on Linux (charge_now).
     * - iOS: Not supported.
     */
    val chargeCounterUah: Availability<Long> = Availability.Unknown,

    /**
     * The estimated remaining time until the battery is fully charged (if charging)
     * or empty (if discharging), in milliseconds.
     *
     * - Android: Supported (API 28+).
     * - JVM: Supported on macOS (time remaining).
     * - iOS: Not supported.
     */
    val remainingEnergyTimeMillis: Availability<Long> = Availability.Unknown,

    /**
     * Indicates if the system's power saving mode is active.
     *
     * - Android: Corresponds to Power Saver Mode.
     * - iOS: Corresponds to Low Power Mode.
     */
    val isPowerSavingMode: Boolean = false,

    /**
     * Indicates if the device is booted in Safe Mode.
     *
     * - Android: Supported.
     * - JVM (Desktop): Supported on Windows/Mac/Linux.
     * - iOS: Always `false` (Not exposed).
     */
    val isSafeMode: Boolean = false,

    /**
     * Indicates if the battery is in a protected state.
     *
     * This usually means the device is plugged in but not charging to preserve battery health
     * (e.g., Adaptive Charging limited to 80%).
     *
     * - Android: Inferred from status.
     * - JVM: Inferred from status.
     * - iOS: Always `false`.
     */
    val isProtected: Boolean = false,
)

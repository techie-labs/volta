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
package io.techie.volta.provider
import io.techie.volta.core.Availability
import io.techie.volta.core.BatteryState
import io.techie.volta.core.BatteryStateProvider
import io.techie.volta.core.ChargingStatusChange
import io.techie.volta.enums.ChargingSource
import io.techie.volta.enums.ChargingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSProcessInfoPowerStateDidChangeNotification
import platform.Foundation.isLowPowerModeEnabled
import platform.UIKit.*

/**
 * iOS implementation of [BatteryStateProvider].
 *
 * Monitors battery changes using [NSNotificationCenter] observers for:
 * - `UIDeviceBatteryLevelDidChangeNotification`
 * - `UIDeviceBatteryStateDidChangeNotification`
 * - `NSProcessInfoPowerStateDidChangeNotification`
 *
 * Note: Many advanced battery properties (Current, Cycle Count, Technology) are not exposed
 * by the public iOS API and will return [Availability.NotSupported].
 *
 * @param scope The CoroutineScope used for emitting events.
 */
class IOSBatteryStateProvider(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BatteryStateProvider {

    private val _battery = MutableStateFlow(BatteryState())
    override val battery: StateFlow<BatteryState> = _battery.asStateFlow()

    private val _chargingEvents = MutableSharedFlow<ChargingStatusChange>(extraBufferCapacity = 1)
    override val chargingEvents: Flow<ChargingStatusChange> = _chargingEvents.asSharedFlow()

    private var observers = mutableListOf<Any>()
    private var lastChargingStatus: ChargingStatus? = null
    private var isObserving = false

    override fun observe() {
        if (isObserving) return
        isObserving = true

        UIDevice.currentDevice.batteryMonitoringEnabled = true
        registerObservers()
        updateBatteryState()
    }

    override fun stop() {
        if (!isObserving) return
        isObserving = false

        val center = NSNotificationCenter.defaultCenter
        observers.forEach { center.removeObserver(it) }
        observers.clear()

        UIDevice.currentDevice.batteryMonitoringEnabled = false
    }

    private fun registerObservers() {
        val center = NSNotificationCenter.defaultCenter
        val notifications = listOf(
            UIDeviceBatteryLevelDidChangeNotification,
            UIDeviceBatteryStateDidChangeNotification,
            NSProcessInfoPowerStateDidChangeNotification,
        )

        notifications.forEach { notificationName ->
            val observer = center.addObserverForName(
                name = notificationName,
                `object` = null,
                queue = null,
            ) { updateBatteryState() }
            observers.add(observer)
        }
    }

    private fun updateBatteryState() {
        val device = UIDevice.currentDevice
        val chargingStatus = getChargingStatus(device)
        val isPowerSaving = NSProcessInfo.processInfo.isLowPowerModeEnabled()

        emitChargingEventIfChanged(chargingStatus)

        _battery.value = BatteryState(
            level = getBatteryLevel(device),
            isCharging = chargingStatus == ChargingStatus.CHARGING || chargingStatus == ChargingStatus.FULL,
            isLow = isPowerSaving,
            chargingStatus = chargingStatus,
            chargingSource = ChargingSource.UNKNOWN,
            voltageMv = Availability.NotSupported,
            temperatureC = Availability.NotSupported,
            health = Availability.NotSupported,
            technology = null,
            cycleCount = Availability.NotSupported,
            currentNowMa = Availability.NotSupported,
            currentAverageMa = Availability.NotSupported,
            chargeCounterUah = Availability.NotSupported,
            remainingEnergyTimeMillis = Availability.NotSupported,
            isPowerSavingMode = isPowerSaving,
            isSafeMode = false,
            isProtected = false,
        )
    }

    private fun getBatteryLevel(device: UIDevice): Int? {
        val raw = device.batteryLevel
        return if (raw >= 0f) (raw * 100).toInt() else null
    }

    private fun getChargingStatus(device: UIDevice): ChargingStatus {
        return when (device.batteryState) {
            UIDeviceBatteryState.UIDeviceBatteryStateCharging -> ChargingStatus.CHARGING
            UIDeviceBatteryState.UIDeviceBatteryStateFull -> ChargingStatus.FULL
            UIDeviceBatteryState.UIDeviceBatteryStateUnplugged -> ChargingStatus.DISCHARGING
            else -> ChargingStatus.UNKNOWN
        }
    }

    private fun emitChargingEventIfChanged(current: ChargingStatus) {
        val previous = lastChargingStatus
        if (previous != null && previous != current) {
            scope.launch {
                _chargingEvents.emit(ChargingStatusChange(from = previous, to = current))
            }
        }
        lastChargingStatus = current
    }
}

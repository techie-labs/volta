package io.techie.volta

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
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
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
            NSProcessInfoPowerStateDidChangeNotification
        )

        notifications.forEach { notificationName ->
            val observer = center.addObserverForName(
                name = notificationName,
                `object` = null,
                queue = null
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
            technology = null, // Not exposed
            cycleCount = Availability.NotSupported, // Not exposed
            currentNowMa = Availability.NotSupported, // Not exposed
            currentAverageMa = Availability.NotSupported, // Not exposed
            chargeCounterUah = Availability.NotSupported, // Not exposed
            remainingEnergyTimeMillis = Availability.NotSupported, // Not exposed
            isPowerSavingMode = isPowerSaving,
            isSafeMode = false,
            isProtected = false
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

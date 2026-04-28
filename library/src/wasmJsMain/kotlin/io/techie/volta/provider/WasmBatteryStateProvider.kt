@file:OptIn(ExperimentalWasmJsInterop::class)

package io.techie.volta.provider

import io.techie.volta.core.Availability
import io.techie.volta.core.BatteryState
import io.techie.volta.core.BatteryStateProvider
import io.techie.volta.core.ChargingStatusChange
import io.techie.volta.enums.ChargingSource
import io.techie.volta.enums.ChargingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.js.Promise

// External declarations for HTML5 Battery Status API
external interface BatteryManager : JsAny {
    val charging: Boolean
    val level: Double
    val chargingTime: Double
    val dischargingTime: Double
    @JsName("onchargingchange")
    var onChargingChange: ((JsAny?) -> JsAny?)?
    @JsName("onlevelchange")
    var onLevelChange: ((JsAny?) -> JsAny?)?
}

@JsFun("() => { return (navigator.getBattery !== undefined) ? navigator.getBattery() : null; }")
private external fun getBatteryPromiseJs(): Promise<BatteryManager>?

class WasmBatteryStateProvider : BatteryStateProvider {
    private val _battery = MutableStateFlow(createUnknownState())
    private val _chargingEvents = MutableSharedFlow<ChargingStatusChange>(extraBufferCapacity = 1)

    override val battery: StateFlow<BatteryState> = _battery.asStateFlow()
    override val chargingEvents: Flow<ChargingStatusChange> = _chargingEvents

    private var batteryManager: BatteryManager? = null

    override fun observe() {
        val promise = getBatteryPromiseJs()
        if (promise == null) {
            _battery.value = createUnknownState()
            return
        }

        promise.then { manager: BatteryManager? ->
            if (manager == null) {
                _battery.value = createUnknownState()
                return@then null
            }
            batteryManager = manager
            updateState(manager)

            manager.onChargingChange = {
                updateState(manager)
                null
            }
            manager.onLevelChange = {
                updateState(manager)
                null
            }
            null
        }.catch {
            _battery.value = createUnknownState()
            null
        }
    }

    override fun stop() {
        batteryManager?.let {
            it.onChargingChange = null
            it.onLevelChange = null
        }
        batteryManager = null
    }

    private fun updateState(manager: BatteryManager) {
        try {
            val levelInt = (manager.level * 100).toInt()
            val isCharging = manager.charging
            
            val newState = BatteryState(
                level = levelInt,
                isCharging = isCharging,
                chargingStatus = if (isCharging) ChargingStatus.CHARGING else ChargingStatus.DISCHARGING,
                temperatureC = Availability.NotSupported,
                voltageMv = Availability.NotSupported,
                health = Availability.NotSupported,
                technology = null,
                chargeCounterUah = Availability.NotSupported,
                currentNowMa = Availability.NotSupported,
                currentAverageMa = Availability.NotSupported,
                remainingEnergyTimeMillis = if (isCharging) {
                    if (manager.chargingTime != Double.POSITIVE_INFINITY) Availability.Available((manager.chargingTime * 1000).toLong()) else Availability.Unknown
                } else {
                    if (manager.dischargingTime != Double.POSITIVE_INFINITY) Availability.Available((manager.dischargingTime * 1000).toLong()) else Availability.Unknown
                },
                chargingSource = ChargingSource.UNKNOWN,
                cycleCount = Availability.NotSupported,
                isPowerSavingMode = false,
                isSafeMode = false,
                isProtected = false
            )

            val previousState = _battery.value
            if (previousState.isCharging != null && previousState.isCharging != isCharging) {
                val fromStatus = previousState.chargingStatus
                val toStatus = if (isCharging) ChargingStatus.CHARGING else ChargingStatus.DISCHARGING
                _chargingEvents.tryEmit(ChargingStatusChange(from = fromStatus, to = toStatus))
            }

            _battery.value = newState
        } catch (e: Throwable) {
            // Fallback for browsers that aggressively block the API
            _battery.value = createUnknownState()
        }
    }

    private fun createUnknownState() = BatteryState(
        level = null,
        isCharging = null,
        chargingStatus = ChargingStatus.UNKNOWN,
        temperatureC = Availability.Unknown,
        voltageMv = Availability.Unknown,
        health = Availability.Unknown,
        technology = null,
        chargeCounterUah = Availability.Unknown,
        currentNowMa = Availability.Unknown,
        currentAverageMa = Availability.Unknown,
        remainingEnergyTimeMillis = Availability.Unknown,
        chargingSource = ChargingSource.UNKNOWN,
        cycleCount = Availability.Unknown,
        isPowerSavingMode = false,
        isSafeMode = false,
        isProtected = false
    )
}

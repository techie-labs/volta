package io.techie.volta.mock

import io.techie.volta.core.Availability
import io.techie.volta.core.BatteryState
import io.techie.volta.core.BatteryStateProvider
import io.techie.volta.core.ChargingStatusChange
import io.techie.volta.enums.ChargingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A mock implementation of [BatteryStateProvider] for Compose Previews and UI Testing.
 */
class VoltaMock(
    initialState: BatteryState = BatteryState(level = 100, isCharging = false)
) : BatteryStateProvider {

    private val _battery = MutableStateFlow(initialState)
    private val _chargingEvents = MutableSharedFlow<ChargingStatusChange>(extraBufferCapacity = 1)

    override val battery: StateFlow<BatteryState> = _battery.asStateFlow()
    override val chargingEvents: Flow<ChargingStatusChange> = _chargingEvents

    override fun observe() {
        // No-op for mock
    }

    override fun stop() {
        // No-op for mock
    }

    // --- Mock Controls ---

    fun setBatteryLevel(level: Int) {
        _battery.value = _battery.value.copy(level = level.coerceIn(0, 100))
    }

    fun setCharging(isCharging: Boolean) {
        val wasCharging = _battery.value.isCharging
        val fromStatus = _battery.value.chargingStatus
        val toStatus = if (isCharging) ChargingStatus.CHARGING else ChargingStatus.DISCHARGING

        _battery.value = _battery.value.copy(
            isCharging = isCharging,
            chargingStatus = toStatus
        )
        
        if (wasCharging != isCharging) {
            _chargingEvents.tryEmit(ChargingStatusChange(from = fromStatus, to = toStatus))
        }
    }

    fun setThermalState(temperatureC: Float) {
        _battery.value = _battery.value.copy(
            temperatureC = Availability.Available(temperatureC)
        )
    }

    fun setPowerSavingMode(enabled: Boolean) {
        _battery.value = _battery.value.copy(isPowerSavingMode = enabled)
    }
}

package io.techie.volta

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BatteryStateProvider {
    val battery: StateFlow<BatteryState>
    val chargingEvents: Flow<ChargingStatusChange>
    fun observe()
    fun stop()
}
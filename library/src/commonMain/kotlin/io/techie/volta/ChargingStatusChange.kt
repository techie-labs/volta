package io.techie.volta

import io.techie.volta.enums.ChargingStatus

data class ChargingStatusChange(
    val from: ChargingStatus,
    val to: ChargingStatus
)
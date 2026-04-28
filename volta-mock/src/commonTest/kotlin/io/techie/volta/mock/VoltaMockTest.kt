package io.techie.volta.mock

import io.techie.volta.enums.ChargingStatus
import kotlin.test.Test
import kotlin.test.assertEquals

class VoltaMockTest {

    @Test
    fun testVoltaMockLevelUpdate() {
        val mock = VoltaMock()
        mock.setBatteryLevel(50)
        assertEquals(50, mock.battery.value.level)
        
        mock.setBatteryLevel(10)
        assertEquals(10, mock.battery.value.level)
    }

    @Test
    fun testVoltaMockChargingUpdate() {
        val mock = VoltaMock()
        mock.setCharging(true)
        assertEquals(true, mock.battery.value.isCharging)
        assertEquals(ChargingStatus.CHARGING, mock.battery.value.chargingStatus)

        mock.setCharging(false)
        assertEquals(false, mock.battery.value.isCharging)
        assertEquals(ChargingStatus.DISCHARGING, mock.battery.value.chargingStatus)
    }

    @Test
    fun testVoltaMockPowerSavingUpdate() {
        val mock = VoltaMock()
        mock.setPowerSavingMode(true)
        assertEquals(true, mock.battery.value.isPowerSavingMode)

        mock.setPowerSavingMode(false)
        assertEquals(false, mock.battery.value.isPowerSavingMode)
    }
}

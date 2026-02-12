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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import io.techie.volta.enums.BatteryHealth
import io.techie.volta.enums.ChargingSource
import io.techie.volta.enums.ChargingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Android implementation of [BatteryStateProvider].
 *
 * Monitors battery changes using [BroadcastReceiver] for `ACTION_BATTERY_CHANGED`
 * and `ACTION_POWER_SAVE_MODE_CHANGED`.
 *
 * Also queries [BatteryManager] for advanced properties like current, cycle count, and capacity.
 *
 * @param context The Android application context.
 * @param scope The CoroutineScope used for emitting events.
 */
class AndroidBatteryStateProvider(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) : BatteryStateProvider {

    private val _battery = MutableStateFlow(BatteryState())
    override val battery: StateFlow<BatteryState> = _battery.asStateFlow()

    private val _chargingEvents = MutableSharedFlow<ChargingStatusChange>(extraBufferCapacity = 1)
    override val chargingEvents: Flow<ChargingStatusChange> = _chargingEvents.asSharedFlow()

    private var batteryReceiver: BroadcastReceiver? = null
    private var powerSaveReceiver: BroadcastReceiver? = null
    private var lastChargingStatus: ChargingStatus? = null

    private val batteryManager by lazy {
        context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    }

    override fun observe() {
        if (batteryReceiver != null) return

        // Register Battery Receiver
        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let { updateBatteryState(it) }
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Register Power Save Mode Receiver
        powerSaveReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val batteryIntent = ctx?.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
                updateBatteryState(batteryIntent)
            }
        }
        context.registerReceiver(powerSaveReceiver, IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED))

        // Initial State Update
        val initialIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        updateBatteryState(initialIntent)
    }

    override fun stop() {
        batteryReceiver?.let { context.unregisterReceiver(it) }
        batteryReceiver = null

        powerSaveReceiver?.let { context.unregisterReceiver(it) }
        powerSaveReceiver = null
    }

    private fun updateBatteryState(intent: Intent?) {
        if (intent == null) return

        val level = getBatteryLevel(intent)
        val chargingStatus = getChargingStatus(intent)
        val source = getChargingSource(intent)
        val isPowerSaving = isPowerSavingMode()
        val isSafeMode = context.packageManager.isSafeMode

        // Protected State Logic
        val isPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
        val statusInt = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isProtected = isPlugged &&
            statusInt == BatteryManager.BATTERY_STATUS_NOT_CHARGING &&
            (level ?: 0) < 100

        emitChargingEventIfChanged(chargingStatus)

        _battery.value = BatteryState(
            level = level,
            isCharging = chargingStatus == ChargingStatus.CHARGING || chargingStatus == ChargingStatus.FULL,
            isLow = level?.let { it <= 15 },
            chargingStatus = chargingStatus,
            chargingSource = source,
            voltageMv = getVoltage(intent),
            temperatureC = getTemperature(intent),
            health = getHealth(intent),
            technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY),
            cycleCount = getCycleCount(intent),
            currentNowMa = getCurrentNow(),
            currentAverageMa = getCurrentAverage(),
            chargeCounterUah = getChargeCounter(),
            remainingEnergyTimeMillis = getRemainingEnergyTime(),
            isPowerSavingMode = isPowerSaving,
            isSafeMode = isSafeMode,
            isProtected = isProtected,
        )
    }

    // --- Basic Properties (Intent) ---

    private fun getBatteryLevel(intent: Intent): Int? {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        return if (level >= 0 && scale > 0) (level * 100 / scale) else null
    }

    private fun getChargingStatus(intent: Intent): ChargingStatus {
        return when (intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)) {
            BatteryManager.BATTERY_STATUS_CHARGING -> ChargingStatus.CHARGING
            BatteryManager.BATTERY_STATUS_FULL -> ChargingStatus.FULL
            BatteryManager.BATTERY_STATUS_DISCHARGING,
            BatteryManager.BATTERY_STATUS_NOT_CHARGING,
            -> ChargingStatus.DISCHARGING
            else -> ChargingStatus.UNKNOWN
        }
    }

    private fun getChargingSource(intent: Intent): ChargingSource {
        return when (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)) {
            BatteryManager.BATTERY_PLUGGED_USB -> ChargingSource.USB
            BatteryManager.BATTERY_PLUGGED_AC -> ChargingSource.AC
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> ChargingSource.WIRELESS
            else -> ChargingSource.UNKNOWN
        }
    }

    private fun getVoltage(intent: Intent): Availability<Int> {
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        return if (voltage > 0) Availability.Available(voltage) else Availability.Unknown
    }

    private fun getTemperature(intent: Intent): Availability<Float> {
        val temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        return if (temp > 0) Availability.Available(temp / 10f) else Availability.Unknown
    }

    private fun getHealth(intent: Intent): Availability<BatteryHealth> {
        val health = when (intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)) {
            BatteryManager.BATTERY_HEALTH_GOOD -> BatteryHealth.GOOD
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> BatteryHealth.OVERHEAT
            BatteryManager.BATTERY_HEALTH_DEAD -> BatteryHealth.DEAD
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> BatteryHealth.OVER_VOLTAGE
            else -> BatteryHealth.UNKNOWN
        }
        return Availability.Available(health)
    }

    // --- Advanced Properties (BatteryManager) ---

    private fun getCycleCount(intent: Intent): Availability<Int> {
        // EXTRA_CYCLE_COUNT was added in API 34 (Upside Down Cake)
        return if (Build.VERSION.SDK_INT >= 34) {
            // Use string literal "android.os.extra.CYCLE_COUNT" to avoid unresolved reference issues
            val count = intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
            if (count != -1) Availability.Available(count) else Availability.Unknown
        } else {
            Availability.NotSupported
        }
    }

    private fun getCurrentNow(): Availability<Long> {
        val current = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        return if (current != Long.MIN_VALUE) Availability.Available(current) else Availability.Unknown
    }

    private fun getCurrentAverage(): Availability<Long> {
        val current = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        return if (current != Long.MIN_VALUE) Availability.Available(current) else Availability.Unknown
    }

    private fun getChargeCounter(): Availability<Long> {
        val counter = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        return if (counter != Long.MIN_VALUE) Availability.Available(counter) else Availability.Unknown
    }

    private fun getRemainingEnergyTime(): Availability<Long> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val time = batteryManager.computeChargeTimeRemaining()
            if (time != -1L) Availability.Available(time) else Availability.Unknown
        } else {
            Availability.NotSupported
        }
    }

    private fun isPowerSavingMode(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        return powerManager?.isPowerSaveMode == true
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

package io.techie.volta

import io.techie.volta.enums.ChargingSource
import io.techie.volta.enums.ChargingStatus
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Desktop (JVM) implementation of [BatteryStateProvider].
 *
 * Polls the system for battery information at a fixed interval using [DesktopBatteryReader].
 *
 * @param scope The CoroutineScope used for polling and emitting events.
 */
class DesktopBatteryStateProvider(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
) : BatteryStateProvider {

    private val _battery = MutableStateFlow(BatteryState())
    override val battery: StateFlow<BatteryState> = _battery.asStateFlow()

    private val _chargingEvents = MutableSharedFlow<ChargingStatusChange>(extraBufferCapacity = 1)
    override val chargingEvents: Flow<ChargingStatusChange> = _chargingEvents.asSharedFlow()

    private var pollingJob: Job? = null
    private var lastChargingStatus: ChargingStatus? = null

    companion object {
        private const val POLLING_INTERVAL_MS = 5000L
    }

    override fun observe() {
        if (pollingJob != null) return

        pollingJob = scope.launch {
            while (isActive) {
                updateBatteryState()
                delay(POLLING_INTERVAL_MS)
            }
        }
    }

    override fun stop() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun updateBatteryState() {
        val info = DesktopBatteryReader.read()
        val chargingStatus = determineChargingStatus(info)

        emitChargingEventIfChanged(chargingStatus)

        _battery.value = BatteryState(
            level = info.level,
            isCharging = info.isCharging,
            isLow = info.level?.let { it <= 15 },
            chargingStatus = chargingStatus,
            chargingSource = if (info.isPlugged == true) ChargingSource.AC else ChargingSource.UNKNOWN,
            voltageMv = info.voltageMv?.let { Availability.Available(it) } ?: Availability.Unknown,
            temperatureC = Availability.NotSupported, // Not typically available via standard CLI
            health = Availability.NotSupported,
            technology = info.technology,
            cycleCount = info.cycleCount?.let { Availability.Available(it) } ?: Availability.Unknown,
            currentNowMa = Availability.NotSupported, // Difficult to get consistently across OSs without root/admin
            currentAverageMa = Availability.NotSupported,
            chargeCounterUah = info.chargeCounterUah?.let { Availability.Available(it) } ?: Availability.Unknown,
            remainingEnergyTimeMillis = info.remainingTimeMillis?.let { Availability.Available(it) } ?: Availability.Unknown,
            isPowerSavingMode = info.isPowerSaving,
            isSafeMode = info.isSafeMode,
            isProtected = isProtected(info)
        )
    }

    private fun determineChargingStatus(info: DesktopBatteryReader.Info): ChargingStatus {
        return when {
            info.isCharging == true -> ChargingStatus.CHARGING
            info.isPlugged == true && info.level == 100 -> ChargingStatus.FULL
            info.isPlugged == true && info.isCharging == false -> ChargingStatus.DISCHARGING
            else -> ChargingStatus.DISCHARGING
        }
    }

    private fun isProtected(info: DesktopBatteryReader.Info): Boolean {
        return (info.isPlugged == true) && (info.isCharging == false) && ((info.level ?: 0) < 100)
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

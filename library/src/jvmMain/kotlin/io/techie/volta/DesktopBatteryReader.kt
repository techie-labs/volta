package io.techie.volta

import java.util.Locale

/**
 * Facade for reading battery information on Desktop (JVM).
 * Delegates to platform-specific implementations.
 */
internal object DesktopBatteryReader {

    data class Info(
        val level: Int? = null,
        val isCharging: Boolean? = null,
        val isPlugged: Boolean? = null,
        val isPowerSaving: Boolean = false,
        val isSafeMode: Boolean = false,
        val technology: String? = null,
        val cycleCount: Int? = null,
        val voltageMv: Int? = null,
        val chargeCounterUah: Long? = null,
        val remainingTimeMillis: Long? = null,
        val chargingSource: String? = null,
        val temperatureC: Float? = null,
        val currentNowMa: Long? = null,
        val designCapacityMah: Long? = null,
        val maxCapacityMah: Long? = null
    )

    private val reader: PlatformBatteryReader by lazy {
        when {
            isWindows -> WindowsBatteryReader()
            isMac -> MacOSBatteryReader()
            isLinux -> LinuxBatteryReader()
            else -> object : PlatformBatteryReader {
                override fun read() = Info()
            }
        }
    }

    fun read(): Info = reader.read()

    private val isWindows get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("win")
    private val isMac get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("mac")
    private val isLinux get() = System.getProperty("os.name").lowercase(Locale.getDefault()).contains("nux")
}

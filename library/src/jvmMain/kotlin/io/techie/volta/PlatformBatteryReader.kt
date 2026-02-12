package io.techie.volta

/**
 * Interface for platform-specific battery readers.
 */
internal interface PlatformBatteryReader {
    fun read(): DesktopBatteryReader.Info
}

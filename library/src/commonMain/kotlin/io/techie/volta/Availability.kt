package io.techie.volta

/**
 * Represents the availability of a value across platforms.
 *
 * - [Available] → value exists and valid
 * - [NotSupported] → platform does not support this feature
 * - [Unknown] → value not yet resolved / loading
 */
sealed interface Availability<out T> {

    /** Value is available and valid. */
    data class Available<T>(val value: T) : Availability<T>

    /** Feature is not supported by the platform (e.g. temperature on iOS). */
    object NotSupported : Availability<Nothing>

    /** Value is supported but not yet known (e.g. initial state). */
    object Unknown : Availability<Nothing>
}

fun <T> Availability<T>.orNull(): T? = (this as? Availability.Available)?.value
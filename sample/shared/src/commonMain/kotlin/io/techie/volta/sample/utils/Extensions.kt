package io.techie.volta.sample.utils

import io.techie.volta.core.Availability

fun <T> Availability<T>.toStringValue(transform: (T) -> String = { it.toString() }): String {
    return when (this) {
        is Availability.Available -> transform(value)
        is Availability.NotSupported -> "N/A"
        is Availability.Unknown -> "--"
    }
}

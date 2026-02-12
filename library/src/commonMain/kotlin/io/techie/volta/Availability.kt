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

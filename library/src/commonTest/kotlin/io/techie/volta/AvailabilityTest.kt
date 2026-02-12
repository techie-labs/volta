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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@Suppress("MagicNumber")
class AvailabilityTest {

    @Test
    fun testAvailable() {
        val value = 100
        val availability = Availability.Available(value)

        assertIs<Availability.Available<Int>>(availability)
        assertEquals(value, availability.value)
    }

    @Test
    fun testNotSupported() {
        val availability = Availability.NotSupported
        assertIs<Availability.NotSupported>(availability)
    }

    @Test
    fun testUnknown() {
        val availability = Availability.Unknown
        assertIs<Availability.Unknown>(availability)
    }

    @Test
    fun testEquality() {
        assertEquals(Availability.Available(50), Availability.Available(50))
        assertEquals(Availability.NotSupported, Availability.NotSupported)
        assertEquals(Availability.Unknown, Availability.Unknown)
    }
}

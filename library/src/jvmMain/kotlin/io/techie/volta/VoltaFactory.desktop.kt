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

import io.techie.volta.internal.VoltaImpl
import io.techie.volta.provider.DesktopBatteryStateProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

actual object VoltaFactory {
    actual fun create(dispatcher: CoroutineDispatcher): Volta {
        val provider = DesktopBatteryStateProvider(
            scope = CoroutineScope(SupervisorJob() + dispatcher),
        )
        return VoltaImpl(provider, dispatcher)
    }
}

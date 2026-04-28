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
package io.techie.volta.compose
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.techie.volta.VoltaFactory
import io.techie.volta.VoltaSensorState
import io.techie.volta.core.BatteryState

@Composable
actual fun rememberBatteryState(): State<VoltaSensorState<BatteryState>> {
    val context = LocalContext.current
    val volta = remember {
        VoltaFactory.initialize(context)
        VoltaFactory.create()
    }

    DisposableEffect(Unit) {
        volta.startMonitoring()
        onDispose { volta.stopMonitoring() }
    }

    return volta.batteryState.collectAsState()
}

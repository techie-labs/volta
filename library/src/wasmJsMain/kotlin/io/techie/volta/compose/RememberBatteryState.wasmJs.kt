package io.techie.volta.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import io.techie.volta.core.BatteryState
import io.techie.volta.provider.WasmBatteryStateProvider

@Composable
actual fun rememberBatteryState(): State<BatteryState> {
    val provider = remember { WasmBatteryStateProvider() }

    DisposableEffect(provider) {
        provider.observe()
        onDispose { provider.stop() }
    }

    return provider.battery.collectAsState()
}

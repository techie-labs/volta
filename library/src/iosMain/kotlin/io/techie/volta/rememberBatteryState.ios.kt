package io.techie.volta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember

@Composable
actual fun rememberBatteryState(): State<BatteryState> {
    val provider = remember { IOSBatteryStateProvider() }

    DisposableEffect(Unit) {
        provider.observe()
        onDispose { provider.stop() }
    }

    return provider.battery.collectAsState()
}
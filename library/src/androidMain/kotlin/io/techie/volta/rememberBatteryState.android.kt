package io.techie.volta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberBatteryState(): BatteryState {
    val context = LocalContext.current
    val provider = remember { AndroidBatteryStateProvider(context.applicationContext) }

    DisposableEffect(Unit) {
        provider.observe()
        onDispose { provider.stop() }
    }

    return provider.battery.collectAsState().value
}
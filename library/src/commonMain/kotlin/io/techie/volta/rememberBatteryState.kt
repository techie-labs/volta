package io.techie.volta

import androidx.compose.runtime.Composable


@Composable
expect fun rememberBatteryState(): BatteryState

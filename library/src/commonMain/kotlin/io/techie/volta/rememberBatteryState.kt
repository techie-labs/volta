package io.techie.volta

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

/**
 * Creates and remembers a [BatteryState] that updates automatically.
 *
 * @return A [State] object holding the current [BatteryState].
 */
@Composable
expect fun rememberBatteryState(): State<BatteryState>

package io.techie.volta.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.techie.volta.core.Availability
import io.techie.volta.core.BatteryState

@Composable
fun ThermalWarningBanner(
    state: BatteryState,
    modifier: Modifier = Modifier
) {
    // Determine if thermal state is critical (e.g., >= 45°C)
    val isCritical = when (val temp = state.temperatureC) {
        is Availability.Available -> temp.value >= 45.0f
        else -> false
    }

    AnimatedVisibility(
        visible = isCritical,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(Color(0xFFEF4444)) // Red warning background
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Thermal Warning",
                tint = Color.White
            )
            Text(
                text = "Device temperature is critically high. Please cool down the device.",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

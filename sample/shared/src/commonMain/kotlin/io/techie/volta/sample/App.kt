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
package io.techie.volta.sample

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.techie.volta.Availability
import io.techie.volta.BatteryState
import io.techie.volta.rememberBatteryState

@Composable
fun App() {
    val batteryState by rememberBatteryState()

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFFFACC15),
            onPrimary = Color(0xFF1E293B),
            background = Color(0xFF0F172A),
            surface = Color(0xFF1E293B),
            onSurface = Color(0xFFE2E8F0),
            surfaceVariant = Color(0xFF334155),
        ),
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                // Header
                HeaderSection()

                // Main Battery Indicator
                BatteryCircularIndicator(batteryState)

                // Status Chips
                StatusChipSection(batteryState)

                // Detailed Grid
                DetailsGrid(batteryState)

                // Additional Info Section (New States)
                AdditionalInfoSection(batteryState)
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            imageVector = Icons.Filled.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Volta Monitor",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun BatteryCircularIndicator(state: BatteryState) {
    val level = state.level ?: 0
    val isCharging = state.isCharging == true
    val animatedLevel by animateFloatAsState(targetValue = level / 100f)

    val batteryColor by animateColorAsState(
        targetValue = when {
            isCharging -> Color(0xFF3DDC84) // Green when charging
            level <= 20 -> Color(0xFFEF4444) // Red when low
            else -> MaterialTheme.colorScheme.primary
        },
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(240.dp),
    ) {
        // Background Circle
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 24.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        // Progress Circle
        CircularProgressIndicator(
            progress = { animatedLevel },
            modifier = Modifier.fillMaxSize(),
            color = batteryColor,
            strokeWidth = 24.dp,
            trackColor = Color.Transparent,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )

        // Center Text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$level%",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = state.chargingStatus.name.replace("_", " "),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun StatusChipSection(state: BatteryState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state.isPowerSavingMode) {
            StatusChip(
                icon = Icons.Rounded.Eco,
                label = "Power Saver",
                color = Color(0xFFF59E0B),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (state.isProtected) {
            StatusChip(
                icon = Icons.Rounded.Shield,
                label = "Protected",
                color = Color(0xFF3B82F6),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (state.isSafeMode) {
            StatusChip(
                icon = Icons.Rounded.Security,
                label = "Safe Mode",
                color = Color(0xFFEF4444),
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        if (state.isCharging == true) {
            StatusChip(
                icon = Icons.Filled.Power,
                label = "Charging",
                color = Color(0xFF3DDC84),
            )
        } else {
            StatusChip(
                icon = Icons.Filled.PowerOff,
                label = "Discharging",
                color = Color(0xFF64748B),
            )
        }
    }
}

@Composable
fun StatusChip(icon: ImageVector, label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun DetailsGrid(state: BatteryState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Memory,
                label = "Technology",
                value = state.technology ?: "Unknown",
            )
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Refresh,
                label = "Cycles",
                value = state.cycleCount.toStringValue(),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Thermostat,
                label = "Temperature",
                value = state.temperatureC.toStringValue { "$it°C" },
            )
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.ElectricalServices,
                label = "Voltage",
                value = state.voltageMv.toStringValue { "$it mV" },
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Speed,
                label = "Current (Now)",
                value = state.currentNowMa.toStringValue { "$it mA" },
            )
            DetailCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.BatteryChargingFull,
                label = "Capacity",
                value = state.chargeCounterUah.toStringValue { "${it / 1000} mAh" },
            )
        }
    }
}

@Composable
fun AdditionalInfoSection(state: BatteryState) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Additional Info",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            InfoRow(label = "Health Status", value = state.health.toStringValue { it.name })
            InfoRow(label = "Charging Source", value = state.chargingSource.name)
            InfoRow(label = "Current (Avg)", value = state.currentAverageMa.toStringValue { "$it mA" })
            InfoRow(
                label = "Time Remaining",
                value = state.remainingEnergyTimeMillis.toStringValue {
                    val mins = (it / 1000) / 60
                    val hours = mins / 60
                    if (hours > 0) "${hours}h ${mins % 60}m" else "${mins}m"
                },
            )
            InfoRow(label = "Safe Mode", value = if (state.isSafeMode) "Yes" else "No")
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

fun <T> Availability<T>.toStringValue(transform: (T) -> String = { it.toString() }): String {
    return when (this) {
        is Availability.Available -> transform(value)
        is Availability.NotSupported -> "N/A"
        is Availability.Unknown -> "--"
    }
}

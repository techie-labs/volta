package io.techie.volta.sample.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.techie.volta.core.BatteryState
import io.techie.volta.sample.utils.toStringValue

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
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f)),
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

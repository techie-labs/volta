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
package io.techie.volta.sample.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.techie.volta.core.BatteryState
import io.techie.volta.ui.VoltaBatteryIcon

@Composable
fun HeaderSection(state: BatteryState) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth(),
    ) {
        VoltaBatteryIcon(
            state = state,
            modifier = Modifier.size(32.dp).width(64.dp),
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
            strokeCap = StrokeCap.Round,
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

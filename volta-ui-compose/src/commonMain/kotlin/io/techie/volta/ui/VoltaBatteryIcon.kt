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
package io.techie.volta.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.techie.volta.core.BatteryState

@Composable
fun VoltaBatteryIcon(
    state: BatteryState,
    modifier: Modifier = Modifier,
) {
    val level = state.level ?: 0
    val isCharging = state.isCharging == true

    // Animate color based on state
    val batteryColor by animateColorAsState(
        targetValue = when {
            isCharging -> Color(0xFF3DDC84) // Green
            level <= 15 -> Color(0xFFEF4444) // Red
            else -> MaterialTheme.colorScheme.onSurface
        },
    )

    // Animate fill level
    val fillRatio by animateFloatAsState(targetValue = level / 100f)

    Box(modifier = modifier.aspectRatio(2f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = 4.dp.toPx()
            val cornerRadius = CornerRadius(4.dp.toPx())

            // Draw battery body outline
            val bodyWidth = size.width * 0.9f
            drawRoundRect(
                color = batteryColor,
                size = Size(bodyWidth, size.height),
                cornerRadius = cornerRadius,
                style = Stroke(width = strokeWidth),
            )

            // Draw battery terminal (nub)
            val nubWidth = size.width * 0.1f
            val nubHeight = size.height * 0.4f
            drawRoundRect(
                color = batteryColor,
                topLeft = Offset(bodyWidth, (size.height - nubHeight) / 2),
                size = Size(nubWidth, nubHeight),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )

            // Draw inner fill
            val fillPadding = strokeWidth * 1.5f
            val maxFillWidth = bodyWidth - (fillPadding * 2)
            drawRoundRect(
                color = batteryColor,
                topLeft = Offset(fillPadding, fillPadding),
                size = Size(maxFillWidth * fillRatio, size.height - (fillPadding * 2)),
                cornerRadius = CornerRadius(2.dp.toPx()),
            )
        }

        // Draw lightning bolt if charging
        if (isCharging) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = "Charging",
                tint = MaterialTheme.colorScheme.surface,
                modifier = Modifier.padding(2.dp),
            )
        }
    }
}

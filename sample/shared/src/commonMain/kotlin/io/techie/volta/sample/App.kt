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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.techie.volta.VoltaSensorState
import io.techie.volta.compose.rememberBatteryState
import io.techie.volta.sample.components.AdditionalInfoSection
import io.techie.volta.sample.components.BatteryCircularIndicator
import io.techie.volta.sample.components.DetailsGrid
import io.techie.volta.sample.components.DevToolsSection
import io.techie.volta.sample.components.HeaderSection
import io.techie.volta.sample.components.StatusChipSection
import io.techie.volta.ui.ThermalWarningBanner

@Composable
fun App() {
    val sensorState by rememberBatteryState()

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
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                when (val state = sensorState) {
                    is VoltaSensorState.Unknown -> {
                        Text("Loading battery state...", color = Color.White)
                    }
                    is VoltaSensorState.HardwareNotSupported -> {
                        Text("Hardware not supported on this device.", color = Color.White)
                    }
                    is VoltaSensorState.PermissionDenied -> {
                        Text("Permission denied. Check your browser or OS settings.", color = Color.White)
                    }
                    is VoltaSensorState.Error -> {
                        Text("Error: ${state.throwable.message}", color = Color.Red)
                    }
                    is VoltaSensorState.Available -> {
                        val batteryState = state.data
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp),
                        ) {
                            // Thermal Warning
                            ThermalWarningBanner(batteryState)

                            // Header
                            HeaderSection(batteryState)

                            // Main Battery Indicator
                            BatteryCircularIndicator(batteryState)

                            // Status Chips
                            StatusChipSection(batteryState)

                            // Detailed Grid
                            DetailsGrid(batteryState)

                            // Additional Info Section
                            AdditionalInfoSection(batteryState)

                            // DevTools Showcase
                            DevToolsSection(batteryState)
                        }
                    }
                }
            }
        }
    }
}

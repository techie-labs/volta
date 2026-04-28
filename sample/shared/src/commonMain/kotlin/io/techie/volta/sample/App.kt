package io.techie.volta.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.techie.volta.compose.rememberBatteryState
import io.techie.volta.sample.components.AdditionalInfoSection
import io.techie.volta.sample.components.BatteryCircularIndicator
import io.techie.volta.sample.components.DetailsGrid
import io.techie.volta.sample.components.DevToolsSection
import io.techie.volta.sample.components.HeaderSection
import io.techie.volta.sample.components.StatusChipSection

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

                // Additional Info Section
                AdditionalInfoSection(batteryState)

                // DevTools Showcase
                DevToolsSection(batteryState)
            }
        }
    }
}

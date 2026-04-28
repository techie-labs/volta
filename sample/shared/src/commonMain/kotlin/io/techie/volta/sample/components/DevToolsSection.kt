package io.techie.volta.sample.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import io.techie.volta.core.BatteryState
import io.techie.volta.core.BatteryStateProvider
import io.techie.volta.core.ChargingStatusChange
import io.techie.volta.devtools.BatteryProfiler
import io.techie.volta.devtools.ExecutionCondition
import io.techie.volta.devtools.whenOptimal
import io.techie.volta.diagnostics.getDiagnosticDump
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch

// A simple mock provider so we can demonstrate Profiler and Smart Sync interactively in the sample UI
private class MockBatteryStateProvider(initialState: BatteryState) : BatteryStateProvider {
    val mutableState = MutableStateFlow(initialState)
    override val battery: StateFlow<BatteryState> = mutableState
    override val chargingEvents: Flow<ChargingStatusChange> = emptyFlow()
    override fun observe() {}
    override fun stop() {}
    
    // Allow updating the mock state when the real state changes
    fun updateState(newState: BatteryState) {
        mutableState.value = newState
    }
}

@Composable
fun DevToolsSection(state: BatteryState) {
    var dumpText by remember { mutableStateOf<String?>(null) }

    // State for Profiler and Smart Sync
    val scope = rememberCoroutineScope()
    val mockProvider = remember { MockBatteryStateProvider(state) }
    
    // Keep mock provider in sync with actual device state
    LaunchedEffect(state) {
        mockProvider.updateState(state)
    }

    val profiler = remember { BatteryProfiler(mockProvider) }
    var profilerLog by remember { mutableStateOf("Profiler is ready.") }
    var isProfiling by remember { mutableStateOf(false) }
    
    var syncLog by remember { mutableStateOf("Smart Sync is ready.") }
    var isSyncing by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. Diagnostic Dump Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "1. Diagnostic Dump",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Instantly get a flat map of the battery state for logging or crash reporting.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(
                    onClick = {
                        val dump = state.getDiagnosticDump()
                        dumpText = dump.entries.joinToString(separator = "\n") { "${it.key}: ${it.value}" }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Generate Diagnostic Dump", color = MaterialTheme.colorScheme.onPrimary)
                }
                if (dumpText != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = dumpText!!,
                            modifier = Modifier.padding(12.dp),
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = TextUnit(12f, TextUnitType.Sp)
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Battery Profiler Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "2. Battery Profiler",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Track battery consumption and duration for specific user sessions or tasks.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                
                Text(
                    text = profilerLog,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            profiler.startSession("DemoSession")
                            isProfiling = true
                            profilerLog = "Session 'DemoSession' started...\nWait a bit or let your battery drop, then stop the session."
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isProfiling,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Start Session", color = MaterialTheme.colorScheme.onPrimary)
                    }
                    Button(
                        onClick = {
                            val report = profiler.stopSession("DemoSession")
                            isProfiling = false
                            if (report != null) {
                                val drop = (report.startBatteryPercent ?: 0) - (report.endBatteryPercent ?: 0)
                                profilerLog = "Session Stopped!\nDuration: ${report.duration}\nBattery Drop: $drop%"
                            } else {
                                profilerLog = "Session was not started properly."
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isProfiling,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Stop Session", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        // 3. Smart Sync Card
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "3. Smart Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Safely execute heavy background tasks only when hardware conditions are optimal (e.g. Level > 20%, Temp < 40C).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                
                Text(
                    text = syncLog,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )

                Button(
                    onClick = {
                        isSyncing = true
                        syncLog = "Waiting for optimal conditions...\n(Min Battery: 20%, Not in Power Saving Mode)"
                        scope.launch {
                            val condition = ExecutionCondition(
                                minBatteryLevel = 20,
                                ignorePowerSavingMode = false
                            )
                            mockProvider.whenOptimal(condition) {
                                syncLog = "Conditions met! ✅\nExecuting heavy background task... Done!"
                                isSyncing = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSyncing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Execute Task Safely", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

<p align="center">
  <img src="banner.svg" width="100%" alt="Volta Banner">
</p>

<p align="center">
  <a href="https://github.com/techie-labs/Volta/actions/workflows/build.yml"><img src="https://github.com/techie-labs/Volta/actions/workflows/build.yml/badge.svg" alt="Build Status"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square" alt="License"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF.svg?style=flat-square&logo=kotlin" alt="Kotlin"></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose%20Multiplatform-1.6.10-4285F4.svg?style=flat-square&logo=jetpackcompose" alt="Compose Multiplatform"></a>
  <a href="https://central.sonatype.com/artifact/io.github.techie-labs/volta"><img src="https://img.shields.io/maven-central/v/io.github.techie-labs/volta?style=flat-square" alt="Maven Central"></a>
</p>

# Volta - Compose Multiplatform Battery Library

## Introduction

Volta is a powerful **compose multiplatform battery library** and **Kotlin Multiplatform (KMP)** solution designed to simplify hardware monitoring across all major platforms. Whether you need to access battery data in your core business logic (via pure KMP) or build reactive user interfaces (via Compose Multiplatform), Volta provides the right tools. While it is highly optimized for mobile—eliminating the need to write separate, platform-specific boilerplate code for Android and iOS developers—it extends its capabilities much further. It provides a unified API to easily access real-time battery health, charging status, and advanced diagnostics across **Android**, **iOS (Arm64 & Apple Silicon)**, **Desktop (JVM)**, and **Web (WasmJs)**.

## Features

### KMP Battery Manager
Our library is built to support both pure Kotlin Multiplatform (KMP) projects and modern Compose Multiplatform UI architectures. It offers lightweight, native performance without forcing a UI framework on your business logic.

### Kotlin Multiplatform Battery Status & Diagnostics
*   **Unified API**: One interface to monitor battery status across **Android**, **iOS** (Arm64), **Desktop** (JVM), and **Web** (Wasm).
*   **Reactive State**: Built on Kotlin `StateFlow` for seamless, real-time integration with your applications.
*   **Deep Diagnostics**: Access advanced data like Cycle Count, Current (mA), Voltage, Temperature, and Battery Technology.
*   **Smart Detection**: Automatically detect Power Saving Mode, Safe Mode, and Protected Battery limits.
*   **Plug-and-Play Widgets**: Includes the `rememberBatteryState()` hook and pre-built components for instant UI updates.

## Installation (Maven Central)

Add Volta to your `commonMain` dependencies in your `build.gradle.kts` file:

```kotlin
commonMain.dependencies {
    // 1. Core Kotlin Multiplatform logic (Required - no UI dependencies)
    implementation("io.github.techie-labs:volta:1.0.0-rc01")
    
    // 2. Optional: Compose Multiplatform Widgets & State hooks
    implementation("io.github.techie-labs:volta-ui-compose:1.0.0-rc01")
    
    // Optional: Mock provider for Previews and Unit Tests
    implementation("io.github.techie-labs:volta-mock:1.0.0-rc01")
}
```

*Note: For Android business logic usage without Compose, initialize via `VoltaFactory.initialize(this)` in your `Application` class.*

## Usage (Code snippet)

Here is a quick example demonstrating how to observe the battery status in your UI:

```kotlin
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import io.techie.volta.compose.rememberBatteryState
import io.techie.volta.VoltaSensorState

@Composable
fun BatteryDashboard() {
    // Access real-time battery status using the library
    val sensorState by rememberBatteryState()

    when (sensorState) {
        is VoltaSensorState.Loading -> Text("Connecting to battery sensor...")
        is VoltaSensorState.PermissionDenied -> Text("⚠️ Please grant battery permissions.")
        is VoltaSensorState.HardwareNotSupported -> Text("⚠️ Battery sensor not supported on this device.")
        is VoltaSensorState.Available -> {
            val battery = (sensorState as VoltaSensorState.Available).data
            Column {
                Text("Battery Level: ${battery.level}%")
                Text("Charging Status: ${battery.chargingStatus}")

                if (battery.isPowerSavingMode) {
                    Text("⚠️ Low Power Mode is ON")
                }
            }
        }
        else -> Text("Unknown sensor state")
    }
}
```

## License

Volta is open-source software licensed under the [Apache 2.0 License](LICENSE).

---
<p align="center">
  Built with ⚡ by <a href="https://github.com/techie-labs">Techie Labs</a>
</p>

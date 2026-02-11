<p align="center">
  <img src="banner.svg" width="100%" alt="Volta Banner">
</p>

<p align="center">
  <a href="https://github.com/techie-labs/Volta/actions"><img src="https://img.shields.io/github/actions/workflow/status/techie-labs/Volta/build.yml?branch=main&logo=github&style=flat-square" alt="Build Status"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square" alt="License"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF.svg?style=flat-square&logo=kotlin" alt="Kotlin"></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose%20Multiplatform-1.6.10-4285F4.svg?style=flat-square&logo=jetpackcompose" alt="Compose Multiplatform"></a>
  <a href="https://central.sonatype.com/artifact/io.techie.volta/volta"><img src="https://img.shields.io/maven-central/v/io.techie.volta/volta?style=flat-square" alt="Maven Central"></a>
</p>

# Volta ⚡

**Volta** is the ultimate battery intelligence library for **Kotlin Multiplatform** and **Compose Multiplatform**.

Designed for modern apps that live everywhere, Volta provides a unified, reactive API to access deep battery diagnostics across **Android**, **iOS**, **Desktop** (Windows, macOS, Linux), and **Web** (Wasm - *Coming Soon*).

Stop writing platform-specific battery code. Let Volta handle the complexity.

## ✨ Why Volta?

*   **Write Once, Monitor Everywhere**: Unified API for all platforms.
*   **Reactive by Design**: Built on `StateFlow`, perfect for modern UI architectures.
*   **Deep Diagnostics**: Go beyond just battery level. Access **Cycle Count**, **Current (mA)**, **Voltage**, **Temperature**, and **Technology**.
*   **Smart Detection**: Automatically detects **Power Saving Mode**, **Safe Mode**, and **Protected Battery** states.
*   **Compose First**: Includes `rememberBatteryState()` for instant UI integration.

## 📦 Installation

Add the dependency to your `commonMain` source set in `build.gradle.kts`:

```kotlin
commonMain.dependencies {
    implementation("io.techie.volta:volta:1.0.0")
}
```

## 🚀 Quick Start

### 1. Compose Multiplatform (The Easy Way)

Just use the `rememberBatteryState()` composable to get a reactive state object.

```kotlin
import androidx.compose.runtime.getValue
import io.techie.volta.rememberBatteryState

@Composable
fun BatteryDashboard() {
    val battery by rememberBatteryState()

    Column {
        // Basic Info
        Text("Level: ${battery.level}%")
        Text("Status: ${battery.chargingStatus}") // Charging, Discharging, Full

        // Smart Alerts
        if (battery.isPowerSavingMode) {
            Text("⚠️ Low Power Mode Active")
        }
        if (battery.isProtected) {
            Text("🛡️ Battery Protection Active (80% Limit)")
        }

        // Advanced Diagnostics (Check availability first!)
        battery.cycleCount.value?.let { cycles ->
            Text("Cycle Count: $cycles")
        }
        battery.currentNowMa.value?.let { current ->
            Text("Current Flow: ${current}mA")
        }
    }
}
```

### 2. Kotlin Multiplatform (Business Logic)

Inject `BatteryStateProvider` into your ViewModels or UseCases.

```kotlin
class DeviceMonitor(private val batteryProvider: BatteryStateProvider) {
    
    fun startMonitoring() {
        batteryProvider.observe() // Start listening to system broadcasts
        
        scope.launch {
            batteryProvider.battery.collect { state ->
                if (state.level != null && state.level!! < 20) {
                    println("Low Battery Warning!")
                }
            }
        }
    }
    
    fun stop() {
        batteryProvider.stop() // Clean up resources
    }
}
```

## 📱 Platform Requirements & Permissions

### 🤖 Android
Add these permissions to your `AndroidManifest.xml` to unlock full capabilities:

```xml
<!-- Required: Basic stats (Level, Status, Plugged) -->
<uses-permission android:name="android.permission.BATTERY_STATS" />

<!-- Optional: Enhanced Power Saver detection -->
<uses-permission android:name="android.permission.POWER_SAVER" />
```
*   **Min SDK**: 24 (Android 7.0)
*   **Cycle Count**: Requires Android 14 (API 34)+.

### 🍎 iOS
No permissions required!
*   **Privacy Note**: iOS restricts access to advanced hardware stats. Fields like *Cycle Count*, *Current*, and *Temperature* will return `Availability.NotSupported`.

### 🖥️ Desktop (JVM)
Volta uses native system tools under the hood:
*   **Windows**: Uses `wmic` and `powercfg`. No admin rights needed usually.
*   **macOS**: Uses `pmset` and `system_profiler`.
*   **Linux**: Reads from `/sys/class/power_supply/`.

## 📊 Feature Matrix

| Feature | Android 🤖 | iOS 🍎 | Windows 🪟 | macOS 🍏 | Linux 🐧 |
| :--- | :---: | :---: | :---: | :---: | :---: |
| **Level & Status** | ✅ | ✅ | ✅ | ✅ | ✅ |
| **Power Saving** | ✅ | ✅ | ✅ | ✅ | ❌ |
| **Voltage** | ✅ | ❌ | ✅ | ❌ | ✅ |
| **Temperature** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Technology** | ✅ | ❌ | ✅ | ❌ | ✅ |
| **Cycle Count** | ✅ (14+) | ❌ | ❌ | ✅ | ✅ |
| **Current (mA)** | ✅ | ❌ | ❌ | ❌ | ❌ |
| **Safe Mode** | ✅ | ❌ | ✅ | ✅ | ✅ |

## 🤝 Contributing

We love contributions! Whether it's a bug fix, new feature, or just better documentation.
Check out [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

## 📄 License

Volta is proudly open-source under the [Apache 2.0 License](LICENSE).

---
<p align="center">
  Built with ⚡ by Techie Labs
</p>

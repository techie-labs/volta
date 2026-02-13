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

# Volta ⚡ — Kotlin Multiplatform Battery Library

**Volta** is a powerful, reactive battery intelligence library for **Kotlin Multiplatform (KMP)** and **Compose Multiplatform**. 

Monitor battery health, charging status, and advanced diagnostics across **Android**, **iOS**, and **Desktop** (Windows, macOS, Linux) with a single, unified API. Stop writing platform-specific code for battery monitoring and start using Volta.

## ✨ Key Features

*   **Unified KMP API**: One interface for Android, iOS, and Desktop battery monitoring.
*   **Reactive State Management**: Built on Kotlin `StateFlow` for seamless integration with modern UI architectures.
*   **Deep Battery Diagnostics**: Access advanced data like **Cycle Count**, **Current (mA)**, **Voltage**, **Temperature**, and **Battery Technology**.
*   **Smart Status Detection**: Automatically detect **Power Saving Mode**, **Safe Mode**, and **Protected Battery** (80% limit) states.
*   **Compose Multiplatform Ready**: Includes the `rememberBatteryState()` hook for instant UI updates.
*   **Lightweight & Native**: Uses native system APIs (Android BatteryManager, iOS UIDevice, Windows WMIC, macOS pmset) for maximum efficiency.

## 📦 Installation

Add Volta to your `commonMain` dependencies in your `build.gradle.kts` file:

```kotlin
commonMain.dependencies {
    implementation("io.github.techie-labs:volta:1.0.0-beta01")
}
```

## 🚀 Quick Start

### 1. Using with Compose Multiplatform

Get reactive battery updates in your UI with just one line of code:

```kotlin
import androidx.compose.runtime.getValue
import io.techie.volta.rememberBatteryState

@Composable
fun BatteryDashboard() {
    val battery by rememberBatteryState()

    Column {
        Text("Battery Level: ${battery.level}%")
        Text("Charging Status: ${battery.chargingStatus}")

        if (battery.isPowerSavingMode) {
            Text("⚠️ Low Power Mode is ON")
        }
    }
}
```

### 2. Using in Kotlin Multiplatform (ViewModel/Logic)

Observe battery changes in your business logic:

```kotlin
class BatteryViewModel(private val batteryProvider: BatteryStateProvider) {
    init {
        batteryProvider.observe()
        
        viewModelScope.launch {
            batteryProvider.battery.collect { state ->
                println("Current Level: ${state.level}%")
            }
        }
    }
}
```

## 📱 Platform Support & Permissions

### 🤖 Android
*   **Min SDK**: 24 (Android 7.0)
*   **Permissions**: Add `<uses-permission android:name="android.permission.BATTERY_STATS" />` for full diagnostics.
*   **Note**: Cycle Count requires Android 14+.

### 🍎 iOS
*   **Permissions**: None required.
*   **Note**: iOS limits access to hardware-level stats like temperature and voltage for third-party apps.

### 🖥️ Desktop (JVM)
*   **Windows**: Uses `wmic` and `powercfg`.
*   **macOS**: Uses `pmset` and `system_profiler`.
*   **Linux**: Reads from `/sys/class/power_supply/`.

## 📊 Feature Matrix

| Feature | Android 🤖 | iOS 🍎 | Windows 🪟 | macOS 🍏 |
| :--- | :---: | :---: | :---: |:--------:|
| **Level & Status** | ✅ | ✅ | ✅ |    ✅     |
| **Power Saving** | ✅ | ✅ | ❌ |    ❌     |
| **Voltage** | ✅ | ❌ | ❌ |    ✅     |
| **Temperature** | ✅ | ❌ | ❌ |    ✅     |
| **Technology** | ✅ | ❌ | ❌ |    ✅     |
| **Cycles** | ✅ (14+) | ❌ | ✅ |    ✅     |
| **Current (Now)** | ✅ | ❌ | ❌ |    ✅     |
| **Safe Mode** | ✅ | ❌ | ❌ |    ❌     |
| **Time Remaining** | ❌ | ❌ | ❌ |    ❌     |
| **Capacity** | ✅ | ❌ | ❌ |     ✅     |

## 🧪 Testing

Volta includes comprehensive unit tests for its core logic. The CI pipeline runs these tests on every push to ensure stability.

```bash
./gradlew allTests
```

## 🤝 Contributing

Contributions are welcome! Please read our [CONTRIBUTING.md](CONTRIBUTING.md) to get started.

## 📄 License

Volta is open-source software licensed under the [Apache 2.0 License](LICENSE).

---
<p align="center">
  Built with ⚡ by <a href="https://github.com/techie-labs">Techie Labs</a>
</p>

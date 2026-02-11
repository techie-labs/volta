<p align="center">
  <img src="banner.svg" width="100%" alt="Kameleoon Banner">
</p>

<p align="center">
  <a href="https://github.com/techie-labs/Kameleoon/actions"><img src="https://img.shields.io/github/actions/workflow/status/techie-labs/Kameleoon/build.yml?branch=main&logo=github&style=flat-square" alt="Build Status"></a>
  <a href="https://opensource.org/licenses/Apache-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square" alt="License"></a>
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.0-7F52FF.svg?style=flat-square&logo=kotlin" alt="Kotlin"></a>
  <a href="https://www.jetbrains.com/lp/compose-multiplatform/"><img src="https://img.shields.io/badge/Compose%20Multiplatform-1.6.10-4285F4.svg?style=flat-square&logo=jetpackcompose" alt="Compose Multiplatform"></a>
</p>

# Kameleoon 🦎

**Kameleoon** (derived from Chameleon) is an adaptive and comprehensive template for building **Compose Multiplatform Libraries** targeting Android, iOS, Desktop (JVM), and Web (Wasm). Just like a chameleon adapts to its environment, this template helps your UI code adapt seamlessly across platforms.

## Project Structure

* **`/library`**: The core module containing your shared library code.
    * `commonMain`: Code shared across all platforms.
    * `androidMain`, `iosMain`, `jvmMain`, `wasmJsMain`: Platform-specific implementations.
* **`/sample`**: A sample application to demonstrate and test your library.
    * Depends on the `:library` module.
    * Runs on Android, Desktop, and Web.

## Features & Tools included

* **Kotlin Multiplatform**: Pre-configured for Android, iOS, Desktop, and Web (Wasm).
* **Compose Multiplatform**: UI framework ready.
* **Maven Publish Plugin**: Easy publishing to Maven Central using `vanniktech/gradle-maven-publish-plugin`.
* **Binary Compatibility Validator**: Ensures your library's public API remains stable.
* **Dokka**: Generates API documentation.
* **Spotless**: Enforces code formatting (Ktlint) and license headers.
* **Detekt**: Static code analysis for Kotlin.
* **GitHub Actions**: CI/CD workflows for building, testing, and checking code quality.
* **Helper Scripts**: Easy setup and workflow management scripts in `/scripts`.

## Getting Started

### 1. Setup Environment

Run the setup script to prepare your local environment:
```shell
./scripts/setup.sh
```
This will create a `local.properties` file from a template, ensure `gradlew` is executable, and setup git hooks.

### 2. Rename and Configure
Update `library/build.gradle.kts` with your library's details:
* `mavenPublishing` block: Update `groupId`, `artifactId`, `version`, and `pom` details (licenses, developers, SCM).
* `android` block: Update `namespace`.

Update `spotless/copyright.kt` with your license header.

### 3. Build and Run Sample

**Android:**
```shell
./gradlew :sample:assembleDebug
```

**Desktop:**
```shell
./gradlew :sample:run
```

**Web (Wasm):**
```shell
./gradlew :sample:wasmJsBrowserRun
```

### 4. Code Quality Checks

Run the full workflow script to check everything at once:
```shell
./scripts/run-workflow.sh
```

Or run individually:
*   **Format Code:** `./gradlew spotlessApply`
*   **Static Analysis:** `./gradlew detekt`
*   **API Check:** `./gradlew apiCheck`
*   **Documentation:** `./gradlew dokkaHtml`

### 5. Publishing

To publish to Maven Central, you need to configure your Sonatype credentials.
The project is set up to use the `vanniktech` plugin. Refer to the [plugin documentation](https://github.com/vanniktech/gradle-maven-publish-plugin) for setting up secrets (GPG key, Sonatype username/password).

A manual workflow is available at `.github/workflows/publish.yml`.

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

---

## License

[Add your license here]

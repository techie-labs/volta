# Contributing to Volta

Thank you for your interest in contributing to Volta! We welcome contributions from everyone.

## Getting Started

1.  **Fork the repository** on GitHub.
2.  **Clone your fork** locally.
3.  **Run the setup script** to prepare your environment:
    ```bash
    ./scripts/setup.sh
    ```
4.  **Open the project** in Android Studio or IntelliJ IDEA.

## Development Workflow

1.  Create a new branch for your feature or bug fix:
    ```bash
    git checkout -b feature/my-awesome-feature
    ```
2.  Make your changes.
3.  **Verify your changes**:
    *   Run code formatting: `./gradlew spotlessApply`
    *   Run static analysis: `./gradlew detekt`
    *   Run tests: `./gradlew allTests`
    *   Check API compatibility: `./gradlew apiCheck`
4.  Commit your changes. Please follow [Conventional Commits](https://www.conventionalcommits.org/).
5.  Push your branch and open a **Pull Request**.

## Architecture Guidelines

Volta uses an **Interface-Driven** architecture to support dependency injection and mocking:
*   **Do not expose implementation classes directly.** All platform-specific code (e.g., `AndroidBatteryStateProvider`) should be kept `internal`.
*   **Use `VoltaSensorState` for hardware data.** Never emit raw `BatteryState` directly; always wrap it in `VoltaSensorState.Available`, `Error`, or `PermissionDenied` so UI can handle edge cases gracefully.
*   **Factory Pattern:** The main entry point is `VoltaFactory.create()`. When adding a new target, ensure `expect object VoltaFactory` has a corresponding `actual` implementation.

## Code Style

We use **Spotless** (with Ktlint) and **Detekt** to enforce code style and quality.
Please ensure your code passes these checks before submitting a PR.

## License

By contributing, you agree that your contributions will be licensed under the project's license.

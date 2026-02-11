# Contributing to Kameleoon

Thank you for your interest in contributing to Kameleoon! We welcome contributions from everyone.

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

## Code Style

We use **Spotless** (with Ktlint) and **Detekt** to enforce code style and quality.
Please ensure your code passes these checks before submitting a PR.

## License

By contributing, you agree that your contributions will be licensed under the project's license.

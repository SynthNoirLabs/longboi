# Longboi Launcher

A modern, minimalist, and AI-ready Android launcher built with Jetpack Compose. Inspired by Niagara but evolving into its own unique "signature feel."

## 🚀 Architecture

The project follows a **modular, UDF-based architecture** designed for scalability and testability.

### Module Structure

- **`:app`**: The main application entry point and dependency injection root.
- **`:feature:*`**: Feature-specific modules (e.g., `:feature:home`, `:feature:allapps`, `:feature:settingsui`).
- **`:core:*`**: Shared business logic, data, and design system modules (e.g., `:core:data`, `:core:designsystem`, `:core:common`).
- **`:benchmark`**: Macrobenchmarks and performance tracking.

### Core Stack

- **UI**: Jetpack Compose with Material 3.
- **DI**: Hilt (Dependency Injection).
- **Async**: Kotlin Coroutines and Flow.
- **Data**: Proto DataStore for settings and preferences.
- **Image Loading**: Coil for optimized icon and image rendering.

## 🛠 Quality Infrastructure

We maintain high code quality standards through automated tooling:

- **Testing**:
  - **Unit Tests**: Full coverage for ViewModels and business logic (JVM/Robolectric).
  - **Screenshot Tests**: Visual regression testing for UI components.
  - **Macrobenchmarks**: Performance monitoring for startup and scroll.
- **Static Analysis & Linting**:
  - **Spotless + Ktlint**: Automated code formatting enforcement.
  - **Android Lint**: Standard Android static analysis.
- **Coverage**: **Kover** integration for project-wide coverage measurement.

## 📈 Development Commands

| Task | Command |
| :--- | :--- |
| **Run All Tests** | `./gradlew testDebugUnitTest` |
| **Check Formatting** | `./gradlew spotlessCheck` |
| **Fix Formatting** | `./gradlew spotlessApply` |
| **Coverage Report** | `./gradlew koverHtmlReport` |
| **Full Build** | `./gradlew assembleDebug` |

## 📦 Getting Started

1. Clone the repository.
2. Open in Android Studio (Ladybug or later recommended).
3. Build and run on a device or emulator.
4. Set as the default home launcher when prompted.

## 🛡 Permissions

- `QUERY_ALL_PACKAGES`: Essential for enumerating installed applications.

## License

Copyright 2025 Longboi Launcher. Licensed under the Apache License, Version 2.0.

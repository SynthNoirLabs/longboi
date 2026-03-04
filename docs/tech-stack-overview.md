---
description: Longboi Launcher Tech Stack Overview
---

# Longboi Launcher — Tech Stack & Architecture (Self-Contained)

## Overview
Longboi Launcher is a modern, minimalist, and AI-ready Android launcher built with Jetpack Compose. The project is designed for scalability, testability, and performance, using a modular architecture and a strict Unidirectional Data Flow (UDF) approach.

## Core Architecture
- **Modular, layered structure**: Feature modules depend on shared core modules. The app module hosts the app entry point and dependency injection root.
- **UDF (Unidirectional Data Flow)**: ViewModels expose immutable **State** and accept **Events**. UI is **stateless**, with state hoisted into ViewModels.
- **Separation of concerns**: UI in Compose, domain logic in core modules, persistence in datastore/Room, and DI via Hilt.

## Module Layout
- **`:app`** — Application entry point, dependency injection root, navigation setup.
- **`:core:*`** — Shared modules (data, design system, models, settings, icons, security, widgets, notifications, etc.).
- **`:feature:*`** — Feature modules (home, all apps, search UI, settings UI, private space, onboarding, backup, widget picker).
- **`:benchmark` / `:baselineprofile`** — Performance tooling and baseline profiling.

## Key Tech Stack (Versions)
### Build & Language
- Android Gradle Plugin (AGP): **8.9.0**
- Kotlin: **2.0.21**
- KSP: **2.0.21-1.0.25**

### UI
- Jetpack Compose BOM: **2026.01.01**
- Material 3
- Activity Compose: **1.12.3**
- AndroidX Lifecycle: **2.10.0**

### Dependency Injection
- Hilt: **2.51.1**

### Data & Persistence
- DataStore (Proto): **1.2.0**
- Protobuf: **3.25.1**
- Room: **2.8.4**

### Images & Media
- Coil: **2.7.0** (used for app icons and images)

### Testing & Quality
- JUnit: **4.13.2**
- Robolectric: **4.16.1**
- Espresso: **3.7.0**
- Screenshot Testing: **1.6.0-alpha08**
- Macrobenchmark: **1.4.1**
- Spotless: **8.2.1**
- Detekt: **1.23.8**
- Kover: **0.9.5**

## Gradle & Build Settings Highlights
- **AndroidX enabled**, **Jetifier disabled**.
- **Non-transitive R** and **resource optimizations** enabled.
- **Build cache** and **configuration cache** enabled.
- **K2 UAST lint** enabled for Kotlin 2.x compatibility.

## Quality & Testing Strategy
- **Unit tests** for ViewModels and business logic (JVM/Robolectric).
- **Screenshot tests** for UI regression coverage.
- **Macrobenchmarks** for startup and scroll performance.
- **Static analysis** with Spotless + Detekt.
- **Coverage reporting** with Kover.

## Development Commands (Common)
- Run unit tests: `./gradlew testDebugUnitTest`
- Formatting check: `./gradlew spotlessCheck`
- Format code: `./gradlew spotlessApply`
- Coverage report: `./gradlew koverHtmlReport`
- Build debug: `./gradlew assembleDebug`

## Design & UX Principles
- Stateless composables, data-driven UI.
- Minimalist, focused launcher experience.
- Performance-forward (benchmarks + baseline profiles).

---
If you want this tailored for onboarding or as a one-page PDF, I can generate that too.

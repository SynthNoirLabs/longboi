---
name: project-context
type: memory
scope: project
---

# Longboi Launcher - Project Context

## Overview

Longboi Launcher is a modern, minimalist Android launcher inspired by Niagara but evolving into its own unique "signature feel." It's built with modern Android stack focusing on performance and AI integration.

## Architecture

- **Modular Design**: `core:*` and `feature:*` module structure
- **UDF Pattern**: Unidirectional Data Flow for all ViewModels
- **Compose UI**: Stateless composables with state hoisting
- **Dependency Injection**: Hilt for DI management

## Key Technologies

- **AGP**: 8.7.3 (Stable)
- **Kotlin**: 2.0.21 with Compose Compiler
- **Compose BOM**: 2025.12.00
- **Hilt**: 2.52 for DI
- **DataStore**: Proto DataStore for type-safe settings

## Module Structure

```
text
com.longboilauncher.app/
├── app/                    # Main application module
├── core/
│   ├── designsystem/       # UI components and theme
│   ├── common/            # Shared utilities
│   ├── data/              # Repository layer
│   └── network/           # API layer
└── feature/
    ├── home/              # Home screen
    ├── settings/          # Settings feature
    └── [other features]/
```

## Coding Standards

- Package names follow `com.longboilauncher.app.*`
- All ViewModels use UDF: State flows down, Events flow up
- Composables are stateless
- Use Coil for image loading with caching
- No hardcoded strings in UI

## Build Configuration

- Always run `./gradlew help` after build changes
- Use Version Catalog for dependencies
- Enable R8 for release builds
- Configure ProGuard for security

## Testing Strategy

- Unit tests for ViewModels (state transitions) using Turbine for Flow testing
- Screenshot tests for UI (Compose Preview or Roborazzi)
- Use Truth for assertions, MockK for mocking (prefer fakes)
- Robolectric for Android framework testing
- Instrumentation tests for critical flows
- Macrobenchmark for performance (cold/warm start, navigation)
- Kover for coverage measurement
- See docs/plans/TESTING_PLAN.md for complete testing strategy

## Current Focus

- Implementing new feature modules
- Performance optimization for scrolling
- AI-powered app recommendations
- Enhancing user customization options

## Important Notes

- Never refer to the app as "Niagara" or "Cascade" - always "Longboi Launcher"
- Follow strict UDF pattern for all new features
- All new features must include basic unit tests
- Use Proto DataStore for all settings storage

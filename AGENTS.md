# Longboi Launcher — Global Project Instructions

You are working on **Longboi Launcher**, a modern, minimalist, and AI-ready Android launcher inspired by Niagara but evolving into its own unique "signature feel."

## Core Project Rules

- **Branding**: Always refer to the app as **Longboi Launcher**. Avoid the old name "Niagara" or "Cascade."
- **Tech Stack**:
  - **AGP**: 8.7.3 (Stable)
  - **Kotlin**: 2.0.21 (Compose Compiler integrated)
  - **Compose BOM**: 2025.12.00
  - **Hilt**: 2.52 (Dependency Injection)
  - **DataStore**: Proto DataStore for settings (type-safe).
- **Architecture**:
  - **Modularization**: Follow the `core:*` and `feature:*` module structure.
  - **UDF**: All ViewModels must follow the Unidirectional Data Flow pattern (State + Event).
  - **Stateless UI**: Composables should be stateless; hoist state to ViewModels.
- **Naming Conventions**:
  - Package: `com.longboilauncher.app`
  - Modules: `core:designsystem`, `feature:home`, etc.
- **Verification**: Always run `./gradlew help` after build changes to ensure environment stability.

## Code Quality Guardrails

- No hardcoded strings in UI; use `strings.xml`.
- Use Coil for image/icon loading with proper caching.
- All new features must include a basic unit test following the UDF state machine pattern.
- Follow the "Testing Plan" for UI verification (Compose Preview Screenshot Testing).

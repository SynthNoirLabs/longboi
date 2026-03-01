# Longboi Launcher — Global Project Instructions

You are working on **Longboi Launcher**, a modern, minimalist, and AI-ready Android launcher inspired by Niagara but evolving into its own unique "signature feel."

## Core Project Rules

### Branding
- **Name**: Always refer to the app as **Longboi Launcher**
- **Legacy Names**: Avoid using "Niagara" or "Cascade" (old working names)
- **Package**: `com.longboilauncher.app`

### Tech Stack (Current - Feb 2026)

**Build Tools:**
- **AGP**: 8.13.2 (Android Gradle Plugin)
- **Gradle**: 8.13
- **Kotlin**: 2.3.0 (with integrated Compose Compiler)
- **KSP**: 2.3.4 (Kotlin Symbol Processing)
- **Java**: 17 (source/target compatibility)

**Core Libraries:**
- **Compose BOM**: 2026.01.00 (latest stable)
- **Hilt**: 2.58 (Dependency Injection)
- **Lifecycle**: 2.10.0 (ViewModel, Runtime)
- **Activity Compose**: 1.12.2
- **Core KTX**: 1.17.0
- **DataStore**: 1.2.0 (Proto DataStore for type-safe settings)
- **Room**: 2.8.4 (Database)
- **Coil**: 2.5.0 (Image loading)
- **Kotlin Serialization**: 1.6.2

**Testing & Quality:**
- **JUnit**: 4.13.2
- **MockK**: 1.14.7
- **Turbine**: 1.1.0 (Flow testing)
- **Robolectric**: 4.14.1
- **Truth**: 1.4.4 (Assertions)
- **Coroutines Test**: 1.9.0
- **Kover**: 0.8.3 (Code coverage)
- **Spotless**: 7.0.2 (Code formatting)

### Architecture

**Modularization:**
- Follow strict `core:*` and `feature:*` module structure
- Core modules provide reusable functionality
- Feature modules are UI-focused and depend on core modules
- App module is the entry point and integration layer

**Active Modules:**
```
app/                      → Main entry point, Hilt root, MainActivity
benchmark/                → Macrobenchmarks

core/
  ├── model/             → Data classes, domain models
  ├── common/            → Shared utilities, extensions
  ├── designsystem/      → Theme, components, animations
  ├── appcatalog/        → LauncherApps API wrapper
  ├── datastore-proto/   → Proto definitions
  ├── datastore/         → DataStore repositories
  ├── settings/          → Settings management
  └── icons/             → Icon loading, icon pack support

feature/
  ├── home/              → Home screen, favorites, pop-ups
  ├── allapps/           → Alphabetical app list with scrubber
  ├── searchui/          → Search, calculator, web search
  ├── settingsui/        → Settings UI, preferences
  ├── privatespace/      → Private space lock/unlock (Android 15+)
  └── backup/            → Import/export settings
```

**Design Patterns:**
- **UDF (Unidirectional Data Flow)**: All ViewModels must follow this pattern
  - State: `StateFlow<UiState>` for current UI state
  - Events: `SharedFlow<UiEvent>` or `Channel` for one-time events
- **Stateless UI**: Composables should be stateless; hoist state to ViewModels
- **Dependency Injection**: Use Hilt for all dependencies
- **Repository Pattern**: Data layer uses repositories to abstract data sources

### Naming Conventions

**Packages:**
- Root: `com.longboilauncher.app`
- Core modules: `com.longboilauncher.core.<module>`
- Feature modules: `com.longboilauncher.feature.<feature>`

**Modules:**
- Core: `core:designsystem`, `core:model`, etc.
- Features: `feature:home`, `feature:allapps`, etc.

**Files:**
- ViewModels: `*ViewModel.kt`
- Screens: `*Screen.kt`
- Components: `*Component.kt` or descriptive name
- Repositories: `*Repository.kt`
- Use cases: `*UseCase.kt`

## Code Quality Guardrails

### Mandatory Practices

1. **No Hardcoded Strings**: Use `strings.xml` for all UI text
2. **Image Loading**: Use Coil with proper caching configuration
3. **Testing**: All new features must include:
   - Unit tests for ViewModels (testing UDF state machine)
   - Compose Preview for UI components
   - Screenshot tests when applicable
4. **Formatting**: Code must pass Spotless checks (`./gradlew spotlessCheck`)
5. **Type Safety**: Use Proto DataStore, not Preferences DataStore
6. **Kotlin Best Practices**:
   - Use coroutines for async operations
   - Use Flow for reactive data streams
   - Prefer `val` over `var`
   - Use data classes for models
   - Use sealed classes/interfaces for states and events

### Build Verification

Always run after build configuration changes:
```bash
./gradlew help
```

This ensures:
- Gradle sync succeeds
- Configuration cache works
- No deprecation warnings
- JDK configuration is correct

### Development Commands

**Via Makefile:**
```bash
make test              # Run all tests
make test-unit         # Unit tests only
make coverage          # Generate coverage report
make format            # Format code with Spotless
make lint              # Run Android Lint
make assemble          # Build debug APK
make clean             # Clean build artifacts
```

**Via Gradle:**
```bash
./gradlew testDebugUnitTest      # Unit tests
./gradlew spotlessCheck          # Check formatting
./gradlew spotlessApply          # Fix formatting
./gradlew koverHtmlReport        # Coverage HTML report
./gradlew lintDebug              # Lint checks
```

## Module Development Guidelines

### Core Modules

**Purpose:** Provide reusable, UI-agnostic functionality

**Rules:**
- Should NOT depend on feature modules
- Can depend on other core modules
- Should be highly cohesive (single responsibility)
- Must include unit tests for public APIs

**Example Dependencies:**
```kotlin
// ✅ Good - core module depending on another core module
implementation(project(":core:model"))
implementation(project(":core:common"))

// ❌ Bad - core module depending on feature
implementation(project(":feature:home"))
```

### Feature Modules

**Purpose:** Implement specific user-facing features

**Rules:**
- Can depend on core modules
- Should NOT depend on other feature modules
- Must use `LongboiTheme` from `:core:designsystem`
- Must use UDF pattern for ViewModels
- Must include Compose Previews
- Should implement screenshot tests

**Required Structure:**
```
feature/example/
├── src/main/kotlin/
│   ├── ExampleScreen.kt       # Stateless composable
│   ├── ExampleViewModel.kt    # State + Event handling
│   ├── ExampleUiState.kt      # Sealed/data class for state
│   └── ExampleUiEvent.kt      # Sealed class for events
└── src/test/kotlin/
    └── ExampleViewModelTest.kt
```

## Testing Requirements

### Unit Tests (ViewModels)

Every ViewModel must test the UDF state machine:

```kotlin
@Test
fun `when event happens, then state updates correctly`() = runTest {
    // Given
    val viewModel = ExampleViewModel()
    
    // When
    viewModel.onEvent(ExampleUiEvent.SomeAction)
    
    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assertThat(state.someProperty).isEqualTo(expectedValue)
    }
}
```

### Compose Previews

All UI components must have previews:

```kotlin
@Preview(showBackground = true)
@Composable
fun ExampleScreenPreview() {
    LongboiTheme {
        ExampleScreen(
            state = ExampleUiState.sample(),
            onEvent = {}
        )
    }
}
```

### Screenshot Tests

For critical UI components, add screenshot tests in the `screenshotTest` source set.

## Performance & Optimization

1. **Gradle Performance**:
   - Configuration cache enabled
   - Parallel builds enabled
   - 6GB heap for Gradle daemon
   - KSP incremental compilation

2. **Compose Performance**:
   - Use `remember` for expensive computations
   - Use `derivedStateOf` for derived state
   - Avoid recomposition with `key()` when needed
   - Use `LazyColumn` for lists, not `Column` with `verticalScroll`

3. **Image Loading**:
   - Always use Coil with proper memory caching
   - Use appropriate image sizes
   - Preload when beneficial

## Common Pitfalls to Avoid

1. **❌ Don't create mutable state in Composables**
   ```kotlin
   // ❌ Bad
   var count by remember { mutableStateOf(0) }
   
   // ✅ Good - hoist to ViewModel
   val count by viewModel.count.collectAsState()
   ```

2. **❌ Don't use hardcoded strings**
   ```kotlin
   // ❌ Bad
   Text("Hello World")
   
   // ✅ Good
   Text(stringResource(R.string.hello_world))
   ```

3. **❌ Don't bypass the repository pattern**
   ```kotlin
   // ❌ Bad - ViewModel directly accessing DataStore
   viewModelScope.launch {
       dataStore.data.collect { ... }
   }
   
   // ✅ Good - Use repository
   viewModelScope.launch {
       repository.getSettings().collect { ... }
   }
   ```

4. **❌ Don't ignore test coverage**
   - New features without tests will be rejected
   - Aim for >80% coverage on ViewModels

## Git Workflow

1. **Branch Naming**:
   - Features: `feature/description`
   - Fixes: `fix/description`
   - Refactoring: `refactor/description`

2. **Commit Messages**:
   - Use conventional commits: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`
   - Be descriptive but concise
   - Reference issues when applicable

3. **PR Requirements**:
   - Tests must pass (`make test`)
   - Code must be formatted (`make format`)
   - Lint must pass (`make lint`)
   - Coverage should not decrease

## IDE Configuration

**Android Studio Requirements:**
- Android Studio Ladybug (2024.2.1) or later
- JDK 17 configured as project SDK
- Kotlin plugin 2.3.0+

**Recommended Settings:**
- Enable "Remove unused imports" on save
- Enable "Reformat code" on save (using Spotless config)
- Set line length to 120 characters

## Permissions Philosophy

Longboi Launcher requires minimal permissions:

- **`QUERY_ALL_PACKAGES`**: Required to display installed apps (core launcher functionality)
- **`BIND_NOTIFICATION_LISTENER_SERVICE`**: Optional for notification badges
- **`BIND_APPWIDGET`**: Standard launcher permission for widgets

**Privacy Commitment:**
- No data collection
- No analytics
- No network requests (unless user-initiated, e.g., web search)
- All data stays on device

## Version Updates

When updating dependencies:

1. Update `gradle/libs.versions.toml`
2. Run `./gradlew help` to verify configuration
3. Run full test suite: `make test`
4. Update this file if major versions change
5. Test on physical device, not just emulator

## Additional Resources

- **README.md**: User-facing documentation
- **Makefile**: Quick reference for common commands
- **feature/AGENTS.md**: Feature module-specific guidelines (if exists)

---

**Last Updated**: Feb 2026
**Gradle**: 8.13 | **AGP**: 8.13.2 | **Kotlin**: 2.3.0 | **Compose**: 2026.01.00 | **Hilt**: 2.58

This is an Android launcher app (Longboi Launcher) written in Kotlin + Jetpack Compose.

## Architecture

Multi-module Gradle project following Now in Android patterns:
- **Core modules** (`core/`): model, common, designsystem, appcatalog, datastore, datastore-proto, settings, icons
- **Feature modules** (`feature/`): home, allapps, searchui, settingsui, privatespace, backup
- **App module** (`app/`): MainActivity, NotificationService, LauncherApplication

## Hard Rules

- Kotlin Coroutines + `StateFlow` for async. **NO LiveData.**
- Hilt for DI. All services **must** be registered in `AndroidManifest.xml`.
- **No XML layouts, no Fragments.** Pure Jetpack Compose.
- Navigation: `AnimatedVisibility` surface switching (HOME, ALL_APPS, SEARCH, SETTINGS). **NOT Navigation Compose.**
- Feature modules **must NOT** depend on `:app`. Core modules **must NOT** depend on feature modules.
- `NotificationState` (in `core:common`) bridges `app` module services to `feature` modules. Don't create circular module dependencies.
- `AppWidgetHost`: `startListening()` in `onStart()`, `stopListening()` in `onStop()`.
- `Theme.kt`: `dynamicColor = false`. Custom color palette, not Monet.

## Before Committing

- `./gradlew spotlessApply` — format code (max line length 120)
- `./gradlew testDebugUnitTest` — run unit tests
- `./gradlew validateDebugScreenshotTest` — verify visual regression
- If UI changed: `./gradlew updateDebugScreenshotTest` to regenerate golden images

## Testing

- Unit tests: Robolectric + MockK + Turbine + Google Truth
- UI tests: `createComposeRule()` with Compose test APIs
- Screenshot tests: `screenshotTest/` source set with `@Preview` composables
- E2E: Maestro flows in `.maestro/`
- Coverage: Kover, minimum 50% enforced

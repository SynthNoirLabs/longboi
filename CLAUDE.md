# CLAUDE.md — AI Agent Context for Longboi Launcher

## What is this project?

Longboi is an open-source Android launcher (home screen replacement) inspired by Niagara Launcher.
Written in Kotlin + Jetpack Compose, targeting API 26+ with full API 35 (Android 15) support.

## Architecture

Multi-module Gradle project following the **Now in Android** architecture pattern:

```
app/              → Entry point (MainActivity, Application, NotificationService)
core/
  model/          → Data classes (AppEntry, FavoriteEntry, GlanceHeaderData, ProfileType)
  common/         → Utilities (gestures, clock, role manager, NotificationState)
  designsystem/   → Theme, UI components (FavoriteAppItem, GlanceHeader, ActionsSheet)
  appcatalog/     → LauncherApps API wrapper (app discovery, launch, shortcuts)
  datastore/      → Proto DataStore repositories (favorites, hidden apps)
  datastore-proto/→ Protobuf definitions
  settings/       → Preferences (theme, haptics, gestures, icon packs)
  icons/          → Icon loading via Coil custom fetcher
feature/
  home/           → Home screen (favorites, glance header, popup panels)
  allapps/        → Alphabetical app list with scrubber
  searchui/       → Search / command palette
  settingsui/     → Settings UI
  privatespace/   → Android 15 Private Space support
  backup/         → Backup/restore
```

## Build commands

```bash
./gradlew assembleDebug                      # Build debug APK
./gradlew testDebugUnitTest                  # Unit tests (JVM, no device)
./gradlew validateDebugScreenshotTest        # Screenshot tests (JVM via LayoutLib)
./gradlew :app:connectedDebugAndroidTest     # Instrumented tests (needs device/emulator)
./gradlew :app:pixel6Api35DebugAndroidTest   # Managed device tests (auto-provisions emulator)
./gradlew koverXmlReport koverHtmlReport     # Code coverage
./gradlew spotlessCheck                      # Lint/format check
./gradlew lintDebug                          # Android lint
maestro test .maestro/                       # Maestro E2E flows (needs device)
```

## Key conventions

- **DI**: Hilt everywhere. Services use `@AndroidEntryPoint` or static singletons for system-managed components.
- **State**: ViewModel + `MutableStateFlow` → `StateFlow`. No LiveData.
- **Navigation**: Manual `AnimatedVisibility` surface switching (HOME, ALL_APPS, SEARCH, SETTINGS), not Navigation Compose.
- **Testing**: Robolectric for unit tests, Compose `createComposeRule()` for UI tests, `screenshotTest/` source set for visual regression.
- **Formatting**: Spotless + ktlint 1.3.1. Max line length 120. Run `./gradlew spotlessApply` before committing.
- **No LiveData, no XML layouts, no Fragments**. Pure Compose.

## When modifying code

1. **Always register services in AndroidManifest.xml** — The manifest is the source of truth. If you create a Service, BroadcastReceiver, or ContentProvider, declare it there.
2. **NotificationState (core:common)** bridges the app module's NotificationService to feature modules. Don't create circular module dependencies.
3. **AppWidgetHost lifecycle** — `startListening()` in `onStart()`, `stopListening()` in `onStop()` in MainActivity. Widgets render empty without this.
4. **Theme.kt** — `dynamicColor = false` by default. The project uses its own color palette, not Monet.
5. **Run tests after changes**: `./gradlew testDebugUnitTest` at minimum. Add tests for new logic.
6. **Screenshot tests**: If you change UI, update golden images with `./gradlew updateDebugScreenshotTest`.

## Testing requirements for PRs

- All unit tests pass (`./gradlew testDebugUnitTest`)
- Spotless check passes (`./gradlew spotlessCheck`)
- Screenshot tests validate (`./gradlew validateDebugScreenshotTest`)
- New features include tests
- Coverage does not drop below 50% (enforced by Kover)

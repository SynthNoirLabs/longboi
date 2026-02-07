# Longboi Launcher

It's Niagara Launcher but open source and probably worse. You're welcome.

A vertical-list Android launcher built from scratch with Jetpack Compose. Heavily inspired by [Niagara Launcher](https://niagaralauncher.app/) because it's genuinely great and the Android ecosystem deserves an open source take on that idea.

## What it does

- Vertical alphabetical app list with a wave scrubber
- Favorites on the home screen with a glance clock
- Search with fuzzy matching, calculator, web search, contacts
- Widgets, app shortcuts, pop-up panels
- Private space support (Android 15+)
- App pairs for split-screen
- Icon pack support
- Backup/restore settings
- Edge-to-edge, predictive back, Material 3

## Project structure

```text
app/              → Entry point, Hilt root, MainActivity
core/
  model/          → Data classes (AppEntry, ProfileType, etc.)
  common/         → Utilities, gestures, role manager
  datastore/      → Proto DataStore repos (favorites, settings, widgets)
  appcatalog/     → LauncherApps API wrapper
  settings/       → Preferences logic, haptics
  designsystem/   → Theme, components, animations
  icons/          → Icon loading, icon pack support
  security/       → PIN hashing, biometric auth
  widgets/        → AppWidgetHost management
  notifications/  → NotificationListenerService
feature/
  home/           → Landing surface, pop-ups
  allapps/        → A-Z list with scrubber
  searchui/       → Command palette
  settingsui/     → Options UI, hidden apps
  backup/         → Import/export
  widgetpicker/   → Widget selection UI
  privatespace/   → Private space lock/unlock
benchmark/        → Macrobenchmarks and baseline profiles
```

## Stack

- **Language**: Kotlin 2.0.21
- **UI**: Jetpack Compose (BOM 2025.12.00), Material 3
- **DI**: Hilt
- **Persistence**: Proto DataStore
- **Images**: Coil
- **Async**: Coroutines + Flow
- **Build**: AGP 8.8.2, Version Catalog

## Build & run

```bash
./gradlew assembleDebug          # build
./gradlew testDebugUnitTest      # unit tests
./gradlew spotlessCheck          # check formatting
./gradlew spotlessApply          # fix formatting
./gradlew koverHtmlReport        # coverage report
```

Open in Android Studio Ladybug or later. Build, run, set as default launcher when prompted.

## Quality

- **Formatting**: Spotless + ktlint
- **Static analysis**: Android Lint + Detekt
- **Testing**: JUnit 4, MockK, Turbine, Robolectric, Compose UI tests
- **Coverage**: Kover
- **Benchmarks**: Macrobenchmark for startup and scroll perf
- **Architecture**: ArchUnit tests enforce module boundaries

There's also a `Makefile` with shortcuts: `make verify`, `make quality`, `make typecheck`.

## Permissions

Longboi needs a few permissions to work as a launcher. None of them are used to collect or send your data anywhere.

- **`QUERY_ALL_PACKAGES`** — Lets the app see what's installed on your phone so it can show your app list. Without this, the launcher would have nothing to display.
- **`BIND_NOTIFICATION_LISTENER_SERVICE`** — Used to show notification badges/counts on apps. You can deny this and the launcher will work fine, just without badges.
- **`BIND_APPWIDGET`** — Needed to host home screen widgets. Standard for any launcher.

## License

Apache 2.0

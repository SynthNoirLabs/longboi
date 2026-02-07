# Longboi Launcher

An Android launcher. Minimal, fast, vertical list. Built with Compose.

## Structure

```
app/              → Entry point, Hilt root, MainActivity
core/             → Shared modules (model, datastore, appcatalog, settings, designsystem, etc.)
feature/          → Screens (home, allapps, searchui, settingsui, backup, widgetpicker, etc.)
benchmark/        → Macrobenchmarks and baseline profiles
```

## Stack

Kotlin 2.0.21, Compose (BOM 2025.12.00), Hilt, Proto DataStore, Coil, Coroutines/Flow.

## Build & Run

```bash
# build
./gradlew assembleDebug

# tests
./gradlew testDebugUnitTest

# formatting
./gradlew spotlessCheck    # check
./gradlew spotlessApply    # fix

# coverage
./gradlew koverHtmlReport
```

Open in Android Studio Ladybug+. Set as default launcher when prompted.

## Permissions

- `QUERY_ALL_PACKAGES` — needed to list installed apps.

## License

Apache 2.0

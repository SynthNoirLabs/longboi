---
name: new-feature
description: Scaffold a new feature module following UDF pattern
---

## Steps

1. Ask for the feature name (e.g., "settings", "search")
2. Create the module directory structure under `feature/{name}/`
3. Create `build.gradle` with standard dependencies (Hilt, Compose, core modules)
4. Create the ViewModel with State and Event sealed interface
5. Create the main Screen composable (stateless)
6. Create a basic unit test for the ViewModel
7. Add the module to `settings.gradle`
8. Run `./gradlew help` to verify the module integrates correctly

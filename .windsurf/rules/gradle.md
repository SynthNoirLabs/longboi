---
trigger: glob
globs: ["**/*.gradle", "**/*.gradle.kts", "**/libs.versions.toml"]
---

# Gradle Build Rules

- Use Version Catalog (`libs.versions.toml`) for all dependencies
- Apply plugins via `alias(libs.plugins.X)`
- Run `./gradlew help` after any build config change to verify stability
- Current stable: AGP 8.7.3, Kotlin 2.0.21, Gradle 8.11.1

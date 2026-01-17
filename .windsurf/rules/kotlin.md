---
trigger: glob
globs: ["**/*.kt"]
---

# Kotlin Rules for Longboi Launcher

- Use Kotlin 2.0+ idioms and language features
- Prefer `data class` for state objects
- Use `sealed interface` for event hierarchies
- Avoid nullable types where possible; use `Result` or sealed types for error handling
- Follow the UDF pattern: State flows down, Events flow up
- Use `StateFlow` for UI state, `SharedFlow` for one-time events

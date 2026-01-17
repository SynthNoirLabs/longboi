---
trigger: glob
globs: ["**/ui/**/*.kt", "**/compose/**/*.kt", "**/screen/**/*.kt"]
---

# Jetpack Compose Rules

- Composables must be stateless; hoist state to ViewModel
- Use `LongboiTheme` from `:core:designsystem`
- Use Coil's `AsyncImage` for icon loading
- Add `@Preview` annotations for all UI components
- Follow Material 3 guidelines
- No hardcoded strings; use `stringResource()`

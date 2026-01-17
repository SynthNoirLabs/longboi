# Feature Module Guidelines

When working in `feature:*` modules:

## UI Development

- **UDF**: Use `StateFlow` for state and `SharedFlow` or `Channel` for one-time events.
- **Stateless Screens**: The main entry point of a feature should be a stateless Composable.
- **Theme**: Use `LongboiTheme` from `:core:designsystem`.
- **Icons**: Use the centralized icon pipeline from `:core:icons`.

## Testing

- Each feature must have Compose Previews.
- Screenshot tests should be set up in the `screenshotTest` source set.
- ViewModels must be tested by observing the state stream in response to events.

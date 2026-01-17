# Code Review Checklist

## Architecture
- [ ] ViewModel follows UDF pattern (State + Event)
- [ ] UI is stateless, state hoisted to ViewModel
- [ ] No business logic in Composables
- [ ] Proper module boundaries respected

## Kotlin
- [ ] Uses data class for state
- [ ] Uses sealed interface for events
- [ ] No unnecessary nullability
- [ ] Proper use of coroutines and Flows

## Compose
- [ ] Composables are stateless
- [ ] Uses `LongboiTheme` from `:core:designsystem`
- [ ] No hardcoded strings (use `stringResource`)
- [ ] Has `@Preview` annotations
- [ ] Uses Coil for image loading

## Testing
- [ ] ViewModel has unit tests
- [ ] Tests verify state transitions
- [ ] Uses Turbine for Flow testing
- [ ] Screenshot test previews present

## Performance
- [ ] No unnecessary recompositions
- [ ] Large lists use `LazyColumn`
- [ ] Icons use proper caching via Coil

---
name: udf-enforcer
description: Enforce UDF pattern in all ViewModels and composables for Longboi Launcher
---

# UDF Pattern Enforcement

## When to Use This Skill

- Before implementing any ViewModel
- When creating new screens
- During code reviews
- When refactoring existing code

## Required UDF Structure

### 1. ViewModel Pattern

#### Template

```kotlin
@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val repository: FeatureRepository
) : ViewModel() {

    // Private mutable state
    private val _uiState = MutableStateFlow(FeatureUiState())

    // Public immutable state (flows down)
    val uiState: StateFlow<FeatureUiState> = _uiState.asStateFlow()

    // Private events channel
    private val _events = MutableSharedFlow<FeatureEvent>()

    // Public events (optional, flows up)
    val events: SharedFlow<FeatureEvent> = _events.asSharedFlow()

    // Handle events (flows up)
    fun onEvent(event: FeatureEvent) {
        when (event) {
            is FeatureEvent.LoadData -> loadData()
            is FeatureEvent.Refresh -> refreshData()
            is FeatureEvent.SelectItem -> selectItem(event.id)
        }
    }

    // Private methods
    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.getData()
                .onSuccess { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            data = data
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message
                        )
                    }
                }
        }
    }
}
```

### 2. UI State Data Class

#### Template

```kotlin
@Stable
data class FeatureUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val selectedItemId: String? = null,
    val error: String? = null,
    val searchQuery: String = ""
) {
    // Computed properties
    val hasData: Boolean get() = data.isNotEmpty()
    val isEmpty: Boolean get() = data.isEmpty() && !isLoading
    val showError: Boolean get() = error != null
}
```

### 3. Event Sealed Interface

#### Template

```kotlin
sealed interface FeatureEvent {
    data object LoadData : FeatureEvent
    data object Refresh : FeatureEvent
    data class SelectItem(val id: String) : FeatureEvent
    data class UpdateQuery(val query: String) : FeatureEvent
}
```

### 4. Stateless Composable

#### Template

```kotlin
@Composable
fun FeatureScreen(
    uiState: FeatureUiState,
    onEvent: (FeatureEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    // No business logic here
    // Only UI logic and event forwarding

    when {
        uiState.isLoading -> LoadingIndicator()
        uiState.showError -> ErrorMessage(
            message = uiState.error,
            onRetry = { onEvent(FeatureEvent.Refresh) }
        )
        uiState.isEmpty -> EmptyState(
            onRefresh = { onEvent(FeatureEvent.Refresh) }
        )
        else -> ContentList(
            items = uiState.data,
            selectedItemId = uiState.selectedItemId,
            onItemClick = { id -> onEvent(FeatureEvent.SelectItem(id)) }
        )
    }
}
```

## Enforcement Rules

### ✅ Required Patterns

1. **State flows down**
   - Use `StateFlow` for UI state
   - Make state immutable publicly
   - Use `@Stable` annotation

2. **Events flow up**
   - Use sealed interface for events
   - Single `onEvent` method in ViewModel
   - No direct method calls from UI

3. **Stateless composables**
   - All state comes from parameters
   - No business logic in composables
   - Only UI logic and event forwarding

4. **Single source of truth**
   - ViewModel owns the state
   - UI observes and reacts
   - No duplicated state

### ❌ Forbidden Patterns

1. **State in composables**

   ```kotlin
   // BAD
   @Composable
   fun BadScreen() {
       var count by remember { mutableStateOf(0) } // Business state
   }
   ```

2. **Direct repository calls in UI**

   ```kotlin
   // BAD
   @Composable
   fun BadScreen() {
       val data = repository.getData() // Direct call
   }
   ```

3. **Multiple state sources**

   ```kotlin
   // BAD
   @Composable
   fun BadScreen(
       isLoading: Boolean,
       data: List<Item>,
       error: String?, // Should be in one UiState
       selectedId: String?
   )
   ```

## Integration Pattern

### 1. In Activity/Fragment

```kotlin
@AndroidEntryPoint
class FeatureActivity : ComponentActivity() {
    private val viewModel: FeatureViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LongboiTheme {
                val uiState by viewModel.uiState.collectAsState()

                FeatureScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent
                )
            }
        }
    }
}
```

### 2. In Navigation

```kotlin
@Composable
fun FeatureNavigation(
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    FeatureScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}
```

## Validation Checklist

### Before Implementation

- [ ] ViewModel extends `ViewModel()`
- [ ] Uses `@HiltViewModel`
- [ ] Has private `MutableStateFlow`
- [ ] Has public `StateFlow`
- [ ] Has sealed interface for events

### During Implementation

- [ ] All state changes in ViewModel
- [ ] UI only receives state and sends events
- [ ] No logic in composables
- [ ] Use `@Stable` for state classes

### After Implementation

- [ ] Tests can verify state transitions
- [ ] UI tests work with state injection
- [ ] No memory leaks from callbacks
- [ ] Proper lifecycle handling

## Common Mistakes & Fixes

### 1. Forgetting @Stable

```kotlin
// BEFORE: Unstable class
data class UiState(val list: List<Item>)

// AFTER: Stable class
@Stable
data class UiState(val list: List<Item>)
```

### 2. Using MutableState in ViewModels

```kotlin
// BEFORE: Wrong state type
class BadViewModel {
    var state by mutableStateOf(UiState())
}

// AFTER: Correct state type
class GoodViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
}
```

### 3. Business Logic in Composables

```kotlin
// BEFORE: Logic in UI
@Composable
fun BadScreen(items: List<Item>) {
    val filtered = items.filter { it.active } // Business logic
}

// AFTER: Logic in ViewModel
@Composable
fun GoodScreen(
    items: List<Item>, // Already filtered
    onEvent: (Event) -> Unit
) {
    // Just display
}
```

## Testing UDF

### ViewModel Tests

```kotlin
@Test
fun `should load data when LoadData event sent`() = runTest {
    // Given
    val viewModel = TestViewModel()

    // When
    viewModel.onEvent(FeatureEvent.LoadData)

    // Then
    assertEquals(
        expected = FeatureUiState(data = testData),
        actual = viewModel.uiState.value
    )
}
```

## Integration

This skill works with:

- `brainstorming` for UDF consideration in design
- `executing-plans` during implementation
- `testing-anti-patterns` for test structure

## Output Format

UDF compliance report:

```
docs/udf/YYYY-MM-DD-[feature]-udf-compliance.md
```

Include:

- UDF structure verification
- State flow diagram
- Event flow diagram
- Compliance checklist

---
name: testing-anti-patterns
description: Gate function to prevent common testing mistakes before writing any tests
---

# Testing Anti-Patterns

## When to Use This Skill

- BEFORE writing any unit tests
- BEFORE writing integration tests
- BEFORE writing UI tests
- When reviewing existing tests

## Forbidden Patterns ❌

### 1. Adding Methods to Production Classes Just for Tests

```kotlin
// BAD: Adding test-only methods
class UserRepository {
    fun getUser(id: String): User { /* ... */ }

    // BAD: Test-only method
    fun getUserForTesting(id: String): User {
        // Special logic just for tests
    }
}

// GOOD: Use test doubles or test builders
class UserRepositoryTest {
    private fun createTestUser(): User {
        return User(id = "test", name = "Test User")
    }
}
```

### 2. Mocking Dependencies You Don't Own

```kotlin
// BAD: Mocking Android framework
@Test
fun `should display user`() {
    val mockContext = mock<Context>() // Don't mock Context
    // ...
}

// GOOD: Use real implementations or test doubles
@Test
fun `should display user`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    // ...
}
```

### 3. Testing Implementation Details

```kotlin
// BAD: Testing private methods
class Calculator {
    private fun validateInput(num: Int) { /* ... */ }
    fun add(a: Int, b: Int): Int { /* ... */ }
}

@Test
fun `should validate input`() { // BAD: Testing private method
    // Can't easily test without reflection
}

// GOOD: Test public behavior
@Test
fun `should return sum for valid inputs`() {
    val result = calculator.add(2, 3)
    assertThat(result).isEqualTo(5)
}
```

### 4. Over-Specifying Mock Behavior

```kotlin
// BAD: Too specific mock setup
whenever(mockRepository.getUser("1")).thenReturn(user)
whenever(mockRepository.getUser("1")).thenReturn(user) // Duplicate
whenever(mockRepository.getUser(any())).thenReturn(user) // Too broad

// GOOD: Minimal, necessary setup
whenever(mockRepository.getUser("1")).thenReturn(user)
```

## Required Patterns ✅

### 1. Test Public Behavior Only

```kotlin
// GOOD: Focus on what, not how
@Test
fun `should emit loading state then success state`() {
    // Given
    val viewModel = HomeViewModel(mockRepository)

    // When
    viewModel.loadData()

    // Then
    assertThat(viewModel.uiState.value)
        .isEqualTo(HomeUiState(data = listOf(item)))
}
```

### 2. Use Real Implementations When Possible

```kotlin
// GOOD: Use real ViewModels
@Test
fun `should handle events correctly`() {
    // Use actual ViewModel, not mock
    val viewModel = createViewModel()

    viewModel.onEvent(HomeEvent.Refresh)

    // Verify state changes
}
```

### 3. Mock at Boundaries Only

```kotlin
// GOOD: Mock external dependencies
@Test
fun `should load data from repository`() {
    // Mock repository (boundary)
    // Test ViewModel behavior
    // Don't mock internal classes
}
```

### 4. Write Readable, Maintainable Tests

```kotlin
// GOOD: Clear structure
@Test
fun `should show error when network fails`() {
    // Arrange
    whenever(mockRepository.getData())
        .throws(NetworkException("No internet"))

    // Act
    viewModel.loadData()

    // Assert
    assertThat(viewModel.uiState.value.error)
        .isEqualTo("No internet")
}
```

## Android-Specific Anti-Patterns

### 1. Don't Test Compose Implementation Details

```kotlin
// BAD: Testing composable internals
@Test
fun `should call correct function`() {
    // Don't test what compose does internally
}

// GOOD: Test visible behavior
@Test
fun `should display text when data provided`() {
    composeTestRule.setContent {
        TestScreen(text = "Hello")
    }

    composeTestRule
        .onNodeWithText("Hello")
        .assertIsDisplayed()
}
```

### 2. Don't Mock Coroutines/Flow

```kotlin
// BAD: Mocking Flow
@Test
fun `should handle flow`() {
    val mockFlow = mock<Flow<Data>>()
    // Complex mock setup
}

// GOOD: Use TestDispatcher or Turbine
@Test
fun `should emit states correctly`() {
    val testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    // Test with real Flow
}
```

## Test Structure Template

### Unit Tests

```kotlin
@Test
fun `should [expected behavior] when [condition]`() {
    // Arrange
    val sut = SystemUnderTest() // System Under Test
    // Set up test data

    // Act
    val result = sut.action()

    // Assert
    assertThat(result).isEqualTo(expected)
}
```

### ViewModel Tests

```kotlin
@Test
fun `should [state change] when [event]`() = runTest {
    // Arrange
    val viewModel = createViewModel()
    val initialState = viewModel.uiState.value

    // Act
    viewModel.onEvent(TestEvent)

    // Assert
    assertThat(viewModel.uiState.value).isNotEqualTo(initialState)
}
```

### Compose Tests

```kotlin
@Test
fun `should display [content] when [state]`() {
    composeTestRule.setContent {
        TestComponent(state = testState)
    }

    composeTestRule
        .onNodeWithText("Expected text")
        .assertIsDisplayed()
}
```

## Integration Checklist

Before writing tests, verify:

- [ ] Not testing private methods
- [ ] Not mocking classes you don't own
- [ ] Tests focus on behavior, not implementation
- [ ] Mocks are minimal and necessary
- [ ] Test names describe behavior, not implementation
- [ ] Tests are independent and isolated

## Gate Function

This skill acts as a gate - if any anti-patterns are detected, stop and revise the test approach before proceeding.

## Required Tools from Testing Plan

### Unit Tests (JVM)

- **JUnit4/5**: Test framework
- **Truth or Kotest**: Assertions (Truth recommended)
- **MockK**: Mocking (prefer fakes over mocks)
- **kotlinx-coroutines-test**: Coroutine testing
- **Turbine**: Testing Flow (required for StateFlow/SharedFlow)

### Robolectric (JVM Android-ish)

Use for:

- Intent resolution logic
- PackageManager wrappers
- Drawable/icon transformations
- Widget sizing math
- System settings intents

### Compose UI Tests (Instrumented)

- Use Compose test APIs
- Verify navigation and interactions
- Verify semantics (labels exist)
- testTag for important nodes

### Screenshot Tests

- **Option A**: Official Compose Preview Screenshot Testing
- **Option B**: Roborazzi (JVM screenshot tests)
- Test components and key screens in stable states

### Performance Tests

- **Macrobenchmark** for critical journeys
- Baseline profile generation
- Track: cold start, warm start, navigation

## Integration

This skill should be triggered by:

- `executing-plans` before writing tests
- `systematic-debugging` when writing bug reproduction tests
- Any manual test writing

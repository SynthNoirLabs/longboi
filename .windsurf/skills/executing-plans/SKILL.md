---
name: executing-plans
description: Execute documented plans with batch processing and human review checkpoints
---

# Executing Plans

## When to Use This Skill

- After brainstorming/design is complete
- When implementing documented features
- For following architecture decisions
- When multiple tasks need coordination

## Process

### 1. Plan Review

- Read the design document thoroughly
- Verify all prerequisites are met
- Check for unclear or missing details
- Identify potential blockers

### 2. Create Task List

Use TodoWrite to track all tasks:

```markdown
- [ ] Task 1: [Description]
- [ ] Task 2: [Description]
- [ ] Task 3: [Description]
```

### 3. Batch Execution

Execute tasks in batches of 3-4 maximum:

- Complete all steps in batch
- Verify each batch works
- Get human approval before continuing

### 4. TDD Cycle for Each Task

Follow RED → GREEN → REFACTOR:

#### RED: Write Failing Test

```kotlin
@Test
fun `should [feature] when [condition]`() {
    // Test that currently fails
    // Describes expected behavior
}
```

#### GREEN: Minimal Implementation

```kotlin
// Simplest code to make test pass
// No extra features
// Focus on core functionality
```

#### REFACTOR: Clean Up

- Improve code structure
- Remove duplication
- Optimize performance
- Keep tests passing

### 5. Verification After Each Batch

```bash
# Run all tests
./gradlew testDebugUnitTest

# Build successfully
./gradlew assembleDebug

# Lint checks pass
./gradlew lintDebug
```

### 6. Commit Strategy

- Commit after each successful batch
- Use descriptive commit messages
- Reference design document in commits

```bash
git commit -m "feat: [feature-name] - batch 1/3

Implements:
- [Task 1 description]
- [Task 2 description]
- [Task 3 description]

Ref: docs/plans/YYYY-MM-DD-design.md
```

## Android Implementation Pattern

### For New Features

1. **Create Test Structure**

   ```
   feature/[name]/src/test/
   ├── [Name]ViewModelTest.kt
   └── [Name]ScreenTest.kt
   ```

2. **Implement ViewModel (UDF Pattern)**

   ```kotlin
   @HiltViewModel
   class [Name]ViewModel @Inject constructor() : ViewModel() {
       private val _uiState = MutableStateFlow([Name]UiState())
       val uiState: StateFlow<[Name]UiState> = _uiState.asStateFlow()

       fun onEvent(event: [Name]Event) {
           when (event) {
               // Handle events
           }
       }
   }
   ```

3. **Implement Stateless Composable**

   ```kotlin
   @Composable
   fun [Name]Screen(
       uiState: [Name]UiState,
       onEvent: ([Name]Event) -> Unit
   ) {
       // UI implementation
   }
   ```

4. **Add to Navigation**
   - Update navigation graph
   - Add route constants
   - Wire up dependencies

### Batch Example

```
Batch 1: Core Structure
- [ ] Create ViewModel with StateFlow
- [ ] Define UiState data class
- [ ] Define Event sealed class
- [ ] Write failing tests

Batch 2: UI Implementation
- [ ] Create Screen composable
- [ ] Add @Preview annotations
- [ ] Implement basic UI
- [ ] Verify tests pass

Batch 3: Integration
- [ ] Add to DI module
- [ ] Update navigation
- [ ] Add to settings.gradle
- [ ] Full integration test
```

## Error Handling

### If Tests Fail

1. Stop immediately
2. Analyze failure
3. Fix before proceeding
4. Don't accumulate errors

### If Build Fails

1. Check error messages
2. Fix compilation issues
3. Verify dependencies
4. Clean and rebuild

### If Design Unclear

1. Stop execution
2. Clarify requirements
3. Update design document
4. Resume when clear

## Integration

This skill works with:

- `brainstorming` (gets design from)
- `udf-enforcer` (ensures pattern compliance)
- `testing-anti-patterns` (before writing tests)
- `compose-performance` (for UI optimization)

## Output Format

After execution:

```
docs/implementation/YYYY-MM-DD-[feature-name]-implementation.md
```

Include:

- Tasks completed
- Tests added
- Deviations from design
- Known limitations
- Next steps

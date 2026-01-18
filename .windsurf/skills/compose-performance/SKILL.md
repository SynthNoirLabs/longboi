---
name: compose-performance
description: Optimize Compose UI performance using best practices and profiling tools
---

# Compose Performance

## When to Use This Skill

- When UI feels slow or laggy
- Before implementing new screens
- When lists scroll poorly
- When animations are janky

## Common Performance Issues

### 1. Unnecessary Recomposition

#### Problem: Passing entire object

```kotlin
// BAD: Recomposes on any user change
@Composable
fun UserScreen(user: User) {
    Text(user.name)
    Text(user.email)
    Text(user.phone) // Changes cause full recomposition
}
```

#### Solution: Pass only needed data

```kotlin
// GOOD: Only recomposes when name/email change
@Composable
fun UserScreen(
    name: String,
    email: String,
    phone: String,
    onPhoneClick: () -> Unit
) {
    Text(name)
    Text(email)
    Button(onClick = onPhoneClick) {
        Text(phone)
    }
}

// Or use data class with equals/hashCode
@Stable
data class UserUiState(
    val name: String,
    val email: String,
    val phone: String
)
```

### 2. Memory Leaks in Composables

#### Problem: Remembering too much

```kotlin
// BAD: Remembering large objects
@Composable
fun ExpensiveScreen() {
    val largeList = remember { generateLargeList() } // Never cleared
}
```

#### Solution: Use remember with keys

```kotlin
// GOOD: Clear when not needed
@Composable
fun ExpensiveScreen(userId: String) {
    val largeList = remember(userId) {
        generateLargeList(userId)
    }
}
```

### 3. List Performance

#### Problem: Using Column instead of LazyColumn

```kotlin
// BAD: Renders all items at once
@Composable
fun BadList(items: List<Item>) {
    Column {
        items.forEach { item ->
            ItemRow(item)
        }
    }
}
```

#### Solution: Use LazyColumn with keys

```kotlin
// GOOD: Only renders visible items
@Composable
fun GoodList(items: List<Item>) {
    LazyColumn {
        items(
            items = items,
            key = { it.id } // Stable key for performance
        ) { item ->
            ItemRow(item)
        }
    }
}
```

## Optimization Techniques

### 1. Use @Stable and @Immutable

```kotlin
@Stable
interface UiState {
    val isLoading: Boolean
    val data: List<Item>
}

@Immutable
data class Item(
    val id: String,
    val title: String
)
```

### 2. Derive State Efficiently

```kotlin
// BAD: Creates new derived state on every recomposition
@Composable
fun BadDerivedState(items: List<Item>) {
    val filteredItems = items.filter { it.isActive } // New list each time
}

// GOOD: Use derivedStateOf
@Composable
fun GoodDerivedState(items: List<Item>) {
    val filteredItems by remember {
        derivedStateOf { items.filter { it.isActive } }
    }
}
```

### 3. Avoid Nested LazyColumns

```kotlin
// BAD: Nested scrolling containers
LazyColumn {
    items(sections) { section ->
        LazyColumn { // Don't do this!
            items(section.items) { item ->
                ItemRow(item)
            }
        }
    }
}

// GOOD: Flatten structure
LazyColumn {
    items(sections.flatMap { it.items }) { item ->
        ItemRow(item)
    }
}
```

## Profiling Tools

### 1. Layout Inspector

```bash
# Enable in debug builds
android {
    buildFeatures {
        compose true
    }
}
```

### 2. Compose Compiler Metrics

```gradle
// Add to build.gradle
android {
    kotlinOptions {
        freeCompilerArgs += [
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.buildDir}/compose_compiler"
        ]
    }
}
```

### 3. Recomposition Counts

```kotlin
// Add to debug builds
@Composable
fun DebugRecompose(name: String) {
    if (BuildConfig.DEBUG) {
        var recomposeCount by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) {
            recomposeCount++
            Log.d("Compose", "$name recomposed: $recomposeCount times")
        }
    }
}
```

## Performance Checklist

### Before Implementing

- [ ] Use LazyColumn for lists > 20 items
- [ ] Define stable data classes
- [ ] Plan state structure carefully

### During Implementation

- [ ] Pass only needed parameters
- [ ] Use remember with keys
- [ ] Avoid inline functions in hot paths

### After Implementation

- [ ] Check recomposition counts
- [ ] Profile with Layout Inspector
- [ ] Test scrolling performance

## Common Fixes

### Fix 1: Expensive Calculations

```kotlin
// BEFORE: Calculates on every recomposition
@Composable
fun ExpensiveCalculation(data: List<Data>) {
    val result = data.map { expensiveTransform(it) }
}

// AFTER: Cache with remember
@Composable
fun ExpensiveCalculation(data: List<Data>) {
    val result = remember(data) {
        data.map { expensiveTransform(it) }
    }
}
```

### Fix 2: Image Loading

```kotlin
// Use Coil for optimized image loading
@Composable
fun OptimizedImage(url: String) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
        contentDescription = null,
        modifier = Modifier.size(100.dp)
    )
}
```

### Fix 3: Animation Performance

```kotlin
// Use animateDpAsState for smooth animations
val animatedHeight by animateDpAsState(
    targetValue = if (expanded) 200.dp else 0.dp,
    animationSpec = tween(300)
)
```

## Integration

This skill works with:

- `brainstorming` for performance considerations in design
- `executing-plans` during implementation
- `android-debugging` for performance issues

## Performance Targets

- **Startup**: < 500ms to first frame
- **Scrolling**: 60fps (16ms per frame)
- **Animation**: Smooth 60fps
- **Memory**: < 100MB for complex screens

## Output Format

Create performance report:

```
docs/performance/YYYY-MM-DD-[screen]-optimization.md
```

Include:

- Before/after metrics
- Changes made
- Performance improvements
- Remaining issues

---
name: systematic-debugging
description: Use for all bug fixes following 4-phase process: investigate → analyze → hypothesize → implement
---

# Systematic Debugging

## When to Use This Skill

- When any bug or error occurs
- Before attempting to fix issues
- For crashes, ANRs, or unexpected behavior
- When tests fail consistently

## Phase 1: Root Cause Investigation

### 1.1 Read Complete Error Messages

```bash
# For Android crashes
adb logcat -d | grep -A 20 -B 5 "FATAL EXCEPTION"

# For ANRs
adb bugreport

# For build errors
./gradlew assembleDebug --stacktrace
```

### 1.2 Reproduce Consistently

- Create minimal reproduction case
- Note exact steps to reproduce
- Identify trigger conditions
- Document frequency (100% vs intermittent)

### 1.3 Check Recent Changes

```bash
# Recent commits in affected area
git log --oneline -10 -- [module/path]

# Diff from last known good
git diff HEAD~1 -- [affected-files]
```

### 1.4 Gather Evidence at Boundaries

- Add logging at component boundaries
- Check state before/after operations
- Verify inputs/outputs at each step
- Use debugger to inspect variables

## Phase 2: Pattern Analysis

### 2.1 Find Working Examples

```bash
# Search for similar patterns in codebase
grep -r "similar_function_or_class" src/

# Find working implementations
find . -name "*.kt" -exec grep -l "working_pattern" {} \;
```

### 2.2 Compare Broken vs Working

- Identify differences in approach
- Check environment differences
- Verify data variations
- Note timing/threading differences

### 2.3 Search for Anti-Patterns

```kotlin
// Common Android issues:
// - Main thread network operations
// - Memory leaks (static references)
// - Lifecycle issues
// - Thread safety problems
```

## Phase 3: Form Hypothesis

### 3.1 State Clear Theory

"Based on the evidence, the bug occurs because:

1. [Root cause]
2. [Why it triggers]
3. [Why it wasn't caught]"

### 3.2 Verify Hypothesis

- Check if hypothesis explains all symptoms
- Look for contradictory evidence
- Test hypothesis with targeted logging

### 3.3 Document Findings

```markdown
## Bug Analysis
**Issue**: [Brief description]
**Evidence**: [Key findings]
**Hypothesis**: [Root cause theory]
**Verification**: [How confirmed]
```

## Phase 4: Implement Fix

### 4.1 Write Test That Captures the Fix

```kotlin
@Test
fun `should handle [scenario] without [error]`() {
    // Arrange
    val initialState = // set up problematic state

    // Act
    val result = // call problematic function

    // Assert
    assertThat(result).isNotNull()
    // Add assertions that would fail before fix
}
```

### 4.2 Implement Minimal Solution

- Fix only the root cause
- Don't add unnecessary complexity
- Keep changes focused and small

### 4.3 Verify Fix Resolves Issue

- Run reproduction case
- Ensure test passes
- Check for regressions
- Test edge cases

## Android-Specific Debugging

### Common Issues & Solutions

#### ANR (Application Not Responding)

```bash
# Find ANR traces
adb pull /data/anr/traces.txt

# Look for:
# - Main thread blocking operations
# - Long-running database queries
# - Slow network calls on main thread
```

#### Memory Leaks

```kotlin
// Use LeakCanary to detect
// Common causes:
// - Static references to Activity
// - Unclosed resources (Cursor, Stream)
// - Inner classes holding outer reference
```

#### Performance Issues

```bash
# Profile with:
adb shell am start -W -n [package]/[activity]
./gradlew assembleDebug --profile
```

## Integration

This skill should trigger:

- `testing-anti-patterns` before writing tests
- `android-debugging` for Android-specific issues
- `mobile-security` if security-related bug

## Output Format

After debugging, create:

```
docs/bugs/YYYY-MM-DD-[issue-name]-fix.md
```

Include:

- Issue description
- Root cause analysis
- Fix implemented
- Tests added
- Prevention measures

# Parallel Track: Phase 18 - Accessibility

**Branch:** `feature/accessibility`
**Independent:** ✅ No dependencies on other phases
**Effort:** ~1 week

---

## Overview

Make Longboi Launcher fully accessible with TalkBack support, proper focus management, and respect for system accessibility settings.

---

## Tasks

### Task 1: Audit & Add Content Descriptions
**Files to modify:**

```
core/designsystem/src/main/java/.../components/
├── AppListItem.kt          → Add contentDescription to clickable row
├── FavoriteAppItem.kt      → Add contentDescription, stateDescription for badges
├── PopupPanel.kt           → Add semantics for panel actions
├── GlanceHeader.kt         → Add time announcement for TalkBack
├── ProgressNotificationCard.kt → Add progress announcements
└── ActionsSheet.kt         → Add action descriptions
```

**Pattern:**
```kotlin
// Before
Icon(imageVector = Icons.Default.Search, contentDescription = null)

// After  
Icon(imageVector = Icons.Default.Search, contentDescription = stringResource(R.string.search))
```

**For interactive elements:**
```kotlin
Modifier.semantics {
    contentDescription = "App: $appName"
    stateDescription = if (hasNotification) "Has notifications" else null
    role = Role.Button
}
```

---

### Task 2: Create Accessibility Strings
**File:** `core/designsystem/src/main/res/values/strings_accessibility.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Navigation -->
    <string name="a11y_open_app_drawer">Open app drawer</string>
    <string name="a11y_close_app_drawer">Close app drawer</string>
    <string name="a11y_open_search">Open search</string>
    <string name="a11y_clear_search">Clear search</string>
    
    <!-- App items -->
    <string name="a11y_app_item">App: %1$s</string>
    <string name="a11y_app_with_notifications">%1$s, has %2$d notifications</string>
    <string name="a11y_favorite_app">Favorite app: %1$s</string>
    
    <!-- Actions -->
    <string name="a11y_app_info">App info</string>
    <string name="a11y_uninstall_app">Uninstall app</string>
    <string name="a11y_hide_app">Hide app</string>
    
    <!-- Clock -->
    <string name="a11y_current_time">Current time: %1$s</string>
    <string name="a11y_current_date">%1$s</string>
    
    <!-- Progress -->
    <string name="a11y_download_progress">Download progress: %1$d percent</string>
    <string name="a11y_download_complete">Download complete</string>
    
    <!-- Scrubber -->
    <string name="a11y_alphabet_scrubber">Alphabet scrubber, drag to jump to letter</string>
    <string name="a11y_scrubber_letter">Letter %1$s</string>
</resources>
```

---

### Task 3: Focus Management
**File:** `core/designsystem/src/main/java/.../accessibility/FocusUtils.kt`

```kotlin
package com.longboilauncher.app.core.designsystem.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.focused
import androidx.compose.ui.semantics.semantics

/**
 * Request focus when a screen/dialog appears for TalkBack users.
 */
@Composable
fun Modifier.requestInitialFocus(
    focusRequester: FocusRequester,
    requestFocus: Boolean = true,
): Modifier {
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
        }
    }
    return this.focusRequester(focusRequester)
}

/**
 * Announce content changes to TalkBack.
 */
@Composable  
fun Modifier.liveRegion(polite: Boolean = true): Modifier =
    semantics {
        liveRegion = if (polite) {
            androidx.compose.ui.semantics.LiveRegionMode.Polite
        } else {
            androidx.compose.ui.semantics.LiveRegionMode.Assertive
        }
    }
```

---

### Task 4: Respect Reduce Motion
**File:** `core/designsystem/src/main/java/.../theme/Motion.kt`

Already have `LocalReduceMotion` - ensure it's used everywhere:

```kotlin
// Pattern for animations
@Composable
fun AnimatedComponent() {
    val reduceMotion = shouldReduceMotion()
    
    val animatedValue by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = if (reduceMotion) {
            snap() // Instant transition
        } else {
            LongboiMotion.Specs.fadeIn()
        }
    )
}
```

**Files to update:**
- `GlanceHeader.kt` - Flip clock animation
- `GridLayout.kt` - Stagger animations  
- `AllAppsScreen.kt` - Elastic overscroll
- `SearchScreen.kt` - Result animations
- `FavoriteAppItem.kt` - Press animations

---

### Task 5: Test with Large Font Scales
**Create test variants:**

```kotlin
// In screenshotTest source set
@Test
fun homeScreen_largeFontScale() {
    composeTestRule.setContent {
        CompositionLocalProvider(
            LocalDensity provides Density(
                density = LocalDensity.current.density,
                fontScale = 2.0f // 200% font scale
            )
        ) {
            HomeScreen(...)
        }
    }
    // Capture screenshot, verify no clipping
}
```

**Known issues to fix:**
- Clock text may overflow
- App labels may clip
- Action buttons may overlap

---

### Task 6: Keyboard Navigation
**Ensure proper tab order:**

```kotlin
Modifier.focusProperties {
    // Define explicit focus order if needed
    next = nextFocusRequester
    previous = previousFocusRequester
}
```

**Key areas:**
- App drawer list navigation
- Search results navigation
- Settings screens
- Popup panel actions

---

## Verification Checklist

- [ ] TalkBack can navigate all interactive elements
- [ ] Each button/item has meaningful description
- [ ] Focus moves logically through UI
- [ ] Animations respect reduce motion setting
- [ ] UI works at 200% font scale
- [ ] No content is cut off or overlapping
- [ ] Live regions announce important changes
- [ ] Keyboard/D-pad can navigate all screens

---

## Commands

```bash
# Create worktree
cd ~/Pruebas/launch
git worktree add ../launch-accessibility -b feature/accessibility

# Build & test
cd ../launch-accessibility
./gradlew assembleDebug

# Run accessibility scanner (if configured)
./gradlew connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.longboilauncher.app.AccessibilityTest
```

---

## Merge Strategy

This phase has NO dependencies and NO dependents in the immediate roadmap. Can be merged independently whenever ready.

```bash
# When complete
git checkout main
git merge feature/accessibility
git branch -d feature/accessibility
git worktree remove ../launch-accessibility
```

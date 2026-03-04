# Longboi Launcher Roadmap - Phases 10-22

## Overview

This roadmap covers architecture, performance, features, accessibility, and security improvements following the completed UI/UX enhancement phases (1-9).

---

## Phase 10: Testing Infrastructure
**Priority: HIGH** | **Effort: Large**

### Goals
- Establish comprehensive testing patterns
- Catch regressions early
- Enable confident refactoring

### Tasks
1. **ViewModel Unit Tests**
   - Test state machine transitions (Event → State changes)
   - Test Effect emissions
   - Mock repositories with Turbine for Flow testing
   - Target: All ViewModels in `feature:*` modules

2. **Screenshot Tests (Roborazzi)**
   - Expand `screenshotTest` source sets
   - Cover all major screens in light/dark themes
   - Test with different font scales
   - CI integration for visual regression detection

3. **Integration Tests**
   - App launch to home screen flow
   - Navigation between screens
   - Settings persistence verification

### Files to Create/Modify
```
feature/*/src/test/java/.../ViewModelTest.kt
feature/*/src/screenshotTest/kotlin/...ScreenshotTest.kt
app/src/androidTest/java/.../IntegrationTest.kt
```

### Success Criteria
- 80%+ ViewModel test coverage
- Screenshot baselines for all screens
- CI pipeline runs tests on every PR

---

## Phase 11: Error Handling
**Priority: HIGH** | **Effort: Medium**

### Goals
- Graceful degradation on failures
- User-friendly error messages
- Centralized error tracking

### Tasks
1. **Create Error Domain Model**
   ```kotlin
   // core/common/src/.../error/
   sealed class LongboiError {
       data class Network(val cause: Throwable) : LongboiError()
       data class Permission(val permission: String) : LongboiError()
       data class AppLoad(val packageName: String) : LongboiError()
       object Unknown : LongboiError()
   }
   ```

2. **Error UI Components**
   - `ErrorBanner` - Dismissible inline error
   - `ErrorScreen` - Full screen error state
   - `RetryButton` with exponential backoff

3. **Result Wrapper**
   ```kotlin
   sealed class Result<T> {
       data class Success<T>(val data: T) : Result<T>()
       data class Error<T>(val error: LongboiError) : Result<T>()
       class Loading<T> : Result<T>()
   }
   ```

### Files to Create
```
core/common/src/main/java/.../error/LongboiError.kt
core/common/src/main/java/.../error/Result.kt
core/designsystem/src/main/java/.../components/ErrorComponents.kt
```

---

## Phase 12: Performance Optimization
**Priority: HIGH** | **Effort: Large**

### Goals
- Sub-500ms cold start time
- Smooth 60fps scrolling
- Efficient memory usage

### Tasks
1. **Cold Start Optimization**
   - Profile with Android Studio Profiler
   - Defer non-critical initialization
   - Use `@Inject` constructor over field injection
   - Lazy load icon packs

2. **Memory Audit**
   - Coil memory cache sizing
   - Icon bitmap recycling
   - Leak detection with LeakCanary (debug builds)

3. **LazyList Optimization**
   - Implement `beyondBoundsItemCount` for prefetching
   - Use `key` parameter correctly
   - Stable item keys for diffing
   - Consider `LazyListState.prefetchItemCount`

4. **Baseline Profiles**
   - Create `baselineprofile` module
   - Generate profiles for critical user journeys
   - Include in release builds

### Files to Create
```
baselineprofile/build.gradle.kts
baselineprofile/src/main/java/.../BaselineProfileGenerator.kt
```

---

## Phase 13: Gesture Navigation
**Priority: MEDIUM** | **Effort: Large**

### Goals
- Intuitive gesture controls
- Customizable actions
- Smooth gesture feedback

### Tasks
1. **Core Gesture System**
   ```kotlin
   enum class GestureType {
       SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT,
       DOUBLE_TAP, PINCH_IN, PINCH_OUT
   }
   
   enum class GestureAction {
       OPEN_APP_DRAWER, OPEN_NOTIFICATIONS, OPEN_QUICK_SETTINGS,
       OPEN_SEARCH, LAUNCH_APP, TOGGLE_FLASHLIGHT, NONE
   }
   ```

2. **Gesture Detection**
   - Create `GestureDetectorModifier`
   - Handle conflicts with existing touch targets
   - Visual feedback during gesture

3. **Settings UI**
   - Gesture mapping screen
   - Preview/test gestures
   - Reset to defaults

### Files to Create
```
core/gestures/src/main/java/.../GestureManager.kt
core/gestures/src/main/java/.../GestureDetector.kt
feature/settings/src/main/java/.../GestureSettingsScreen.kt
```

---

## Phase 14: Widget Support
**Priority: MEDIUM** | **Effort: X-Large**

### Goals
- Display Android widgets on home screen
- Resize and position widgets
- Persist widget configuration

### Tasks
1. **Widget Host Setup**
   - Implement `AppWidgetHost`
   - Handle widget lifecycle
   - Permission handling for binding widgets

2. **Widget Placement**
   - Grid-based positioning
   - Drag-to-resize handles
   - Collision detection

3. **Widget Picker**
   - List available widgets by app
   - Preview widget before adding
   - Search/filter widgets

4. **Persistence**
   - Store widget IDs, positions, sizes
   - Restore on launcher restart
   - Handle widget updates

### Files to Create
```
core/widgets/src/main/java/.../WidgetHost.kt
core/widgets/src/main/java/.../WidgetRepository.kt
feature/home/src/main/java/.../WidgetContainer.kt
feature/widgets/src/main/java/.../WidgetPickerScreen.kt
```

---

## Phase 15: Search Enhancements
**Priority: MEDIUM** | **Effort: Medium**

### Goals
- Unified search across multiple sources
- Quick actions from search
- Web search fallback

### Tasks
1. **Search Sources**
   - Apps (existing)
   - App shortcuts (`ShortcutManager`)
   - Contacts (with permission)
   - Settings entries
   - Web search (configurable provider)

2. **Search Result Types**
   ```kotlin
   sealed class SearchResult {
       data class App(val entry: AppEntry) : SearchResult()
       data class Shortcut(val info: ShortcutInfo) : SearchResult()
       data class Contact(val name: String, val uri: Uri) : SearchResult()
       data class WebSearch(val query: String) : SearchResult()
   }
   ```

3. **Quick Actions**
   - Call contact directly
   - Open app shortcut
   - Copy to clipboard

### Files to Create
```
core/search/src/main/java/.../SearchRepository.kt
core/search/src/main/java/.../sources/ShortcutSearchSource.kt
core/search/src/main/java/.../sources/ContactSearchSource.kt
```

---

## Phase 16: Backup/Restore
**Priority: LOW** | **Effort: Medium**

### Goals
- Export all user settings
- Import on new device
- Cloud backup option

### Tasks
1. **Export Format**
   ```kotlin
   data class LauncherBackup(
       val version: Int,
       val timestamp: Long,
       val favorites: List<FavoriteApp>,
       val hiddenApps: List<String>,
       val gestureMapping: Map<GestureType, GestureAction>,
       val preferences: Map<String, Any>,
       val widgetLayout: List<WidgetState>?,
   )
   ```

2. **Export/Import Flow**
   - Serialize to JSON
   - Share via system share sheet
   - Import from file picker

3. **Cloud Backup (Optional)**
   - Google Drive integration
   - Auto-backup option

### Files to Create
```
core/backup/src/main/java/.../BackupManager.kt
core/backup/src/main/java/.../BackupSerializer.kt
feature/settings/src/main/java/.../BackupRestoreScreen.kt
```

---

## Phase 17: App Hiding
**Priority: LOW** | **Effort: Small**

### Goals
- Hide apps from app drawer
- PIN protection for hidden apps
- Quick access to hidden apps

### Tasks
1. **Hidden Apps Repository**
   - Store hidden package names in DataStore
   - Filter from app list queries

2. **PIN Protection**
   - Set/change PIN screen
   - Biometric unlock option
   - Lockout after failed attempts

3. **Hidden Apps Drawer**
   - Accessible via gesture or secret tap
   - Shows only hidden apps
   - Option to unhide

### Files to Create
```
core/settings/src/main/java/.../HiddenAppsRepository.kt
feature/settings/src/main/java/.../HiddenAppsScreen.kt
feature/settings/src/main/java/.../PinSetupScreen.kt
```

---

## Phase 18: Accessibility
**Priority: HIGH** | **Effort: Medium**

### Goals
- Full TalkBack support
- Dynamic text scaling
- Respect system accessibility settings

### Tasks
1. **Content Descriptions**
   - Audit all interactive elements
   - Add meaningful descriptions
   - Test with TalkBack enabled

2. **Focus Management**
   - Proper focus order
   - Focus indicators
   - Keyboard navigation

3. **Text Scaling**
   - Test at 200% font scale
   - Ensure layouts don't break
   - Use `sp` for all text sizes

4. **Motion Preferences**
   - Honor `LocalReduceMotion`
   - Disable animations when reduce motion is on
   - Provide static alternatives

### Files to Modify
```
core/designsystem/src/main/java/.../components/*.kt (add contentDescription)
feature/*/src/main/java/.../*.kt (accessibility modifiers)
```

---

## Phase 19: Security - App Lock
**Priority: MEDIUM** | **Effort: Medium**

### Goals
- Lock individual apps
- Biometric authentication
- Secure PIN fallback

### Tasks
1. **Biometric Integration**
   - Use AndroidX Biometric library
   - Handle different biometric types
   - Graceful fallback to PIN

2. **App Lock Manager**
   ```kotlin
   interface AppLockManager {
       fun isAppLocked(packageName: String): Boolean
       fun lockApp(packageName: String)
       fun unlockApp(packageName: String)
       suspend fun authenticate(): AuthResult
   }
   ```

3. **Lock Screen UI**
   - Biometric prompt
   - PIN entry screen
   - "Remember for X minutes" option

### Files to Create
```
core/security/src/main/java/.../AppLockManager.kt
core/security/src/main/java/.../BiometricAuthenticator.kt
feature/applock/src/main/java/.../AppLockScreen.kt
```

---

## Phase 20: Onboarding
**Priority: MEDIUM** | **Effort: Small**

### Goals
- Welcome new users
- Explain key features
- Guide to set as default launcher

### Tasks
1. **Onboarding Flow**
   - Welcome screen with branding
   - Feature highlights (3-4 screens)
   - Permission requests with explanations
   - "Set as default launcher" prompt

2. **First Run Detection**
   - DataStore flag for onboarding completion
   - Skip option
   - "Show again" in settings

3. **Default Launcher Prompt**
   - Explain benefits
   - Deep link to system settings
   - Handle already-default case

### Files to Create
```
feature/onboarding/src/main/java/.../OnboardingScreen.kt
feature/onboarding/src/main/java/.../OnboardingViewModel.kt
```

---

## Phase 21: Shortcut Actions
**Priority: LOW** | **Effort: Medium**

### Goals
- Long-press app shortcuts
- System shortcuts (flashlight, etc.)
- Custom shortcut creation

### Tasks
1. **Query App Shortcuts**
   - Use `LauncherApps.getShortcuts()`
   - Cache shortcuts per app
   - Handle dynamic shortcuts

2. **Shortcut UI**
   - Show shortcuts on long-press
   - Animate shortcut menu appearance
   - Pin shortcut to home

3. **System Shortcuts**
   - Flashlight toggle
   - Wi-Fi toggle
   - Bluetooth toggle
   - Screenshot (if possible)

### Files to Create
```
core/shortcuts/src/main/java/.../ShortcutRepository.kt
core/designsystem/src/main/java/.../components/ShortcutMenu.kt
```

---

## Phase 22: Analytics & Crash Reporting
**Priority: HIGH** | **Effort: Small**

### Goals
- Track crashes in production
- Understand user behavior (privacy-respecting)
- Debug production issues

### Tasks
1. **Firebase Crashlytics Setup**
   - Add Firebase dependencies
   - Initialize in Application class
   - Custom crash keys for context

2. **Privacy-Respecting Analytics**
   - Opt-in only
   - No PII collection
   - Track feature usage patterns

3. **Debug Helpers**
   - Log breadcrumbs before crashes
   - Custom exception handlers
   - ANR detection

### Files to Create/Modify
```
app/build.gradle.kts (Firebase dependencies)
app/google-services.json
app/src/main/java/.../LauncherApplication.kt
core/analytics/src/main/java/.../AnalyticsManager.kt
```

---

## Recommended Execution Order

### Sprint 1 (Foundation)
1. **Phase 18: Accessibility** - Essential for all users
2. **Phase 11: Error Handling** - Prevents bad UX
3. **Phase 22: Analytics** - Enables data-driven decisions

### Sprint 2 (Quality)
4. **Phase 10: Testing** - Enables confident development
5. **Phase 12: Performance** - Critical for launcher UX

### Sprint 3 (Core Features)
6. **Phase 20: Onboarding** - User retention
7. **Phase 13: Gestures** - Key differentiator
8. **Phase 15: Search** - High-value feature

### Sprint 4 (Advanced Features)
9. **Phase 21: Shortcuts** - Power user feature
10. **Phase 17: App Hiding** - Privacy feature
11. **Phase 19: App Lock** - Security feature

### Sprint 5 (Polish)
12. **Phase 16: Backup** - Data safety
13. **Phase 14: Widgets** - Complex, defer if needed

---

## Dependencies Between Phases

```
Phase 11 (Error Handling) ──► Phase 10 (Testing)
Phase 19 (App Lock) ──► Phase 17 (App Hiding)
Phase 13 (Gestures) ──► Phase 20 (Onboarding)
Phase 22 (Analytics) ──► All other phases
```

---

## Estimated Timeline

| Phase | Effort | Estimate |
|-------|--------|----------|
| 10 | Large | 2 weeks |
| 11 | Medium | 1 week |
| 12 | Large | 2 weeks |
| 13 | Large | 2 weeks |
| 14 | X-Large | 3+ weeks |
| 15 | Medium | 1 week |
| 16 | Medium | 1 week |
| 17 | Small | 3 days |
| 18 | Medium | 1 week |
| 19 | Medium | 1 week |
| 20 | Small | 3 days |
| 21 | Medium | 1 week |
| 22 | Small | 2 days |

**Total: ~15-17 weeks** (sequential), **~8-10 weeks** (parallelized)

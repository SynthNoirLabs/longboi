# Wallpaper + Review Fixes Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use /superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make the wallpaper visible behind Longboi Launcher with legible scrim overlays, and address review fixes (package-change refresh, AppEntry equality, grid keys, string resources).

**Architecture:** Use a transparent window background and root compose surface to reveal the system wallpaper, then add a consistent scrim layer for overlay screens. Register a `LauncherApps.Callback` in `HomeViewModel` to refresh the app catalog on package changes. Use stable keys in the grid renderer and update `AppEntry` equality semantics to reflect UI-relevant fields. Move all UI strings in the touched screens into module `strings.xml` resources.

**Tech Stack:** Kotlin 2.x, Jetpack Compose (Material3), Hilt, StateFlow/UDF, Robolectric/Compose UI tests, Proto DataStore.

---

## Task 1: Package-change refresh (tests + implementation)

**Files:**
- Modify: `feature/home/src/test/java/com/longboilauncher/app/feature/home/HomeViewModelTest.kt`
- Modify: `feature/home/src/main/java/com/longboilauncher/app/feature/home/HomeViewModel.kt`

**Step 1: Write the failing test**
```kotlin
@Test
fun `package changes trigger catalog refresh`() = runTest(testDispatcher) {
    createMocks()
    val callbackSlot = slot<LauncherApps.Callback>()
    every { appCatalogRepository.registerPackageListener(capture(callbackSlot)) } just Runs

    val viewModel = createViewModel()

    callbackSlot.captured.onPackageAdded("com.test.new", Process.myUserHandle())

    coVerify(exactly = 2) { appCatalogRepository.refreshAppCatalog() }
}
```

**Step 2: Run test to verify it fails**
Run: `./gradlew :feature:home:testDebugUnitTest --tests "*HomeViewModelTest*package changes trigger catalog refresh"`
Expected: FAIL (no package listener wiring yet)

**Step 3: Write minimal implementation**
```kotlin
private val packageCallback = object : LauncherApps.Callback() {
    override fun onPackageAdded(packageName: String, user: UserHandle) {
        onEvent(HomeEvent.RefreshCatalog)
    }
    override fun onPackageRemoved(packageName: String, user: UserHandle) {
        onEvent(HomeEvent.RefreshCatalog)
    }
    override fun onPackageChanged(packageName: String, user: UserHandle) {
        onEvent(HomeEvent.RefreshCatalog)
    }
    override fun onPackagesAvailable(packages: Array<out String>, user: UserHandle, replacing: Boolean) {
        onEvent(HomeEvent.RefreshCatalog)
    }
    override fun onPackagesUnavailable(packages: Array<out String>, user: UserHandle, replacing: Boolean) {
        onEvent(HomeEvent.RefreshCatalog)
    }
    override fun onPackagesSuspended(packages: Array<out String>, user: UserHandle) {
        onEvent(HomeEvent.RefreshCatalog)
    }
    override fun onPackagesUnsuspended(packages: Array<out String>, user: UserHandle) {
        onEvent(HomeEvent.RefreshCatalog)
    }
}

init {
    appCatalogRepository.registerPackageListener(packageCallback)
    ...
}

override fun onCleared() {
    appCatalogRepository.unregisterPackageListener(packageCallback)
    super.onCleared()
}
```

**Step 4: Run test to verify it passes**
Run: `./gradlew :feature:home:testDebugUnitTest --tests "*HomeViewModelTest*package changes trigger catalog refresh"`
Expected: PASS

**Step 5: Commit**
Run: `git add feature/home/src/main/java/com/longboilauncher/app/feature/home/HomeViewModel.kt feature/home/src/test/java/com/longboilauncher/app/feature/home/HomeViewModelTest.kt && git commit -m "Refresh app catalog on package changes"`

---

## Task 2: AppEntry equality semantics (tests + implementation)

**Files:**
- Modify: `core/model/src/test/java/com/longboilauncher/app/core/model/AppEntryTest.kt`
- Modify: `core/model/src/main/java/com/longboilauncher/app/core/model/AppEntry.kt`

**Step 1: Write the failing test**
```kotlin
@Test
fun `AppEntry equality includes label changes`() {
    val entry1 = AppEntry(
        packageName = "com.example.app",
        className = "com.example.app.MainActivity",
        label = "Example App",
        userIdentifier = 0,
    )
    val entry2 = entry1.copy(label = "Renamed")

    assertNotEquals(entry1, entry2)
}
```

**Step 2: Run test to verify it fails**
Run: `./gradlew :core:model:testDebugUnitTest --tests "*AppEntryTest*equality includes label changes"`
Expected: FAIL (custom equals ignores label)

**Step 3: Write minimal implementation**
```kotlin
// Remove the custom equals() and hashCode() so the data class uses all constructor fields.
```

**Step 4: Run test to verify it passes**
Run: `./gradlew :core:model:testDebugUnitTest --tests "*AppEntryTest*equality includes label changes"`
Expected: PASS

**Step 5: Commit**
Run: `git add core/model/src/main/java/com/longboilauncher/app/core/model/AppEntry.kt core/model/src/test/java/com/longboilauncher/app/core/model/AppEntryTest.kt && git commit -m "Update AppEntry equality to include UI fields"`

---

## Task 3: Grid keys for favorites

**Files:**
- Modify: `core/designsystem/src/main/java/com/longboilauncher/app/core/designsystem/layout/GridLayout.kt`
- Modify: `feature/home/src/main/java/com/longboilauncher/app/feature/home/HomeScreen.kt`

**Step 1: (Optional) Add a Compose UI test for key stability**
If we add a test, place it in `feature/home/src/androidTest/java/.../GridLayoutKeyTest.kt` using `createComposeRule()` and reorder a list to ensure state follows items when `itemKey` is provided.

**Step 2: Implement stable key support**
```kotlin
fun <T> GridLayout(
    items: List<T>,
    modifier: Modifier = Modifier,
    columns: Int = 4,
    animateEntrance: Boolean = true,
    itemKey: ((T) -> Any)? = null,
    content: @Composable (T) -> Unit,
) {
    if (itemKey != null) {
        LazyVerticalGrid(...) {
            itemsIndexed(items, key = { _, item -> itemKey(item) }) { ... }
        }
    } else {
        LazyVerticalGrid(...) { itemsIndexed(items) { ... } }
    }
}
```

```kotlin
GridLayout(
    items = uiState.favorites,
    modifier = Modifier.weight(1f),
    columns = 4,
    itemKey = { it.id },
) { favorite -> ... }
```

**Step 3: Manual verification**
Open Home in grid mode and reorder favorites; verify items don’t visually jump or reset.

**Step 4: Commit**
Run: `git add core/designsystem/src/main/java/com/longboilauncher/app/core/designsystem/layout/GridLayout.kt feature/home/src/main/java/com/longboilauncher/app/feature/home/HomeScreen.kt && git commit -m "Add stable keys to favorites grid"`

---

## Task 4: Wallpaper visibility + overlay scrims

**Files:**
- Modify: `app/src/main/java/com/longboilauncher/app/MainActivity.kt`
- Modify: `app/src/main/res/values/themes.xml`
- Modify: `feature/allapps/src/main/java/com/longboilauncher/app/feature/allapps/AllAppsScreen.kt`
- Modify: `feature/searchui/src/main/java/com/longboilauncher/app/feature/searchui/SearchScreen.kt`
- Modify: `feature/settingsui/src/main/java/com/longboilauncher/app/feature/settingsui/SettingsScreen.kt`
- Modify: `feature/settingsui/src/main/java/com/longboilauncher/app/feature/settingsui/HiddenAppsScreen.kt`
- Modify: `feature/backup/src/main/java/com/longboilauncher/app/feature/backup/BackupScreen.kt`
- Modify: `feature/widgetpicker/src/main/java/com/longboilauncher/app/feature/widgetpicker/WidgetPickerScreen.kt`

**Step 1: Write minimal implementation**
- Set `Surface` in `MainActivity` to `Color.Transparent`.
- Add theme attributes:
  - `<item name="android:windowShowWallpaper">true</item>`
  - `<item name="android:windowBackground">@android:color/transparent</item>`
- For overlay screens, add a `Box` with `MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)` as a background layer and set any root `Surface/Scaffold` container colors to transparent.

**Step 2: Manual verification**
- Set a distinctive wallpaper.
- Open Home, All Apps, Search, Settings, Hidden Apps, Backup, Widget Picker.
- Confirm wallpaper is visible with a legible scrim overlay.

**Step 3: Commit**
Run: `git add app/src/main/java/com/longboilauncher/app/MainActivity.kt app/src/main/res/values/themes.xml feature/allapps/src/main/java/com/longboilauncher/app/feature/allapps/AllAppsScreen.kt feature/searchui/src/main/java/com/longboilauncher/app/feature/searchui/SearchScreen.kt feature/settingsui/src/main/java/com/longboilauncher/app/feature/settingsui/SettingsScreen.kt feature/settingsui/src/main/java/com/longboilauncher/app/feature/settingsui/HiddenAppsScreen.kt feature/backup/src/main/java/com/longboilauncher/app/feature/backup/BackupScreen.kt feature/widgetpicker/src/main/java/com/longboilauncher/app/feature/widgetpicker/WidgetPickerScreen.kt && git commit -m "Show wallpaper with scrim overlays"`

---

## Task 5: String resource sweep (UI literals)

**Files:**
- Create: `feature/searchui/src/main/res/values/strings.xml`
- Create: `feature/backup/src/main/res/values/strings.xml`
- Modify: `feature/settingsui/src/main/res/values/strings.xml`
- Modify: `feature/widgetpicker/src/main/res/values/strings.xml`
- Modify: `feature/searchui/src/main/java/com/longboilauncher/app/feature/searchui/SearchScreen.kt`
- Modify: `feature/settingsui/src/main/java/com/longboilauncher/app/feature/settingsui/SettingsScreen.kt`
- Modify: `feature/settingsui/src/main/java/com/longboilauncher/app/feature/settingsui/HiddenAppsScreen.kt`
- Modify: `feature/backup/src/main/java/com/longboilauncher/app/feature/backup/BackupScreen.kt`
- Modify: `feature/widgetpicker/src/main/java/com/longboilauncher/app/feature/widgetpicker/WidgetPickerScreen.kt`

**Step 1: Move UI literals into resources**
- Create module-specific strings for:
  - Search placeholder, hints, empty states, content descriptions.
  - Settings dialog titles/buttons, gesture labels, favorites info text.
  - Hidden Apps headers/subtitles/content descriptions.
  - Backup screen titles, body text, button labels, snackbar messages.
  - Widget picker title, search hint, empty state, expand/collapse labels.

**Step 2: Update usages**
Replace string literals with `stringResource(...)` and `contentDescription = stringResource(...)`.

**Step 3: Update tests if needed**
If UI tests rely on literal text, keep resource values identical to existing strings so tests remain stable.

**Step 4: Commit**
Run: `git add feature/searchui/src/main feature/backup/src/main feature/settingsui/src/main feature/widgetpicker/src/main && git commit -m "Move UI strings to resources"`

---

## Task 6: Verification

**Automated:**
- `./gradlew :feature:home:testDebugUnitTest`
- `./gradlew :core:model:testDebugUnitTest`

**Manual:**
- Set a wallpaper and verify it appears on Home and behind overlays with scrims.
- Open All Apps/Search/Settings/Hidden Apps/Backup/Widget Picker for readability.

---

**Ready to execute with** `/superpowers:executing-plans`?

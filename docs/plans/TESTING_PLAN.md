# Testing & Quality Plan — Longboi-like Launcher (Pixel 9 Pro / Android 16)

This is the **end-to-end testing architecture + setup guide** for your personal launcher.

It’s designed for:

- **Fast feedback** while you build (unit + Robolectric + screenshot)
- **Confidence** that the launcher won’t brick your daily phone (instrumentation + manual smoke)
- **Performance stability** (macrobenchmark + baseline profile checks)

---

## 1) Testing goals and philosophy

### 1.1 What we’re protecting

Launchers are “special” apps:

- They must stay stable as **default Home**.
- They interact with OS-only surfaces (widgets, notifications, roles/profiles).
- A regression can lock you out of your daily workflow.

So the goal isn’t just “coverage”, it’s:

1) **No crash loops**
2) **No broken core navigation** (home → all apps → search → open app)
3) **No missing apps / wrong profiles**
4) **No Private Space leakage**
5) **No jank** (especially wave scrub + search)

### 1.2 Test pyramid (practical)

You want lots of small tests, fewer large tests.

Recommended weighting (by count/time):

- **Static checks**:
  - **Spotless + Ktlint**: Enforced project-wide for consistent formatting.
  - **Android Lint**: Integrated with custom rules for launcher-specific permissions.
- **Unit tests** (pure Kotlin): many
- **Robolectric** (Android framework on JVM): some
- **Compose UI tests** (instrumented): some
- **End-to-end / system UIAutomator**: a handful of smoke flows
- **Screenshot tests**: targeted “goldens” using `androidx.screenshot` (experimental).
- **Macrobenchmarks**: a few critical journeys only
- **Coverage**: Measured project-wide using **Kover**.

---

## 2) Testing architecture: make the app testable by design

### 2.1 Dependency boundaries

Define interfaces at boundaries so you can swap real/fake implementations:

- `AppCatalog` (installed apps / profiles)
- `ShortcutCatalog`
- `IconRepository`
- `SearchIndex`
- `UsageStatsRepository` (optional)
- `NotificationRepository` (optional)
- `WeatherRepository` (optional)
- `Clock` / `NowProvider` (time)

**Rule:** UI talks to domain interfaces, not directly to framework APIs.

### 2.2 Unidirectional data flow (UDF)

Use a reducer style for key screens:

- state is a data class
- events are sealed types
- reducer returns new state + side effects

This makes behavior unit-testable with “given state + event → expected state”.

### 2.3 Time, coroutines, randomness

- Inject `NowProvider` and **`ClockTicker`** so time-sensitive features (like the glance clock) are deterministic.
- Use `kotlinx-coroutines-test` with a test dispatcher.
- Use `advanceTimeBy` and `runCurrent` to control time in tests without infinite loops.

### 2.4 Compose testability

- Add `testTag` to important nodes.
- Prefer stable semantics:
  - `contentDescription` for icons/buttons
  - clear labels in action sheets
- Keep business logic out of composables; composables should be easy to snapshot.

---

## 3) Test suite layout (modules + where tests live)

### 3.1 Folder layout (standard)

- `src/test/` → JVM unit tests (fast)
- `src/androidTest/` → instrumentation tests (device/emulator)

### 3.2 Optional: dedicated modules

Highly recommended for a launcher:

1) **:benchmark** module
   - macrobenchmark tests
   - baseline profile generation flows

2) **:screenshot** (optional)
   - screenshot tests on JVM (Roborazzi) to keep them isolated from app logic

---

## 4) Tooling choices (recommended stack)

### 4.1 Unit tests (JVM)

- JUnit4/5 (pick one; JUnit4 is simplest for Android)
- Truth (assertions) or Kotest
- MockK (mocking) — but prefer fakes over mocks
- kotlinx-coroutines-test
- Turbine (testing Flow)

### 4.2 Robolectric (JVM “Android-ish”)

Use Robolectric for:

- Intent resolution logic
- PackageManager wrappers
- Drawable/icon transformations
- Widget sizing math
- Parsing system settings intents

### 4.3 Compose UI tests (instrumented)

Use Compose’s test APIs:

- verify navigation and interactions
- verify semantics (labels exist)
- verify gesture mapping behavior

### 4.4 Screenshot tests (visual regression)

Pick **one** approach:

**Option A — Official Compose Preview Screenshot Testing**

- host-side screenshot tests integrated with previews
- fast and scalable for many UI variants

**Option B — Roborazzi (JVM screenshot tests)**

- can run on the JVM (fast)
- can capture screenshots and compare against golden images
- good for design system + components

Best practice:

- Screenshot test **components** and **key screens in stable states**.
- Avoid snapshotting highly dynamic content.

### 4.5 Performance tests

- **Macrobenchmark** for:
  - cold start
  - warm start
  - home→all apps
  - wave scrub
  - search overlay
- Baseline profile generation and verification

### 4.6 Accessibility checks

- Espresso accessibility checks for view-based (and hybrid) flows
- Compose semantics assertions + automated a11y checks (where supported)

---

## 5) Setup: Gradle + Android Studio configuration

### 5.1 Gradle tasks you’ll rely on

- `./gradlew test` → local unit tests
- `./gradlew connectedAndroidTest` → instrumentation (connected device/emulator)
- `./gradlew <managedDevice>DebugAndroidTest` → Gradle Managed Devices (recommended)
- `./gradlew :benchmark:connectedAndroidTest` → macrobenchmarks

### 5.2 Gradle Managed Devices (recommended)

- Define a managed emulator config for API 36 (Android 16) and one older API for sanity.
- Run instrumentation tests on managed devices to reduce flakiness and improve consistency.

### 5.3 Android Test Orchestrator (recommended)

Enable Orchestrator for instrumentation tests so each test runs in its own Instrumentation instance.

### 5.4 Screenshot baseline management

- Store golden images under version control.
- Create a standard process:
  - if screenshot changes are intended: regenerate goldens + commit
  - if not: fix UI bug

### 5.5 Benchmark module

- Configure a benchmark build type
- Ensure macrobenchmark runs on a *non-debuggable* variant for realistic numbers

---

## 6) What to test (launcher-specific checklists)

### 6.1 App catalog correctness

**Unit tests**

- label sorting rules
- profile grouping rules
- hidden app filtering
- archived state handling

**Robolectric**

- PackageManager events mapping
- LauncherApps query behavior wrappers

**Instrumentation**

- install/uninstall app updates list
- work profile on/off changes list

### 6.2 Home surface behavior

**Compose UI tests**

- favorites render in correct order
- long press opens actions sheet
- swipe right opens pop-up (if enabled)
- drag reorder persists
- swipe up opens all apps
- swipe down opens notifications/search (depending on setting)

### 6.3 All Apps + wave scrub

**Unit tests**

- section indexing: letter → list index
- wave scrub mapping: y-position → letter

**Compose UI tests**

- scrub jumps to section
- typing filters list

**Macrobenchmark**

- scrub performance (no jank)

### 6.4 Search overlay (command palette)

**Unit tests**

- query normalization (case, accents)
- fuzzy matching
- ranking rules (favorites boost, recency)
- provider enable/disable

**Compose UI tests**

- opens instantly
- first result opens on Enter
- long press opens actions

### 6.5 Pop-ups

**Unit tests**

- pop-up content composition rules (slots)

**Compose UI tests**

- opens/dismisses reliably
- doesn’t block one-handed reach

### 6.6 Widgets host

Widgets are hard to fully automate.

**Unit tests**

- widget layout persistence format

**Instrumentation (focused)**

- add widget flow
- resize handles appear
- after rotation, widget remains
- after reboot simulation (if possible), widgets restore best-effort

### 6.7 Notifications (optional)

**Unit tests**

- notification filtering rules
- privacy redaction handling rules

**Instrumentation**

- dots appear when notifications exist
- inline preview displays when permitted

### 6.8 Private Space (Android 15+)

**Unit tests**

- locked state filters private apps from search and suggestions

**Instrumentation (API 35/36)**

- private container appears
- when locked: no private apps in search
- unlock shows apps

### 6.9 Archived apps

**Unit tests**

- archived apps display style selection

**Instrumentation**

- archived app indicates restore action

---

## 7) Performance and regression gates

### 7.1 Benchmarks you should keep forever

- Cold start time
- Warm start time
- All Apps open time
- Search open time
- Wave scrub sustained frame time

### 7.2 Baseline profile workflows

- Create baseline profile for:
  - open home
  - open all apps
  - open search
  - open app
- Measure macrobenchmark with and without profiles

### 7.3 Jank regression checks

- Track frame timing and set a “no regression” rule.
- If performance drops, treat as a bug.

---

## 8) CI plan (even for personal projects)

### 8.1 PR / commit gates

Run on every push:

- ktlint/detekt (if you use them)
- Android Lint
- unit tests
- Robolectric tests
- screenshot tests (optional)

### 8.2 Nightly/weekly gates

- instrumentation tests on managed device API 36
- macrobenchmark suite

### 8.3 Artifacts

- upload screenshot diffs
- upload benchmark reports
- keep a history for trendline

---

## 9) Flakiness and stability playbook

### 9.1 Reduce flakiness by design

- No sleeps; use idling/sync primitives
- Inject clocks and dispatchers
- Avoid relying on network

### 9.2 When a test flakes

- Re-run once
- If it passes on re-run: mark flaky and fix root cause
- If repeatable: treat as regression

### 9.3 Crash loop protection

- Add “safe mode” startup path:
  - if launcher crashes N times within M seconds, disable pop-ups/widgets/notifications module and start minimal UI

---

## 10) Manual test plan (critical for a launcher)

You still need manual checks because of system UI and OEM behaviors.

### 10.1 Manual smoke flows (every major change)

1. Set as default launcher → reboot → still default
2. Open home → open all apps → open an app → back home
3. Search → open result → back home
4. Rotate device → everything still usable
5. Change font size + display size → layout remains readable
6. Enable/disable notification previews (if you use them)
7. Private Space lock/unlock (if enabled)
8. Add/remove a widget

### 10.2 Pixel-specific feel checks

- Wave scrub haptics feel right
- Scroll at 120Hz feels stable
- Edge-to-edge insets correct

---

## 11) Definition of Done (per feature)

A feature is “done” only if:

- unit tests cover core logic
- at least one UI test covers the main happy path
- accessibility semantics are present
- it doesn’t degrade startup/scroll benchmarks
- it passes manual smoke on your Pixel 9 Pro

---

## 12) Practical starter checklist (first week)

1. Add test dependencies + basic harness
2. Set up:
   - unit tests for reducers/state
   - Robolectric for PackageManager wrapper
3. Compose UI test for:
   - favorites list
   - swipe up opens all apps
4. Add one screenshot golden for:
   - home light
   - home dark
5. Create a macrobenchmark skeleton for cold start

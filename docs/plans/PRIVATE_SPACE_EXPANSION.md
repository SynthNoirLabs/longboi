# Private Space Expansion Plan (Phase 3.2 - 3.5)

This document outlines the implementation details for the next stages of the **Longboi Launcher** Private Space feature.

## Phase 3.2: Configurable Authentication & Auto-lock

### 1. Data Model (Proto) - DONE

Modified `core/datastore-proto/src/main/proto/user_settings.proto`:

- Added `enum PrivateSpaceAuthMethod { NONE, BIOMETRIC, PIN }`
- Added `enum PrivateSpaceAutoLockPolicy { IMMEDIATELY, AFTER_TIMEOUT, ON_SCREEN_OFF }`
- Updated `UserSettings` message with these new fields and `private_space_pin_hash`.

### 2. PreferencesRepository (:core:settings) - DONE

- Exposed new settings as `StateFlow`.
- Added setters for `authMethod`, `autoLockPolicy`, and `privateSpacePin`.

### 3. Security Module (:core:security - NEW)

Create a new module to handle authentication logic:

- `AuthGate`: Interface for requesting unlock.
- `AuthGateProvider`: Strategy to return the correct implementation based on user settings.
- `BiometricAuthGate`: Implementation using `BiometricPrompt`.
- `PinAuthGate`: Implementation using a custom Compose dialog for PIN entry.
- `NoneAuthGate`: Simple bypass implementation.

### 4. ViewModel Wiring (:feature:allapps)

- Update `AllAppsViewModel` to use the configured `AuthGate` when `UnlockPrivateSpace` event is received.
- The ViewModel should trigger an `Effect` that the UI handles to show the appropriate auth prompt (biometric dialog or PIN entry).

### 5. Auto-lock Logic

- Implement a `PrivateSpaceAutoLockManager` (in `:core:common` or `:core:settings`).
- **Screen Off**: Register a `BroadcastReceiver` for `ACTION_SCREEN_OFF`.
- **Timeout**: Store `last_activity_timestamp` in `PreferencesRepository`. On app resume (e.g., in `MainActivity`), if `currentTime - last_activity > timeout`, lock the space.
- Default policy: Configurable between "Immediately", "On Screen Off", or "X Minutes".

---

## Phase 3.3: Platform Integration

### 1. AppCatalogRepository (:core:appcatalog)

- Integrate `UserManager.getUserProfiles()` and `UserManager.isPrivateProfile(UserHandle)` (Android 15+).
- Map apps from private profiles to `ProfileType.PRIVATE`.
- Ensure fallback for devices < Android 15.

---

## Phase 3.4: Settings UI

### 1. UI Implementation (:feature:settingsui)

- Create a new `PrivateSpaceSettingsScreen`.
- Add selectors for Auth Method and Auto-lock Policy.
- Implement PIN setup/change flow with validation.

---

## Phase 3.5: UX/Accessibility Polish

### 1. Visuals & Haptics

- Refine the 'Unlock' and 'Lock' rows in `AllAppsScreen` to match Material 3 list item styles.
- Trigger haptic feedback via `Vibrator` or `HapticFeedback` on lock/unlock success/failure.

### 2. Accessibility

- Add proper `contentDescription` and `semantics` for the Private Space header and action rows.
- Ensure TalkBack correctly announces the state change.

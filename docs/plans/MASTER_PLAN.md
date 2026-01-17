# Longboi-like Launcher Master Plan — Pixel 9 Pro (Android 16) Personal Build

**Default style:** Longboi-like (list-first, one-handed, minimal)
**Device/OS target:** Pixel 9 Pro, Android 16 (Evolution X ROM)
**Goal:** a launcher that feels *native-Pixel fast*, visually calm, and obsessively usable.

This is a **complete, detailed spec**: surfaces, behaviors, settings, data model, and an execution roadmap.

---

## 0) North Star: what “excellent” means here

### 0.1 Non-negotiables
1. **One-handed, thumb-first**: primary interactions reachable without gymnastics.
2. **Speed**: no perceptible lag; all scrolling/scrubbing is 120Hz-smooth when possible.
3. **Calm visuals**: typography + whitespace over visual noise.
4. **Predictable**: gestures are consistent; no hidden traps.
5. **Modern Android**: Private Space, archiving, edge-to-edge, themed icons.
6. **Personal-first**: you optimize for *your* flow (not “feature parity for marketing”).

### 0.2 Design principles (use these to decide every feature)
- **Default should be enough.** Advanced settings exist, but defaults are beautiful.
- **Progressive disclosure.** Hide power features behind “Advanced”.
- **Everything is reversible.** Undo, reset layout, safe mode.
- **Permission minimization.** Ask only when enabling a feature.

---

## 1) UX model overview (what the launcher *is*)

You’re building a **single primary home surface** with:
- A **glance header** (time/date + next event + optional weather)
- A **Favorites list** (your top apps)
- An **All Apps list** reachable instantly
- **Search** as a command palette overlay
- Optional **pop-ups** per app (shortcuts/widgets/notifications)

**No default grid pages.** Grid mode can exist as an optional “classic mode”, but the default experience is list-first.

---

## 2) Surfaces and UI spec

### 2.1 Home surface (default landing)

#### Layout
- **Status rail (optional):** small top padding; can hide clock if you prefer full-screen clean.
- **Glance header (always on by default):**
  - Large clock + date
  - Next calendar event (one line, with time)
  - Optional: weather (temp + condition)
  - Optional: next alarm
- **Favorites list:**
  - Default 10 items (range 6–16)
  - Each row: icon + label + optional notification dot/count + optional quick indicator (e.g., “playing”)
- **Adaptive/Context row (optional):**
  - “Now Playing” media tile (only when active)
  - “Recent” or “Suggested” row (opt-in)
- **All Apps entry:**
  - Default gesture (swipe up) opens All Apps list.
  - Optional: a subtle “All Apps” hint affordance (can be hidden).

#### Core interactions
- **Tap** favorite → open
- **Long press** favorite → actions sheet
- **Swipe right** on favorite → pop-up panel (if enabled)
- **Drag** favorite → reorder / remove / add to collection
- **Long press empty area** → Edit mode
- **Swipe up** → All Apps (default)
- **Swipe down** → Notifications shade (default) OR Search (toggle)
- **Type anywhere** (if keyboard present) → Search overlay

#### Visual details
- Item height and spacing should feel “breathing” (Longboi-like).
- Text size tuned for readability; supports system font size.
- Use Material 3 typography but don’t over-style.

---

### 2.2 All Apps surface (fast A–Z list)

#### Layout
- Vertical list with section headers (A, B, C…)
- **Wave alphabet scrubber** on right edge:
  - Always visible by default
  - Can auto-hide until touch
- Optional top chips (collapsed):
  - Recents
  - Work profile
  - Private Space (if unlocked)

#### Interactions
- **Scroll** normally
- **Scrub alphabet** (thumb along edge) to jump letters
- **Type** immediately to search
- **Long press** app → actions sheet
- **Drag** app → pin to favorites

#### Sorting and visibility
- Default: alphabetical by label
- Optional: “Top used” section (opt-in, requires usage permission)
- Hidden apps:
  - Hidden apps do not appear in list
  - Optional: hidden apps also excluded from search

---

### 2.3 Search surface (command palette overlay)

This should feel like: “I can do anything from here.”

#### Trigger
- Swipe up (optional alternative)
- Tap search icon (optional)
- Type anywhere (hardware keyboard)
- Long press gesture (optional)

#### Result types (toggleable scopes)
1. **Apps** (always)
2. **App shortcuts** (dynamic/static)
3. **Settings shortcuts** (system intents / panels)
4. **Contacts** (optional permission)
5. **Web** (optional provider)
6. **Calculator + conversions** (always, local)
7. **Actions** (where feasible):
   - “Timer 5m”
   - “Flashlight” (via intent if available)
   - “Wi‑Fi panel / Bluetooth panel” (panels)

#### Ranking and behavior
- Results update per keystroke
- Fuzzy matching and acronym matching
- Prioritize:
  - exact prefix match
  - frequently launched
  - recently launched
  - pinned favorites

#### UI/UX details
- First result opens on Enter
- Long press result opens actions
- Clear “history” and “web suggestions” toggles

---

### 2.4 Pop-ups / panels (Longboi-like)

Pop-ups are optional but powerful if consistent.

#### Opening gestures
- Swipe right on app row (recommended)
- Long press → “Open pop-up”

#### Pop-up contents (slots)
- Slot A: App shortcuts (always)
- Slot B: Optional mini widgets (per app)
- Slot C: Optional notifications (per app)
- Slot D: Optional “App pair” shortcuts

#### Constraints
- Pop-up must open instantly
- One-handed placement: open toward center, not off-screen
- Must be dismissible by swipe/tap outside

---

### 2.5 Edit mode (customization)

#### Entry
- Long press empty home

#### Sections
1. **Favorites**
   - reorder, remove, add
2. **Glance header**
   - toggle modules: calendar/weather/alarm
   - reorder modules
3. **Pop-ups**
   - enable/disable globally
   - per app pop-up customization
4. **Gestures**
5. **Appearance**
6. **Backup/Restore**

#### Edit UX rules
- Always show Undo
- Add flows should be searchable
- “Reset layout” resets only layout

---

## 3) Interaction design: gestures, haptics, motion

### 3.1 Gestures (recommended default mapping)
- Swipe up: All Apps
- Swipe down: Notifications
- Double tap: Screen off (optional; may require accessibility/Device Admin depending)
- Swipe right on app: Pop-up
- Long press: Actions sheet

### 3.2 Gesture customization (safe subset)
Allow users (you) to remap:
- swipe up/down
- double tap
- long press empty

Avoid default edge gestures that fight system back.

### 3.3 Haptics
Haptics should be subtle and meaningful:
- Wave scrub: light tick per letter
- Reorder snap: tick
- Drop targets: stronger tick

Respect system haptic settings.

### 3.4 Motion and animations
- Favor short, easing transitions
- Reduce motion support: disable non-essential animation
- Always keep scroll performance stable

---

## 4) Visual design system (Material You, but calmer)

### 4.1 Color
- Default to Material You dynamic color
- Provide optional:
  - “Neutral mode” (grays with accent)
  - “High contrast” mode
- Keep backgrounds simple and low texture

### 4.2 Typography
- One primary font (system)
- Adjust letter spacing for readability
- Respect system font scale
- Optional custom font (advanced)

### 4.3 Density and spacing
- Global density slider: Compact / Default / Relaxed
- Item height affects usability more than icon size

### 4.4 Icon style pipeline
You want icons to look consistent no matter the pack.

Support:
- Adaptive icons
- Themed icons
- Icon packs (optional)
- Per-app overrides

Quality features:
- Auto masking correction
- Background unification for non-adaptive icons
- Themed icon fallback generation

---

## 5) Organization features (minimal, but effective)

### 5.1 Favorites (primary)
- Pin/unpin
- Reorder
- Optional grouping headers (e.g., “Work”, “Daily”) without making full folders

### 5.2 Collections (lightweight folders)
Instead of grid folders, use **list collections**:
- A collection is a collapsible group in favorites
- Supports renaming + emoji
- Can be opened as a pop-up panel too

### 5.3 Hidden apps
- Hide from All Apps
- Optional hide from search
- Optional “Hidden section” behind a lock (PIN/biometric)

---

## 6) Widgets strategy (don’t let widgets ruin the aesthetic)

### 6.1 External widgets (AppWidgetHost)
- Full widget picker with search + previews
- Add to:
  - top glance area (recommended)
  - optional “widget page” (advanced)

### 6.2 Widget stacks (optional)
- Stack multiple widgets in the same slot
- Swipe within stack
- Show subtle page dots

### 6.3 Built-in widgets (Glance)
Create a small set that match your design:
- Clock/date
- Agenda
- Weather
- Battery
- Notes

---

## 7) Notifications (default minimal, expandable)

### 7.1 Default: dots
- Dot badges per app
- Optional counts
- Per-app toggle

### 7.2 Optional: inline previews
Requires Notification Listener permission.

If enabled:
- Show 1-line preview in favorites row
- Tap opens app
- Long press opens full notification actions

Constraints:
- Must respect privacy settings
- Handle redacted content gracefully

---

## 8) Modern Android 16-first requirements

### 8.1 Private Space support
Design it as a first-class container.

Rules:
- Private apps are separate and clearly labeled
- When locked:
  - not visible in search
  - not visible in favorites
  - no suggestions
- Provide:
  - “Unlock Private Space” row
  - “Lock now” action

### 8.2 App archiving support
- Archived apps appear visually distinct
- Tapping triggers restore flow
- Search may show archived apps with a “restore” action

### 8.3 Edge-to-edge everywhere
- Correct inset handling for:
  - status bar
  - nav bar
  - IME
  - gesture areas

### 8.4 Predictive back correctness
- Your surfaces should participate correctly
- Don’t fight system back

### 8.5 Themed icons evolution
- Support monochrome icons
- Have sane fallback rendering

---

## 9) Permissions strategy (keep it clean)

### 9.1 No-permission baseline
The launcher should be great with **zero** extra permissions.

### 9.2 Optional permissions (feature-gated)
- Notification access → inline previews / notification panel
- Usage access → “most used”, wellbeing
- Contacts → contact search
- Location → weather (or use manual city)

Each permission request should be:
- explained in plain language
- only prompted when you enable the feature

---

## 10) Technical architecture (Compose-first, pragmatic)

### 10.1 Suggested modules
- **core-appcatalog**: package & profile data, listeners
- **core-icons**: icon rendering, caching, theming
- **core-search**: indexing + ranking + providers
- **feature-home**: home UI
- **feature-allapps**: list + wave scrub
- **feature-search**: command palette UI
- **feature-popups**: pop-up UI + configuration
- **feature-widgets**: widget host + picker
- **feature-notifications** (optional)
- **feature-privatespace**
- **feature-settings**
- **feature-backup**
- **core-designsystem**: tokens, typography, components

### 10.2 State management
- UDF: state down, events up
- StateFlow for persistent UI state
- SharedFlow for one-shot events
- SavedStateHandle where needed

### 10.3 Storage
- **Proto DataStore** for settings + theme + gestures
- **Room** (optional) for:
  - favorites ordering
  - collections
  - widget bindings
  - pop-up configurations

### 10.4 App catalog subsystem
- Maintain an in-memory list of AppEntries keyed by:
  - user/profile
  - package
  - activity

Track:
- label
- component name
- install/update timestamps
- enabled/disabled/suspended
- archived state
- profile category (personal/work/private)

Listeners:
- package add/remove/change
- shortcuts changes
- profile availability changes

### 10.5 Icon pipeline subsystem
- Memory cache (LRU)
- Optional disk cache
- Render path:
  1) icon pack override?
  2) themed icon available?
  3) adaptive icon normal
  4) fallback: mask + background unify

Use async decoding; never block UI thread.

### 10.6 Search subsystem
Start simple:
- In-memory index of apps + shortcuts

Upgrade paths:
- AppSearch for fast device indexing
- Add provider interfaces:
  - Apps
  - Shortcuts
  - Settings intents
  - Contacts
  - Web
  - Calculator

Ranking features:
- launch frequency
- recency
- exact match boost
- pinned favorites boost

### 10.7 Widgets host subsystem
- AppWidgetHost lifecycle safe handling
- Resizing and configuration flows
- Recovery after reboot

---

## 11) Performance targets (Pixel 9 Pro)

### 11.1 Startup
- Cold start: no blank UI; show cached list immediately
- Warm start: instant

### 11.2 Scrolling
- All Apps list: stable, no layout thrash
- Wave scrub: no jank (precompute section indices)

### 11.3 Battery
- No polling loops
- Only react to broadcasts/callbacks

### 11.4 Tools
- Baseline profiles
- Macrobenchmark: startup + list scroll + wave scrub
- JankStats in debug
- StrictMode in debug

---

## 12) Testing and “don’t break my phone” safety

### 12.1 Functional tests
- Default launcher role set/unset
- App install/uninstall updates list
- Work profile toggles
- Private Space lock/unlock
- Archived app behavior

### 12.2 UI tests
- Compose UI tests for:
  - home actions sheet
  - search overlay
  - settings toggles

### 12.3 Visual regression
- Screenshot tests on:
  - light/dark
  - large font
  - different dynamic color palettes

### 12.4 Safe mode
- A “safe mode” launch: disables custom renderers/pop-ups if crash loop detected

---

## 13) Detailed feature checklist (complete)

### 13.1 Home
- Glance header modules: clock/date, calendar, alarm, weather, media
- Favorites list: reorder, pin/unpin, rename, hide
- Collections: collapsible groups
- Optional adaptive suggestions

### 13.2 All Apps
- A–Z list with headers
- Wave scrub + floating letter overlay
- Search-on-type
- Hidden apps handling
- Profile grouping (personal/work/private)

### 13.3 Search
- Apps, shortcuts, settings, calculator, conversions
- Optional: contacts, web
- History controls
- Provider toggles

### 13.4 App actions
- App info
- Uninstall
- Shortcuts
- Pin/unpin
- Rename
- Hide

### 13.5 Pop-ups
- Enable/disable
- Content slots
- Per-app customization

### 13.6 Widgets
- Host
- Picker
- Resize
- Reconfigure
- Optional stacks

### 13.7 Notifications
- Dots
- Optional counts
- Optional inline previews

### 13.8 Appearance
- Dynamic color
- Light/dark/system
- Density
- Typography scale
- Icon size
- Themed icons toggles

### 13.9 Gestures
- swipe up/down mapping
- double tap mapping
- haptics intensity

### 13.10 Backup
- Export/import settings
- Export/import layout

### 13.11 Android 16 readiness
- Private Space container
- Archived apps UX
- edge-to-edge
- predictive back correctness
- themed icons evolution

---

## 14) Build roadmap (high confidence order)

### Phase 1 — Core daily driver (switch full time)
1. ROLE_HOME onboarding
2. App catalog (personal/work)
3. Home favorites list + actions sheet
4. All Apps list + A–Z headers
5. Search overlay (apps only)
6. Material You theming + settings

### Phase 2 — Longboi “signature feel”
1. Wave alphabet scrubber
2. Haptics tuning + motion tuning
3. Search upgrades: shortcuts + calculator
4. Pop-ups v1 (shortcuts only)

### Phase 3 — Modern Android correctness
1. Private Space container + lock/unlock + search safety
2. Archived apps visuals + restore flow
3. Edge-to-edge polish + insets audit
4. Predictive back audit on all surfaces

### Phase 4 — Power features (only what you personally want)
1. Widgets host + picker + resize
2. Pop-ups v2 (widgets + notifications)
3. Search providers: settings panels, conversions
4. App pairs

### Phase 5 — Personal delighters
- Precision mode (grid pages) if you still want it
- On-device suggestions
- Fun experiments (handwriting/radial)

---

## 15) First concrete implementation target (what to code first)

**A minimal list-first launcher you can live on:**
- Home = glance header + favorites list
- Swipe up = All Apps list
- Type = Search overlay
- Long press = actions sheet

Once this is stable and fast, add wave scrub + pop-ups.


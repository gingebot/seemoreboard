# Feature Spec: Settings MVP (Feature #4)

## 1. Overview

This feature introduces the first user‑configurable settings for the Low‑Vision Keyboard:

- **Haptics Enabled** (Boolean)
- **High‑Contrast Mode Enabled** (Boolean)

These settings are stored in DataStore, exposed via `KeyboardSettings`, observed by
`KeyboardViewModel`, and applied reactively to the keyboard UI.

This feature does **not** introduce a full settings app.  
Instead, it adds a simple **IME‑embedded settings panel** accessible via a dedicated key.

---

## 2. Goals

- Allow users to toggle haptic feedback on/off.
- Allow users to toggle high‑contrast mode on/off.
- Persist settings across sessions.
- Update keyboard UI reactively when settings change.
- Exercise the settings architecture end‑to‑end.

---

## 3. User Story

As a user with low vision,  
I want to turn haptics and high‑contrast mode on or off,  
so I can customize the keyboard to my comfort level.

---

## 4. Functional Requirements

### 4.1 Settings Panel

- A dedicated **“⚙ Settings”** key appears in the bottom row.
- Tapping it opens a simple settings panel overlay.
- The panel contains two toggles:
  - **Haptics Enabled**
  - **High‑Contrast Mode**

### 4.2 Haptics Toggle

- When OFF:
  - No haptic feedback is triggered on key press.
- When ON:
  - `performHapticFeedback(KEYBOARD_TAP)` fires normally.

### 4.3 High‑Contrast Toggle

- When OFF:
  - Keys use a softer theme (e.g., dark gray background, white text).
- When ON:
  - Keys use the existing high‑contrast theme (black background, white text).

### 4.4 Persistence

- Settings are stored in DataStore via `DataStoreSettingsRepository`.
- Settings persist across keyboard restarts and device reboots.

### 4.5 Reactive Updates

- `KeyboardViewModel` observes `settingsFlow`.
- `KeyboardState` updates automatically.
- UI updates immediately when toggles change.

---

## 5. Non‑Functional Requirements

- No Compose UI yet (View-based fallback).
- Settings panel must be accessible and large-touch-target friendly.
- No new modules — only additive changes to existing ones.
- IME service must not contain settings logic.

---

## 6. Architecture Impact

### 6.1 New Files

**`ui/components/SettingsPanelView.kt`**  
- View-based overlay containing two toggles.
- Large touch targets.
- High-contrast friendly.

### 6.2 Updated Files

**`domain/settings/KeyboardSettings.kt`**  
- Ensure fields exist:
  - `hapticEnabled: Boolean`
  - `highContrastEnabled: Boolean`

**`domain/settings/SettingsUseCases.kt`**  
- Add:
  - `setHapticEnabled(Boolean)`
  - `setHighContrastEnabled(Boolean)`

**`data/settings/DataStoreSettingsRepository.kt`**  
- Persist the two new settings.
- Update `settingsFlow`.

**`ui/keyboard/KeyboardState.kt`**  
- Add:
  - `hapticEnabled`
  - `highContrastEnabled`

**`ui/keyboard/KeyboardViewModel.kt`**  
- Observe settings.
- Update `KeyboardState` reactively.
- Provide:
  - `toggleHaptics()`
  - `toggleHighContrast()`

**`ui/components/QwertyKeyboardView.kt`**  
- Apply high-contrast mode to keys.
- Add “⚙ Settings” key to bottom row.
- Show/hide `SettingsPanelView`.

---

## 7. Acceptance Criteria

### 7.1 Functional
- Settings panel opens and closes.
- Haptics toggle works immediately.
- High-contrast toggle updates key visuals immediately.
- Settings persist across IME restarts.

### 7.2 Visual
- Settings panel is large, simple, and readable.
- High-contrast mode visibly changes key backgrounds.

### 7.3 Stability
- No crashes.
- Typing still works normally.

## 8. Testing Instructions

### 8.1 Build & Install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 8.2 Enable Keyboard
Settings → General Management → Keyboard List and Default → Enable Low Vision Keyboard → Set as default.

### 8.3 Test Settings
1. Open a text field.
2. Tap the ⚙ Settings key.
3. Toggle haptics:
    - verify vibration stops/starts.
4. Toggle high-contrast:
    - verify key backgrounds change.
5. Close panel and type:
 - verify settings persist.

## 9. Cline Implementation Plan (Summary)
1. Create SettingsPanelView.kt.
2. Add “⚙ Settings” key to QwertyKeyboardView.
3. Wire toggles to KeyboardViewModel.
4. Update DataStore repository.
5. Update KeyboardState.
6. Apply high-contrast mode in key rendering.
7. Build + deploy + test.
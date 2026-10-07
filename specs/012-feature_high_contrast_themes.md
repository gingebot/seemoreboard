# Feature 012 — High‑Contrast Themes + Unified Preview

## 1. Overview

Feature 012 introduces a simple, accessible, and predictable system for selecting high‑contrast keyboard themes. Users can cycle through themes using a single button, view the active theme name, and see a unified preview that reflects both theme and zoom settings.

This feature enhances visual accessibility for low‑vision users while keeping the settings UI minimal and easy to navigate.

---

## 2. Goals

- Provide multiple high‑contrast themes optimized for low‑vision accessibility.
- Allow users to switch themes using a single, large, easy‑to‑tap button.
- Display the active theme name clearly.
- Use a single preview area shared by both theme and zoom settings.
- Apply themes only to keys (not the entire keyboard background).
- Ensure themes integrate cleanly with existing architecture and settings flow.
- Maintain full compatibility with magnifier and TalkBack.

---

## 3. Behaviour Requirements

### 3.1 Theme Cycling Button

- A single button labeled **“Change Theme”** cycles through all available themes.
- Cycling order:
  1. Black & White  
  2. White & Black  
  3. Yellow & Black  
  4. Blue & White  
  (then loops back)
- Each press updates:
  - `KeyboardSettings.theme`
  - `KeyboardState.theme`
- Button must be large and accessible.

### 3.2 Active Theme Label

- A text field displays the current theme name:
  ```
  Active Theme: White & Black
  ```
- Updated reactively when theme changes.
- TalkBack must announce the new theme name.

### 3.3 Unified Preview Area

A single preview composable shows:

- The current theme  
- The current zoom level  

Preview displays a row of 5 keys:
```
Q W E R T
```

Preview updates immediately when:

- Theme changes  
- Zoom slider moves  

### 3.4 Keys‑Only Theming

Themes affect:

- Key background color  
- Key label color  
- Key border color  

Themes do **not** affect:

- Keyboard background  
- Pan container  
- IME root background  

This ensures visual stability and avoids disorientation.

---

## 4. Theme Definitions

### 4.1 Black & White (Ultra High Contrast)
- Background: **Black**
- Label: **White**
- Border: **White (thick)**

### 4.2 White & Black (Inverse High Contrast)
- Background: **White**
- Label: **Black**
- Border: **Black (thick)**

### 4.3 Yellow & Black (Classic Accessibility Theme)
- Background: **Yellow**
- Label: **Black**
- Border: **Black**

### 4.4 Blue & White (Calm High Contrast)
- Background: **Dark Blue**
- Label: **White**
- Border: **White**

---

## 5. Architecture Changes

### 5.1 KeyboardTheme.kt

Add enum:
```kotlin
enum class KeyboardTheme {
    BLACK_WHITE,
    WHITE_BLACK,
    YELLOW_BLACK,
    BLUE_WHITE
}
```

### 5.2 KeyboardSettings

Add:
```kotlin
val theme: KeyboardTheme = KeyboardTheme.BLACK_WHITE
```

### 5.3 SettingsUseCases

Add:
```kotlin
fun cycleTheme(current: KeyboardTheme): KeyboardTheme {
    val all = KeyboardTheme.values()
    val nextIndex = (all.indexOf(current) + 1) % all.size
    return all[nextIndex]
}

fun setTheme(theme: KeyboardTheme)
```

### 5.4 DataStoreSettingsRepository

Persist:
- `theme`

### 5.5 KeyboardViewModel

Observe:
- `theme`

Expose:
```kotlin
fun cycleTheme()
fun setTheme(theme: KeyboardTheme)
```

### 5.6 KeyboardState

Add:
```kotlin
val theme: KeyboardTheme
```

### 5.7 KeyButton.kt

Apply theme colors to:
- background  
- border  
- label  

### 5.8 SettingsActivity

Add:
- “Change Theme” button  
- “Active Theme” label  
- Unified preview composable  

---

## 6. Acceptance Criteria

1. “Change Theme” button cycles through all themes.
2. Active theme label updates correctly.
3. Preview updates immediately when theme changes.
4. Preview updates immediately when zoom changes.
5. Themes apply only to keys.
6. Keyboard background remains neutral.
7. All themes meet high‑contrast accessibility requirements.
8. TalkBack announces theme changes.
9. Magnifier works correctly on all UI elements.

---

## 7. Testing Checklist

- Theme cycling order is correct.
- Theme persists across IME restarts.
- Preview reflects theme + zoom accurately.
- KeyButton renders correct colors for each theme.
- Borders are visible at all zoom levels.
- Active theme label matches actual theme.
- TalkBack announces theme name.
- No flicker or layout jumps when theme changes.
- SettingsActivity recomposes smoothly.

---

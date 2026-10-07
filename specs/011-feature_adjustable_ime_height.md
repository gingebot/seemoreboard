# Feature 011 — Adjustable IME Height (Slider)

## 1. Overview

Feature 011 introduces **control over keyboard height** via a **height slider** inside the full‑screen SettingsActivity.

This feature is designed for low‑vision accessibility, giving users ergonomic control over how large the keyboard appears on screen.

It builds directly on:

- Feature 008 (Stable Pan Architecture)  
- Feature 009 (Zoom Slider + Preview)  
- Feature 010 (Full‑Screen Settings Activity + Android Settings Integration)  

---

## 2. Goals

- Allow users to adjust keyboard height precisely via a slider.
- Persist height via DataStore.
- Ensure IME height updates immediately and smoothly.
- Ensure accessibility tools (magnifier, TalkBack, large text) work correctly.
- Ensure IME stability and predictable layout behaviour.

---

## 3. Behaviour Requirements

### 3.1 Height Slider (SettingsActivity)

- Range: **200dp → 600dp**
- Step: **10dp**
- Default: **350dp**
- Slider updates `KeyboardSettings.keyboardHeightDp`.
- Slider updates `KeyboardState.keyboardHeightDp` reactively via Flow.
- IME height updates immediately when slider moves.
- Preview key scales according to zoom but is unaffected by height.

### 3.2 IME Height Application

The persisted keyboard height is applied by the Compose root container hosting `KeyboardScreen`. The IME service provides the root view and leaves the surrounding window measurement to Android so portrait/landscape insets and extract mode remain correct.

Height changes must:

- trigger recomposition
- resize the Compose keyboard root
- not cause flicker or layout thrashing  

The keyboard content (`PanContainer` + `KeyboardLayout`) is centred within the fixed‑height container so that panning never exposes whitespace below the keyboard at any zoom level.

---

## 4. Architecture Changes

### 4.1 Settings Model

Update `KeyboardSettings`:

```
val keyboardHeightDp: Int = 350
```

### 4.2 Settings Use Cases

Add:

```
fun setKeyboardHeightDp(dp: Int)
```

### 4.3 DataStoreSettingsRepository

Persist:

- `keyboardHeightDp`

### 4.4 KeyboardViewModel

Observe:

- `keyboardHeightDp`

Update `KeyboardState` reactively.

Add:

```
fun setKeyboardHeightDp(dp: Int)
```

### 4.5 KeyboardState

Add:

```
val keyboardHeightDp: Int
```

### 4.6 KeyboardScreen

- Wrap keyboard content in a container whose height is `keyboardHeightDp.dp`.
- Keep the keyboard content centred within the container and leave surrounding IME window sizing, orientation/insets, and extract mode to Android.

### 4.7 SettingsActivity

Add:

- Height slider  

### 4.8 architecture.md Updates

Must update:

- UI Layer → IME height is adjustable  
- Settings Layer → new height setting  
- IME Integration → IME container height controlled by settings  
- Accessibility → slider semantics  
- Roadmap → future ergonomic features build on height control  

---

## 5. Acceptance Criteria

1. Height slider appears in SettingsActivity.
2. Slider adjusts IME height smoothly.
3. Height persists across sessions.
4. Magnifier works on both IME and SettingsActivity.
5. No layout thrashing or IME flicker.

---

## 6. Testing Checklist

- Slider adjusts height correctly.
- Height clamps to 200dp → 600dp.
- Height persists after closing keyboard.
- Height persists after device rotation.
- SettingsActivity updates IME height immediately.
- Keyboard content is centred and panning never exposes whitespace below the keyboard.

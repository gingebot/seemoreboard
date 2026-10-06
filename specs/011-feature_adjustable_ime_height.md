# Feature 011 — Adjustable IME Height (Slider + Drag Handle + Toggle)

## 1. Overview

Feature 011 introduces **full control over keyboard height**, allowing users to adjust the IME height using:

1. A **height slider** inside the full‑screen SettingsActivity  
2. A **drag handle** at the top of the keyboard for quick adjustment  
3. A **toggle** in SettingsActivity to enable/disable the drag handle  

This feature is designed for low‑vision accessibility, giving users ergonomic control over how large the keyboard appears on screen.

It builds directly on:

- Feature 008 (Stable Pan Architecture)  
- Feature 009 (Zoom Slider + Preview)  
- Feature 010 (Full‑Screen Settings Activity + Android Settings Integration)  

---

## 2. Goals

- Allow users to adjust keyboard height precisely via a slider.
- Allow users to adjust keyboard height quickly via a drag handle.
- Provide a toggle to enable/disable the drag handle.
- Persist height and drag handle state via DataStore.
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

### 3.2 Drag Handle (KeyboardScreen)

A large, high‑contrast bar at the top of the keyboard:

```
╔══════════════════════════════════╗
║   ⠿ Drag to resize keyboard     ║
╚══════════════════════════════════╝
```

- Visible only when `dragHandleEnabled == true`.
- Dragging up/down adjusts IME height in real time.
- Height is clamped to the same slider range (200dp → 600dp).
- Height persists via DataStore.
- Drag handle is a single large touch target for accessibility.

### 3.3 Drag Handle Toggle (SettingsActivity)

A toggle in SettingsActivity:

```
[✓] Enable drag handle for resizing
```

- When enabled → drag handle appears on keyboard.
- When disabled → drag handle is hidden.
- Toggle persists via DataStore.

### 3.4 IME Height Application

IME height is applied in:

- `LowVisionKeyboardService.onCreateInputView()`
- The Compose root container hosting `KeyboardScreen`

Height changes must:

- trigger recomposition  
- update IME container height  
- not cause flicker or layout thrashing  

### 3.5 Accessibility

- Drag handle must be reachable by TalkBack.
- Drag handle must have clear semantics (“Resize keyboard height”).
- Slider must be accessible.
- Magnifier must work on both SettingsActivity and IME.

---

## 4. Architecture Changes

### 4.1 Settings Model

Update `KeyboardSettings`:

```
val keyboardHeightDp: Int = 350
val dragHandleEnabled: Boolean = true
```

### 4.2 Settings Use Cases

Add:

```
fun setKeyboardHeightDp(dp: Int)
fun setDragHandleEnabled(enabled: Boolean)
```

### 4.3 DataStoreSettingsRepository

Persist:

- `keyboardHeightDp`
- `dragHandleEnabled`

### 4.4 KeyboardViewModel

Observe:

- `keyboardHeightDp`
- `dragHandleEnabled`

Update `KeyboardState` reactively.

Add:

```
fun setKeyboardHeightDp(dp: Int)
fun setDragHandleEnabled(enabled: Boolean)
```

### 4.5 KeyboardState

Add:

```
val keyboardHeightDp: Int
val dragHandleEnabled: Boolean
```

### 4.6 KeyboardScreen

- Wrap keyboard content in a container whose height is `keyboardHeightDp.dp`.
- Show drag handle only when `state.dragHandleEnabled == true`.
- Implement drag gesture on drag handle:
  - update height in real time
  - clamp to safe range
  - call `viewModel.setKeyboardHeightDp(newHeight)`

### 4.7 SettingsActivity

Add:

- Height slider  
- Drag handle toggle  
- Height preview (optional)  

### 4.8 architecture.md Updates

Must update:

- UI Layer → IME height is adjustable  
- Settings Layer → new height + drag handle settings  
- IME Integration → IME container height controlled by settings  
- Accessibility → drag handle semantics  
- Roadmap → future ergonomic features build on height control  

---

## 5. Acceptance Criteria

1. Height slider appears in SettingsActivity.
2. Drag handle appears on keyboard when enabled.
3. Drag handle is hidden when disabled.
4. Dragging the handle adjusts IME height smoothly.
5. Slider adjusts IME height smoothly.
6. Height persists across sessions.
7. Drag handle toggle persists across sessions.
8. Magnifier works on both IME and SettingsActivity.
9. TalkBack reads drag handle semantics correctly.
10. No layout thrashing or IME flicker.

---

## 6. Testing Checklist

- Drag handle visible when enabled.
- Drag handle hidden when disabled.
- Drag handle adjusts height correctly.
- Slider adjusts height correctly.
- Height clamps to 200dp → 600dp.
- Height persists after closing keyboard.
- Height persists after device rotation.
- SettingsActivity updates IME height immediately.
- TalkBack announces drag handle correctly.
- Magnifier enlarges drag handle and slider correctly.

---

# Feature Spec: Zoom & Pan MVP (Feature #3)

## 1. Overview

This feature adds **basic zoom and pan** to the existing static QWERTY keyboard, using
the architecture’s zoom/pan flow and `ZoomPanHandlers`. It keeps the current
View-based UI (Compose pending lifecycle workaround) but introduces:

- scalable keyboard surface (zoom)
- pannable layout (horizontal/vertical pan)
- updated hitboxes for keys under zoom/pan

No settings UI, no advanced gestures, no inertia — just functional pinch/drag
to zoom and move the keyboard.

---

## 2. Goals

- Allow low‑vision users to enlarge the keyboard and move it around.
- Exercise the zoom/pan data flow:
  - `ZoomPanHandlers` → `KeyboardViewModel` → `KeyboardState` → layout.
- Prepare the layout engine for future Compose migration.

---

## 3. User Story

As a user with low vision,  
I want to zoom and pan the keyboard,  
so I can make keys larger and move them into my comfortable viewing area.

---

## 4. Functional Requirements

### 4.1 Zoom Behavior

- Support pinch gesture to adjust `zoomLevel`.
- Define:
  - `minZoom` (e.g. `1.0f`)
  - `maxZoom` (e.g. `2.5f`)
- Zoom must:
  - increase visual size of keys
  - increase touch target size
- Zoom is applied uniformly to the entire keyboard surface.

### 4.2 Pan Behavior

- Support drag gesture to adjust:
  - `offsetX`
  - `offsetY`
- Pan must:
  - move the keyboard surface within the IME view
  - respect reasonable bounds (no infinite scrolling)

### 4.3 Hitbox & Input Behavior

- Key hit detection must respect zoom/pan:
  - touch coordinates transformed by `zoomLevel`, `offsetX`, `offsetY`
- When a key is tapped:
  - `KeyboardViewModel.onKeyPress(key)` is still used
  - `KeyAction.TypeCharacter(key)` → `InputConnectionBridge.typeCharacter(key)`

### 4.4 Haptics & Logging

- Haptic feedback remains unchanged.
- Logging still reports:
  - `[SeeMoreBoard] Key pressed: X`

---

## 5. Non‑Functional Requirements

- Use `ZoomPanHandlers` in `ui/gestures/`.
- Keep IME service free of gesture logic.
- Keep zoom/pan state in `KeyboardState`.
- Use `KeyboardViewModel` as the single source of truth for zoom/pan.
- No Compose UI yet (View-based rendering only).

---

## 6. Architecture Impact

### 6.1 Updated Files

**`ui/gestures/ZoomPanHandlers.kt`**
- Implement:
  - pinch detection for zoom
  - drag detection for pan
- Expose callbacks:
  - `onZoomChanged(zoom: Float)`
  - `onPanChanged(offsetX: Float, offsetY: Float)`

**`ui/keyboard/KeyboardState.kt`**
- Ensure fields exist:
  - `zoomLevel: Float`
  - `offsetX: Float`
  - `offsetY: Float`
- Default values:
  - `zoomLevel = 1.0f`
  - `offsetX = 0f`
  - `offsetY = 0f`

**`ui/keyboard/KeyboardViewModel.kt`**
- Add:
  - `updateZoom(zoom: Float)`
  - `updatePan(offsetX: Float, offsetY: Float)`
- Update `KeyboardState` via copy.

**`domain/layout/LayoutCalculator.kt`**
- Update hitbox calculations to account for:
  - `zoomLevel`
  - `offsetX`
  - `offsetY`

**`ui/components/QwertyKeyboardView.kt`**
- Integrate `ZoomPanHandlers`:
  - attach gesture listeners
  - apply zoom/pan to drawing and hit detection

### 6.2 No New Files

- All changes are additive within existing modules.

---

## 7. Acceptance Criteria

### 7.1 Zoom

- Pinch gesture increases/decreases key size.
- Keys remain functional at all zoom levels.
- Touch targets grow with zoom.

### 7.2 Pan

- Drag gesture moves the keyboard surface.
- Keys remain tappable after panning.
- Keyboard does not disappear completely off-screen.

### 7.3 Data Flow

- `ZoomPanHandlers` → `KeyboardViewModel` → `KeyboardState` → `LayoutCalculator`.
- No gesture logic in IME service.

### 7.4 Stability

- No crashes during zoom/pan.
- Typing still works as before.

---

## 8. Testing Instructions

### 8.1 Build & Install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 8.2 Enable Keyboard

On the Samsung device:

1. Open **Settings**
2. Go to **General Management**
3. Tap **Keyboard List and Default**
4. Enable **SeeMoreBoard**
5. Set **SeeMoreBoard** as the default input method


## 8.2 Enable Keyboard
Settings → General Management → Keyboard List and Default → Enable SeeMoreBoard → Set as default.

## 8.3 Test Zoom & Pan
1. Open a text field.
2. Perform pinch gesture:
    - verify keys grow/shrink.
3. Perform drag gesture:
    - verify keyboard moves.
4. Tap various keys at different zoom/pan states:
    - verify correct characters are inserted.
    - verify haptics and logging still work.

## 9. Cline Implementation Plan (Summary)
1. Implement zoom/pan gesture handling in ZoomPanHandlers.kt.
2. Wire gesture callbacks to KeyboardViewModel (updateZoom, updatePan).
3. Ensure KeyboardState holds zoom/pan values.
4. Update LayoutCalculator to use zoom/pan for hitboxes.
5. Integrate ZoomPanHandlers into QwertyKeyboardView.
6. Build, deploy, and test on device.
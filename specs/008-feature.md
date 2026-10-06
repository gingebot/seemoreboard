# Feature 008 — Stable Pan Architecture (2× Zoom, No Pinch)

## 1. Overview

This feature replaces the existing zoom/pan gesture system with a dedicated pan container that provides:

- smooth, predictable panning  
- natural inertial (fling) movement  
- strict clamping to content bounds  
- tap‑to‑cancel fling behaviour  
- zoom applied directly to the keyboard surface  
- zoom locked at 2× for this transition phase  
- no pinch‑zoom gestures  

This creates a stable foundation for future features:
- Feature 009 — Zoom Slider + Preview  
- Feature 010 — Adjustable IME Height  

---

## 2. Goals

- Remove pinch‑zoom entirely.  
- Lock zoom at 2×.  
- Introduce a new PanContainer composable.  
- Apply zoom and pan via graphicsLayer.  
- Remove all zoom/pan math from KeyboardScreen.  
- Keep hitboxes correct via existing LayoutCalculator.  
- Keep IME container behaviour unchanged.  
- Ensure panning is smooth, clamped, and inertial.  

---

## 3. Behaviour Requirements

### 3.1 Pan Behaviour

- Dragging moves the keyboard surface.  
- Fast drag/swipe produces a short inertial glide.  
- Glide slows naturally and stops when velocity is low.  
- Panning is clamped so the keyboard cannot be dragged off‑screen.  
- Tap during glide immediately stops movement and does not trigger key presses.  

### 3.2 Zoom Behaviour (Transition Phase)

- Zoom is fixed at 2×.  
- No pinch‑zoom.  
- No zoom changes from UI.  
- KeyboardState.zoomLevel is set to 2f on start.  

### 3.3 Hitboxes & Layout

- LayoutCalculator continues to use zoomLevel, offsetX, offsetY.  
- Hitboxes remain correct under pan.  

---

## 4. Architecture Changes

### 4.1 New File: ui/gestures/PanContainer.kt

Create a new composable that wraps the keyboard surface and handles all pan behaviour.

### 4.1.1 PanContainer API

@Composable  
fun PanContainer(  
    zoom: Float,  
    offsetX: Float,  
    offsetY: Float,  
    onPanChange: (dx: Float, dy: Float) -> Unit,  
    modifier: Modifier = Modifier,  
    content: @Composable () -> Unit  
)

### 4.1.2 Internal Structure

- Root: BoxWithConstraints  
  - captures viewport size  

- Inner content box:  
  - wrapContentSize(unbounded = true)  
  - onSizeChanged { contentSize = it }  
  - graphicsLayer {  
    - scaleX = zoom  
    - scaleY = zoom  
    - translationX = currentOffset.x  
    - translationY = currentOffset.y  
    }  

- Local pan state:  
  currentOffset = remember { mutableStateOf(Offset.Zero) }

- Clamping:  
  maxX = (contentSize.width - viewportSize.width).coerceAtLeast(0f)  
  maxY = (contentSize.height - viewportSize.height).coerceAtLeast(0f)  

  clampOffset(current, delta) = Offset(  
    x = (current.x + delta.x).coerceIn(-maxX, 0f),  
    y = (current.y + delta.y).coerceIn(-maxY, 0f)  
  )

- Drag:  
  - Use rememberDraggable2DState  
  - Cancel any active fling jobs  
  - Clamp offset  
  - Update currentOffset  
  - Call onPanChange(delta.x, delta.y)  

- Fling:  
  - Use AnimationState + animateDecay(exponentialDecay())  
  - Separate jobs for X and Y  
  - On drag stop:  
    - start decay animation if velocity exceeds minimum threshold  
    - clamp each frame  
    - update currentOffset  
    - call onPanChange(dx, dy)  
    - stop when clamped or velocity < threshold  

- Tap‑to‑cancel:  
  - When flinging:  
    - overlay a full‑size Box with pointerInput  
    - intercept any press  
    - cancel fling jobs  
    - consume pointer events  

---

## 4.2 KeyboardScreen.kt Integration

Replace the entire ComposeZoomPan block with PanContainer.

### 4.2.1 Remove:

- ComposeZoomPan(...)  
- onZoomChange  
- onPanChange  
- all zoom/pan clamping math  
- all viewport‑based zoom logic  

### 4.2.2 Add:

PanContainer(  
    zoom = state.zoomLevel,  
    offsetX = state.offsetX,  
    offsetY = state.offsetY,  
    onPanChange = { dx, dy ->  
        viewModel.updatePan(dx, dy)  
    }  
) {  
    KeyboardLayout(  
        state = state,  
        onKeyPress = { key ->  
            if (state.hapticEnabled) {  
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)  
            }  
            viewModel.onKeyPress(key)  
        },  
        onToggleShift = { viewModel.toggleShift() },  
        onToggleLayer = { viewModel.toggleLayer() },  
        onOpenSettings = { viewModel.openSettings() }  
    )  
}

SettingsPanel remains unchanged.

---

## 4.3 KeyboardState Updates

KeyboardState must contain:

zoomLevel: Float = 2f  
offsetX: Float = 0f  
offsetY: Float = 0f  

### 4.3.1 Zoom

- Default zoomLevel = 2f  
- No gesture changes to zoomLevel in this feature  

### 4.3.2 Pan

updatePan(dx, dy) =  
    offsetX += dx  
    offsetY += dy  

Clamping is handled inside PanContainer.

---

## 5. IME & Layout Engine Impact

- IME service: no changes  
- InputConnectionBridge: no changes  
- LayoutCalculator: continues using zoomLevel/offsetX/offsetY  
- Hitboxes remain correct  

---

## 6. Acceptance Criteria

- Dragging is smooth and responsive.  
- Fast swipes produce natural inertial movement.  
- Movement clamps correctly at edges.  
- Tap during glide cancels movement without triggering keys.  
- Zoom remains fixed at 2×.  
- IME container height remains stable.  
- Hitboxes remain accurate under pan.  

---

## 7. Testing Checklist

- Slow drag → stops immediately.  
- Fast drag → glides then slows.  
- Tap during glide → stops instantly.  
- Attempt to drag beyond edges → clamped.  
- Type while panned → correct key hitboxes.  
- Open/close keyboard → stable behaviour.  
- Rotate device (if supported) → stable behaviour.  

---

## 8. Cline Implementation Prompt

REFERENCE DOCUMENTS:  
- architecture/architecture.md  
- KeyboardScreen.kt  
- KeyboardViewModel.kt  
- KeyboardState.kt  
- LayoutCalculator.kt  

TASK:  
Implement Feature 008 — Stable Pan Architecture (2× zoom, no pinch):  
- create ui/gestures/PanContainer.kt  
- apply zoom and pan via graphicsLayer  
- implement drag + inertial fling + clamping + tap-to-cancel  
- remove ComposeZoomPan and all zoom/pan math from KeyboardScreen  
- lock zoomLevel at 2f  
- keep offsetX/offsetY updated via ViewModel.updatePan()  

PLAN:  
1. Add PanContainer.kt with:  
   - BoxWithConstraints root  
   - wrapContentSize(unbounded = true) inner box  
   - graphicsLayer { scaleX = zoom; scaleY = zoom; translationX/Y = currentOffset }  
   - draggable2D for drag  
   - clamp offsets based on contentSize - viewportSize  
   - animateDecay(exponentialDecay()) for fling  
   - tap-to-cancel overlay during fling  
   - call onPanChange(dx, dy) whenever currentOffset changes  
2. In KeyboardScreen.kt:  
   - remove ComposeZoomPan and its onZoomChange/onPanChange logic  
   - wrap KeyboardLayout in PanContainer  
3. In KeyboardState:  
   - ensure zoomLevel defaults to 2f  
   - keep offsetX/offsetY  
4. In KeyboardViewModel:  
   - implement updatePan(dx, dy)  
5. Verify LayoutCalculator still uses zoomLevel/offsetX/offsetY correctly  
6. Test drag, fling, clamping, tap-to-cancel, IME stability, and hitboxes  

RULES:  
- No new modules  
- Place PanContainer in ui/gestures  
- Do not modify IME service or InputConnectionBridge  
- Keep changes minimal and additive  

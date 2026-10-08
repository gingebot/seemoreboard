# Feature Spec: Shift Key + Symbols Layer (Feature #5)

## 1. Overview

This feature adds:
- A functional **Shift key** (toggle + hold behavior)
- A **Symbols layer** (123 / punctuation layout)
- A **Layer switch key** to toggle between:
  - Alphabet layer (QWERTY)
  - Symbols layer

This is the first multi‑layer keyboard feature and builds directly on the existing
QwertyKeyboardView, KeyboardViewModel, and LayoutCalculator.

Zoom/pan, haptics, high‑contrast, and settings must continue to work unchanged.

---

## 2. Goals

- Provide uppercase typing via Shift.
- Provide access to numbers and punctuation via a Symbols layer.
- Maintain architecture separation:
  - UI → ViewModel → Layout → InputConnectionBridge.
- Keep all logic additive and isolated.

---

## 3. User Story

As a user with low vision,  
I want to type uppercase letters and symbols,  
so I can write normal text without switching keyboards.

---

## 4. Functional Requirements

### 4.1 Shift Key

Shift has two modes:

1. **Shift ON (latched)**  
   - Next character typed is uppercase.
   - After typing one character, Shift automatically turns OFF.

2. **Shift LOCK (double‑tap)**  
   - All characters remain uppercase until Shift is tapped again.

3. **Shift OFF**  
   - Normal lowercase typing.

Shift visual states:
- OFF: normal background
- ON: highlighted background
- LOCK: stronger highlight (e.g., thicker border)

### 4.2 Symbols Layer

Symbols layer replaces QWERTY with:

1 2 3 4 5 6 7 8 9 0
/ : ; ( ) £ & @ "
! ? , . ' + = _ *


(You may adjust symbols slightly for layout fit.)

### 4.3 Layer Switch Key

A dedicated key toggles between:

- **ABC** → Alphabet layer  
- **123** → Symbols layer  

Visual state must reflect the active layer.

### 4.4 Input Behavior

Alphabet layer:
- Lowercase by default.
- Uppercase when Shift ON or LOCK.

Symbols layer:
- Characters inserted exactly as displayed.
- Shift has no effect.

### 4.5 Haptics & Logging

- Haptics fire normally (unless disabled).
- Logging shows:

[SeeMoreBoard] Key pressed: X


---

## 5. Non‑Functional Requirements

- View-based UI only (Compose pending lifecycle workaround).
- No IME service changes.
- No new modules.
- All logic must be additive.

---

## 6. Architecture Impact

### 6.1 Updated Files

**`ui/components/QwertyKeyboardView.kt`**
- Add Shift key.
- Add ABC/123 layer switch key.
- Render alphabet or symbols layout based on `KeyboardState.activeLayer`.
- Render Shift visual states.

**`ui/keyboard/KeyboardState.kt`**
Add:
- `shiftState: ShiftState` (enum: OFF, ON, LOCK)
- `activeLayer: KeyboardLayer` (enum: ALPHA, SYMBOLS)

**`ui/keyboard/KeyboardViewModel.kt`**
Add:
- `toggleShift()`
- `toggleLayer()`
- Logic to:
- uppercase letters when shiftState != OFF
- reset shiftState from ON → OFF after one character
- ignore shiftState in symbols layer

**`domain/layout/LayoutCalculator.kt`**
- Provide hitboxes for both layers.
- Provide key definitions for both layers.

**`domain/input/KeyAction.kt`**
- No changes required (characters already supported).

### 6.2 New Files

None.

---

## 7. Acceptance Criteria

### 7.1 Shift
- Single tap → Shift ON (one uppercase character).
- Double tap → Shift LOCK (persistent uppercase).
- Tap again → Shift OFF.
- Visual state updates correctly.

### 7.2 Symbols Layer
- ABC ↔ 123 key switches layers.
- Symbols layer keys insert correct characters.
- Shift has no effect in symbols layer.

### 7.3 Stability
- Zoom/pan still work.
- Settings still work.
- No crashes.

---

## 8. Testing Instructions

### 8.1 Build & Install

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### 8.2 Enable Keyboard
Settings → General Management → Keyboard List and Default → Enable SeeMoreBoard → Set as default.

### 8.3 Test Shift

1. Tap Shift → type “a” → result: “A”
2. Shift turns OFF automatically.
3. Double tap Shift → type “a” → result: “A”
4. Shift stays LOCKED until tapped again.

### 8.4 Test Symbols Layer
1. Tap 123 → symbols appear.
2. Tap “£” → “£” appears.
3. Tap ABC → alphabet returns.

---

## 9. Cline Implementation Plan (Summary)
1. Add Shift + ABC/123 keys to QwertyKeyboardView.
2. Add shiftState + activeLayer to KeyboardState.
3. Add toggleShift() + toggleLayer() to ViewModel.
4. Update LayoutCalculator for two layers.
5. Update rendering logic for uppercase + symbols.
6. Build + deploy + test.
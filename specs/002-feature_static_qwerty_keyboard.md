# Feature Spec: Static Multi‑Key QWERTY Keyboard (MVP #2)

## 1. Overview

This feature expands the keyboard from a single key (“A”) to a full static QWERTY layout.
It does **not** include zoom, pan, gestures, or Compose UI.  
Rendering is done using the temporary View‑based fallback (`SingleKeyView.kt` pattern),
as documented in the architecture’s Compose/IME lifecycle constraint.

This feature exercises:
- UI layer (View-based components)
- ViewModel → KeyAction → InputConnectionBridge pipeline
- Layout engine (basic static layout)
- High‑contrast accessibility requirements
- Haptic feedback
- Logging

---

## 2. Goals

- Provide a functional multi‑key keyboard for real typing.
- Validate the layout engine and ViewModel logic.
- Maintain architecture separation (UI layer, domain layer, IME layer).
- Prepare for future zoom/pan and Compose migration.

---

## 3. User Story

As a user with low vision,  
I want a simple, high‑contrast QWERTY keyboard,  
so I can type basic text without zoom/pan features.

---

## 4. Functional Requirements

### 4.1 Layout
- Render a static QWERTY layout:
Q W E R T Y U I O P
A S D F G H J K L
Z X C V B N M

- Keys must be:
- high‑contrast (black background, white text)
- large touch targets (≥48dp)
- evenly spaced
- centered within rows

### 4.2 Input Behavior
When a key is tapped:
1. `KeyboardViewModel.onKeyPress(key)`
2. Emit `KeyAction.TypeCharacter(key)`
3. `InputConnectionBridge.typeCharacter(key)`
4. `InputConnection.commitText(key, 1)`
5. Trigger haptic feedback
6. Log the event

### 4.3 IME Behavior
- Keyboard appears in all editor types.
- No crashes during lifecycle events.

---

## 5. Non‑Functional Requirements

- Must use View-based UI (temporary fallback).
- Must NOT use Compose until lifecycle workaround is implemented.
- Must keep UI logic out of IME service.
- Must follow architecture module boundaries.
- Must use Logging utility.
- Must use InputConnectionBridge abstraction.

---

## 6. Architecture Impact

### 6.1 New Files

**`ui/components/QwertyKeyboardView.kt`**
- A ViewGroup (LinearLayout or custom) that renders rows of keys.
- Uses `KeyButtonView` for individual keys.

**`ui/components/KeyButtonView.kt`**
- A reusable high‑contrast button view.
- Handles:
- background color
- text label
- click callback
- minimum touch target size

### 6.2 Updated Files

**`ui/keyboard/KeyboardScreen.kt`**
- Temporarily unused (Compose pending activation).

**`ui/keyboard/KeyboardViewModel.kt`**
- Add `onKeyPress(key: String)` logic.
- No business logic in UI layer.

**`domain/input/KeyAction.kt`**
- Already contains `TypeCharacter`; no change needed.

**`ime/integration/InputConnectionBridge.kt`**
- Ensure `typeCharacter(key)` works for all characters.

**`ime/service/SeeMoreBoardService.kt`**
- Replace `SingleKeyView` with `QwertyKeyboardView`.

---

## 7. Acceptance Criteria

### 7.1 Functional
- All QWERTY keys render correctly.
- Tapping any key inserts the correct character.
- Haptic feedback fires.
- Logcat shows:
[SeeMoreBoard] Key pressed: X


### 7.2 Visual
- High‑contrast black/white theme.
- Keys ≥48dp.
- Rows centered and evenly spaced.

### 7.3 IME Lifecycle
- Keyboard loads via `SeeMoreBoardService`.
- Keyboard appears in all text fields.
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

### 8.3 Test Input
1. Open any text field.
2. Tap keys across all rows.
3. Verify:
    - Characters appear correctly.
    - Haptics fire.
    - Logcat shows correct key events.

### 9. Cline Implementation Plan
1. Create KeyButtonView.kt (View-based button).
2. Create QwertyKeyboardView.kt (ViewGroup with rows).
3. Update KeyboardViewModel.onKeyPress(key).
4. Ensure InputConnectionBridge.typeCharacter(key) works.
5. Update SeeMoreBoardService to return QwertyKeyboardView.
6. Build + deploy.
7. Test on device.
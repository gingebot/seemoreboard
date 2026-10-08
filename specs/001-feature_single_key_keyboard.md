# Feature Spec: Single‑Key Keyboard (MVP #1)

## 1. Overview

This feature introduces the simplest possible functional keyboard: a single large key
that inserts the character **"A"** into the active text field. It validates the entire IME
pipeline end‑to‑end:

- IME service creation
- Compose UI rendering inside an IME
- ViewModel state updates
- InputConnectionBridge integration
- Haptic feedback
- Logging
- Deployment + testing on a real device

This is the “Hello World” of the Low‑Vision Keyboard project.

---

## 2. Goals

- Provide a minimal, stable starting point for Android IME development.
- Allow the developer to practice build → install → enable → test workflows.
- Validate that the architecture modules integrate correctly.
- Ensure Cline + DeepSeek can safely implement features within the architecture.

---

## 3. User Story

As a developer learning Android IME workflows,  
I want a keyboard with a single key,  
so I can verify that IME creation, rendering, input, and deployment all work correctly.

---

## 4. Functional Requirements

### 4.1 UI Behavior
- Display a single large key labeled **"A"**.
- The key must fill most of the screen (full width, full height minus IME insets).
- The key must be high‑contrast:
  - black background
  - white text
  - centered label

### 4.2 Input Behavior
When the user taps the key:

1. Insert `"A"` into the active text field using:
   - `InputConnection.commitText("A", 1)`
2. Trigger haptic feedback:
   - `performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)`
3. Log the event using `utils/Logging.kt`.

### 4.3 IME Behavior
- The keyboard must appear when selected in system settings.
- The keyboard must remain visible for all editor types.
- No settings, gestures, or accessibility features are required for this MVP.

---

## 5. Non‑Functional Requirements

- Must use Jetpack Compose for UI.
- Must use the existing architecture modules:
  - `ime/service`
  - `ime/integration`
  - `ui/keyboard`
  - `ui/components`
  - `domain/input`
- Must not introduce new modules.
- Must not introduce zoom, pan, or layout engine logic yet.
- Must build and install via Gradle wrapper (`./gradlew assembleDebug`).

---

## 6. Architecture Impact

### 6.1 New Files

**`ui/components/SingleKeyButton.kt`**
- A simple composable rendering the large “A” key.

### 6.2 Updated Files

**`ui/keyboard/KeyboardScreen.kt`**
- Replace the full keyboard layout with a single key.

**`ui/keyboard/KeyboardViewModel.kt`**
- Add a simple counter or state update (optional).
- Add `onKeyPress("A")` handler.

**`domain/input/KeyAction.kt`**
- Add `KeyAction.TypeCharacter("A")`.

**`ime/integration/InputConnectionBridge.kt`**
- Ensure `typeCharacter("A")` is implemented.

**`utils/Logging.kt`**
- Add a log entry for key presses.

---

## 7. Acceptance Criteria

### 7.1 Functional
- The keyboard displays one large “A” key.
- Tapping the key inserts “A” into any text field.
- Haptic feedback fires on tap.
- Logcat shows a message indicating the key was pressed.

### 7.2 Visual
- The key is centered.
- The key is high‑contrast.
- The key meets minimum touch target requirements (48dp).

### 7.3 IME Lifecycle
- The keyboard loads via `SeeMoreBoardService`.
- The keyboard appears in all text fields.
- No crashes occur during IME lifecycle events.

---

## 8. Testing Instructions

### 8.1 Build & Install
```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

## 8.2 Enable Keyboard

On the Samsung device:

1. Open **Settings**
2. Go to **General Management**
3. Tap **Keyboard List and Default**
4. Enable **SeeMoreBoard**
5. Set **SeeMoreBoard** as the default input method

---

## 8.3 Test Input

1. Open any app with a text field (Messages, Notes, Chrome).
2. Tap inside the text field.
3. The single‑key keyboard should appear.
4. Tap the **“A”** key.

### Verify the following:

- The character **A** appears in the text field.
- Haptic feedback fires.
- Logcat shows the event:

[SeeMoreBoard] Key pressed: A


---

## 9. Cline Implementation Plan

1. Create `SingleKeyButton.kt` composable.
2. Update `KeyboardScreen.kt` to render only this button.
3. Add `onKeyPress("A")` to `KeyboardViewModel`.
4. Add `KeyAction.TypeCharacter("A")`.
5. Update `InputConnectionBridge` to commit `"A"`.
6. Add logging for key presses.
7. Build + deploy to device.
8. Test IME lifecycle and input behavior.

---

## 10. Future Extensions

Once this MVP is validated, the next features will be:

- Multi‑key layout (QWERTY)
- Layout engine
- Zoom + pan
- Settings
- Accessibility
- Audio feedback
- Predictive text

This MVP establishes the foundation for all future work.

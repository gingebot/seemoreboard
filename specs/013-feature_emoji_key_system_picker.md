# Feature 013 — Embedded Jetpack Emoji Picker

## 1. Overview

Feature 013 connects the existing emoji key to Jetpack's official `EmojiPickerView`, embedded directly in the IME. This provides a complete emoji picker without maintaining emoji data or implementing a custom emoji grid.

The Jetpack picker provides emoji categories, navigation, variants, recent emojis, EmojiCompat support, and accessibility behavior across supported Android devices.

## 2. Goals

- Connect the existing emoji key to an embedded emoji picker.
- Insert selected emoji through the active `InputConnection`.
- Keep the emoji key accessible through TalkBack and magnification.
- Avoid maintaining custom emoji data, categories, variants, or recents.
- Preserve IME stability and existing keyboard height/zoom/pan behavior.

## 3. Behaviour Requirements

### 3.1 Emoji Key

The existing 🙂 key remains in Row 4 and remains part of the normal zoomable/pannable keyboard layout. Its accessibility description is **“Open emoji picker”**.

### 3.2 Opening the Picker

When the emoji key is pressed:

1. The IME switches its Compose content from the keyboard to an embedded `EmojiPickerView`.
2. The picker occupies the current IME root height; it is not affected by keyboard zoom or pan.
3. The keyboard's zoom and pan state are retained while the picker is open.

### 3.3 Selecting an Emoji

When the user selects an `EmojiViewItem`:

1. Read `EmojiViewItem.emoji`.
2. Commit it to the active editor through `InputConnectionBridge.commitText(emoji, 1)`.
3. Close the picker and restore the keyboard.

### 3.4 No Custom Emoji Data

This feature does not implement or maintain a custom emoji grid, category data, search, skin-tone/variant data, or recent-emoji storage. These are supplied by `androidx.emoji2:emoji2-emojipicker:1.6.0`.

## 4. Architecture Changes

### 4.1 Dependency

```kotlin
implementation("androidx.emoji2:emoji2-emojipicker:1.6.0")
```

`EmojiPickerView` is an Android `FrameLayout` hosted in Compose with `AndroidView`.

### 4.2 KeyboardState

Add `val emojiPickerVisible: Boolean = false`.

### 4.3 KeyAction

Retain `object EmojiKeyPressed : KeyAction()` and dispatch the behavior through `KeyboardViewModel.handleEmojiKey()`.

### 4.4 KeyboardViewModel

Add `handleEmojiKey()`, `closeEmojiPicker()`, and `commitEmoji(emoji: String)`. The first shows the picker, the second restores the keyboard, and the third inserts the selected emoji.

### 4.5 InputConnectionBridge

Add `fun commitText(text: String)`. The bridge remains the only layer that writes selected emoji to the active editor.

The previous `KEYCODE_SYM` system-picker attempt is not used because Android exposes no reliable public API for a third-party IME to open another keyboard's picker.

### 4.6 KeyboardScreen

- Render `EmojiPickerView` with `AndroidView` when `emojiPickerVisible` is true.
- Configure `setOnEmojiPickedListener`.
- Commit selection and close the picker.
- Keep the picker inside the existing IME root container.
- Do not apply keyboard zoom/pan transforms to the picker.

### 4.7 KeyButton and Accessibility

Keep the emoji key in the existing `KeyButton` layout, set its content description to **“Open emoji picker”**, and retain existing zoom/pan behavior.

### 4.8 architecture.md

Document embedded Jetpack picker integration, `EmojiPickerView`/`AndroidView` hosting, `InputConnectionBridge.commitText()` insertion, accessibility semantics, and the absence of custom emoji data/UI.

## 5. Acceptance Criteria

1. Tapping 🙂 opens the embedded Jetpack emoji picker.
2. Picker displays Jetpack-supplied navigation, variants, and recents.
3. Selecting an emoji inserts it into the active text field.
4. Picker closes after selection and the keyboard returns.
5. Emoji key remains zoomable and pannable.
6. Keyboard zoom/pan state is retained after picker close.
7. TalkBack announces “Open emoji picker”.
8. Picker selection remains accessible.
9. No custom emoji data or grid is maintained by this project.
10. No IME flicker or crash occurs when opening/closing the picker.

## 6. Testing Checklist

- Open picker at 1x, and while zoomed and panned.
- Verify picker fills the current IME height.
- Select emoji in WhatsApp, Messages, Chrome, and Notes.
- Verify keyboard return and zoom/pan retention after selection.
- Verify TalkBack announces the emoji key.
- Verify Jetpack-provided variants, navigation, and recents.
- Rotate and switch IMEs while the picker is closed.

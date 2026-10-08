# Feature 013 — Embedded Custom Low-Vision Emoji Picker

## 1. Overview

Feature 013 connects the existing emoji key to a custom, low-vision emoji picker embedded directly in the IME. It uses EmojiCompat for rendering, app-owned category data, recent emoji, and long-press variants.

The custom picker provides emoji categories, navigation, variants, recent emojis, EmojiCompat support, and accessibility behavior while allowing low-vision sizing.

## 2. Goals

- Connect the existing emoji key to an embedded emoji picker.
- Insert selected emoji through the active `InputConnection`.
- Keep the emoji key accessible through TalkBack and magnification.
- Keep emoji data, categories, variants, and recents app-owned so the visual design remains configurable.
- Preserve IME stability and existing keyboard height/zoom/pan behavior.

## 3. Behaviour Requirements

### 3.1 Emoji Key

The existing 🙂 key remains in Row 4 and remains part of the normal zoomable/pannable keyboard layout. Its accessibility description is **“Open emoji picker”**.

### 3.2 Opening the Picker

When the emoji key is pressed:

1. The IME switches its Compose content from the keyboard to the custom full-height emoji picker.
2. The picker occupies the current IME root height; it is not affected by keyboard zoom or pan.
3. The keyboard's zoom and pan state are retained while the picker is open.

### 3.3 Selecting an Emoji

When the user selects an emoji cell or variant:

1. Read the selected emoji string.
2. Commit it to the active editor through `InputConnectionBridge.commitText(emoji, 1)`.
3. Close the picker and restore the keyboard.

### 3.4 App-Owned Emoji Data

The picker maintains app-owned category data and a custom emoji grid so glyph size and touch targets can meet low-vision requirements. EmojiCompat supplies bundled font rendering. Recent emoji are persisted in DataStore, and long-press variant selection is supported.

## 4. Architecture Changes

### 4.1 Dependency

```kotlin
implementation("androidx.emoji2:emoji2-bundled:1.6.0")
```

The custom picker is hosted in Compose and uses native `TextView` cells to preserve EmojiCompat spans and reliable glyph rendering.

### 4.2 KeyboardState

Add `val emojiPickerVisible: Boolean = false`, plus the persisted emoji size and recent-emoji state used by the custom picker.

### 4.3 KeyAction

Retain `object EmojiKeyPressed : KeyAction()` and dispatch the behavior through `KeyboardViewModel.handleEmojiKey()`.

### 4.4 KeyboardViewModel

Add `handleEmojiKey()`, `closeEmojiPicker()`, and `commitEmoji(emoji: String)`. The first shows the picker, the second restores the keyboard, and the third inserts the selected emoji.

### 4.5 InputConnectionBridge

Add `fun commitText(text: String)`. The bridge remains the only layer that writes selected emoji to the active editor.

The previous `KEYCODE_SYM` system-picker attempt is not used because Android exposes no reliable public API for a third-party IME to open another keyboard's picker.

### 4.6 KeyboardScreen

- Render the custom full-height emoji grid when `emojiPickerVisible` is true.
- Render category navigation and recent emoji.
- Support long-press variant selection.
- Commit selection and close the picker.
- Keep the picker inside the existing IME root container.
- Do not apply keyboard zoom/pan transforms to the picker.

### 4.7 KeyButton and Accessibility

Keep the emoji key in the existing `KeyButton` layout, set its content description to **“Open emoji picker”**, and retain existing zoom/pan behavior.

### 4.8 architecture.md

Document the custom category picker, EmojiCompat/native `TextView` rendering, recent emoji persistence, variant selection, `InputConnectionBridge.commitText()` insertion, and accessibility semantics.

## 5. Acceptance Criteria

1. Tapping 🙂 opens the embedded custom emoji picker.
2. Picker displays app-owned category navigation, variants, and recents.
3. Selecting an emoji inserts it into the active text field.
4. Picker closes after selection and the keyboard returns.
5. Emoji key remains zoomable and pannable.
6. Keyboard zoom/pan state is retained after picker close.
7. TalkBack announces “Open emoji picker”.
8. Picker selection remains accessible.
9. Category CSV data, the custom grid, recent emoji, and variant relationships are maintained by this project.
10. No IME flicker or crash occurs when opening/closing the picker.

## 6. Testing Checklist

- Open picker at 1x, and while zoomed and panned.
- Verify picker fills the current IME height.
- Select emoji in WhatsApp, Messages, Chrome, and Notes.
- Verify keyboard return and zoom/pan retention after selection.
- Verify TalkBack announces the emoji key.
- Verify app-owned variants, category navigation, and recents.
- Rotate and switch IMEs while the picker is closed.

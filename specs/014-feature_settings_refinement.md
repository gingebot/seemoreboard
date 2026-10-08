# Feature 014 — Settings Refinement

## 1. Overview

Feature 014 refines the full-screen `SettingsActivity` so the settings screen is a more accurate, accessible preview of the keyboard configuration.

The feature makes three focused changes:

1. Moves the active theme label below the keyboard preview and immediately above the theme button.
2. Replaces the small sample-key preview with the full keyboard layout inside a height-adjustable preview container.
3. Replaces the stacked emoji-size options with a same-row dropdown control.

This feature does not add new persisted settings. It reuses the existing `keyboardHeightDp`, `zoomLevel`, `theme`, and `emojiSize` settings.

## 2. Goals

- Make the settings preview representative of the actual keyboard.
- Let users see the effect of the configured keyboard height before returning to the IME.
- Keep the active theme status visually associated with the theme control.
- Reduce vertical space used by emoji-size selection while retaining large accessible controls.
- Preserve existing DataStore persistence and reactive settings updates.
- Preserve magnifier, TalkBack, and large-text compatibility.

## 3. Non-Goals

- No new keyboard-height setting.
- No separate preview-height setting.
- No changes to IME window measurement or keyboard runtime layout.
- No changes to emoji rendering, category data, variants, recents, or emoji insertion.
- The preview is not an interactive keyboard and must not insert text.

## 4. Behaviour Requirements

### 4.1 Full Keyboard Preview

The settings screen must render the complete current keyboard layout inside a dedicated preview container.

The preview must:

- Use the same keyboard layout source as the IME (`LayoutCalculator`/`KeyboardLayout`).
- Show all rows and special keys, including Shift, symbols/layer controls, backspace, Enter, settings, and emoji.
- Use the current persisted theme.
- Reflect the current zoom value.
- Be non-interactive; tapping preview keys must not type, change layers, open settings, or open the emoji picker.
- Remain visually contained within the preview bounds.

The preview may use disabled key controls or a preview-specific rendering wrapper, but it must use the same labels, ordering, sizing rules, and theme colors as the active keyboard.

### 4.2 Height-Adjustable Preview Container

The existing `keyboardHeightDp` setting controls both:

- The configured normal keyboard height in the IME.
- The height of the full keyboard preview container in SettingsActivity.

The height slider remains:

- Range: 200dp–600dp.
- Step: 10dp.
- Default: 350dp.

When the slider changes:

- The preview container height updates immediately.
- The full keyboard remains contained within the new height.
- The value is persisted through `SettingsUseCases.setKeyboardHeightDp()`.
- The IME receives the updated value through the existing settings `Flow`.

The preview container must not force the SettingsActivity itself to resize. If the full layout cannot fit at a selected height, it may clip or scroll internally, but it must not expand outside its container or change the Activity window size.

### 4.3 Active Theme Placement

The active theme label must appear:

1. Below the full keyboard preview container.
2. Immediately above the “Change Theme” control.

The label must display the current theme name, for example:

```text
Active Theme: White & Black
```

The label must update reactively when the theme changes and expose the current value to TalkBack as a polite live-region update.

### 4.4 Theme Control

The existing “Change Theme” control remains below the active theme label.

Each activation must continue cycling through:

1. Black & White
2. White & Black
3. Yellow & Black
4. Blue & White
5. Back to Black & White

The control remains large, high contrast, and accessible.

### 4.5 Emoji Size Dropdown

The emoji-size setting must be presented as one horizontal setting row:

- Left: `Emoji Size` label.
- Right: dropdown showing the current selected value.

The dropdown must provide exactly these options:

- Standard — 40sp glyphs, 68dp cells, five columns.
- Large — 56sp glyphs, 88dp cells, four columns.
- Extra large — 64sp glyphs, 104dp cells, three columns.

Selecting an option must:

- Call `SettingsUseCases.setEmojiSize()`.
- Persist the value through DataStore.
- Update the IME picker on its next composition/update through `settingsFlow`.
- Update the selected value displayed in the row.

The dropdown and its options must have accessible labels and touch targets suitable for low vision. The current value must remain understandable without relying only on color.

### 4.6 Settings Ordering

The refined settings order must be:

1. Zoom control.
2. Keyboard height control.
3. Full keyboard preview container.
4. Active theme label.
5. Change Theme control.
6. Haptics control.
7. Emoji Size row with dropdown.
8. Close control.

Small spacing adjustments are allowed, but the relative ordering above must remain unchanged.

## 5. Architecture Changes

### 5.1 SettingsActivity

Update `SettingsActivity.kt` to:

- Render the full keyboard preview instead of the five-key `Q W E R T` sample.
- Bind preview container height to `keyboardHeightDp`.
- Move the active theme label below the preview and above the theme control.
- Replace stacked emoji-size text controls with a dropdown row.
- Keep all controls connected to `SettingsUseCases`.

### 5.2 Preview Reuse

Prefer reusing the existing keyboard layout definitions and rendering components rather than duplicating key labels or row structure in SettingsActivity.

If `KeyboardLayout` cannot be reused directly because it is coupled to IME actions, extract a pure, non-interactive preview component that accepts:

- `KeyboardState` or equivalent layout state.
- `KeyboardTheme`.
- `zoomLevel`.
- A disabled/non-interactive rendering mode.

The preview must not call `InputConnectionBridge`.

### 5.3 Persistence

No new persistence keys are required. Feature 014 uses the existing:

- `keyboardHeightDp`.
- `theme`.
- `emojiSize`.
- `zoomLevel`.

### 5.4 Accessibility

- The preview container must have a meaningful content description, such as `Keyboard preview`.
- Disabled preview keys must not appear as actionable typing controls.
- The active theme label remains a polite live region.
- The emoji-size dropdown must expose its current value and available options to TalkBack.
- The keyboard-height slider must expose its current dp value.

## 6. Acceptance Criteria

1. SettingsActivity shows the complete keyboard layout in a preview container.
2. The preview uses the current theme, zoom, labels, row ordering, and special keys.
3. Tapping preview keys never inserts text or triggers keyboard actions.
4. The preview container height follows `keyboardHeightDp`.
5. Changing keyboard height updates the preview immediately and persists the setting.
6. The active theme label appears below the preview and above “Change Theme”.
7. The active theme label updates immediately after theme changes.
8. “Change Theme” remains functional and cycles through all four themes.
9. Emoji Size appears on one row with a dropdown control.
10. The dropdown exposes Standard, Large, and Extra large options with the defined sizes/columns.
11. Selecting an emoji size persists it and updates the IME picker configuration.
12. Existing zoom, haptics, height, theme, and emoji-size settings continue to persist through DataStore.
13. TalkBack and magnifier can use all settings controls.
14. The SettingsActivity remains scrollable and does not resize or crash when the preview height changes.

## 7. Testing Checklist

- Open SettingsActivity from the IME.
- Confirm the full keyboard preview contains every current keyboard row and special key.
- Change zoom and confirm the preview scales.
- Change keyboard height through the slider and confirm the preview container changes height.
- Confirm preview taps do not insert or perform actions.
- Change theme and confirm the active theme label is below the preview and updates.
- Open the Emoji Size dropdown and select each option.
- Confirm the selected emoji-size value is displayed on the same row.
- Return to the IME and confirm the emoji picker uses the selected size.
- Restart the app/IME and confirm height, theme, zoom, and emoji size persist.
- Navigate all controls with TalkBack.
- Use system magnification over the full preview and dropdown.
- Rotate the device and confirm SettingsActivity remains usable.

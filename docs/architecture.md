# 1. System Overview

## 1.1 Purpose

An Android keyboard (IME) designed for people with low vision, with:

- Zoomable keys (scalable keyboard surface)
- Pannable layout (dynamic panning across enlarged keyboard)
- High‑contrast themes applied to keys with large touch targets
- Haptic feedback (MVP)
- Audio feedback / spoken keys (later roadmap)
- Predictive text / suggestions (later roadmap)

## 1.2 High‑level Components

- IME Service Layer  
  Android `InputMethodService` implementation that integrates with the system as a keyboard.

- Keyboard UI Layer (Jetpack Compose)  
  Renders the keyboard, handles pan with dynamic zoom, processes touch input.

- Keyboard Layout Engine  
  Defines key positions, labels, hitboxes, and recalculates layout under zoom/pan.

- Input Handling Layer  
  Bridges UI events to `InputConnection`.

- Settings Layer  
  Stores preferences (zoom, pan sensitivity, haptics, audio, keyboard height) via DataStore.
  Settings UI is a full‑screen Activity launched from IME (⚙ key). Android system settings integration is declared via `android.view.im.settingsActivity` metadata but not honoured by Samsung devices (metadata reads as null system‑side for third‑party IMEs).

- Accessibility Layer  
  Integrates accessibility APIs and TextToSpeech (later roadmap).

# 2. Technology Stack

## 2.1 Platform & Language

- **Android** (IME / Input Method Framework)
- **Kotlin** (primary language)
- **Jetpack Compose** (UI framework)

## 2.2 Core APIs & Libraries

### IME / Input Method Framework
- `InputMethodService`
- `EditorInfo`
- `InputConnection`

### Compose UI
- `Modifier.scale()`
- `Modifier.offset()`
- `Modifier.pointerInput()`
- `Modifier.draggable()`
- `Modifier.onSizeChanged()`
- Theme-aware `KeyButton` rendering for key background, label, and border colors

### State & Async
- Kotlin Coroutines
- Kotlin Flow

### Persistence
- Jetpack DataStore (Preferences)

### Accessibility
- `AccessibilityManager`
- `AccessibilityEvent`
- `AccessibilityNodeInfo`

### Haptics
- `performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)`

### Audio (later roadmap)
- `TextToSpeech`

## 2.3 Optional Libraries

- **Accompanist** (gestures, animations)
- **Timber** (logging)
- **Jetpack Navigation** (if a separate settings app is added)

## 2.4 Build Configuration

Build dependencies are managed via `build.gradle` (project-level) and
`app/build.gradle` (module-level). Key configuration:

- `compileSdk` and `minSdk` defined for Android API compatibility
- Compose compiler and UI dependencies declared
- DataStore, Coroutines, and Flow dependencies included
- ProGuard / R8 rules configured for production builds

The Gradle wrapper (`gradlew`) is used for all build tasks; no IDE build is required.

# 3. Module & Folder Structure

The project is organized into clear, isolated modules so that each part of the keyboard
(UI, IME, layout engine, settings, accessibility) can evolve independently and predictably.

```text
app/
│
├── ime/
│   ├── service/
│   │   └── LowVisionKeyboardService.kt
│   ├── integration/
│   │   └── InputConnectionBridge.kt
│   └── config/
│       ├── ImeConfig.kt
│       └── method.xml
│
├── ui/
│   ├── keyboard/
│   │   ├── KeyboardScreen.kt
│   │   ├── KeyboardViewModel.kt
│   │   └── KeyboardState.kt
│   ├── components/
│   │   └── KeyButton.kt
│   ├── settings/
│   │   └── SettingsActivity.kt
│   ├── theme/
│   │   └── Color.kt
│   └── gestures/
│       └── PanContainer.kt
│
├── domain/
│   ├── layout/
│   │   ├── KeyboardLayout.kt
│   │   ├── KeyDefinition.kt
│   │   └── LayoutCalculator.kt
│   ├── settings/
│   │   ├── KeyboardSettings.kt
│   │   └── SettingsUseCases.kt
│   └── input/
│       └── KeyAction.kt
│
├── data/
│   ├── settings/
│   │   └── DataStoreSettingsRepository.kt
│   └── suggestions/   (later roadmap)
│       └── SuggestionsRepository.kt
│
├── accessibility/
│   ├── AccessibilityManagerWrapper.kt
│   └── SpokenFeedbackController.kt   (later roadmap)
│
└── utils/
    └── Logging.kt
```

This structure ensures:

- IME logic stays isolated
- Compose UI stays clean
- Layout calculations remain pure Kotlin
- Settings are centralized
- Accessibility is modular
- Future features (audio, suggestions) slot in cleanly

# 4. Data & Control Flow

## 4.1 Typing Flow (MVP)

1. User taps a key on the zoomed/panned keyboard (Compose UI).
2. `KeyboardScreen` forwards the event to `KeyboardViewModel`.
3. `KeyboardViewModel` maps the tap to a `KeyAction` using:
   - `KeyboardLayout`
   - `LayoutCalculator`
4. `KeyAction` is passed to `InputConnectionBridge`.
5. `InputConnectionBridge` calls methods on `InputConnection`:
   - `commitText()` for characters
   - `deleteSurroundingText()` for backspace
   - `sendKeyEvent()` for enter, etc.
6. Haptic feedback is triggered via:
   - `performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)`

---

## 4.2 Zoom & Pan Flow

1. User performs drag gesture on the keyboard surface.
2. `PanContainer` interprets gestures and updates:
   - `offsetX` via drag and inertial fling
   - `offsetY` via drag and inertial fling
   - tap‑to‑cancel during fling
3. Zoom is user‑controlled via a slider in Settings (range 1×–3×, defaults to 1×). No pinch‑zoom.
4. Zoom changes trigger `LaunchedEffect` in `PanContainer` to reclamp `currentOffset`.
4. `KeyboardViewModel.updatePan()` updates `KeyboardState` offset values.
5. `KeyboardScreen` recomposes with `graphicsLayer` scale/translation.
6. `LayoutCalculator` recalculates key hitboxes based on zoom/pan.

---

## 4.3 Settings Flow

1. User taps ⚙ key → IME launches `SettingsActivity` via `Intent.FLAG_ACTIVITY_NEW_TASK`.
2. Android system settings may launch `SettingsActivity` via `android.view.im.settingsActivity` manifest meta‑data — works on stock Android; **not honoured on Samsung devices** (third‑party IME metadata is ignored).
3. User changes settings (zoom slider, height slider, haptic toggle, high contrast toggle).
4. `SettingsUseCases` update `KeyboardSettings` persisted via `DataStoreSettingsRepository`.
5. `KeyboardViewModel` observes settings via `Flow<KeyboardSettings>` and applies reactively to the IME.
6. SettingsActivity uses its own `zoomValue` local state for smooth slider tracking, persisting via `SettingsUseCases`.

# 5. IME Integration

## 5.1 IME Service

`LowVisionKeyboardService` is the core entry point of the keyboard.  
It extends `InputMethodService` and manages the lifecycle of the keyboard UI and the connection
to the active text field.

### Responsibilities

- Create and attach the Compose keyboard view with a configurable root height (`keyboardHeightDp`).
- Allow Android to measure the IME window and orientation/insets; the Compose root expresses the persisted keyboard height without forcing the window size.
- Handle IME lifecycle events:
  - `onCreateInputView()`
  - `onStartInputView()`
  - `onFinishInputView()`
  - `onEvaluateInputViewShown()` (controls visibility per editor type)
- Provide the active `InputConnection` to the keyboard logic.
- Read `EditorInfo` to understand the input type:
  - numeric fields
  - password fields
  - email fields
  - multiline fields
- Delegate fullscreen/extract-mode decisions to Android's `InputMethodService` and the host editor configuration. In landscape or fullscreen contexts, Android may provide its system-managed extract editor and action controls.
- Manage IME configuration via `ImeConfig`.

### Compose Integration

The IME service hosts a ComposeView that renders:

- `KeyboardScreen`
- `KeyboardViewModel`
- `KeyboardState`

This ensures the keyboard UI is fully Compose‑driven rather than XML‑based.

### AndroidManifest Declaration

The IME service must be declared in `AndroidManifest.xml` with:

- A `<service>` element for `LowVisionKeyboardService`
- The `android.permission.BIND_INPUT_METHOD` permission
- An `<intent-filter>` for `android.view.InputMethod`
- A `<meta-data>` element referencing the input method subtype XML

This is what registers the keyboard with the Android system and makes it available
for user selection in Settings.

---

## 5.2 InputConnection Bridge

`InputConnectionBridge` wraps the raw `InputConnection` and exposes safe, high‑level methods
for the rest of the keyboard to use.

### Responsibilities

- `typeCharacter(char: Char)`
- `deleteCharacter()`
- `submit()` (Enter key)
- `sendKeyEvent(KeyEvent)`

### Why this abstraction matters

- Keeps IME logic out of UI code.
- Makes Cline’s reasoning simpler and safer.
- Allows future features (suggestions, autocorrect) to hook into input handling cleanly.

# 6. Accessibility Layer

The keyboard must be fully usable by people with low vision and compatible with Android’s
accessibility services, including TalkBack. This layer ensures visual clarity, large touch
targets, spoken feedback (later roadmap), and proper accessibility events.

---

## 6.1 Goals

- Ensure compatibility with TalkBack and other screen readers.
- Provide large, easy‑to‑hit touch targets.
- Provide spoken feedback for key presses (later roadmap).
- Provide clear visual feedback:
  - high‑contrast mode
  - focus states
  - pressed states
- Emit correct accessibility events for key interactions.

---

## 6.2 Components

### AccessibilityManagerWrapper

A thin wrapper around Android’s `AccessibilityManager`.

Responsibilities:

- Detect whether accessibility services (e.g., TalkBack) are enabled.
- Emit `AccessibilityEvent` notifications when keys are pressed.
- Provide hooks for future accessibility features.

### SpokenFeedbackController (later roadmap)

A controller that uses Android’s `TextToSpeech` engine to speak:

- key labels (“A”, “B”, “C”)
- special keys (“Space”, “Enter”, “Delete”)
- actions (“Character deleted”, “Shift enabled”)

Responsibilities:

- Initialize and manage the TTS engine.
- Speak text based on keyboard events.
- Respect user settings (`audioEnabled`).

---

## 6.3 Accessibility Events

The keyboard should emit:

- `TYPE_VIEW_CLICKED` when a key is pressed.
- `TYPE_ANNOUNCEMENT` when spoken feedback is triggered.
- `TYPE_VIEW_ACCESSIBILITY_FOCUS` when a key becomes focused (if needed).

---

## 6.4 Touch Target Requirements

All keys must meet or exceed Android’s recommended minimum touch target size:

- **48dp x 48dp minimum**
- Scaled appropriately when zoomed

Zooming must **increase** touch target size, not decrease it.

---

## 6.5 Keyboard Themes

Keyboard themes provide accessible key palettes while keeping the surrounding keyboard and IME surfaces stable:

- Select accessible key background and label colors
- Provide a visible key border for each palette
- Apply only to key components through `KeyButton`
- Use the selected `KeyboardTheme` palette

The available themes are `BLACK_WHITE`, `WHITE_BLACK`, `YELLOW_BLACK`, and `BLUE_WHITE`.

---

## 6.6 Integration with Compose

Compose modifiers used:

- `Modifier.semantics { }`
- `Modifier.clearAndSetSemantics { }`
- `Modifier.onGloballyPositioned { }` (for hitbox calculations)
- `Modifier.pointerInput { }` (for gestures)

These ensure accessibility metadata is correctly attached to UI elements.

---

## 6.7 Future Extensions

The architecture supports:

- Braille input mode (optional future feature)
- Audio‑only typing mode
- Dynamic focus navigation for users who cannot pan manually

# 7. Settings & Preferences

The keyboard allows users to customize zoom, panning behavior, haptics, audio feedback,
keyboard height, and key themes. All settings must persist across sessions and update the keyboard UI
reactively.

---

## 7.1 Settings Model

`KeyboardSettings` defines all configurable options:

- `zoomLevel: Float`  
- `panSensitivity: Float`  
- `hapticEnabled: Boolean`  
- `audioEnabled: Boolean` (later roadmap)  
- `keyboardHeightDp: Int` — adjustable IME height (200–600dp, default 350dp)
- `theme: KeyboardTheme` — persisted key color theme, default `BLACK_WHITE`

### Setting descriptions

- **zoomLevel** – default zoom applied when the keyboard opens.  
- **panSensitivity** – controls how fast the keyboard moves when dragged.  
- **hapticEnabled** – enables/disables haptic feedback on key press.  
- **audioEnabled** – enables spoken feedback via TextToSpeech (later roadmap).  
- **keyboardHeightDp** – controls IME window height in density‑independent pixels via a slider in Settings.
- **theme** – selects the key background, label, and border palette; it does not change the keyboard or IME backgrounds.

Supported key themes:

- `BLACK_WHITE` — black background, white label and border
- `WHITE_BLACK` — white background, black label and border
- `YELLOW_BLACK` — yellow background, black label and border
- `BLUE_WHITE` — dark blue background, white label and border

---

## 7.2 Persistence Layer

Settings are stored using **Jetpack DataStore (Preferences)** via a
`DataStoreSettingsRepository`.

Responsibilities:

- Read settings from DataStore.  
- Write updated settings to DataStore.  
- Expose settings as a reactive `Flow<KeyboardSettings>`.
- Persist `theme` as its enum name and safely fall back to `BLACK_WHITE` for unknown or missing values.  

---

## 7.3 Settings Use Cases

`SettingsUseCases` provide a clean API for updating settings:

- `setZoomLevel(Float)`  
- `setPanSensitivity(Float)`  
- `setHapticEnabled(Boolean)`  
- `setAudioEnabled(Boolean)` (later roadmap)  
- `setKeyboardHeightDp(Int)` — clamps to 200–600 range  
- `cycleTheme(KeyboardTheme)`  
- `setTheme(KeyboardTheme)`  

These use cases ensure:

- UI never writes directly to DataStore.  
- Cline has a predictable place to add new settings.  
- Future features (audio, suggestions) integrate cleanly.  

---

## 7.4 Reactive Updates

`KeyboardViewModel` observes `settingsFlow` from `DataStoreSettingsRepository`
and updates `KeyboardState` automatically when:

- zoom changes  
- pan sensitivity changes  
- haptics toggle changes  
- keyboard height changes (via slider)  
- theme changes, which update key rendering and the unified Settings preview

The keyboard UI always reflects the latest settings without manual refresh.

---

## 7.5 Integration with IME

Settings must be applied when:

- the keyboard view is created (`onCreateInputView`) — initial height from default/persisted value
- the input view starts (`onStartInputView`) and Android measures the Compose root using the persisted height  
- the user changes settings mid‑session — `KeyboardState.keyboardHeightDp` recomposes the Compose root without overriding Android's orientation/inset measurement

Pan offset values must be applied immediately to avoid disorientation.
Height changes recompose the Compose root; Android retains control of the surrounding IME window, orientation, insets, and extract-mode layout.
Theme changes recompose key components only; keyboard and IME root backgrounds remain unchanged.
`SettingsActivity` displays the active theme and a unified `Q W E R T` preview scaled with the current zoom.

---

## 7.6 Future Extensions

The settings architecture supports:

- custom color themes  
- custom key layouts  
- audio feedback profiles  
- gesture sensitivity tuning  
- accessibility presets  
- adjustable IME height (slider)
- ergonomic resizing controls  

# 8. AI Assistant Integration Rules

These rules ensure Cline and DeepSeek consistently follow the architecture and modify
the project predictably and safely. Both assistants must adhere to these rules equally.

---

## 8.1 Respect the Architecture

Always place code in the correct module:

- IME logic → `ime/`
- Compose UI → `ui/`
- Layout engine → `domain/layout/`
- Input handling → `domain/input/`
- Settings → `data/settings/`
- Accessibility → `accessibility/`
- Utilities → `utils/`

Do NOT create new modules unless explicitly instructed.

Always reason in terms of the defined layers:

- IME service layer
- Compose UI layer
- Keyboard layout engine
- Input handling layer
- Settings layer
- Accessibility layer

All solutions must map cleanly onto these layers.

---

## 8.2 Modify Existing Structures

When adding features, extend these core structures:

- `KeyboardLayout`
- `KeyboardState`
- `KeyboardSettings`
- `KeyboardViewModel`

instead of creating duplicate or parallel structures.

### Feature implementation examples

- New key behavior → update `KeyAction`, `KeyboardViewModel`, `InputConnectionBridge`
- New setting → update `KeyboardSettings`, `SettingsUseCases`, `DataStoreSettingsRepository`
- New accessibility feature → update `accessibility/` module

---

## 8.3 Use the Defined Technology Stack

Must use:

- Jetpack Compose for all UI
- DataStore for settings
- InputMethodService + InputConnection for IME
- Coroutines + Flow for async/state

Must NOT use:

- XML layouts
- `KeyboardView`
- Prebuilt keyboard libraries

---

## 8.4 Build / Deploy / Debug Workflow

Follow this workflow for every change:

1. Build the project using Gradle.
2. Install the APK onto the connected device.
3. Use logcat to debug IME behavior.
4. Apply fixes in the correct module.

---

## 8.5 Error Handling & Fixes

When fixing bugs, trace logic in this order:

1. UI (Compose)
2. ViewModel
3. Layout engine
4. InputConnection bridge
5. IME service

This ensures bugs are diagnosed from the surface down to the IME core. Use logcat output to
identify IME issues and propose fixes in the correct module.

Avoid mixing UI logic with IME logic.

---

## 8.6 Consistency Requirements

Always:

- Follow naming conventions defined in Section 9.
- Keep file structure consistent.
- Avoid introducing architectural drift.
- Maintain separation of concerns.
- Keep functions small and single‑purpose.
- Avoid unnecessary abstractions.

---

## 8.7 Future Feature Hooks

Design solutions with future roadmap items in mind:

- predictive text
- audio feedback
- advanced accessibility modes

These must integrate into existing modules rather than creating new ones.

---

## 8.8 Safety & Stability

Always:

- Avoid risky refactors unless explicitly requested.
- Avoid deleting core architecture files.
- Avoid rewriting modules unnecessarily.
- Prefer additive changes over destructive ones.

This ensures long‑term stability of the codebase.

# 9. Naming & Conventions

Consistent naming and structure ensure the keyboard remains maintainable, predictable,
and easy for Cline + DeepSeek to extend.

---

## 9.1 File Naming

Use **PascalCase** for Kotlin files containing classes:

- `KeyboardViewModel.kt`
- `LowVisionKeyboardService.kt`
- `InputConnectionBridge.kt`
- `KeyboardLayout.kt`
- `KeyboardSettings.kt`

Use **lowercase** for directories:

- `ui/`
- `ime/`
- `domain/`
- `data/`
- `accessibility/`
- `utils/`

---

## 9.2 Class Naming

Use **PascalCase** for all classes:

- `KeyboardScreen`
- `KeyboardState`
- `KeyDefinition`
- `LayoutCalculator`
- `AccessibilityManagerWrapper`

Compose UI classes end with:

- `...Screen`
- `...View`

ViewModels end with:

- `...ViewModel`

Settings-related classes end with:

- `...Settings`
- `...SettingsRepository`
- `...SettingsUseCases`

---

## 9.3 Function Naming

Use **camelCase** for functions:

- `updatePan()`
- `handleKeyPress()`
- `calculateLayout()`
- `applySettings()`
- `performHapticFeedback()`

Functions should be:

- short  
- single‑purpose  
- side‑effect‑free when possible  

---

## 9.4 State & Data Models

Use **data classes** for:

- `KeyboardState`
- `KeyboardSettings`
- `KeyDefinition`

State classes must be immutable and updated via copy operations.

---

## 9.5 Compose Conventions

Compose UI must follow:

- `@Composable` functions are pure and side‑effect‑free.
- Use `remember` and `rememberSaveable` for local UI state.
- Use `ViewModel` for business logic and persistent state.
- Use `Modifier` chains for layout, gestures, and accessibility.

---

## 9.6 Package Structure

Packages must mirror the folder structure:

- `ui.keyboard`
- `ui.components`
- `ui.gestures`
- `ime.service`
- `ime.integration`
- `ime.config`
- `domain.layout`
- `domain.settings`
- `domain.input`
- `data.settings`
- `data.suggestions`
- `accessibility`
- `utils`

This ensures imports remain predictable and organized.

---

## 9.7 Logging

Use a single logging utility:

- `utils/Logging.kt`

Avoid scattering `Log.d()` calls throughout the codebase.

---

## 9.8 Constants

Constants must be placed in:

- `ImeConfig.kt`
- or inside companion objects when scoped to a single class

Avoid magic numbers in UI code.

---

## 9.9 Comments & Documentation

Use comments sparingly:

- Only when logic is non‑obvious.
- Prefer clear naming over excessive comments.
- Document public functions and classes when needed.

---

## 9.10 Future‑Proofing

Naming conventions must support future roadmap items:

- predictive text  
- audio feedback  
- advanced accessibility modes  
- custom layouts  

This ensures new features integrate cleanly without renaming existing modules.

# 10. Roadmap Hooks (Future Features)

The architecture is intentionally designed to support future expansion without requiring
major refactors. Each roadmap item slots cleanly into existing modules.

---

## 10.1 Predictive Text & Suggestions

Future predictive text features will integrate into:

- `data/suggestions/`
- `domain/input/`
- `KeyboardViewModel`
- `KeyboardState`

Key components:

- SuggestionsRepository (data source)
- SuggestionEngine (ranking logic)
- UI integration in KeyboardScreen

Predictive text must NOT alter IME core behavior; it only proposes input.

---

## 10.2 Audio Feedback (Spoken Keys)

Audio feedback integrates into:

- `accessibility/SpokenFeedbackController`
- `KeyboardViewModel` (triggering speech)
- `KeyboardSettings` (`audioEnabled`)

Features include:

- speaking key labels
- speaking special keys (“Space”, “Enter”, “Delete”)
- speaking actions (“Character deleted”)

---

## 10.3 Advanced Accessibility Modes

Future accessibility enhancements may include:

- dynamic focus navigation
- audio‑only typing mode
- braille input mode
- gesture‑based typing

These integrate into:

- `accessibility/`
- `ui.gestures/`
- `KeyboardState`

---

## 10.4 Custom Layouts

The architecture supports multiple keyboard layouts:

- QWERTY (current)
- high‑contrast variants
- simplified layouts
- large‑key layouts
- regional layouts (optional)

These integrate into:

- `KeyboardLayout`
- `KeyDefinition`
- `LayoutCalculator`

---

## 10.5 Theming & Visual Customization

Future theming options include:

- custom color palettes
- adjustable contrast levels
- font size scaling
- key shape variations

These integrate into:

- `ui.theme/`
- `KeyboardSettings`
- `KeyboardState`

---

## 10.6 Gesture Enhancements

Future gesture features may include:

- inertia‑based panning
- double‑tap zoom
- edge snapping
- multi‑finger shortcuts

These integrate into:

- `ui.gestures/PanContainer.kt`
- `KeyboardViewModel`

---

## 10.7 Cloud Sync (Optional)

If settings sync is ever added:

- integrate with Android backup APIs
- store settings in cloud‑safe formats
- maintain DataStore as the local source of truth

---

## 10.8 Architecture Stability

All roadmap items must:

- extend existing modules
- avoid creating parallel systems
- preserve IME stability
- maintain accessibility compliance

The architecture is intentionally future‑proofed to avoid major rewrites.

# 11. Summary

This architecture defines a clear, modular, future‑proof structure for the Low‑Vision Android Keyboard.
It ensures the keyboard is accessible, maintainable, and easy for Cline + DeepSeek to extend.

---

## 11.1 Core Principles

- **Compose‑first UI**  
  All rendering, gestures, and accessibility semantics are implemented in Jetpack Compose.

- **IME‑correctness**  
  Input handling is isolated in `InputConnectionBridge` and managed by `LowVisionKeyboardService`.

- **Separation of concerns**  
  Each module has a single responsibility:
  - IME integration
  - UI rendering
  - layout calculation
  - settings persistence
  - accessibility support

- **Predictable data flow**  
  UI → ViewModel → Layout Engine → InputConnection → IME

- **Reactive state**  
  Settings and keyboard state update via Kotlin Flow.

---

## 11.2 Maintainability

The architecture ensures:

- predictable file structure  
- consistent naming conventions  
- minimal coupling between modules  
- safe extension points for future features  
- clear debugging paths (UI → ViewModel → Layout → IME)

---

## 11.3 Accessibility Priority

Accessibility is not an add‑on — it is a core design pillar:

- large touch targets  
- zoom + pan  
- high‑contrast mode  
- spoken feedback (future)  
- correct accessibility events  

---

## 11.4 Future‑Proof Design

The architecture supports:

- predictive text  
- audio feedback  
- custom layouts  
- advanced accessibility modes  
- gesture enhancements  
- theming and visual customization  

All future features integrate into existing modules without architectural drift.

---

## 11.5 AI‑Friendly Structure

The project is intentionally designed for AI‑assisted development:

- clear module boundaries  
- explicit responsibilities  
- consistent naming  
- minimal ambiguity  
- safe extension points  

This ensures Cline + DeepSeek can implement features reliably and without breaking IME behavior.

---

## 11.6 Final Statement

This architecture is the **single source of truth** for how the Low‑Vision Keyboard must be built,
extended, and maintained.  
All future specs, tasks, and features must align with this document.

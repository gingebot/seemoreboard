# Feature Spec: Compose Migration Plan (Feature #6)

## 1. Overview

This feature defines the structured, safe migration from the temporary View‑based UI
(`QwertyKeyboardView`, `KeyButtonView`, `SettingsPanelHolder`, etc.) to the intended
Compose‑first architecture described in `architecture.md`.

Compose cannot currently run inside the IME window due to the
`WindowRecomposerFactory.LifecycleAware` → `ViewTreeLifecycleOwner` constraint.
This spec introduces the lifecycle workaround, activates Compose UI, and begins
replacing View-based components with their Compose equivalents.

Migration is staged, additive, and reversible. No functionality is removed until
Compose is fully stable inside the IME.

---

## 2. Goals

- Enable Jetpack Compose inside the IME window.
- Replace View-based UI with Compose equivalents.
- Activate existing Compose files (`KeyboardScreen.kt`, `SingleKeyButton.kt`).
- Preserve all existing features:
  - QWERTY
  - Zoom/pan
  - Settings
  - Shift + symbols
  - High contrast
  - Haptics
- Maintain architecture separation and IME correctness.
- Avoid regressions during migration.

---

## 3. User Story

As a user with low vision,  
I want a smoother, more scalable, more accessible keyboard UI,  
so I can type comfortably at any zoom level with clear visuals and gestures.

---

## 4. Migration Strategy

Migration is performed in **three phases**, each safe and reversible.

### Phase 1 — Enable Compose in IME Window

Implement a lifecycle workaround that provides a `ViewTreeLifecycleOwner` to the IME
window’s root view. Options include:

- Injecting a synthetic `LifecycleOwner` into the IME root view.
- Using `setViewTreeLifecycleOwner()` on the IME root panel.
- Using `WindowRecomposerFactory.setFactory()` if available in the current Compose BOM.
- Fallback: wrapping the IME root in a lifecycle-aware container view.

Acceptance criteria:
- ComposeView can attach without throwing lifecycle exceptions.
- A simple Compose UI (e.g., a single “A” key) renders inside the IME.

### Phase 2 — Activate Compose KeyboardScreen

Replace the View-based root view with:

```kotlin
return ComposeView(this).apply {
    setContent { KeyboardScreen(viewModel) }
}
```
Implement the Compose equivalents of:

- QWERTY layout
- Symbols layout
- Shift key
- ABC/123 layer switch
- Settings key
- High contrast mode
- Zoom/pan gestures (Compose pointer input)
- Key hitboxes (Compose layout coordinates)
- Haptics (via View interop)
- Logging

Acceptance criteria:
- Compose keyboard renders fully.
- All keys work.
- Zoom/pan work.
- Settings panel works.
- Shift + symbols work.
- No crashes.

### Phase 3 — Remove View-based UI

Once Compose is stable:
- Delete:
  - `QwertyKeyboardView.kt`
  - `KeyButtonView.kt`
  - `SettingsPanelHolder`
  - `SingleKeyView.kt`
- Remove temporary gesture code.
- Remove temporary layout code.
- Update architecture doc to reflect Compose activation.

Acceptance criteria:
- No references to View-based UI remain.
- All features work identically or better in Compose.

---

## 5. Functional Requirements

### 5.1 Compose Lifecycle Workaround

Must:
- Provide a valid `LifecycleOwner` to the IME window root.
- Allow ComposeView to attach without exceptions.
- Work on Samsung devices (non-standard IME window structure).

### 5.2 Compose KeyboardScreen

Must implement:
- Alphabet layer (QWERTY)
- Symbols layer
- Shift states (OFF, ON, LOCK)
- ABC/123 layer switch
- Settings panel (Compose)
- High contrast mode
- Zoom/pan gestures
- Key hitboxes via Compose layout coordinates
- Haptics (via View interop)
- Logging

### 5.3 Accessibility

Compose must provide:
- `Modifier.semantics { }`
- `Modifier.clearAndSetSemantics { }`
- Correct accessibility events
- Large touch targets (≥48dp)
- High contrast visuals

### 5.4 Performance

Compose must:
- Recompose efficiently under zoom/pan
- Avoid frame drops
- Avoid memory leaks

---

## 6. Architecture Impact

### 6.1 New Files

**`ui/keyboard/KeyboardScreen.kt`**  
Compose root screen for the keyboard.

**`ui/components/KeyButton.kt`**  
Compose equivalent of `KeyButtonView`.

**`ui/components/SettingsPanel.kt`**  
Compose equivalent of `SettingsPanelHolder`.

**`ui/gestures/ComposeZoomPan.kt`**  
Compose gesture handlers.

### 6.2 Updated Files

**`ime/service/SeeMoreBoardService.kt`**
- Replace View-based root with ComposeView.

**`domain/layout/LayoutCalculator.kt`**
- Provide Compose-friendly hitbox calculations.

**`ui/keyboard/KeyboardViewModel.kt`**
- No major changes; ensure compatibility with Compose state.

### 6.3 Removed Files (Phase 3)

- `QwertyKeyboardView.kt`
- `KeyButtonView.kt`
- `SettingsPanelHolder`
- `SingleKeyView.kt`

---

## 7. Acceptance Criteria

### 7.1 Compose Enabled
- ComposeView attaches without lifecycle errors.
- Simple Compose UI renders inside IME.

### 7.2 Full Compose Keyboard
- Alphabet layer works.
- Symbols layer works.
- Shift works (ON, LOCK, OFF).
- ABC/123 layer switch works.
- Settings panel works.
- High contrast mode works.
- Zoom/pan work.
- Haptics work.
- Logging works.

### 7.3 No Regressions
- No crashes.
- No lost features.
- No performance degradation.

### 7.4 Cleanup
- All View-based UI removed.
- Architecture doc updated.

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

---

## 8.3 Test Compose Activation

1. Open a text field.
2. Verify that the Compose UI renders instead of the View-based fallback.
3. Test alphabet keys:
   - lowercase typing
   - uppercase typing via Shift ON
   - persistent uppercase via Shift LOCK
4. Test symbols layer:
   - tap 123 → symbols appear
   - tap ABC → alphabet returns
5. Test zoom/pan:
   - pinch to zoom
   - drag to pan
   - verify keys remain tappable at all zoom/pan states
6. Test settings:
   - open settings panel
   - toggle haptics
   - toggle high contrast
   - verify immediate reactive updates
7. Test accessibility:
   - enable TalkBack
   - verify semantics and announcements
8. Test performance:
   - max zoom
   - rapid panning
   - rapid typing
   - verify no frame drops or lag

---

## 9. Cline Implementation Plan (Summary)

1. Implement lifecycle workaround so ComposeView can attach inside IME.
2. Replace IME root view with ComposeView using setContent { KeyboardScreen(viewModel) }.
3. Implement Compose KeyboardScreen:
   - alphabet layer
   - symbols layer
   - shift states
   - ABC/123 layer switch
   - settings key
   - high contrast mode
4. Implement Compose KeyButton:
   - large touch targets
   - high contrast visuals
   - accessibility semantics
5. Implement Compose SettingsPanel:
   - haptics toggle
   - high contrast toggle
   - reactive state
6. Implement Compose zoom/pan gestures:
   - pointerInput
   - transformable
   - layout coordinate mapping
7. Update LayoutCalculator for Compose hitboxes.
8. Remove View-based UI once Compose is stable.
9. Build, deploy, and test on device.


## Adendum

⭐ PART 1 — Compose Scaffolding Code
This is the starter code Cline will use to build the full Compose keyboard.

It is intentionally minimal, correct, and safe for IME windows.

1.1 ComposeView in IME Service (Scaffold)
kotlin
override fun onCreateInputView(): View {
    val composeView = ComposeView(this)

    composeView.setContent {
        KeyboardScreen(viewModel)
    }

    return composeView
}
This is the final goal — but cannot run yet until the lifecycle workaround is applied.

1.2 KeyboardScreen Scaffold
kotlin
@Composable
fun KeyboardScreen(viewModel: KeyboardViewModel) {
    val state by viewModel.keyboardState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        KeyboardLayout(
            state = state,
            onKeyPress = { viewModel.onKeyPress(it) },
            onToggleShift = { viewModel.toggleShift() },
            onToggleLayer = { viewModel.toggleLayer() },
            onOpenSettings = { viewModel.openSettings() }
        )

        if (state.isSettingsOpen) {
            SettingsPanel(
                state = state,
                onToggleHaptics = { viewModel.toggleHaptics() },
                onToggleHighContrast = { viewModel.toggleHighContrast() },
                onClose = { viewModel.closeSettings() }
            )
        }
    }
}
1.3 KeyButton Scaffold
kotlin
@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    highContrast: Boolean
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(if (highContrast) Color.Black else Color.DarkGray)
            .clickable(onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 24.sp
        )
    }
}
1.4 Compose Zoom/Pan Scaffold
kotlin
@Composable
fun ZoomPanContainer(
    zoom: Float,
    offsetX: Float,
    offsetY: Float,
    onZoom: (Float) -> Unit,
    onPan: (Float, Float) -> Unit,
    content: @Composable () -> Unit
) {
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        onZoom(zoom * zoomChange)
        onPan(offsetX + panChange.x, offsetY + panChange.y)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = zoom
                scaleY = zoom
                translationX = offsetX
                translationY = offsetY
            }
            .transformable(transformState)
    ) {
        content()
    }
}
This is the Compose equivalent of your View-based zoom/pan.

⭐ PART 2 — IME Lifecycle Workaround Options
These are the critical options Cline needs.
Compose cannot run inside IME without one of these.

I’m giving you four safe options, ordered from safest to most advanced.

Option A — Synthetic LifecycleOwner Injection (Safest)
Attach a fake lifecycle owner to the IME root view:

kotlin
val root = window?.window?.decorView ?: composeView

ViewTreeLifecycleOwner.set(root, object : LifecycleOwner {
    private val registry = LifecycleRegistry(this).apply {
        currentState = Lifecycle.State.RESUMED
    }
    override val lifecycle: Lifecycle = registry
})
Pros:

Works on Samsung

Works on all OEMs

Minimal risk

Recommended for MVP

Option B — Wrap ComposeView in LifecycleContainer
kotlin
val container = FrameLayout(this)
ViewTreeLifecycleOwner.set(container, lifecycleOwner)
container.addView(composeView)
return container
Pros:

Clean separation

Easy debugging

Works on most OEMs

Option C — WindowRecomposerFactory Override (Advanced)
If your Compose BOM supports it:

kotlin
WindowRecomposerFactory.setFactory { context ->
    Recomposer(context)
}
Pros:

Most “correct” solution

Matches future Compose releases
Cons:

Not available on all BOM versions

OEM-dependent

Option D — Force LifecycleOwner on IME Root Panel
Samsung IME windows often use parentPanel instead of decorView.

kotlin
val rootPanel = composeView.rootView.findViewById<View>(android.R.id.content)
ViewTreeLifecycleOwner.set(rootPanel, lifecycleOwner)
Pros:

Works specifically on Samsung
Cons:

Requires OEM testing
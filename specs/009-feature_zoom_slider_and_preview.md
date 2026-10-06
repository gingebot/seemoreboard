# Feature 009 — Zoom Slider + Preview

## 1. Overview

Feature 009 introduces a **user‑controlled zoom slider** in the Settings panel, allowing users to adjust the keyboard zoom level between **1× and 3×**. The slider includes a **live preview key** that shows how large keys will appear at the selected zoom level.

This feature builds directly on Feature 008:

- PanContainer already applies zoom via `graphicsLayer`.
- Zoom was temporarily fixed at 2×.
- This feature restores dynamic zoom — controlled via settings, not gestures.

Pinch‑zoom remains permanently disabled.

---

## 2. Goals

- Add a zoom slider to SettingsPanel.
- Add a preview key that scales live with the slider.
- Store zoom level in DataStore (`KeyboardSettings.zoomLevel`).
- Apply zoom dynamically through PanContainer.
- Ensure hitboxes update correctly via LayoutCalculator.
- Ensure panning boundaries update correctly when zoom changes.
- Maintain IME stability and accessibility.

---

## 3. Behaviour Requirements

### 3.1 Zoom Slider

- Range: **1.0× → 3.0×**
- Step: **0.1** increments (or continuous Compose slider)
- **Default zoom level: 1.0×**
- Slider updates `KeyboardSettings.zoomLevel` via `SettingsUseCases.setZoomLevel()`.
- Slider updates `KeyboardState.zoomLevel` reactively via Flow.

### 3.2 Preview Key

- A single key rendered inside SettingsPanel.
- Uses the same KeyButton composable.
- Scales using the selected zoom level.
- Does not respond to taps.
- Shows exactly how keys will appear at the chosen zoom.

### 3.3 Keyboard Zoom Application

PanContainer applies zoom via:

```
graphicsLayer {
    scaleX = zoom
    scaleY = zoom
}
```

Changing zoom:

- recalculates contentSize  
- updates clamping boundaries  
- preserves current pan offset (clamped if needed)

### 3.4 Hitboxes

- LayoutCalculator must use the new zoomLevel.
- Hitboxes must scale correctly.
- No drift between visual key and touch target.

### 3.6 No Pinch‑Zoom

- Pinch‑zoom remains permanently removed.
- ComposeZoomPan remains deleted.

---

## 4. Architecture Changes

### 4.1 Settings Model

Update `KeyboardSettings`:

```
data class KeyboardSettings(
    val zoomLevel: Float = 1f,
    ...
)
```

### 4.2 Settings Use Cases

Add:

```
fun setZoomLevel(level: Float)
```

### 4.3 DataStoreSettingsRepository

Add persistence for `zoomLevel`.

### 4.4 KeyboardViewModel

- Observe zoomLevel from settingsFlow.
- Update `KeyboardState.zoomLevel` reactively.
- Add `setZoomLevel(level: Float)` → calls SettingsUseCases.setZoomLevel().

### 4.5 KeyboardState

Ensure:

```
val zoomLevel: Float
```

is updated from settings.

### 4.6 SettingsPanel UI

Add:

- Slider for zoom
- Preview key

Structure example:

```
Box(
    Modifier
        .fillMaxSize()
        .background(...)
) {
    Column {
        Text("Zoom Level")
        Slider(
            value = state.zoomLevel,
            onValueChange = { viewModel.setZoomLevel(it) },
            valueRange = 1f..3f
        )
        PreviewKey(zoom = state.zoomLevel)
    }
}
```

### 4.7 PanContainer

No structural changes — it already supports dynamic zoom.

Zoom changes must:

- trigger recomposition  
- update contentSize  
- update clamping boundaries  
- clamp currentOffset if needed  

### 4.8 LayoutCalculator

No structural changes — it already uses zoomLevel.

---

## 5. Acceptance Criteria

1. Zoom slider appears in SettingsPanel.
2. Preview key scales smoothly with slider.
3. Keyboard zoom updates instantly when slider moves.
4. Panning boundaries adjust correctly for new zoom.
5. Hitboxes remain accurate at all zoom levels.
7. No pinch‑zoom.
8. IME container remains stable.
9. No jitter, no layout thrashing.

---

## 6. Testing Checklist

- Move slider slowly → keyboard scales smoothly.
- Move slider quickly → keyboard scales without jitter.
- Pan at different zoom levels → clamping correct.
- Type at different zoom levels → hitboxes correct.
- Open/close keyboard → zoom persists.
- Rotate device (if supported) → zoom persists.
- SettingsPanel preview matches actual keyboard size.
- SettingsPanel fills entire screen.

---

## 7. Cline Implementation Prompt

REFERENCE DOCUMENTS:
- specs/009-feature_zoom_slider_and_preview.md
- architecture/architecture.md
- KeyboardScreen.kt
- SettingsPanel.kt
- KeyboardViewModel.kt
- KeyboardState.kt
- KeyboardSettings.kt
- SettingsUseCases.kt
- DataStoreSettingsRepository.kt
- PanContainer.kt
- LayoutCalculator.kt

TASK:
Implement Feature 009 exactly as defined in specs/009-feature_zoom_slider_and_preview.md.

REQUIRED OUTPUTS:
- Updated architecture.md  
- Updated SettingsPanel.kt  
- Updated KeyboardSettings.kt  
- Updated SettingsUseCases.kt  
- Updated DataStoreSettingsRepository.kt  
- Updated KeyboardViewModel.kt  
- Updated KeyboardState.kt  
- Updated PanContainer.kt (if needed)  
- Any minor LayoutCalculator adjustments if required  

RULES:
- No new modules  
- Keep changes minimal and additive  
- Do not reintroduce pinch‑zoom  
- SettingsPanel must fill the entire screen  
- Default zoom must be 1×  
- architecture.md MUST be updated to reflect all changes  

~~~~markdown

---

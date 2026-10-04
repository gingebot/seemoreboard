# Feature Spec: Keyboard Layout & Appearance Improvements (Feature #7)

## 1. Overview

This feature updates the keyboard layout, key shapes, spacing, and visual appearance to
match a modern, low‑vision‑friendly design. It introduces rectangular keys with curved
corners, adaptive spacing, center‑justified rows, a wide space bar, and a refined bottom
row layout. It also adds a disabled emoji key placeholder.

This feature does not add new functional behaviour. It strictly improves layout and
appearance.

---

## 2. Goals

- Improve readability and touch accuracy for low‑vision users.
- Introduce distinct rectangular keys with curved corners.
- Center‑justify all rows across full keyboard width.
- Add missing keys (Shift, Backspace, Emoji, Return).
- Implement a wide space bar that expands to fill remaining space.
- Ensure keys scale uniformly under zoom/pan.
- Improve visual clarity with borders, shadows, and contrast.
- Maintain full IME correctness and Compose architecture.

---

## 3. Updated Keyboard Layout

### 3.1 Row 1
Q  W  E  R  T  Y  U  I  O  P

### 3.2 Row 2
A  S  D  F  G  H  J  K  L

### 3.3 Row 3
Shift   Z  X  C  V  B  N  M   ⌫

- Shift at far left  
- Backspace at far right  

### 3.4 Row 4
123   ⚙   🙂   [SPACE BAR]   .   ⏎
- Wide space bar  
- Space bar expands to fill remaining space  
- Emoji key present but disabled  
- Return key uses ⏎ symbol  

---

## 4. Key Appearance

### 4.1 Shape
- Rectangular keys  
- Medium corner radius (8dp)  
- Thin border (1dp dark grey)  
- Subtle drop shadow  

### 4.2 Colors
- Key background: **Black**  
- Key text: **White**  
- Keyboard background: **Slightly lighter grey**  
- High contrast mode: same palette for now  

### 4.3 Spacing
- Adaptive spacing between keys  
- Equal spacing horizontally  
- Rows center‑justified across full width  

### 4.4 Press Behaviour
- On press, the key inverts:
  - Background: White
  - Text: Black
  - Border: Black
  - Shadow: reduced or removed
  - Scale: 1.05

### 4.5 Scaling
- Keys scale uniformly  
- Keys scale as a group  
- Spacing adapts proportionally  

---

## 5. Interaction Behaviour

### 5.1 Zoom/Pan
- Entire keyboard surface scales together  
- Keys maintain shape, spacing, and proportions  
- No per‑key scaling differences  

### 5.2 Touch Targets
- Minimum 48dp x 48dp  
- Increase with zoom  
- Borders + shadows improve visibility  

### 5.3 Disabled Emoji Key
- Present visually  
- Emits no action  
- Does not open a panel  
- Does not affect layout  

---

## 6. Architecture Impact

### 6.1 Updated Files
- `ui/keyboard/KeyboardScreen.kt`
- `ui/components/KeyButton.kt`
- `ui/components/SettingsPanel.kt`
- `ui/gestures/ComposeZoomPan.kt`
- `domain/layout/LayoutCalculator.kt`

### 6.2 No New Modules
All changes remain within existing architecture.

---

## 7. Acceptance Criteria

### 7.1 Layout
- Rows are center‑justified.
- Keys have rectangular shapes with curved corners.
- Adaptive spacing works at all zoom levels.
- Space bar expands to fill remaining space.
- Emoji key present but disabled.

### 7.2 Appearance
- Borders visible.
- Shadows visible.
- Colors correct.
- Text centered.
- No clipped text.
- No overlapping keys.

### 7.3 Behaviour
- Keys scale uniformly under zoom.
- Hitboxes match visual key shapes.
- No gesture interference.
- No IME regressions.

---

## 8. Testing Instructions

1. Open keyboard in multiple apps.
2. Verify layout matches spec.
3. Test zoom/pan at all levels.
4. Verify hitboxes match key shapes.
5. Test settings panel.
6. Test shift + symbols.
7. Test performance at max zoom.
8. Verify disabled emoji key does nothing.

---

## 9. Cline Implementation Plan (Summary)

1. Update KeyboardLayout definitions.
2. Update KeyboardScreen row rendering.
3. Implement new KeyButton shape (rounded rectangle).
4. Add borders + shadows.
5. Implement adaptive spacing + center justification.
6. Implement wide space bar.
7. Add disabled emoji key.
8. Update LayoutCalculator for new hitboxes.
9. Test across apps.

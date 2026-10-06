# Manual Testing Session - Feature 008: Smooth Zoom

## Device Information
**Device:** SM-A566B - 16 (connected via ADB)
**Package:** com.lowvision.keyboard
**Version:** 1.0
**Build:** Debug
**Installation Date:** $(date)

## Installation Status
✅ APK built successfully
✅ Installed on device: 192.168.0.239:40253
✅ IME service registered: com.lowvision.keyboard/.ime.service.LowVisionKeyboardService

## How to Test

### Step 1: Enable the Keyboard
1. On device, go to: **Settings → System → Languages & input → On-screen keyboard**
2. Enable "Low Vision Keyboard"
3. Set as default or select when typing

### Step 2: Open Test Field
1. Open any app with text input (Notes, Messages, Chrome)
2. Tap a text field to show keyboard
3. If needed, tap keyboard switcher icon and select "Low Vision Keyboard"

### Step 3: Test Focal Point Zoom

**Test 3.1: Center Zoom**
- Place two fingers at center of keyboard (around G/H keys)
- Pinch outward slowly
- **Expected:** G and H keys stay under your fingers
- **Expected:** Smooth zoom with no jitter
- Result: [ ]

**Test 3.2: Top-Left Corner Zoom**
- Reset zoom to 1.0 (pinch fully inward)
- Place two fingers at top-left (Q/W keys)
- Pinch outward
- **Expected:** Q and W keys stay under your fingers (not jumping to center)
- Result: [ ]

**Test 3.3: Bottom-Right Corner Zoom**
- Reset zoom
- Place fingers at bottom-right (around M/. keys)
- Pinch outward
- **Expected:** Bottom-right area stays stable
- Result: [ ]

**Test 3.4: Left Edge Zoom**
- Reset zoom
- Place fingers along left edge (A/Z area)
- Pinch outward
- **Expected:** Left edge stays visible and stable
- Result: [ ]

### Step 4: Test Smoothness

**Test 4.1: Slow Zoom**
- Very slowly pinch in and out
- **Expected:** Smooth motion, no visible steps or jumps
- Result: [ ]

**Test 4.2: Micro-movements**
- Place fingers in pinch position
- Make tiny movements (< 1mm) without zooming intent
- **Expected:** Keyboard stays stable, no jitter
- Result: [ ]

**Test 4.3: Rapid Zoom Changes**
- Rapidly pinch in and out several times
- **Expected:** Stable, no crashes, recovers smoothly
- Result: [ ]

### Step 5: Test Bounds

**Test 5.1: Minimum Zoom**
- Zoom in to ~1.5x
- Pan around
- Zoom out fully (pinch inward)
- **Expected:** Zoom stops at 1.0x, keyboard centered, offsets = 0
- Result: [ ]

**Test 5.2: Maximum Zoom**
- Zoom in as much as possible
- **Expected:** Zoom stops at 2.5x, cannot zoom further
- Result: [ ]

**Test 5.3: Pan Bounds - Up**
- Zoom to 2.0x
- Try to pan upward past top edge
- **Expected:** Cannot show blank space above keyboard
- Result: [ ]

**Test 5.4: Pan Bounds - Down**
- Try to pan downward past bottom
- **Expected:** Cannot show blank space below keyboard
- Result: [ ]

**Test 5.5: Pan Bounds - Left/Right**
- Try to pan left and right past edges
- **Expected:** No blank space on either side
- Result: [ ]

### Step 6: Test Integration

**Test 6.1: Key Taps When Zoomed**
- Zoom to 1.8x
- Pan to different areas
- Tap letters, space, backspace
- **Expected:** Correct characters appear in text field
- Result: [ ]

**Test 6.2: Shift Key**
- While zoomed, tap Shift
- Tap a letter
- **Expected:** Uppercase letter appears
- Result: [ ]

**Test 6.3: Layer Switch**
- While zoomed, tap "123?" to switch to symbols
- **Expected:** Zoom level maintained
- Tap "ABC" to return
- **Expected:** Zoom still maintained
- Result: [ ]

**Test 6.4: Settings**
- While zoomed, tap settings gear icon
- Toggle haptics on/off
- Close settings
- **Expected:** Returns to zoomed keyboard, zoom maintained
- Result: [ ]

**Test 6.5: Zoom + Pan Combined**
- Zoom to 1.5x
- Pan to upper-left
- Zoom to 2.0x (should maintain relative position)
- Pan to lower-right
- Zoom out to 1.2x
- **Expected:** All operations work smoothly together
- Result: [ ]

### Step 7: Comparison Test

**Test 7.1: vs Android Magnifier**
1. Enable Android accessibility magnifier:
   - Settings → Accessibility → Magnification → Enable
2. Use magnifier on keyboard
3. Note the zoom behavior (focal point, smoothness)
4. Disable magnifier
5. Use keyboard's built-in zoom
6. Compare:
   - Smoothness: [ Similar / Better / Worse ]
   - Focal point accuracy: [ Similar / Better / Worse ]
   - Responsiveness: [ Similar / Better / Worse ]
   - Overall quality: [ Similar / Better / Worse ]

## Issues Found

### Critical Issues
(Issues that prevent use or break core functionality)
- [ ] None found

### Major Issues  
(Issues that significantly impact usability)
- [ ] None found

### Minor Issues
(Issues that are noticeable but don't prevent use)
- [ ] None found

## Overall Assessment

**Smoothness:** [ Pass / Fail ]
**Focal Point Accuracy:** [ Pass / Fail ]
**Bounds Enforcement:** [ Pass / Fail ]
**Integration:** [ Pass / Fail ]
**Overall:** [ Pass / Fail ]

**Ready for Production:** [ Yes / No / Needs Fixes ]

## Notes

(Add any additional observations, edge cases, or suggestions)

---

**Tester:** ________________
**Date:** ________________
**Time Spent:** ________________

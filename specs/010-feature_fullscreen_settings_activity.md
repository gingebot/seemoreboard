# Feature 010 — Full‑Screen Settings Activity + Android Settings Integration

## 1. Overview

Feature 010 introduces a **dedicated full‑screen Settings Activity** that replaces the IME‑embedded settings panel. This aligns the keyboard with modern Android keyboard design patterns (Gboard, Samsung Keyboard, SwiftKey), improves accessibility, and removes IME window constraints.

Additionally, this feature wires the new Settings Activity into **Android system settings**, so that:

**Settings → General Management → Keyboard List → Low Vision Keyboard → “Keyboard settings”**

will launch the same full‑screen Settings Activity.

This ensures:

- magnifier support  
- TalkBack focus navigation  
- large text mode  
- consistent UX with other keyboards  
- future‑proof settings architecture  

---

## 2. Goals

- Create a new full‑screen Settings Activity.
- Move all settings UI (including Feature 009 zoom slider + preview) into this Activity.
- Replace IME‑embedded SettingsPanel with a simple “open settings” action.
- Wire SettingsActivity into Android system settings via manifest metadata.
- Ensure accessibility tools work normally (magnifier, TalkBack, large text).
- Ensure IME remains stable and lightweight.
- Ensure settings persist via DataStore as before.
- Ensure navigation back to IME is seamless.

---

## 3. Behaviour Requirements

### 3.1 Launching Settings from IME

- When the user taps the settings key (⚙), the IME launches SettingsActivity.
- IME uses:
  ```
  Intent.FLAG_ACTIVITY_NEW_TASK
  ```
  because IME is not an Activity.

### 3.2 Launching Settings from Android System Settings

Android Settings must launch the same SettingsActivity.

This requires:

```
<meta-data
    android:name="android.view.im.settingsActivity"
    android:value="com.lowvision.keyboard.ui.settings.SettingsActivity" />
```

inside the IME service declaration.

### 3.3 Settings Activity UI

- Must use Jetpack Compose.
- Must fill the entire screen (`Modifier.fillMaxSize()`).
- Must include:
  - Zoom slider (Feature 009)
  - Preview key (Feature 009)
  - Haptics toggle
  - High‑contrast toggle
  - Future settings (IME height, themes, audio feedback)
- Must scroll if content exceeds screen height.

### 3.4 Accessibility

- Magnifier must work.
- TalkBack focus navigation must work.
- Large text mode must work.
- No IME‑window constraints apply.

### 3.5 Persistence

- SettingsActivity uses:
  - `KeyboardSettings`
  - `SettingsUseCases`
  - `DataStoreSettingsRepository`
- All settings update immediately and propagate to IME via Flow.

### 3.6 IME Integration

- IME no longer hosts SettingsPanel.
- IME only triggers:
  ```
  context.startActivity(Intent(context, SettingsActivity::class.java)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
  ```

---

## 4. Architecture Changes

### 4.1 New File: SettingsActivity.kt

Location:

```
app/src/main/kotlin/com/lowvision/keyboard/ui/settings/SettingsActivity.kt
```

Responsibilities:

- Full‑screen settings UI.
- Compose content.
- Hosts all settings controls.
- Observes settingsFlow.
- Calls SettingsUseCases to update settings.

### 4.2 Remove IME‑embedded SettingsPanel

- Delete or deprecate `SettingsPanel.kt`.
- Remove `isSettingsOpen` from KeyboardState.
- Remove settings overlay logic from KeyboardScreen.

### 4.3 Update KeyboardScreen

Replace settings overlay with:

```
onOpenSettings = { viewModel.openSettingsActivity() }
```

### 4.4 Update KeyboardViewModel

Add:

```
fun openSettingsActivity() {
    context.startActivity(
        Intent(context, SettingsActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}
```

### 4.5 AndroidManifest Integration

Inside the `<service>` declaration for `LowVisionKeyboardService`:

```
<meta-data
    android:name="android.view.im.settingsActivity"
    android:value="com.lowvision.keyboard.ui.settings.SettingsActivity" />
```

Also ensure:

```
<activity
    android:name=".ui.settings.SettingsActivity"
    android:exported="true" />
```

### 4.6 Update architecture.md

Must update:

- UI Layer → Settings now lives in a full‑screen Activity.
- Settings Flow → IME launches SettingsActivity.
- Module Structure → add ui/settings/ folder.
- Zoom & Pan Flow → settings no longer inside IME.
- Accessibility → magnifier now supported.
- Android Settings Integration → SettingsActivity registered via manifest.
- Roadmap → settings Activity supports future features.

---

## 5. Acceptance Criteria

1. SettingsActivity launches full‑screen from IME.
2. SettingsActivity launches full‑screen from Android system settings.
3. IME no longer displays settings UI.
4. Zoom slider + preview key work inside the Activity.
5. All settings persist via DataStore.
6. Magnifier works.
7. TalkBack works.
8. Large text mode works.
9. IME remains stable.
10. No layout thrashing or IME height changes.

---

## 6. Testing Checklist

- Tap ⚙ → full‑screen settings opens.
- Open Android Settings → “Keyboard settings” → full‑screen settings opens.
- Change zoom → keyboard updates immediately.
- Change haptics → keyboard updates immediately.
- Change high‑contrast → keyboard updates immediately.
- Use magnifier → settings UI magnifies correctly.
- Use TalkBack → settings UI is fully navigable.
- Rotate device → settings UI remains stable.
- Return to IME → keyboard remains stable.

---

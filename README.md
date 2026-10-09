# SeeMoreBoard

SeeMoreBoard is an Android input method (IME) designed for people with low vision. It provides large, high-contrast keyboard keys, configurable zoom and keyboard height, haptic feedback, themes, a custom emoji picker, and contextual editor actions such as Search, Go, Next, Done, and Send.

The application is written in Kotlin with Jetpack Compose and uses Android DataStore for settings persistence.

## Requirements

- JDK 17
- Android SDK with API 34 installed
- An Android device or emulator for IME and instrumentation testing
- Android Debug Bridge (`adb`) for installation and device validation
- [Task](https://taskfile.dev/) 3.x is recommended for common workflows

The repository includes the Gradle wrapper, so Gradle does not need to be installed separately.

## Common commands

Task is the preferred command interface:

```bash
task                 # Standard validation: tests, lint, build, diff check
task build           # Assemble the debug APK
task test            # Run JVM unit tests
task lint            # Run Android lint
task check           # Run the complete local validation suite
task connected-test  # Run instrumentation tests on a connected device
task install         # Build and install the debug APK
task deploy          # Alias for build and install
task logcat          # Stream SeeMoreBoard logs
task clean           # Remove build outputs
task release         # Assemble the release APK
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

When more than one device is connected, select one with:

```bash
task install ADB_DEVICE=-s\ SERIAL
task logcat ADB_DEVICE=-s\ SERIAL
```

Equivalent Gradle commands are available directly:

```bash
./gradlew testDebugUnitTest
./gradlew lint
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

## Installing and enabling the IME

After `task install`:

1. Open Android Settings → System/General management → Keyboard or Input methods.
2. Enable **SeeMoreBoard**.
3. Select SeeMoreBoard as the current/default keyboard.
4. Open keyboard settings using the keyboard settings key, or use the system keyboard settings entry where supported.

If SeeMoreBoard is not the active keyboard, its settings page shows a **Tap to enable** button. Tapping it opens Android’s input-method picker; Android requires the user to confirm the keyboard selection.

The IME subtype is configured as **English (UK)**. Device manufacturers may place input-method settings in different locations, and some Samsung versions do not launch the settings activity through the standard IME metadata entry.

The APK also exposes `SeeMoreBoard Settings` as its Android launcher activity. This allows cloud testing services that require a traditional `MAIN`/`LAUNCHER` activity to install and launch the APK. Launching SeeMoreBoard opens the settings screen; it does not display the keyboard by itself. To test the keyboard, enable and select SeeMoreBoard as the active input method, then focus a text field in a host application.

For IME-focused device testing, the active input method can also be selected with ADB:

```bash
adb shell ime list -s
adb shell ime enable com.lowvision.keyboard/.ime.service.SeeMoreBoardService
adb shell ime set com.lowvision.keyboard/.ime.service.SeeMoreBoardService
```

## Project structure

```text
app/src/main/kotlin/com/lowvision/keyboard/
├── data/                    # DataStore settings repository
├── domain/                  # Layout and settings models/use cases
├── ime/
│   ├── integration/         # InputConnection bridge
│   └── service/             # InputMethodService entry point
├── ui/
│   ├── components/          # Shared Compose key components
│   ├── keyboard/            # Interactive IME keyboard and state
│   ├── settings/            # Settings activity and non-interactive preview
│   └── gestures/            # Production keyboard pan/zoom container
└── utils/
```

Important entry points:

- `SeeMoreBoardService` — Android IME service.
- `KeyboardScreen` — interactive production keyboard.
- `KeyboardPreview` — clipped, non-interactive settings preview.
- `LayoutCalculator` — shared keyboard layout source.
- `InputConnectionBridge` — text, deletion, Enter, and editor-action integration.
- `SettingsActivity` — persisted keyboard settings UI.

## Architecture and documentation

- [Architecture](docs/architecture.md)
- [Build and deploy notes](docs/build_deploy_debug.md)
- [Feature specifications](specs/)

The Android namespace and application ID remain `com.lowvision.keyboard` for package and upgrade stability. `SeeMoreBoard` is the project and user-facing product name.

## Testing and device validation

The normal development loop is:

```bash
task check
task deploy
```

Then manually verify keyboard rendering, zoom, pan, keyboard-height changes, contextual editor actions, settings persistence, TalkBack, magnification, rotation, and different editor types.

Generated files under `build/` and `.gradle/` are not source files and should not be edited or committed.
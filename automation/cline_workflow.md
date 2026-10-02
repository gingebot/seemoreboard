# Cline Automation Workflow
Automated build → install → debug loop for Android development inside WSL.

---

## Full Automation Script

```bash
# Build the app
./gradlew assembleDebug

# Install on device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Start logcat
adb logcat

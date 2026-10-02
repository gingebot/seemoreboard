# Build, Deploy, and Debug Android Apps from WSL

This document describes how to build your Android app inside WSL, install it on your physical device, and debug it — all without Android Studio.

---

## Build the App

Run Gradle:

```bash
./gradlew assembleDebug
```

APK output location:
```
app/build/outputs/apk/debug/app-debug.apk
```
Install the APK on Your Device
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
-r replaces the existing installation.

Debug Logs
Stream all logs:

```bash
adb logcat
```

Filter logs:

```bash
adb logcat | grep MyApp
```
This is ideal for automated debugging loops.
You are implementing a feature according to the specification provided.

Rules:
- Follow the spec exactly unless I explicitly say otherwise.
- Modify existing files when appropriate.
- Create new files when needed.
- After implementing changes, run:
    ./gradlew assembleDebug
- If the build fails, fix the errors and rebuild.
- After a successful build, run:
    adb install -r app/build/outputs/apk/debug/app-debug.apk
- Then run:
    adb logcat
- Watch for crashes, warnings, or incorrect behaviour.
- When I describe issues, refine the implementation and repeat the loop.

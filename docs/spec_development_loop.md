# Hybrid Spec‑Driven Development Loop
This document describes the hybrid spec‑driven workflow used in this project.  
It combines structured feature specifications with rapid AI‑assisted iteration using Cline and DeepSeek.

The goal is to produce predictable, high‑quality features while maintaining fast development cycles.

---

## Overview

Hybrid spec‑driven development blends two approaches:

- **Spec‑Driven:**  
  Each feature begins with a clear written specification that defines intent, behaviour, constraints, and acceptance criteria.

- **Iterative Refinement:**  
  Cline and DeepSeek generate the initial implementation, then refine it through rapid build → deploy → debug cycles based on real device testing.

This workflow ensures clarity and consistency while keeping development fast and flexible.

---

## The Development Loop

### 1. Write a Feature Spec
Each feature starts with a short but structured specification.

A good spec includes:

- **Feature name**
- **User story**
- **Acceptance criteria**
- **UI behaviour**
- **Data model changes**
- **Edge cases**
- **Non‑functional constraints** (performance, security, offline behaviour)

Example:

Feature: User Login

User story:
As a user, I want to log in using email + password so I can access my account.

Acceptance criteria:

- Email and password fields
- Client-side validation
- Error message for invalid credentials
- Successful login stores JWT token
- Navigates to dashboard

Edge cases:

- Empty fields
- Invalid email format
- Network failure


Specs live in:  
`specs/<feature-name>.md`

---

### 2. Provide the Spec to Cline
Paste the spec into Cline and instruct:

> Implement this feature according to the specification.  
> Respect acceptance criteria and edge cases.  
> Modify existing files as needed.

Cline will:

- Generate UI components  
- Add logic  
- Create new files  
- Update navigation  
- Add API calls  
- Modify Gradle if required  

---

### 3. Build the App
Cline runs:

```bash
./gradlew assembleDebug
```

If the build fails:

- Cline reads the error
- Fixes the code
- Rebuilds automatically

This continues until the build succeeds.

### 4. Deploy to Device
Cline installs the APK:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Your physical Android device now runs the updated feature.

### 5. Test on Real Hardware
Interact with the app normally.

Meanwhile, Cline streams logs:

```bash
adb logcat
```

Cline monitors for:

- Crashes
- Exceptions
- Incorrect behaviour
- UI issues
- Network errors

# 6. Refine the Implementation
You describe issues or improvements:

>The login button doesn’t disable during loading.
>The error message doesn’t appear.
>The API call needs a timeout.
>The UI spacing is off.

Cline updates the code, rebuilds, redeploys, and retests.

This loop continues until the feature meets the spec.

### 7. Lock the Feature
Once the feature is correct:

- Update the spec if needed
- Commit the code
- Move to the next feature

The spec becomes the authoritative description of the feature.
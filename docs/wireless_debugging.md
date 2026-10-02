# Wireless Debugging (Android + WSL)

Wireless debugging allows you to build, install, and debug Android apps directly from WSL without using Android Studio or USB cables. This workflow pairs perfectly with VS Code, Cline, and DeepSeek.

---

## 1. Enable Developer Options on Your Android Device

Developer Options are hidden by default. You unlock them by tapping your phone’s build number several times.

### Steps

1. Open **Settings**
2. Scroll down and tap **About phone**
3. Locate **Build number**
   - On Samsung: **Settings → About phone → Software information → Build number**
   - On Xiaomi: **Settings → About phone → MIUI version**
   - On Huawei: **Settings → About phone → Software version**
4. Tap **Build number** **7 times**
5. Enter your device PIN if prompted
6. You should see: **“You are now a developer!”**

### Access Developer Options

Developer Options now appear under:

- **Settings → System → Developer options**  
  or  
- **Settings → Developer options** (Samsung)

If the menu doesn’t appear, restart your phone.

---

## 2. Enable Wireless Debugging

Inside **Developer options**:

1. Scroll to **Debugging**
2. Enable **USB debugging**  
   > Required even for wireless debugging
3. Enable **Wireless debugging**
4. Tap **Wireless debugging**
5. Tap **Pair device with pairing code**

Your phone will display:

- **IP address & port** (e.g., `192.168.1.42:37099`)
- **Pairing port** (e.g., `45629`)
- **Pairing code** (e.g., `123456`)

Keep this screen open.

---

## 3. Pair Your Device from WSL

Use the pairing port shown on your phone:

```bash
adb pair <phone-ip>:<pairing-port>
```

Example:

```bash
adb pair 192.168.1.42:45629
```
Enter the pairing code when prompted.

## 3. Connect for Debugging
```bash
adb connect <phone-ip>:<debug-port>
```
Example:

```bash
adb connect 192.168.1.42:37099
```

## 4. Verify Connection
```bash
adb devices
```
Expected output:

```Code
192.168.1.42:37099   device
```

Your phone is now fully accessible from WSL.
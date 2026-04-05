# RadioGolha Mobile Runbook

This document explains how to open the Android phone emulator and run the debug build for the Kotlin Multiplatform mobile app in this repository.

---

## Project Location

- Mobile project root: `mobile/`
- Android app module: `mobile/composeApp/`
- Debug APK output: `mobile/composeApp/build/outputs/apk/debug/composeApp-debug.apk`

---

## Local Tooling Assumptions

This runbook matches the current machine setup:

- Android Studio is installed at:
  - `/Users/espitman/Applications/Android Studio.app`
- Android SDK is installed at:
  - `/Users/espitman/Library/Android/sdk`
- The project uses the JetBrains Runtime bundled with Android Studio:
  - `/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home`

If your paths differ, adjust `JAVA_HOME`, `ANDROID_HOME`, and `ANDROID_SDK_ROOT`.

---

## 1. Build The Debug APK

From the repo root:

```bash
cd /Users/espitman/Documents/Projects/radioGolha/mobile

export JAVA_HOME="/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

./gradlew :composeApp:assembleDebug
```

Expected output APK:

```text
/Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

---

## 2. List Available Emulators

```bash
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

"$HOME/Library/Android/sdk/emulator/emulator" -list-avds
```

Current known phone AVD on this machine:

```text
Medium_Phone_API_36.0
```

---

## 3. Start The Phone Emulator

```bash
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

"$HOME/Library/Android/sdk/emulator/emulator" \
  -avd Medium_Phone_API_36.0 \
  -gpu swiftshader_indirect \
  -no-snapshot-load
```

Notes:

- `-gpu swiftshader_indirect` is a safe option for this machine.
- `-no-snapshot-load` avoids bad cached emulator state during development.

---

## 4. Wait For Boot Completion

In another terminal:

```bash
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

"$HOME/Library/Android/sdk/platform-tools/adb" devices -l
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 shell getprop sys.boot_completed
```

When boot is complete:

- `adb devices -l` shows `emulator-5554 device`
- `getprop sys.boot_completed` returns `1`

---

## 5. Install The Debug APK

```bash
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

"$HOME/Library/Android/sdk/platform-tools/adb" \
  -s emulator-5554 \
  install -r -t \
  /Users/espitman/Documents/Projects/radioGolha/mobile/composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

Expected result:

```text
Performing Streamed Install
Success
```

---

## 6. Launch The App

```bash
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

"$HOME/Library/Android/sdk/platform-tools/adb" \
  -s emulator-5554 \
  shell am start -W -n com.radiogolha.mobile/.MainActivity
```

Expected result:

```text
Status: ok
Activity: com.radiogolha.mobile/.MainActivity
Complete
```

---

## 7. Verify The App Is In Foreground

```bash
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

"$HOME/Library/Android/sdk/platform-tools/adb" \
  -s emulator-5554 \
  shell dumpsys activity top | rg "com.radiogolha.mobile|MainActivity"
```

Expected match:

```text
ACTIVITY com.radiogolha.mobile/.MainActivity
```

---

## One-Shot Flow

If you want the shortest repeatable debug flow:

```bash
cd /Users/espitman/Documents/Projects/radioGolha/mobile

export JAVA_HOME="/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="/Users/espitman/Library/Android/sdk"
export ANDROID_SDK_ROOT="/Users/espitman/Library/Android/sdk"

./gradlew :composeApp:assembleDebug
"$HOME/Library/Android/sdk/emulator/emulator" -avd Medium_Phone_API_36.0 -gpu swiftshader_indirect -no-snapshot-load
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 install -r -t ./composeApp/build/outputs/apk/debug/composeApp-debug.apk
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 shell am start -W -n com.radiogolha.mobile/.MainActivity
```

---

## Android Studio Option

You can also:

1. Open `mobile/` in Android Studio
2. Select the `Medium_Phone_API_36.0` emulator
3. Run the `composeApp` Android configuration

This is convenient for iterative UI work, but the CLI steps above are the canonical reproducible flow.

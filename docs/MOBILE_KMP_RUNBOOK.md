# RadioGolha Mobile Runbook

This document explains how to open the Android phone emulator and run the debug build for the Kotlin Multiplatform mobile app in this repository.

Canonical rule for this repo:

- Every time Codex runs the mobile app on the emulator, it should use `adb install -r` to preserve user data (like favorites).
- The app now automatically detects changes in the bundled `golha_database.db` by comparing file sizes, so a full uninstall is no longer required to refresh the archive.

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

## 5. Install/Update The Debug APK

Use `install -r` to update the app while preserving user data (like favorites stored in `user_data.db`).

// Skip uninstall to preserve favorites

Then install the debug APK:

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

Why this is better:

- Standard `install -r` preserves the `user_data.db` where favorites are stored.
- The app now has logic in `HomeDataLoader.android.kt` to Refresh the `golha_database.db` asset if its size changes, ensuring the archive is always up-to-date without wiping user data.

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

## 8. Update The Database Without Reinstall

Debug builds now include a database import tool under `حساب من`.

How it works:

- Push a fresh `golha_database.db` into the app's external debug path.
- Open the app.
- Go to `حساب من`.
- Tap `دریافت دیتابیس جدید`.
- The app copies that database into internal storage and refreshes the in-app data without reinstall.

Push command:

```bash
cd /Users/espitman/Documents/Projects/radioGolha
make mobile-push-db
```

The path shown inside the app should match:

```text
/storage/emulated/0/Android/data/com.radiogolha.mobile/files/golha_database.db
```

Notes:

- This import tool is intended for `debug` builds only.
- This flow is the preferred way to test database-only changes quickly.
- Reinstall is still the canonical path when you also need a new APK or bundled asset changes.
- `make mobile-push-db` uses `database/golha_database.db` and pushes it to `emulator-5554` by default.
- If needed, you can override the target device:

```bash
cd /Users/espitman/Documents/Projects/radioGolha
make mobile-push-db ANDROID_DEVICE=emulator-5554
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
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 install -r -t ./composeApp/build/outputs/apk/debug/composeApp-debug.apk
"$HOME/Library/Android/sdk/platform-tools/adb" -s emulator-5554 shell am start -W -n com.radiogolha.mobile/.MainActivity
```

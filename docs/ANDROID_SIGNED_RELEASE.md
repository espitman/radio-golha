# Android Signed Release

This document describes the exact local process for building a signed Android release APK for the RadioGolha mobile app.

## Prerequisites

- Android SDK installed on macOS
- Android build-tools available under `$HOME/Library/Android/sdk/build-tools`
- A working JDK with `keytool`
- Rust toolchain installed

In this workspace, `keytool` is available at:

```bash
/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/keytool
```

## Local Signing Files

Keep signing material local only. Do not commit it.

Recommended local paths:

- Keystore: `mobile/signing/radiogolha-release.jks`
- Signing properties: `mobile/signing/release-signing.properties`

Example `mobile/signing/release-signing.properties`:

```properties
storeFile=/absolute/path/to/mobile/signing/radiogolha-release.jks
storePassword=your-store-password
keyAlias=radiogolha-release
keyPassword=your-key-password
```

## Create The Keystore

Run:

```bash
export JAVA_HOME="/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home"
"$JAVA_HOME/bin/keytool" \
  -genkeypair \
  -v \
  -keystore mobile/signing/radiogolha-release.jks \
  -alias radiogolha-release \
  -keyalg RSA \
  -keysize 2048 \
  -validity 3650
```

## Build The Release APK

Run from `mobile/`:

```bash
export JAVA_HOME="/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew --no-daemon :composeApp:assembleRelease
```

This produces the unsigned APK at:

```text
mobile/composeApp/build/outputs/apk/release/composeApp-release-unsigned.apk
```

## Align And Sign The APK

Use the newest available Android build-tools. Example with `36.0.0`:

```bash
ZIPALIGN="$HOME/Library/Android/sdk/build-tools/36.0.0/zipalign"
APKSIGNER="$HOME/Library/Android/sdk/build-tools/36.0.0/apksigner"

$ZIPALIGN -f -p 4 \
  mobile/composeApp/build/outputs/apk/release/composeApp-release-unsigned.apk \
  mobile/composeApp/build/outputs/apk/release/composeApp-release-aligned.apk

$APKSIGNER sign \
  --ks mobile/signing/radiogolha-release.jks \
  --ks-key-alias radiogolha-release \
  mobile/composeApp/build/outputs/apk/release/composeApp-release-aligned.apk
```

## Verify The Signature

```bash
$HOME/Library/Android/sdk/build-tools/36.0.0/apksigner verify --verbose --print-certs \
  mobile/composeApp/build/outputs/apk/release/composeApp-release-aligned.apk
```

## Copy To Desktop

```bash
cp mobile/composeApp/build/outputs/apk/release/composeApp-release-aligned.apk \
  "$HOME/Desktop/radiogolha-mobile-release-signed.apk"
```

## Current Notes

- The current Android build pulls the archive database from `database/golha_database.db`.
- The current Rust Android bridge task copies the Rust `debug` shared library into both debug and release APK builds. This does not block producing a signed APK, but it is worth revisiting if you want fully optimized release artifacts later.

#!/bin/bash

set -euo pipefail

PROJECT_ROOT="/Users/espitman/Documents/Projects/radioGolha"
MOBILE_DIR="$PROJECT_ROOT/mobile"
MODULE_DIR="$MOBILE_DIR/composeApp"
SIGNING_PROPS="$MOBILE_DIR/signing/release-signing.properties"
DESKTOP_DIR="$HOME/Desktop"

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
JAVA_HOME="${JAVA_HOME:-/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

echo "🚀 Building signed Android TV release APK..."

if [ ! -x "$MOBILE_DIR/gradlew" ]; then
  echo "❌ Error: gradlew not found at $MOBILE_DIR/gradlew"
  exit 1
fi

if [ ! -f "$SIGNING_PROPS" ]; then
  echo "❌ Error: signing properties not found at $SIGNING_PROPS"
  exit 1
fi

STORE_FILE="$(awk -F= '$1=="storeFile" {print $2}' "$SIGNING_PROPS" | tail -1)"
if [ -z "$STORE_FILE" ] || [ ! -f "$STORE_FILE" ]; then
  echo "❌ Error: release keystore not found. Check storeFile in $SIGNING_PROPS"
  exit 1
fi

BUILD_TOOLS_DIR="$(find "$ANDROID_SDK_ROOT/build-tools" -mindepth 1 -maxdepth 1 -type d 2>/dev/null | sort -V | tail -1)"
APKSIGNER="$BUILD_TOOLS_DIR/apksigner"

if [ ! -x "$APKSIGNER" ]; then
  echo "❌ Error: apksigner not found under $ANDROID_SDK_ROOT/build-tools"
  exit 1
fi

cd "$MOBILE_DIR"
./gradlew --no-daemon :composeApp:assembleTvRelease

APK_PATH="$MODULE_DIR/build/outputs/apk/tv/release/composeApp-tv-release.apk"
if [ ! -f "$APK_PATH" ]; then
  APK_PATH="$(find "$MODULE_DIR/build/outputs/apk/tv/release" -name "*.apk" -type f | sort | tail -1)"
fi

if [ -z "${APK_PATH:-}" ] || [ ! -f "$APK_PATH" ]; then
  echo "❌ Error: TV release APK not found."
  exit 1
fi

echo "🔐 Verifying APK signature..."
"$APKSIGNER" verify --verbose --print-certs "$APK_PATH" >/tmp/radiogolha-tv-release-verify.txt
cat /tmp/radiogolha-tv-release-verify.txt

OUT_APK="$DESKTOP_DIR/radiogolha-tv-release-signed.apk"
cp "$APK_PATH" "$OUT_APK"

echo "✅ Signed Android TV release APK copied to:"
echo "$OUT_APK"

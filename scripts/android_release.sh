#!/bin/bash

set -euo pipefail

PROJECT_ROOT="/Users/espitman/Documents/Projects/radioGolha"
MOBILE_DIR="$PROJECT_ROOT/mobile"
MODULE_DIR="$MOBILE_DIR/composeApp"
DESKTOP_DIR="$HOME/Desktop"

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
JAVA_HOME="${JAVA_HOME:-/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

if [ ! -x "$MOBILE_DIR/gradlew" ]; then
  echo "❌ Error: gradlew not found at $MOBILE_DIR/gradlew"
  exit 1
fi

echo "🚀 Building Android release APK..."
cd "$MOBILE_DIR"
./gradlew :composeApp:assembleRelease

APK_PATH="$MODULE_DIR/build/outputs/apk/release/composeApp-release.apk"
if [ ! -f "$APK_PATH" ]; then
  echo "❌ Error: Release APK not found at $APK_PATH"
  exit 1
fi

TIMESTAMP="$(date +%Y%m%d-%H%M%S)"
OUT_APK="$DESKTOP_DIR/radiogolha-release.apk"
cp "$APK_PATH" "$OUT_APK"

echo "✅ Release APK copied to:"
echo "$OUT_APK"

#!/bin/bash

set -euo pipefail

# Configuration
PROJECT_ROOT="/Users/espitman/Documents/Projects/radioGolha"
MOBILE_DIR="$PROJECT_ROOT/mobile"
APK_PATH="$MOBILE_DIR/composeApp/build/outputs/apk/tv/debug/composeApp-tv-debug.apk"
APP_ID="com.radiogolha.mobile.tv"
MAIN_ACTIVITY="com.radiogolha.mobile.tv.SplashTvActivity"
REQUESTED_AVD="${1:-Android_TV_1080p_API_36}"
AVD_NAME=""

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
ANDROID_HOME="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
JAVA_HOME="${JAVA_HOME:-/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home}"
PATH="$JAVA_HOME/bin:$ANDROID_SDK_ROOT/platform-tools:$PATH"

ADB_BIN="$ANDROID_SDK_ROOT/platform-tools/adb"
EMU_BIN="$ANDROID_SDK_ROOT/emulator/emulator"

echo "🚀 Starting Android TV build and launch process..."

if [ ! -x "$ADB_BIN" ]; then
  echo "❌ Error: adb not found at $ADB_BIN"
  exit 1
fi

if [ ! -x "$EMU_BIN" ]; then
  echo "❌ Error: emulator not found at $EMU_BIN"
  exit 1
fi

if [ ! -x "$MOBILE_DIR/gradlew" ]; then
  echo "❌ Error: gradlew not found at $MOBILE_DIR/gradlew"
  exit 1
fi

SERIAL="$($ADB_BIN devices | awk '$1 ~ /^emulator-/ && $2 == "device" {print $1; exit}')"

if [ -z "$SERIAL" ]; then
  AVAILABLE_AVDS="$("$EMU_BIN" -list-avds || true)"
  if [ -z "$AVAILABLE_AVDS" ]; then
    echo "❌ Error: No Android AVD found."
    echo "Create a TV AVD in Android Studio Device Manager first."
    exit 1
  fi

  if echo "$AVAILABLE_AVDS" | grep -Fxq "$REQUESTED_AVD"; then
    AVD_NAME="$REQUESTED_AVD"
  else
    AVD_NAME="$(echo "$AVAILABLE_AVDS" | grep -Ei 'tv|television' | head -1 || true)"
    if [ -z "$AVD_NAME" ]; then
      AVD_NAME="$(echo "$AVAILABLE_AVDS" | head -1)"
    fi
    echo "⚠️ Requested AVD '$REQUESTED_AVD' not found. Using '$AVD_NAME'."
  fi

  echo "📟 No online emulator found. Booting $AVD_NAME..."
  nohup "$EMU_BIN" -avd "$AVD_NAME" -gpu swiftshader_indirect -no-snapshot-load -no-boot-anim >/tmp/radiogolha_android_tv_emulator.log 2>&1 &

  for _ in $(seq 1 180); do
    SERIAL="$($ADB_BIN devices | awk '$1 ~ /^emulator-/ && $2 == "device" {print $1; exit}')"
    if [ -n "$SERIAL" ]; then
      break
    fi
    sleep 2
  done
fi

if [ -z "${SERIAL:-}" ]; then
  echo "❌ Error: Emulator did not come online in time."
  "$ADB_BIN" devices -l || true
  exit 1
fi

echo "📺 Target Device: $SERIAL"
echo "🕒 Waiting for boot completion..."
"$ADB_BIN" -s "$SERIAL" wait-for-device
for _ in $(seq 1 120); do
  BOOT="$("$ADB_BIN" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')"
  if [ "$BOOT" = "1" ]; then
    break
  fi
  sleep 2
done

HOST_TIME_MS="$(($(date +%s) * 1000))"
echo "🕒 Syncing emulator clock..."
"$ADB_BIN" -s "$SERIAL" shell cmd alarm set-time "$HOST_TIME_MS" >/dev/null 2>&1 || true

echo "🔨 Building Android TV debug APK..."
cd "$MOBILE_DIR"
./gradlew :composeApp:assembleTvDebug

if [ ! -f "$APK_PATH" ]; then
  echo "❌ Error: APK not found at $APK_PATH"
  exit 1
fi

echo "📲 Installing APK (preserve app data with -r)..."
"$ADB_BIN" -s "$SERIAL" install -r -t "$APK_PATH"

echo "🎬 Launching $APP_ID/$MAIN_ACTIVITY..."
"$ADB_BIN" -s "$SERIAL" shell am start -W -n "$APP_ID/$MAIN_ACTIVITY"

echo "✅ Android TV launch succeeded!"

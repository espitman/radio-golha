#!/bin/bash
set -euo pipefail

# Configuration
PROJECT_ROOT="/Users/espitman/Documents/Projects/radioGolha"
IOS_PROJECT_DIR="$PROJECT_ROOT/mobile/iosApp"
BUNDLE_ID="com.radiogolha.ios"
SCHEME="iosApp"
PREFERRED_DEVICE_NAME="${1:-}"

echo "🚀 Starting iOS build and launch process..."

# 1. Identify booted simulator (or boot one if needed)
BOOTED_IDS="$(xcrun simctl list devices booted | grep -E 'iPhone|iPad' | grep 'Booted' | sed -E 's/.*\(([-A-Z0-9]+)\).*/\1/' || true)"
DEVICE_ID="$(echo "$BOOTED_IDS" | head -1 | tr -d '\r')"

if [ -z "$DEVICE_ID" ]; then
    echo "📟 No booted simulator found. Booting one..."

    if [ -n "$PREFERRED_DEVICE_NAME" ]; then
        DEVICE_ID="$(xcrun simctl list devices available | grep -F "$PREFERRED_DEVICE_NAME" | grep '(Shutdown)' | head -1 | sed -E 's/.*\(([-A-Z0-9]+)\).*/\1/' || true)"
    fi

    if [ -z "$DEVICE_ID" ]; then
        DEVICE_ID="$(xcrun simctl list devices available | grep -E 'iPhone|iPad' | grep '(Shutdown)' | head -1 | sed -E 's/.*\(([-A-Z0-9]+)\).*/\1/' || true)"
    fi

    if [ -z "$DEVICE_ID" ]; then
        DEVICE_ID="$(xcrun simctl list devices available | grep -E 'iPhone|iPad' | grep '(Booted)' | head -1 | sed -E 's/.*\(([-A-Z0-9]+)\).*/\1/' || true)"
    fi

    if [ -z "$DEVICE_ID" ]; then
        echo "❌ Error: Could not find any available iOS simulator device."
        exit 1
    fi

    xcrun simctl boot "$DEVICE_ID" >/dev/null 2>&1 || true
    xcrun simctl bootstatus "$DEVICE_ID" -b
    open -a Simulator --args -CurrentDeviceUDID "$DEVICE_ID" >/dev/null 2>&1 || open -a Simulator
else
    # If multiple simulators are already booted, keep one target only.
    if [ "$(echo "$BOOTED_IDS" | sed '/^$/d' | wc -l | tr -d ' ')" -gt 1 ]; then
        while IFS= read -r id; do
            if [ -n "$id" ] && [ "$id" != "$DEVICE_ID" ]; then
                xcrun simctl shutdown "$id" >/dev/null 2>&1 || true
            fi
        done <<< "$BOOTED_IDS"
    fi
fi

echo "📱 Target Device: $DEVICE_ID"

# 2. Build the app
echo "🧹 Cleaning previous builds to ensure fresh state..."
rm -rf ~/Library/Developer/Xcode/DerivedData/iosApp-*
export JAVA_HOME="/Users/espitman/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
cd "$PROJECT_ROOT/mobile"
./gradlew clean > /dev/null

echo "🔨 Pre-building Kotlin framework for iOS Simulator..."
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64 > /dev/null

echo "🛠 Building Xcode project (this might take a minute)..."
cd "$IOS_PROJECT_DIR"
xcodebuild -project iosApp.xcodeproj -scheme "$SCHEME" -destination "id=$DEVICE_ID" clean build > /dev/null

# 3. Get the build path
APP_PATH=$(xcodebuild -project iosApp.xcodeproj -scheme "$SCHEME" -destination "id=$DEVICE_ID" -showBuildSettings | grep CONFIGURATION_BUILD_DIR | awk '{print $3}')/iosApp.app

echo "📦 Found App at: $APP_PATH"

# 4. Install and Launch
echo "📲 Installing app on simulator..."
xcrun simctl install "$DEVICE_ID" "$APP_PATH"

echo "🎬 Launching $BUNDLE_ID..."
xcrun simctl launch "$DEVICE_ID" "$BUNDLE_ID"

echo "✅ Success!"

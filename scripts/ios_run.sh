#!/bin/bash

# Configuration
PROJECT_ROOT="/Users/espitman/Documents/Projects/radioGolha"
IOS_PROJECT_DIR="$PROJECT_ROOT/mobile/iosApp"
BUNDLE_ID="com.radiogolha.ios"
SCHEME="iosApp"

echo "🚀 Starting iOS build and launch process..."

# 1. Identify booted simulator
DEVICE_ID=$(xcrun simctl list devices booted | grep -E 'iPhone|iPad' | grep 'Booted' | head -1 | sed -E 's/.*\(([-A-Z0-9]+)\).*/\1/')

if [ -z "$DEVICE_ID" ]; then
    echo "❌ Error: No booted simulator found. Please start a simulator first."
    exit 1
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

if [ $? -ne 0 ]; then
    echo "❌ Error: Xcode build failed."
    exit 1
fi

# 3. Get the build path
APP_PATH=$(xcodebuild -project iosApp.xcodeproj -scheme "$SCHEME" -destination "id=$DEVICE_ID" -showBuildSettings | grep CONFIGURATION_BUILD_DIR | awk '{print $3}')/iosApp.app

echo "📦 Found App at: $APP_PATH"

# 4. Install and Launch
echo "📲 Installing app on simulator..."
xcrun simctl install "$DEVICE_ID" "$APP_PATH"

echo "🎬 Launching $BUNDLE_ID..."
xcrun simctl launch "$DEVICE_ID" "$BUNDLE_ID"

echo "✅ Success!"

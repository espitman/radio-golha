#!/bin/bash

# Configuration
PROJECT_ROOT="/Users/espitman/Documents/Projects/radioGolha"
BUNDLE_ID="com.radiogolha.ios"

echo "🧹 Resetting iOS App..."

# 1. Identify booted simulator
DEVICE_ID=$(xcrun simctl list devices booted | grep -E 'iPhone|iPad' | grep 'Booted' | head -1 | sed -E 's/.*\(([-A-Z0-9]+)\).*/\1/')

if [ -z "$DEVICE_ID" ]; then
    echo "❌ Error: No booted simulator found. Please start a simulator first."
    exit 1
fi

echo "📱 Target Device: $DEVICE_ID"

# 2. Uninstall
echo "🗑 Uninstalling $BUNDLE_ID to clear data..."
xcrun simctl uninstall "$DEVICE_ID" "$BUNDLE_ID"

# 3. Call the run script
"$PROJECT_ROOT/scripts/ios_run.sh"

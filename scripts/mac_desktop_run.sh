#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="$ROOT_DIR/mac-desktop-app"
cd "$APP_DIR"

# Optional: ./scripts/mac_desktop_run.sh --clean
if [[ "${1:-}" == "--clean" ]]; then
  swift package clean
fi

swift build

BIN_PATH="$(swift build --show-bin-path)/RadioGolhaDesktop"
LOG_PATH="/tmp/radio_golha_desktop.log"

# If already running, restart to pick up latest build.
pkill -f "/RadioGolhaDesktop$" 2>/dev/null || true

nohup "$BIN_PATH" >"$LOG_PATH" 2>&1 &
disown || true

echo "RadioGolhaDesktop started."
echo "Log: $LOG_PATH"

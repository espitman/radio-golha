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
swift run RadioGolhaDesktop

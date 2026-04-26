#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="$ROOT_DIR/tauri-desktop-app"

if [[ ! -d "$APP_DIR" ]]; then
  echo "Tauri desktop app directory not found: $APP_DIR" >&2
  exit 1
fi

if ! command -v pnpm >/dev/null 2>&1; then
  echo "pnpm is required to run the Tauri desktop app." >&2
  exit 1
fi

cd "$APP_DIR"

if [[ ! -d node_modules ]]; then
  echo "Installing Tauri desktop dependencies..."
  pnpm install
fi

echo "Running Tauri desktop app..."
pnpm tauri dev "$@"

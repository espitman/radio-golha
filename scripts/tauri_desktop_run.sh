#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="$ROOT_DIR/tauri-desktop-app"

if [[ ! -d "$APP_DIR" ]]; then
  echo "Tauri desktop app directory not found: $APP_DIR" >&2
  exit 1
fi

cd "$APP_DIR"

if [[ ! -d node_modules ]]; then
  pnpm install
fi

pnpm tauri dev

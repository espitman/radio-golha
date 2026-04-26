#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
APP_DIR="$ROOT_DIR/electron-desktop-app"

cd "$APP_DIR"

if ! command -v pnpm >/dev/null 2>&1; then
  echo "pnpm is required. Install it first."
  exit 1
fi

if [ ! -d node_modules ]; then
  pnpm install
fi

pnpm dev

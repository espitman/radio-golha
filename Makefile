# Golha Database Management

PYTHON = ./scraper/.venv/bin/python3
DB_SCRIPT = scraper/generate_sqlite_db.py
DOWNLOAD_AUDIO_SCRIPT = scraper/download_audio_archive.py
DB_FILE = database/golha_database.db
ANDROID_SDK_ROOT ?= $(HOME)/Library/Android/sdk
ADB = $(ANDROID_SDK_ROOT)/platform-tools/adb
ANDROID_DEVICE ?= emulator-5554
ANDROID_DEBUG_DB_PATH = /sdcard/Android/data/com.radiogolha.mobile/files/golha_database.db

.PHONY: db download-audio mobile-push-db clean help

help:
	@echo "🔍 Golha Project Makefile Commands:"
	@echo "  make db      - Generate SQLite database from all JSON files"
	@echo "  make download-audio - Download archive MP3 files with resumable state"
	@echo "  make mobile-push-db - Push the latest database into the Android debug import path"
	@echo "  make clean   - Remove generating database file"
	@echo "  make help    - Show this help message"

db:
	@echo "🚀 Building Golha Database..."
	@mkdir -p database
	@$(PYTHON) $(DB_SCRIPT)
	@echo "🏆 Database generated at $(DB_FILE)"

download-audio:
	@echo "🎵 Downloading archive audio files..."
	@$(PYTHON) $(DOWNLOAD_AUDIO_SCRIPT)

mobile-push-db:
	@echo "📦 Pushing debug database to Android device $(ANDROID_DEVICE)..."
	@"$(ADB)" -s "$(ANDROID_DEVICE)" push "$(DB_FILE)" "$(ANDROID_DEBUG_DB_PATH)"
	@echo "✅ Database pushed to $(ANDROID_DEBUG_DB_PATH)"
	@echo "➡️  Now open the app, go to حساب من, and tap دریافت دیتابیس جدید"

clean:
	@echo "🗑️  Removing database file..."
	@rm -f $(DB_FILE)
	@echo "✅ Cleaned."

# Golha Database Management

PYTHON = ./scraper/.venv/bin/python3
DB_SCRIPT = scraper/generate_sqlite_db.py
DOWNLOAD_AUDIO_SCRIPT = scraper/download_audio_archive.py
DB_FILE = database/golha_database.db

.PHONY: db download-audio clean help

help:
	@echo "🔍 Golha Project Makefile Commands:"
	@echo "  make db      - Generate SQLite database from all JSON files"
	@echo "  make download-audio - Download archive MP3 files with resumable state"
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

clean:
	@echo "🗑️  Removing database file..."
	@rm -f $(DB_FILE)
	@echo "✅ Cleaned."

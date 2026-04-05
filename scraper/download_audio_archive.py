#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import sqlite3
import sys
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import Any
from urllib.parse import urlparse

import httpx


ROOT_DIR = Path(__file__).resolve().parents[1]
DEFAULT_DB_PATH = ROOT_DIR / "database" / "golha_database.db"
DEFAULT_OUTPUT_DIR = ROOT_DIR / "downloads" / "audio"
DEFAULT_STATE_FILE = DEFAULT_OUTPUT_DIR / "download_state.json"


@dataclass
class ProgramAudio:
    program_id: int
    title: str
    audio_url: str


def utc_now() -> str:
    return datetime.now(timezone.utc).isoformat()


def sanitize_filename(value: str) -> str:
    value = value.strip().replace(" ", "_")
    value = re.sub(r"[^\w\-.]+", "_", value, flags=re.UNICODE)
    return value.strip("._") or "audio"


def is_valid_audio_url(url: str) -> bool:
    normalized = (url or "").strip()
    return normalized.lower().startswith(("http://", "https://")) and normalized.lower().endswith(".mp3")


def build_destination_path(output_dir: Path, item: ProgramAudio) -> Path:
    return output_dir / f"{item.program_id}.mp3"


def load_program_audio_rows(db_path: Path) -> list[ProgramAudio]:
    conn = sqlite3.connect(db_path)
    conn.row_factory = sqlite3.Row
    try:
        rows = conn.execute(
            """
            SELECT id, title, audio_url
            FROM program
            WHERE audio_url IS NOT NULL AND TRIM(audio_url) <> ''
            ORDER BY id ASC
            """
        ).fetchall()
    finally:
        conn.close()

    return [
        ProgramAudio(
            program_id=int(row["id"]),
            title=str(row["title"] or ""),
            audio_url=str(row["audio_url"] or "").strip(),
        )
        for row in rows
    ]


def default_state() -> dict[str, Any]:
    return {
        "version": 1,
        "created_at": utc_now(),
        "updated_at": utc_now(),
        "current": None,
        "stats": {
            "completed": 0,
            "skipped": 0,
            "failed": 0,
        },
        "entries": {},
    }


def load_state(state_path: Path) -> dict[str, Any]:
    if not state_path.exists():
        return default_state()
    try:
        return json.loads(state_path.read_text(encoding="utf-8"))
    except json.JSONDecodeError:
        backup_path = state_path.with_suffix(".corrupt.json")
        state_path.replace(backup_path)
        print(f"State file was corrupt. Moved to {backup_path}", file=sys.stderr)
        return default_state()


def save_state(state_path: Path, state: dict[str, Any]) -> None:
    state["updated_at"] = utc_now()
    state_path.parent.mkdir(parents=True, exist_ok=True)
    tmp_path = state_path.with_suffix(".tmp")
    tmp_path.write_text(json.dumps(state, ensure_ascii=False, indent=2), encoding="utf-8")
    tmp_path.replace(state_path)


def set_entry(state: dict[str, Any], program_id: int, payload: dict[str, Any]) -> None:
    state["entries"][str(program_id)] = payload
    stats = {"completed": 0, "skipped": 0, "failed": 0}
    for entry in state["entries"].values():
        status = entry.get("status")
        if status in stats:
            stats[status] += 1
    state["stats"] = stats


def mark_skipped(state: dict[str, Any], item: ProgramAudio, reason: str, path: Path | None = None) -> None:
    set_entry(
        state,
        item.program_id,
        {
            "status": "skipped",
            "reason": reason,
            "title": item.title,
            "audio_url": item.audio_url,
            "path": str(path) if path else None,
            "updated_at": utc_now(),
        },
    )


def mark_failed(state: dict[str, Any], item: ProgramAudio, reason: str, path: Path | None = None) -> None:
    previous = state["entries"].get(str(item.program_id), {})
    attempts = int(previous.get("attempts", 0)) + 1
    set_entry(
        state,
        item.program_id,
        {
            "status": "failed",
            "reason": reason,
            "attempts": attempts,
            "title": item.title,
            "audio_url": item.audio_url,
            "path": str(path) if path else None,
            "updated_at": utc_now(),
        },
    )


def mark_completed(state: dict[str, Any], item: ProgramAudio, path: Path, bytes_written: int) -> None:
    set_entry(
        state,
        item.program_id,
        {
            "status": "completed",
            "title": item.title,
            "audio_url": item.audio_url,
            "path": str(path),
            "bytes": bytes_written,
            "updated_at": utc_now(),
        },
    )


def should_skip_existing(state: dict[str, Any], item: ProgramAudio, destination: Path) -> bool:
    entry = state["entries"].get(str(item.program_id), {})
    if destination.exists() and destination.stat().st_size > 0:
        if entry.get("status") != "completed":
            mark_completed(state, item, destination, destination.stat().st_size)
        return True
    return entry.get("status") == "completed"


def download_one(
    client: httpx.Client,
    item: ProgramAudio,
    destination: Path,
    state: dict[str, Any],
    state_path: Path,
    chunk_size: int,
) -> None:
    if not is_valid_audio_url(item.audio_url):
        mark_skipped(state, item, "invalid_audio_url", destination)
        save_state(state_path, state)
        print(f"[skip] {item.program_id} invalid url: {item.audio_url}")
        return

    if should_skip_existing(state, item, destination):
        print(f"[skip] {item.program_id} already downloaded")
        save_state(state_path, state)
        return

    destination.parent.mkdir(parents=True, exist_ok=True)
    temp_path = destination.with_suffix(destination.suffix + ".part")
    resume_from = temp_path.stat().st_size if temp_path.exists() else 0

    state["current"] = {
        "program_id": item.program_id,
        "title": item.title,
        "audio_url": item.audio_url,
        "destination": str(destination),
        "resume_from": resume_from,
        "started_at": utc_now(),
    }
    save_state(state_path, state)

    headers = {}
    if resume_from > 0:
        headers["Range"] = f"bytes={resume_from}-"

    try:
        with client.stream("GET", item.audio_url, headers=headers, follow_redirects=True) as response:
            response.raise_for_status()

            if resume_from > 0 and response.status_code != 206:
                resume_from = 0
                temp_path.unlink(missing_ok=True)

            mode = "ab" if resume_from > 0 and response.status_code == 206 else "wb"
            bytes_written = resume_from

            with temp_path.open(mode) as handle:
                for chunk in response.iter_bytes(chunk_size=chunk_size):
                    if not chunk:
                        continue
                    handle.write(chunk)
                    bytes_written += len(chunk)

        temp_path.replace(destination)
        mark_completed(state, item, destination, bytes_written)
        state["current"] = None
        save_state(state_path, state)
        print(f"[ok]   {item.program_id} -> {destination.name}")
    except KeyboardInterrupt:
        state["current"] = {
            **(state.get("current") or {}),
            "interrupted_at": utc_now(),
            "resume_from": temp_path.stat().st_size if temp_path.exists() else resume_from,
        }
        save_state(state_path, state)
        raise
    except Exception as exc:  # noqa: BLE001
        mark_failed(state, item, str(exc), destination)
        state["current"] = None
        save_state(state_path, state)
        print(f"[fail] {item.program_id} {item.title}: {exc}", file=sys.stderr)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Download RadioGolha audio files one by one with resumable state logging."
    )
    parser.add_argument("--db", type=Path, default=DEFAULT_DB_PATH, help="Path to golha SQLite database.")
    parser.add_argument(
        "--output-dir",
        type=Path,
        default=DEFAULT_OUTPUT_DIR,
        help="Directory where MP3 files should be saved.",
    )
    parser.add_argument(
        "--state-file",
        type=Path,
        default=DEFAULT_STATE_FILE,
        help="JSON file used to persist progress and resume state.",
    )
    parser.add_argument("--limit", type=int, default=0, help="Optional max number of rows to process in this run.")
    parser.add_argument("--timeout", type=float, default=60.0, help="HTTP timeout in seconds.")
    parser.add_argument("--chunk-size", type=int, default=1024 * 256, help="Streaming chunk size in bytes.")
    parser.add_argument(
        "--retry-failed",
        action="store_true",
        help="Retry items previously marked as failed in the state file.",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    state = load_state(args.state_file)
    rows = load_program_audio_rows(args.db)
    total_rows = len(rows)
    processed = 0

    print(f"Loaded {total_rows} program audio rows from {args.db}")
    print(f"Downloading into {args.output_dir}")
    print(f"State file: {args.state_file}")

    with httpx.Client(timeout=args.timeout, headers={"User-Agent": "radioGolha-audio-downloader/1.0"}) as client:
        for item in rows:
            if args.limit and processed >= args.limit:
                break

            existing = state["entries"].get(str(item.program_id), {})
            if existing.get("status") == "failed" and not args.retry_failed:
                continue
            if existing.get("status") == "skipped":
                continue

            destination = build_destination_path(args.output_dir, item)
            download_one(client, item, destination, state, args.state_file, args.chunk_size)
            processed += 1
            time.sleep(0.1)

    save_state(args.state_file, state)
    stats = state.get("stats", {})
    print(
        f"Done. completed={stats.get('completed', 0)} "
        f"skipped={stats.get('skipped', 0)} failed={stats.get('failed', 0)}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

import glob
import json
import sqlite3

from orchestra_utils import split_orchestra_and_leader, canonicalize_orchestra_name


BACKUP_DB_PATH = "database/backups/golha_database_20260404_223233.db"


def build_timeline_leader_index():
    conn = sqlite3.connect(BACKUP_DB_PATH)
    cursor = conn.cursor()
    cursor.execute(
        """
        SELECT
          pt.program_id,
          pt.start_time,
          pt.end_time,
          o.name
        FROM program_timeline pt
        JOIN program_timeline_orchestras pto ON pto.timeline_id = pt.id
        JOIN orchestra o ON o.id = pto.orchestra_id
        ORDER BY pt.program_id, pt.start_time, pt.end_time
        """
    )

    index = {}
    for program_id, start_time, end_time, orchestra_name in cursor.fetchall():
        canonical_name, leader_name = split_orchestra_and_leader(orchestra_name)
        if not leader_name:
            continue
        key = (str(program_id), start_time or "", end_time or "")
        info = index.setdefault(key, {})
        info[canonical_name] = leader_name

    conn.close()
    return index


def main():
    leader_index = build_timeline_leader_index()
    updated_files = 0
    updated_items = 0

    for file_path in glob.glob("data/programs/*.json"):
        with open(file_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        program_id = str(data.get("id", "")).strip()
        changed = False

        for entry in data.get("timeline", []):
            key = (program_id, entry.get("start", "") or "", entry.get("end", "") or "")
            timeline_leaders = leader_index.get(key, {})
            if not timeline_leaders:
                continue

            for item in entry.get("items", []):
                canonical_name = canonicalize_orchestra_name(item.get("name", ""))
                leader_name = timeline_leaders.get(canonical_name)
                if not leader_name:
                    continue
                if item.get("leader") != leader_name:
                    item["leader"] = leader_name
                    changed = True
                    updated_items += 1

        if changed:
            with open(file_path, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
            updated_files += 1

    print(f"Restored orchestra leaders in {updated_items} timeline items across {updated_files} files.")


if __name__ == "__main__":
    main()

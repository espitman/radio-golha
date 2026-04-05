import sqlite3
import json
import os
import re
from role_utils import normalize_role_text, classify_timeline_role
from name_utils import normalize_person_name
from orchestra_utils import split_orchestra_and_leader, canonicalize_orchestra_name
from mode_utils import split_mode_names
from program_number_utils import extract_program_number

def fa_to_en_digits(text):
    if not text: return 0
    fa_digits = "۰۱۲۳۴۵۶۷۸۹"
    en_digits = "0123456789"
    mapping = str.maketrans(fa_digits, en_digits)
    nums_only = re.sub(r'[^\d۰-۹]', '', str(text))
    return int(nums_only.translate(mapping)) if nums_only else 0

db_path = 'database/golha_database.db'
if os.path.exists(db_path): os.remove(db_path)
if not os.path.exists('database'): os.makedirs('database')

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

cursor.executescript('''
CREATE TABLE artist (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE, avatar TEXT);
CREATE TABLE category (id INTEGER PRIMARY KEY AUTOINCREMENT, name_en TEXT, title_fa TEXT);
CREATE TABLE performer (id INTEGER PRIMARY KEY AUTOINCREMENT, artist_id INTEGER, FOREIGN KEY(artist_id) REFERENCES artist(id));
CREATE TABLE singer (id INTEGER PRIMARY KEY AUTOINCREMENT, artist_id INTEGER, FOREIGN KEY(artist_id) REFERENCES artist(id));
CREATE TABLE announcer (id INTEGER PRIMARY KEY AUTOINCREMENT, artist_id INTEGER, FOREIGN KEY(artist_id) REFERENCES artist(id));
CREATE TABLE composer (id INTEGER PRIMARY KEY AUTOINCREMENT, artist_id INTEGER, FOREIGN KEY(artist_id) REFERENCES artist(id));
CREATE TABLE arranger (id INTEGER PRIMARY KEY AUTOINCREMENT, artist_id INTEGER, FOREIGN KEY(artist_id) REFERENCES artist(id));
CREATE TABLE poet (id INTEGER PRIMARY KEY AUTOINCREMENT, artist_id INTEGER, FOREIGN KEY(artist_id) REFERENCES artist(id));
CREATE TABLE orchestra_leader (id INTEGER PRIMARY KEY AUTOINCREMENT, artist_id INTEGER, FOREIGN KEY(artist_id) REFERENCES artist(id));
CREATE TABLE orchestra (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE instrument (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE mode (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);

CREATE TABLE program (id INTEGER PRIMARY KEY, title TEXT, category_id INTEGER, no INTEGER, sub_no TEXT, url TEXT, audio_url TEXT, FOREIGN KEY(category_id) REFERENCES category(id));

CREATE TABLE program_performers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, performer_id INTEGER, instrument_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(performer_id) REFERENCES performer(id), FOREIGN KEY(instrument_id) REFERENCES instrument(id));
CREATE TABLE program_singers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, singer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(singer_id) REFERENCES singer(id));
CREATE TABLE program_announcers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, announcer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(announcer_id) REFERENCES announcer(id));
CREATE TABLE program_composers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, composer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(composer_id) REFERENCES composer(id));
CREATE TABLE program_arrangers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, arranger_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(arranger_id) REFERENCES arranger(id));
CREATE TABLE program_orchestras (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, orchestra_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(orchestra_id) REFERENCES orchestra(id));
CREATE TABLE program_orchestra_leaders (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, orchestra_id INTEGER, orchestra_leader_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(orchestra_id) REFERENCES orchestra(id), FOREIGN KEY(orchestra_leader_id) REFERENCES orchestra_leader(id));
CREATE TABLE program_poets (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, poet_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(poet_id) REFERENCES poet(id));
CREATE TABLE program_modes (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, mode_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(mode_id) REFERENCES mode(id));

CREATE TABLE program_timeline (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, start_time TEXT, end_time TEXT, mode_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(mode_id) REFERENCES mode(id));
CREATE TABLE program_timeline_modes (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, mode_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(mode_id) REFERENCES mode(id));

CREATE TABLE program_timeline_performers (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, performer_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(performer_id) REFERENCES performer(id));
CREATE TABLE program_timeline_singers (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, singer_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(singer_id) REFERENCES singer(id));
CREATE TABLE program_timeline_announcers (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, announcer_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(announcer_id) REFERENCES announcer(id));
CREATE TABLE program_timeline_orchestras (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, orchestra_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(orchestra_id) REFERENCES orchestra(id));
CREATE TABLE program_timeline_orchestra_leaders (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, orchestra_id INTEGER, orchestra_leader_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(orchestra_id) REFERENCES orchestra(id), FOREIGN KEY(orchestra_leader_id) REFERENCES orchestra_leader(id));
CREATE TABLE program_timeline_poets (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, poet_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(poet_id) REFERENCES poet(id));

CREATE TABLE program_transcript_verses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    program_id INTEGER,
    segment_order INTEGER,
    verse_order INTEGER,
    text TEXT NOT NULL,
    FOREIGN KEY(program_id) REFERENCES program(id)
);
''')

_cache = {}
def get_id(table, col, val):
    if not val: return None
    v = val.strip()
    key = f"{table}:{col}:{v}"
    if key in _cache: return _cache[key]
    cursor.execute(f"SELECT id FROM {table} WHERE {col} = ?", (v,))
    row = cursor.fetchone()
    if row:
        _cache[key] = row[0]
        return row[0]
    cursor.execute(f"INSERT INTO {table} ({col}) VALUES (?)", (v,))
    res = cursor.lastrowid
    _cache[key] = res
    return res

def get_role_id(table, artist_id):
    if artist_id is None: return None
    key = f"{table}:artist_id:{artist_id}"
    if key in _cache: return _cache[key]
    cursor.execute(f"SELECT id FROM {table} WHERE artist_id = ?", (artist_id,))
    row = cursor.fetchone()
    if row:
        _cache[key] = row[0]
        return row[0]
    cursor.execute(f"INSERT INTO {table} (artist_id) VALUES (?)", (artist_id,))
    res = cursor.lastrowid
    _cache[key] = res
    return res


def insert_program_orchestra_leader(program_id, orchestra_name, leader_name, seen_pairs):
    orchestra_name = canonicalize_orchestra_name(orchestra_name)
    leader_name = normalize_person_name(leader_name)
    if not orchestra_name or not leader_name:
        return

    key = (program_id, orchestra_name, leader_name)
    if key in seen_pairs:
        return

    orchestra_id = get_id('orchestra', 'name', orchestra_name)
    leader_artist_id = get_id('artist', 'name', leader_name)
    leader_id = get_role_id('orchestra_leader', leader_artist_id)
    cursor.execute(
        "INSERT INTO program_orchestra_leaders (program_id, orchestra_id, orchestra_leader_id) VALUES (?, ?, ?)",
        (program_id, orchestra_id, leader_id),
    )
    seen_pairs.add(key)


def insert_timeline_orchestra_leader(timeline_id, orchestra_name, leader_name, seen_pairs):
    orchestra_name = canonicalize_orchestra_name(orchestra_name)
    leader_name = normalize_person_name(leader_name)
    if not orchestra_name or not leader_name:
        return

    key = (timeline_id, orchestra_name, leader_name)
    if key in seen_pairs:
        return

    orchestra_id = get_id('orchestra', 'name', orchestra_name)
    leader_artist_id = get_id('artist', 'name', leader_name)
    leader_id = get_role_id('orchestra_leader', leader_artist_id)
    cursor.execute(
        "INSERT INTO program_timeline_orchestra_leaders (timeline_id, orchestra_id, orchestra_leader_id) VALUES (?, ?, ?)",
        (timeline_id, orchestra_id, leader_id),
    )
    seen_pairs.add(key)

with open('data/programmes_by_category.json', 'r') as f:
    categories_dict = json.load(f)

for en_key, p_list in categories_dict.items():
    if not p_list: continue
    fa_name = p_list[0].get('category', en_key)
    cursor.execute("INSERT INTO category (name_en, title_fa) VALUES (?, ?)", (en_key.replace('-', ' '), fa_name))
    cat_id = cursor.lastrowid
    
    for p in p_list:
        pid = p['id']
        meta_path = f"data/programs/{pid}.json"
        if not os.path.exists(meta_path): continue
        with open(meta_path, 'r') as mf: meta = json.load(mf)
        audio_path = f"data/audio_links/{pid}.json"
        audio_url = ""
        if os.path.exists(audio_path):
            with open(audio_path, 'r') as af: audio_url = json.load(af).get('audio_url', '')
            
        program_no, program_sub_no = extract_program_number(p.get('title', ''), p.get('no', ''))
        if p.get('sub_no'):
            program_sub_no = str(p.get('sub_no')).strip() or program_sub_no
        cursor.execute(
            "INSERT INTO program (id, title, category_id, no, sub_no, url, audio_url) VALUES (?, ?, ?, ?, ?, ?, ?)",
            (pid, p.get('title'), cat_id, program_no, program_sub_no, meta.get('url'), audio_url),
        )
        
        smr = meta.get('summary', {})
        seen_program_orchestras = set()
        seen_program_orchestra_leaders = set()
        seen_program_modes = set()
        for name in smr.get('composers', []): cursor.execute("INSERT INTO program_composers (program_id, composer_id) VALUES (?, ?)", (pid, get_role_id('composer', get_id('artist', 'name', normalize_person_name(name)))))
        for name in smr.get('arrangers', []): cursor.execute("INSERT INTO program_arrangers (program_id, arranger_id) VALUES (?, ?)", (pid, get_role_id('arranger', get_id('artist', 'name', normalize_person_name(name)))))
        for name in smr.get('orchestras', []):
            orchestra_name = canonicalize_orchestra_name(name)
            if orchestra_name and orchestra_name not in seen_program_orchestras:
                cursor.execute("INSERT INTO program_orchestras (program_id, orchestra_id) VALUES (?, ?)", (pid, get_id('orchestra', 'name', orchestra_name)))
                seen_program_orchestras.add(orchestra_name)
        for name in smr.get('singers', []): cursor.execute("INSERT INTO program_singers (program_id, singer_id) VALUES (?, ?)", (pid, get_role_id('singer', get_id('artist', 'name', normalize_person_name(name)))))
        for name in smr.get('announcers', []): cursor.execute("INSERT INTO program_announcers (program_id, announcer_id) VALUES (?, ?)", (pid, get_role_id('announcer', get_id('artist', 'name', normalize_person_name(name)))))
        for poet_val in smr.get('poets', []):
            pname = poet_val if isinstance(poet_val, str) else poet_val.get('name', '')
            pname = normalize_person_name(pname)
            if pname: cursor.execute("INSERT INTO program_poets (program_id, poet_id) VALUES (?, ?)", (pid, get_role_id('poet', get_id('artist', 'name', pname))))
        for p_info in smr.get('performers', []): cursor.execute("INSERT INTO program_performers (program_id, performer_id, instrument_id) VALUES (?, ?, ?)", (pid, get_role_id('performer', get_id('artist', 'name', normalize_person_name(p_info['name']))), get_id('instrument', 'name', p_info['instrument'])))
        for m_name in smr.get('modes', []):
            for split_mode in split_mode_names(m_name):
                if split_mode and split_mode not in seen_program_modes:
                    cursor.execute("INSERT INTO program_modes (program_id, mode_id) VALUES (?, ?)", (pid, get_id('mode', 'name', split_mode)))
                    seen_program_modes.add(split_mode)

        leader_details = smr.get('orchestra_leader_details', [])
        if leader_details:
            for detail in leader_details:
                insert_program_orchestra_leader(pid, detail.get('orchestra', ''), detail.get('leader', ''), seen_program_orchestra_leaders)
        else:
            for name in smr.get('orchestras', []):
                orchestra_name, leader_name = split_orchestra_and_leader(name)
                insert_program_orchestra_leader(pid, orchestra_name, leader_name, seen_program_orchestra_leaders)

        # Timeline - Improved Detection Logic
        for entry in meta.get('timeline', []):
            timeline_modes = entry.get('modes') or split_mode_names(entry.get('mode'))
            primary_mode = timeline_modes[0] if timeline_modes else entry.get('mode')
            mid = get_id('mode', 'name', primary_mode)
            cursor.execute("INSERT INTO program_timeline (program_id, start_time, end_time, mode_id) VALUES (?, ?, ?, ?)", (pid, entry.get('start'), entry.get('end'), mid))
            tid = cursor.lastrowid
            seen_timeline_orchestras = set()
            seen_timeline_orchestra_leaders = set()
            seen_timeline_modes = set()

            for timeline_mode in timeline_modes:
                if timeline_mode and timeline_mode not in seen_timeline_modes:
                    cursor.execute("INSERT INTO program_timeline_modes (timeline_id, mode_id) VALUES (?, ?)", (tid, get_id('mode', 'name', timeline_mode)))
                    seen_timeline_modes.add(timeline_mode)
            
            for item in entry.get('items', []):
                role = normalize_role_text(item.get('role', ''))
                name = normalize_person_name(item.get('name', '').strip())
                if not name: continue

                role_type = classify_timeline_role(role, name)
                if role_type == "singer":
                    aid = get_id('artist', 'name', name)
                    cursor.execute("INSERT INTO program_timeline_singers (timeline_id, singer_id) VALUES (?, ?)", (tid, get_role_id('singer', aid)))
                elif role_type == "announcer":
                    aid = get_id('artist', 'name', name)
                    cursor.execute("INSERT INTO program_timeline_announcers (timeline_id, announcer_id) VALUES (?, ?)", (tid, get_role_id('announcer', aid)))
                elif role_type == "poet":
                    aid = get_id('artist', 'name', name)
                    cursor.execute("INSERT INTO program_timeline_poets (timeline_id, poet_id) VALUES (?, ?)", (tid, get_role_id('poet', aid)))
                elif role_type == "orchestra":
                    orchestra_name, leader_name = split_orchestra_and_leader(item.get('name', ''))
                    orchestra_name = orchestra_name or canonicalize_orchestra_name(name)
                    if orchestra_name and orchestra_name not in seen_timeline_orchestras:
                        cursor.execute("INSERT INTO program_timeline_orchestras (timeline_id, orchestra_id) VALUES (?, ?)", (tid, get_id('orchestra', 'name', orchestra_name)))
                        seen_timeline_orchestras.add(orchestra_name)
                    insert_timeline_orchestra_leader(tid, orchestra_name, item.get('leader', '') or leader_name, seen_timeline_orchestra_leaders)
                else:
                    aid = get_id('artist', 'name', name)
                    cursor.execute("INSERT INTO program_timeline_performers (timeline_id, performer_id) VALUES (?, ?)", (tid, get_role_id('performer', aid)))

        transcript_path = f"data/transcripts/{pid}.json"
        if os.path.exists(transcript_path):
            with open(transcript_path, 'r') as tf:
                transcript = json.load(tf)

            for segment_index, segment in enumerate(transcript.get('segments', []), start=1):
                for verse_index, verse in enumerate(segment.get('verses', []), start=1):
                    verse_text = str(verse or '').strip()
                    if not verse_text:
                        continue
                    cursor.execute(
                        """
                        INSERT INTO program_transcript_verses
                          (program_id, segment_order, verse_order, text)
                        VALUES (?, ?, ?, ?)
                        """,
                        (pid, segment_index, verse_index, verse_text),
                    )

conn.commit()
conn.close()
print("Granular DB v5.3 - Transcript verses imported.")

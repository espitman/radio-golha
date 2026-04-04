import sqlite3
import json
import os
import re
from role_utils import normalize_role_text, classify_timeline_role
from name_utils import normalize_person_name

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
CREATE TABLE orchestra (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE instrument (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE mode (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);

CREATE TABLE program (id INTEGER PRIMARY KEY, title TEXT, category_id INTEGER, no INTEGER, url TEXT, audio_url TEXT, FOREIGN KEY(category_id) REFERENCES category(id));

CREATE TABLE program_performers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, performer_id INTEGER, instrument_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(performer_id) REFERENCES performer(id), FOREIGN KEY(instrument_id) REFERENCES instrument(id));
CREATE TABLE program_singers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, singer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(singer_id) REFERENCES singer(id));
CREATE TABLE program_announcers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, announcer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(announcer_id) REFERENCES announcer(id));
CREATE TABLE program_composers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, composer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(composer_id) REFERENCES composer(id));
CREATE TABLE program_arrangers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, arranger_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(arranger_id) REFERENCES arranger(id));
CREATE TABLE program_orchestras (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, orchestra_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(orchestra_id) REFERENCES orchestra(id));
CREATE TABLE program_poets (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, poet_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(poet_id) REFERENCES poet(id));
CREATE TABLE program_modes (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, mode_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(mode_id) REFERENCES mode(id));

CREATE TABLE program_timeline (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, start_time TEXT, end_time TEXT, mode_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(mode_id) REFERENCES mode(id));

CREATE TABLE program_timeline_performers (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, performer_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(performer_id) REFERENCES performer(id));
CREATE TABLE program_timeline_singers (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, singer_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(singer_id) REFERENCES singer(id));
CREATE TABLE program_timeline_announcers (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, announcer_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(announcer_id) REFERENCES announcer(id));
CREATE TABLE program_timeline_orchestras (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, orchestra_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(orchestra_id) REFERENCES orchestra(id));
CREATE TABLE program_timeline_poets (id INTEGER PRIMARY KEY AUTOINCREMENT, timeline_id INTEGER, poet_id INTEGER, FOREIGN KEY(timeline_id) REFERENCES program_timeline(id), FOREIGN KEY(poet_id) REFERENCES poet(id));
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
            
        cursor.execute("INSERT INTO program (id, title, category_id, no, url, audio_url) VALUES (?, ?, ?, ?, ?, ?)", (pid, p.get('title'), cat_id, fa_to_en_digits(p.get('no')), meta.get('url'), audio_url))
        
        smr = meta.get('summary', {})
        for name in smr.get('composers', []): cursor.execute("INSERT INTO program_composers (program_id, composer_id) VALUES (?, ?)", (pid, get_role_id('composer', get_id('artist', 'name', normalize_person_name(name)))))
        for name in smr.get('arrangers', []): cursor.execute("INSERT INTO program_arrangers (program_id, arranger_id) VALUES (?, ?)", (pid, get_role_id('arranger', get_id('artist', 'name', normalize_person_name(name)))))
        for name in smr.get('orchestras', []): cursor.execute("INSERT INTO program_orchestras (program_id, orchestra_id) VALUES (?, ?)", (pid, get_id('orchestra', 'name', name)))
        for name in smr.get('singers', []): cursor.execute("INSERT INTO program_singers (program_id, singer_id) VALUES (?, ?)", (pid, get_role_id('singer', get_id('artist', 'name', normalize_person_name(name)))))
        for name in smr.get('announcers', []): cursor.execute("INSERT INTO program_announcers (program_id, announcer_id) VALUES (?, ?)", (pid, get_role_id('announcer', get_id('artist', 'name', normalize_person_name(name)))))
        for poet_val in smr.get('poets', []):
            pname = poet_val if isinstance(poet_val, str) else poet_val.get('name', '')
            pname = normalize_person_name(pname)
            if pname: cursor.execute("INSERT INTO program_poets (program_id, poet_id) VALUES (?, ?)", (pid, get_role_id('poet', get_id('artist', 'name', pname))))
        for p_info in smr.get('performers', []): cursor.execute("INSERT INTO program_performers (program_id, performer_id, instrument_id) VALUES (?, ?, ?)", (pid, get_role_id('performer', get_id('artist', 'name', normalize_person_name(p_info['name']))), get_id('instrument', 'name', p_info['instrument'])))
        for m_name in smr.get('modes', []): cursor.execute("INSERT INTO program_modes (program_id, mode_id) VALUES (?, ?)", (pid, get_id('mode', 'name', m_name)))

        # Timeline - Improved Detection Logic
        for entry in meta.get('timeline', []):
            mid = get_id('mode', 'name', entry.get('mode'))
            cursor.execute("INSERT INTO program_timeline (program_id, start_time, end_time, mode_id) VALUES (?, ?, ?, ?)", (pid, entry.get('start'), entry.get('end'), mid))
            tid = cursor.lastrowid
            
            for item in entry.get('items', []):
                role = normalize_role_text(item.get('role', ''))
                name = normalize_person_name(item.get('name', '').strip())
                if not name: continue
                aid = get_id('artist', 'name', name)

                role_type = classify_timeline_role(role, name)
                if role_type == "singer":
                    cursor.execute("INSERT INTO program_timeline_singers (timeline_id, singer_id) VALUES (?, ?)", (tid, get_role_id('singer', aid)))
                elif role_type == "announcer":
                    cursor.execute("INSERT INTO program_timeline_announcers (timeline_id, announcer_id) VALUES (?, ?)", (tid, get_role_id('announcer', aid)))
                elif role_type == "poet":
                    cursor.execute("INSERT INTO program_timeline_poets (timeline_id, poet_id) VALUES (?, ?)", (tid, get_role_id('poet', aid)))
                elif role_type == "orchestra":
                    cursor.execute("INSERT INTO program_timeline_orchestras (timeline_id, orchestra_id) VALUES (?, ?)", (tid, get_id('orchestra', 'name', name)))
                else:
                    cursor.execute("INSERT INTO program_timeline_performers (timeline_id, performer_id) VALUES (?, ?)", (tid, get_role_id('performer', aid)))

conn.commit()
conn.close()
print("Granular DB v5.1 - Timeline Poets FIXED!")

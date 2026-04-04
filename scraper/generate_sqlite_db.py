import sqlite3
import json
import os
import re

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

# Schema v4: Based on User's Snippet
cursor.executescript('''
CREATE TABLE category (id INTEGER PRIMARY KEY AUTOINCREMENT, name_en TEXT, title_fa TEXT);
CREATE TABLE performer (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE singer (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE announcer (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE poet (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE instrument (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE mode (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE composer (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE arranger (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE orchestra (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);

CREATE TABLE program (
    id INTEGER PRIMARY KEY,
    title TEXT,
    category_id INTEGER,
    no INTEGER,
    url TEXT,
    audio_url TEXT,
    FOREIGN KEY(category_id) REFERENCES category(id)
);

CREATE TABLE program_performers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, artist_id INTEGER, instrument_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(artist_id) REFERENCES performer(id), FOREIGN KEY(instrument_id) REFERENCES instrument(id));
CREATE TABLE program_singers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, singer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(singer_id) REFERENCES singer(id));
CREATE TABLE program_announcers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, announcer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(announcer_id) REFERENCES announcer(id));
CREATE TABLE program_poets (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, poet_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(poet_id) REFERENCES poet(id));
CREATE TABLE program_modes (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, mode_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(mode_id) REFERENCES mode(id));
CREATE TABLE program_composers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, composer_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(composer_id) REFERENCES composer(id));
CREATE TABLE program_arrangers (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, arranger_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(arranger_id) REFERENCES arranger(id));
CREATE TABLE program_orchestras (id INTEGER PRIMARY KEY AUTOINCREMENT, program_id INTEGER, orchestra_id INTEGER, FOREIGN KEY(program_id) REFERENCES program(id), FOREIGN KEY(orchestra_id) REFERENCES orchestra(id));
''')

def get_id(table, col, val):
    if not val: return None
    v = val.strip()
    cursor.execute(f"SELECT id FROM {table} WHERE {col} = ?", (v,))
    row = cursor.fetchone()
    if row: return row[0]
    cursor.execute(f"INSERT INTO {table} ({col}) VALUES (?)", (v,))
    return cursor.lastrowid

with open('data/programmes_by_category.json', 'r') as f:
    categories_dict = json.load(f)

print("Building Granular Database v4...")

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
        
        audio_url = ""
        audio_path = f"data/audio_links/{pid}.json"
        if os.path.exists(audio_path):
            with open(audio_path, 'r') as af: audio_url = json.load(af).get('audio_url', '')
            
        cursor.execute("INSERT INTO program (id, title, category_id, no, url, audio_url) VALUES (?, ?, ?, ?, ?, ?)",
                       (pid, p.get('title'), cat_id, fa_to_en_digits(p.get('no')), meta.get('url'), audio_url))
        
        sumry = meta.get('summary', {})
        
        # New Fields Integration
        for name in sumry.get('composers', []):
            cid = get_id('composer', 'name', name)
            cursor.execute("INSERT INTO program_composers (program_id, composer_id) VALUES (?, ?)", (pid, cid))
        for name in sumry.get('arrangers', []):
            aid = get_id('arranger', 'name', name)
            cursor.execute("INSERT INTO program_arrangers (program_id, arranger_id) VALUES (?, ?)", (pid, aid))
        for name in sumry.get('orchestras', []):
            oid = get_id('orchestra', 'name', name)
            cursor.execute("INSERT INTO program_orchestras (program_id, orchestra_id) VALUES (?, ?)", (pid, oid))
            
        # Traditional Summary Linkage
        for p_info in sumry.get('performers', []):
            artist_id = get_id('performer', 'name', p_info['name'])
            inst_id = get_id('instrument', 'name', p_info['instrument'])
            cursor.execute("INSERT INTO program_performers (program_id, artist_id, instrument_id) VALUES (?, ?, ?)", (pid, artist_id, inst_id))
        
        for name in sumry.get('singers', []):
            sid = get_id('singer', 'name', name)
            cursor.execute("INSERT INTO program_singers (program_id, singer_id) VALUES (?, ?)", (pid, sid))
            
        for name in sumry.get('announcers', []):
            an_id = get_id('announcer', 'name', name)
            cursor.execute("INSERT INTO program_announcers (program_id, announcer_id) VALUES (?, ?)", (pid, an_id))
            
        for p_item in sumry.get('poets', []):
            p_name = p_item if isinstance(p_item, str) else p_item.get('name', '')
            if p_name:
                poet_id = get_id('poet', 'name', p_name)
                cursor.execute("INSERT INTO program_poets (program_id, poet_id) VALUES (?, ?)", (pid, poet_id))
                
        for m_name in sumry.get('modes', []):
            mid = get_id('mode', 'name', m_name)
            cursor.execute("INSERT INTO program_modes (program_id, mode_id) VALUES (?, ?)", (pid, mid))

conn.commit()
conn.close()
print("Granular Database Rebuilt SUCCESSFULLY!")

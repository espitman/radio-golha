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

# Schema v3: Strongly Typed and Normalized
cursor.executescript('''
CREATE TABLE Artists (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE Poets (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE Instruments (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE PoeticStyles (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE MusicalModes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE ProgrammeTypes (id INTEGER PRIMARY KEY AUTOINCREMENT, name_fa TEXT UNIQUE, name_en TEXT UNIQUE);

CREATE TABLE Programmes (
    id INTEGER PRIMARY KEY,
    type_id INTEGER,
    number INTEGER,
    url TEXT,
    audio_url TEXT,
    FOREIGN KEY(type_id) REFERENCES ProgrammeTypes(id)
);

CREATE TABLE Timeline_Segments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    programme_id INTEGER,
    mode_id INTEGER,
    start_time TEXT,
    end_time TEXT,
    FOREIGN KEY(programme_id) REFERENCES Programmes(id),
    FOREIGN KEY(mode_id) REFERENCES MusicalModes(id)
);

CREATE TABLE Timeline_Details (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    segment_id INTEGER,
    artist_id INTEGER,
    instrument_id INTEGER,
    poet_id INTEGER,
    role TEXT, -- singer, performer, announcer, poet_mention
    FOREIGN KEY(segment_id) REFERENCES Timeline_Segments(id),
    FOREIGN KEY(artist_id) REFERENCES Artists(id),
    FOREIGN KEY(instrument_id) REFERENCES Instruments(id),
    FOREIGN KEY(poet_id) REFERENCES Poets(id)
);

CREATE TABLE Transcript_Segments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    programme_id INTEGER,
    speaker_id INTEGER,
    poet_id INTEGER,
    style_id INTEGER,
    segment_order INTEGER,
    FOREIGN KEY(programme_id) REFERENCES Programmes(id),
    FOREIGN KEY(speaker_id) REFERENCES Artists(id),
    FOREIGN KEY(poet_id) REFERENCES Poets(id),
    FOREIGN KEY(style_id) REFERENCES PoeticStyles(id)
);

CREATE TABLE Verses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    segment_id INTEGER,
    bait_text TEXT,
    verse_order INTEGER,
    FOREIGN KEY(segment_id) REFERENCES Transcript_Segments(id)
);
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

for en_key, p_list in categories_dict.items():
    if not p_list: continue
    fa_name = p_list[0].get('category', en_key)
    type_id = get_id('ProgrammeTypes', 'name_fa', fa_name)
    cursor.execute("UPDATE ProgrammeTypes SET name_en = ? WHERE id = ?", (en_key.replace('-', ' '), type_id))
    
    for p in p_list:
        pid = p['id']
        meta_path = f"data/programs/{pid}.json"
        if not os.path.exists(meta_path): continue
        with open(meta_path, 'r') as mf: meta = json.load(mf)
        
        audio_url = ""
        audio_path = f"data/audio_links/{pid}.json"
        if os.path.exists(audio_path):
            with open(audio_path, 'r') as af: audio_url = json.load(af).get('audio_url', '')
            
        cursor.execute("INSERT INTO Programmes (id, type_id, number, url, audio_url) VALUES (?, ?, ?, ?, ?)",
                       (pid, type_id, fa_to_en_digits(p.get('no')), meta.get('url'), audio_url))
        
        # Timeline Integration
        for entry in meta.get('timeline', []):
            mode_id = get_id('MusicalModes', 'name', entry.get('mode'))
            cursor.execute("INSERT INTO Timeline_Segments (programme_id, mode_id, start_time, end_time) VALUES (?, ?, ?, ?)",
                           (pid, mode_id, entry.get('start'), entry.get('end')))
            seg_id = cursor.lastrowid
            
            for item in entry.get('items', []):
                role = item.get('role', '')
                raw_name = item.get('name', '')
                raw_inst = item.get('instrument', '')
                
                a_id = None; p_id = None; i_id = None
                
                if "سرایندگان" in role or "شاعر" in role:
                    p_id = get_id('Poets', 'name', raw_name)
                    # instrument field here might be style like Ghaside
                elif "گوینده" in role:
                    a_id = get_id('Artists', 'name', raw_name)
                else:
                    a_id = get_id('Artists', 'name', raw_name)
                    i_id = get_id('Instruments', 'name', raw_inst)
                
                cursor.execute("INSERT INTO Timeline_Details (segment_id, artist_id, instrument_id, poet_id, role) VALUES (?, ?, ?, ?, ?)",
                               (seg_id, a_id, i_id, p_id, role))

        # Transcripts
        trans_path = f"data/transcripts/{pid}.json"
        if os.path.exists(trans_path):
            with open(trans_path, 'r') as tf: trans = json.load(tf)
            for s_idx, seg in enumerate(trans.get('segments', [])):
                spk_id = get_id('Artists', 'name', seg.get('speaker'))
                poet_id = get_id('Poets', 'name', seg.get('poet'))
                sty_id = get_id('PoeticStyles', 'name', seg.get('style'))
                cursor.execute("INSERT INTO Transcript_Segments (programme_id, speaker_id, poet_id, style_id, segment_order) VALUES (?, ?, ?, ?, ?)",
                               (pid, spk_id, poet_id, sty_id, s_idx))
                t_seg_id = cursor.lastrowid
                for v_idx, verse in enumerate(seg.get('verses', [])):
                    cursor.execute("INSERT INTO Verses (segment_id, bait_text, verse_order) VALUES (?, ?, ?)",
                                   (t_seg_id, verse, v_idx))

conn.commit()
conn.close()
print("Final ID-Centric Database Rebuilt Successfully!")

import sqlite3
import json
import os
import re

db_path = 'golha_database.db'
if os.path.exists(db_path): os.remove(db_path)

conn = sqlite3.connect(db_path)
cursor = conn.cursor()

# 1. Create Tables
cursor.executescript('''
CREATE TABLE Artists (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE Poets (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE Instruments (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE PoeticStyles (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);
CREATE TABLE MusicalModes (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE);

CREATE TABLE Programmes (
    id INTEGER PRIMARY KEY,
    type TEXT,
    number INTEGER,
    url TEXT,
    audio_url TEXT
);

CREATE TABLE Programme_Performers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    programme_id INTEGER,
    artist_id INTEGER,
    instrument_id INTEGER,
    role TEXT,
    FOREIGN KEY(programme_id) REFERENCES Programmes(id),
    FOREIGN KEY(artist_id) REFERENCES Artists(id),
    FOREIGN KEY(instrument_id) REFERENCES Instruments(id)
);

CREATE TABLE Programme_Timeline (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    programme_id INTEGER,
    mode_id INTEGER,
    start_time TEXT,
    end_time TEXT,
    FOREIGN KEY(programme_id) REFERENCES Programmes(id),
    FOREIGN KEY(mode_id) REFERENCES MusicalModes(id)
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

def get_or_create(table, column, value):
    if not value: return None
    cursor.execute(f"SELECT id FROM {table} WHERE {column} = ?", (value,))
    row = cursor.fetchone()
    if row: return row[0]
    cursor.execute(f"INSERT INTO {table} ({column}) VALUES (?)", (value,))
    return cursor.lastrowid

# 2. Load Data and Populate
with open('data/programmes.json', 'r') as f:
    programmes_index = json.load(f)

print(f"Populating database for {len(programmes_index)} programs...")

for p in programmes_index:
    pid = str(p['id'])
    
    # Metadata
    meta_path = f"data/programs/{pid}.json"
    audio_path = f"data/audio_links/{pid}.json"
    trans_path = f"data/transcripts/{pid}.json"
    
    if not os.path.exists(meta_path): continue
    
    with open(meta_path, 'r') as mf: meta = json.load(mf)
    audio_url = ""
    if os.path.exists(audio_path):
        with open(audio_path, 'r') as af: audio_url = json.load(af).get('audio_url', '')
    
    # Extract Type & Number
    # Example: "Golhaye-Taze No 1"
    # Actually, we'll try to find a better way, but for now just from programmes.json if possible
    # Let's use the ID and general categorization later or just simple split
    p_type = p.get('category', '')
    p_num = p.get('number', 0)

    cursor.execute("INSERT INTO Programmes (id, type, number, url, audio_url) VALUES (?, ?, ?, ?, ?)",
                   (pid, p_type, p_num, meta.get('url'), audio_url))

    # Performers Summary
    sumry = meta.get('summary', {})
    for p_info in sumry.get('performers', []):
        a_id = get_or_create('Artists', 'name', p_info['name'])
        i_id = get_or_create('Instruments', 'name', p_info['instrument'])
        cursor.execute("INSERT INTO Programme_Performers (programme_id, artist_id, instrument_id, role) VALUES (?, ?, ?, ?)",
                       (pid, a_id, i_id, 'performer'))
    
    for s_name in sumry.get('singers', []):
        a_id = get_or_create('Artists', 'name', s_name)
        cursor.execute("INSERT INTO Programme_Performers (programme_id, artist_id, instrument_id, role) VALUES (?, ?, ?, ?)",
                       (pid, a_id, None, 'singer'))

    # Timeline
    for entry in meta.get('timeline', []):
        m_id = get_or_create('MusicalModes', 'name', entry.get('mode'))
        cursor.execute("INSERT INTO Programme_Timeline (programme_id, mode_id, start_time, end_time) VALUES (?, ?, ?, ?)",
                       (pid, m_id, entry.get('start'), entry.get('end')))

    # Transcript
    if os.path.exists(trans_path):
        with open(trans_path, 'r') as tf: trans = json.load(tf)
        for idx, seg in enumerate(trans.get('segments', [])):
            spk_id = get_or_create('Artists', 'name', seg.get('speaker'))
            poet_id = get_or_create('Poets', 'name', seg.get('poet'))
            sty_id = get_or_create('PoeticStyles', 'name', seg.get('style'))
            
            cursor.execute("INSERT INTO Transcript_Segments (programme_id, speaker_id, poet_id, style_id, segment_order) VALUES (?, ?, ?, ?, ?)",
                           (pid, spk_id, poet_id, sty_id, idx))
            seg_id = cursor.lastrowid
            
            for v_idx, verse in enumerate(seg.get('verses', [])):
                cursor.execute("INSERT INTO Verses (segment_id, bait_text, verse_order) VALUES (?, ?, ?)",
                               (seg_id, verse, v_idx))

conn.commit()
conn.close()
print("Database Generated Successfully: golha_database.db")

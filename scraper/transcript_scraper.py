import httpx
from bs4 import BeautifulSoup
import json
import os
import re
import asyncio
import sys

def parse_poet_style(text):
    match = re.search(r'([^()]+)\s*\(([^()]+)\)', text)
    if match:
        return match.group(1).strip(), match.group(2).strip()
    return text.strip(), ""

async def scrape_structured_transcript(client, pid):
    url = f"https://www.golha.co.uk/fa/transcript/{pid}"
    try:
        response = await client.get(url, timeout=20.0)
        if response.status_code != 200: return None
        soup = BeautifulSoup(response.text, 'lxml')
        table = soup.find('table')
        if not table:
             div = soup.select_one('.innertube.fatext')
             if div: table = div.find('table')
        if not table: return None
        
        rows = table.find_all('tr')
        segments = []; current_speaker = ""; current_verses = []
        
        # Keywords for speakers in brackets or colon
        speaker_patterns = [r'\(گوینده\)', r'\(ترانه\)', r'\(آواز\)', r'\(خواننده\)', r'\(دکلمه\)']
        
        for row in rows:
            cells = row.find_all('td')
            row_text = row.get_text(strip=True).replace('\u200e', '')
            if not row_text: continue
            
            # 1. Speaker Detection (Colon OR Parentheses keywords)
            is_speaker = ":" in row_text or any(re.search(p, row_text) for p in speaker_patterns)
            keywords = ["دکلمه", "آواز", "گوینده", "خواننده", "ترانه", "غزل"]
            
            if is_speaker and any(kw in row_text for kw in keywords):
                if current_verses:
                    segments.append({"speaker": current_speaker, "verses": current_verses, "poet": "", "style": ""})
                    current_verses = []
                
                # Extract speaker name
                if ":" in row_text:
                    current_speaker = row_text.split(":", 1)[1].strip()
                else:
                    # e.g. "روشنک (گوینده)" -> "روشنک"
                    current_speaker = re.sub(r'\(.*?\)', '', row_text).strip()
                continue

            # 2. Poetic Row ZIP Alignment
            # A poetic row usually has at least 2 cells with text
            active_cells = [c for c in cells if c.get_text(strip=True)]
            if len(active_cells) >= 2:
                # Extract parts from cell A and cell B
                col1 = [p.get_text(strip=True).replace('\u200e', '') for p in active_cells[0].find_all('p')]
                col2 = [p.get_text(strip=True).replace('\u200e', '') for p in active_cells[1].find_all('p')]
                
                # Fallback: if no P tags, treat the whole cell as one hemistich
                if not col1 and active_cells[0].get_text(strip=True): col1 = [active_cells[0].get_text(strip=True)]
                if not col2 and active_cells[1].get_text(strip=True): col2 = [active_cells[1].get_text(strip=True)]
                
                if col1 and col2:
                    for m1, m2 in zip(col1, col2):
                        if m1 and m2: current_verses.append(f"{m1} / {m2}")
                    continue

            # 3. Poet Detection
            if current_verses and (("(" in row_text and ")" in row_text) or len(row_text) < 40):
                poet, style = parse_poet_style(row_text)
                segments.append({"speaker": current_speaker, "verses": current_verses, "poet": poet, "style": style})
                current_verses = []
                continue

        if current_verses:
            segments.append({"speaker": current_speaker, "verses": current_verses, "poet": "", "style": ""})

        return {"id": pid, "url": url, "segments": segments}
    except Exception: return None

async def main():
    if not os.path.exists('data/transcripts'): os.makedirs('data/transcripts')
    with open('data/programmes.json', 'r') as f: programmes = json.load(f)
    ids = [str(p['id']) for p in programmes]
    
    async with httpx.AsyncClient(headers={"User-Agent": "Mozilla/5.0"}) as client:
        if len(sys.argv) > 1 and sys.argv[1] == "full":
            total = len(ids); semaphore = asyncio.Semaphore(15)
            for i in range(0, total, 50):
                chunk = ids[i:i+50]
                tasks = [scrape_structured_transcript(client, pid) for pid in chunk]
                results = await asyncio.gather(*tasks)
                for j, res in enumerate(results):
                    if res:
                        with open(f"data/transcripts/{chunk[j]}.json", 'w', encoding='utf-8') as f:
                            json.dump(res, f, ensure_ascii=False, indent=2)
                print(f"Master Build v2 Progress: [{min(i+50, total)}/{total}]", flush=True)
        else:
            pid = sys.argv[1] if len(sys.argv) > 1 else "1"
            res = await scrape_structured_transcript(client, pid)
            if res:
                with open(f"data/transcripts/{pid}.json", 'w', encoding='utf-8') as f:
                    json.dump(res, f, ensure_ascii=False, indent=2)
                print(f"ID {pid} Master Scrape v2 Saved.")

if __name__ == "__main__": asyncio.run(main())

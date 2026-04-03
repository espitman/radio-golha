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
        
        for row in rows:
            cells = row.find_all('td')
            row_text = row.get_text(strip=True).replace('\u200e', '')
            if not row_text: continue
            
            # 1. Speaker Detection
            if ":" in row_text and any(kw in row_text for kw in ["دکلمه", "آواز", "گوینده", "خواننده"]):
                if current_verses:
                    segments.append({"speaker": current_speaker, "verses": current_verses, "poet": "", "style": ""})
                    current_verses = []
                current_speaker = row_text.split(":", 1)[1].strip()
                continue

            # 2. Poetic Row ZIP Alignment
            verse_cells = [c for c in cells if c.find_all('p') or len(c.get_text(strip=True)) > 20]
            if len(verse_cells) >= 2:
                col1 = [p.get_text(strip=True).replace('\u200e', '') for p in verse_cells[0].find_all('p')]
                col2 = [p.get_text(strip=True).replace('\u200e', '') for p in verse_cells[1].find_all('p')]
                if not col1 and verse_cells[0].get_text(strip=True): col1 = [verse_cells[0].get_text(strip=True)]
                if not col2 and verse_cells[1].get_text(strip=True): col2 = [verse_cells[1].get_text(strip=True)]
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
                print(f"Master Build Progress: [{min(i+50, total)}/{total}]", flush=True)
        else:
            pid = sys.argv[1] if len(sys.argv) > 1 else "1"
            res = await scrape_structured_transcript(client, pid)
            if res:
                with open(f"data/transcripts/{pid}.json", 'w', encoding='utf-8') as f:
                    json.dump(res, f, ensure_ascii=False, indent=2)
                print(f"ID {pid} Master Scrape Saved.")

if __name__ == "__main__": asyncio.run(main())

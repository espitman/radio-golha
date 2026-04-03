import httpx
from bs4 import BeautifulSoup
import json
import re
import sys
import asyncio
import os

async def scrape_single_programme(client, pid):
    url = f"https://www.golha.co.uk/fa/programme/{pid}"
    try:
        response = await client.get(url, timeout=20.0)
        if response.status_code != 200: return None
        soup = BeautifulSoup(response.text, 'lxml')
        items_container = soup.find('div', id='programme-items')
        if not items_container: return None
        rows = items_container.find_all('div', class_='item_div')
        results = []
        performers_set = set(); announcers_set = set(); modes_set = set(); poets_set = set(); singers_set = set()

        for row in rows:
            text = row.get_text(separator=" ", strip=True)
            time_match = re.search(r'\((\d{1,2}:\d{2})\s*-\s*(\d{1,2}:\d{2})\)', text)
            start_time = time_match.group(1) if time_match else ""; end_time = time_match.group(2) if time_match else ""
            mode_match = re.search(r'^\(([^)]+)\)\s*\(', text); mode = mode_match.group(1).strip() if mode_match else ""
            if mode: modes_set.add(mode)
            items = []; current_role = ""
            for el in row.children:
                if el.name == 'em':
                    current_role = el.get_text(strip=True).replace(':', '').strip()
                elif el.name == 'a' and 'popupwindow' in el.get('class', []):
                    name = el.get_text(strip=True); instrument = ""
                    nxt = el.next_sibling
                    if nxt and isinstance(nxt, str):
                        inst_match = re.search(r'\(([^)]+)\)', nxt)
                        if inst_match: instrument = inst_match.group(1).strip()
                    items.append({"role": current_role, "name": name, "instrument": instrument})
                    # Summary Logic
                    role_lower = current_role.lower()
                    if "نوازند" in role_lower: performers_set.add((name, instrument))
                    elif "خوانند" in role_lower: singers_set.add(name)
                    elif "گوینده" in role_lower: announcers_set.add(name)
                    elif any(x in role_lower for x in ["سرایند", "شاعر"]): poets_set.add((name, instrument))

            results.append({"mode": mode, "start": start_time, "end": end_time, "items": items})

        summary = {
            "performers": [{"name": p[0], "instrument": p[1]} for p in sorted(list(performers_set))],
            "singers": sorted(list(singers_set)),
            "announcers": sorted(list(announcers_set)),
            "modes": sorted(list(modes_set)),
            "poets": [{"name": p[0], "type": p[1]} for p in sorted(list(poets_set))]
        }
        return {"id": pid, "url": url, "timeline": results, "summary": summary}
    except Exception as e: return None

async def main():
    if not os.path.exists('data/programs'): os.makedirs('data/programs')
    with open('data/programmes.json', 'r') as f: programmes = json.load(f)
    ids = [str(p['id']) for p in programmes]; total = len(ids); semaphore = asyncio.Semaphore(15)
    async with httpx.AsyncClient(headers={"User-Agent": "Mozilla/5.0"}) as client:
        for i in range(0, total, 15):
            chunk = ids[i:i+15]; tasks = [scrape_single_programme(client, pid) for pid in chunk]
            results = await asyncio.gather(*tasks)
            for j, res in enumerate(results):
                if res:
                    with open(f"data/programs/{chunk[j]}.json", 'w', encoding='utf-8') as f:
                        json.dump(res, f, ensure_ascii=False, indent=2)
            print(f"Progress: [{min(i+15, total)}/{total}] ({(min(i+15, total))/total*100:.1f}%)", flush=True)

if __name__ == "__main__": asyncio.run(main())

import httpx
from bs4 import BeautifulSoup
import json
import re
import sys

def scrape_single_programme(pid):
    url = f"https://www.golha.co.uk/fa/programme/{pid}"
    try:
        response = httpx.get(url, timeout=20.0, headers={"User-Agent": "Mozilla/5.0"})
        if response.status_code != 200: return None
        
        soup = BeautifulSoup(response.text, 'lxml')
        items_container = soup.find('div', id='programme-items')
        if not items_container: return None
            
        rows = items_container.find_all('div', class_='item_div')
        results = []
        
        # Aggregate lists for summary
        performers_set = set() # (name, instrument)
        announcers_set = set()
        modes_set = set()
        poets_set = set() # (name, type)

        for row in rows:
            text = row.get_text(separator=" ", strip=True)
            
            # Time Range
            time_match = re.search(r'\((\d{1,2}:\d{2})\s*-\s*(\d{1,2}:\d{2})\)', text)
            start_time = time_match.group(1) if time_match else ""
            end_time = time_match.group(2) if time_match else ""
            
            # Mode
            mode_match = re.search(r'^\(([^)]+)\)\s*\(', text)
            mode = mode_match.group(1).strip() if mode_match else ""
            if mode: modes_set.add(mode)

            # Parsing elements
            items = []
            current_role = ""
            
            for el in row.children:
                if el.name == 'em':
                    current_role = el.get_text(strip=True).replace(':', '')
                elif el.name == 'a' and 'popupwindow' in el.get('class', []):
                    name = el.get_text(strip=True)
                    instrument = ""
                    nxt = el.next_sibling
                    if nxt and isinstance(nxt, str):
                        inst_match = re.search(r'\(([^)]+)\)', nxt)
                        if inst_match:
                            instrument = inst_match.group(1).strip()
                    
                    items.append({"role": current_role, "name": name, "instrument": instrument})
                    
                    # Aggregate for summaries
                    if "نوازندگان" in current_role:
                        performers_set.add((name, instrument))
                    elif "گوینده" in current_role:
                        announcers_set.add(name)
                    elif "سراینده" in current_role or "شاعر" in current_role:
                        poets_set.add((name, instrument)) # instrument here is often the poetry type (غزل etc)

            results.append({
                "mode": mode,
                "start": start_time,
                "end": end_time,
                "items": items
            })

        # Format summaries
        summary = {
            "performers": [{"name": p[0], "instrument": p[1]} for p in sorted(list(performers_set))],
            "announcers": sorted(list(announcers_set)),
            "modes": sorted(list(modes_set)),
            "poets": [{"name": p[0], "type": p[1]} for p in sorted(list(poets_set))]
        }

        return {
            "id": pid,
            "url": url,
            "timeline": results,
            "summary": summary
        }
    except Exception as e:
        print(f"Exception: {e}")
        return None

if __name__ == "__main__":
    pid = sys.argv[1] if len(sys.argv) > 1 else "1"
    res = scrape_single_programme(pid)
    if res:
        with open(f"data/{pid}.json", 'w', encoding='utf-8') as f:
            json.dump(res, f, ensure_ascii=False, indent=2)
        print(f"Saved data/{pid}.json")

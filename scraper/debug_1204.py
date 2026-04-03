import httpx
from bs4 import BeautifulSoup

def debug_1204():
    url = "https://www.golha.co.uk/fa/transcript/1204"
    headers = {"User-Agent": "Mozilla/5.0"}
    response = httpx.get(url, timeout=20.0, headers=headers)
    soup = BeautifulSoup(response.text, 'lxml')
    table = soup.find('table')
    if not table:
        div = soup.select_one('.innertube.fatext')
        if div: table = div.find('table')
    
    if not table:
        print("Table NOT found")
        return
        
    rows = table.find_all('tr')
    for i, row in enumerate(rows):
        cells = row.find_all('td')
        texts = [c.get_text(strip=True) for c in cells]
        print(f"Row {i} ({len(cells)} cells): {texts}")

if __name__ == "__main__":
    debug_1204()

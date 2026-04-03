import httpx
from bs4 import BeautifulSoup
import json
import asyncio
import re
import sys

async def scrape_programme(client, id):
    url = f"https://www.golha.co.uk/fa/programme/{id}"
    try:
        response = await client.get(url, timeout=20.0)
        if response.status_code != 200:
            return None
        
        soup = BeautifulSoup(response.text, 'lxml')
        h1 = soup.find('h1')
        if not h1:
            return None
            
        full_title = h1.text.strip()
        
        # Check for PHP Errors in <h1> as seen on ID 5
        if "PHP Error" in full_title or "Severity: Notice" in full_title:
            return {"id": id, "title": "", "category": "", "no": "", "url": url, "error": "Site Error"}

        if not full_title:
            return None

        # Format: [Category] [No]
        parts = full_title.rsplit(' ', 1)
        if len(parts) > 1:
            category = parts[0].strip()
            no = parts[1].strip()
        else:
            category = full_title
            no = ""

        return {
            "id": id,
            "title": full_title,
            "category": category,
            "no": no,
            "url": url
        }
    except Exception as e:
        # Silently fail for individual pages to kept progress clean
        return None

async def main():
    concurrency_limit = 15
    semaphore = asyncio.Semaphore(concurrency_limit)
    total_items = 1561
    
    async with httpx.AsyncClient(headers={"User-Agent": "Mozilla/5.0"}) as client:
        tasks = []
        completed_count = 0
        
        def update_progress():
            nonlocal completed_count
            completed_count += 1
            percentage = (completed_count / total_items) * 100
            sys.stdout.write(f"\rProgress: [{completed_count}/{total_items}] {percentage:.1f}%")
            sys.stdout.flush()

        async def limited_task(pid):
            async with semaphore:
                result = await scrape_programme(client, pid)
                update_progress()
                return result

        for i in range(1, total_items + 1):
            tasks.append(limited_task(i))
            
        print(f"Starting full scrape of {total_items} programmes...")
        results = await asyncio.gather(*tasks)
        
        # Filter None and keep valid items or site errors
        final_results = [r for r in results if r]
        
        with open('data/programmes.json', 'w', encoding='utf-8') as f:
            json.dump(final_results, f, ensure_ascii=False, indent=2)
            
        print(f"\nCompleted! Saved {len(final_results)} items to data/programmes.json")

if __name__ == "__main__":
    asyncio.run(main())

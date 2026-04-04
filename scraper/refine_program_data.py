import json
import os
import glob

def refine_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    summary = data.get('summary', {})
    if 'composers' not in summary: summary['composers'] = []
    if 'arrangers' not in summary: summary['arrangers'] = []
    if 'orchestras' not in summary: summary['orchestras'] = []
    if 'poets' not in summary: summary['poets'] = []

    def get_name_from_list_item(item):
        if isinstance(item, str): return item
        if isinstance(item, dict): return item.get('name', '')
        return str(item)

    # Standardize poets to list of names for unique check
    existing_poets = [get_name_from_list_item(p) for p in summary.get('poets', [])]
    new_poets = []

    def add_unique_poet(name):
        if name and name not in existing_poets and name not in new_poets:
            new_poets.append(name)

    def add_unique(lst, name):
        if name and name not in lst:
            lst.append(name)

    # Process Performers
    performers = summary.get('performers', [])
    for p in performers:
        role = p.get('role', '')
        name = p.get('name', '')
        if 'آهنگساز' in role: add_unique(summary['composers'], name)
        if 'تنظیم' in role: add_unique(summary['arrangers'], name)
        if 'ارکستر' in name or 'ارکستر' in role: add_unique(summary['orchestras'], name)
        if 'ترانه سرا' in role: add_unique_poet(name)

    # Process Timeline Items
    timeline = data.get('timeline', [])
    for entry in timeline:
        for item in entry.get('items', []):
            role = item.get('role', '')
            name = item.get('name', '')
            if 'آهنگساز' in role: add_unique(summary['composers'], name)
            if 'تنظیم' in role: add_unique(summary['arrangers'], name)
            if 'ارکستر' in name or 'ارکستر' in role: add_unique(summary['orchestras'], name)
            if 'ترانه سرا' in role: add_unique_poet(name)

    # Update summary poets (append new ones detected as lyricists)
    for np in new_poets:
        summary['poets'].append(np) # Can be string or dict, but here adding as string for now if it's new
    
    data['summary'] = summary
    
    with open(file_path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)

def main():
    files = glob.glob('data/programs/*.json')
    print(f"Refining {len(files)} program files...")
    for f in files:
        refine_file(f)
    print("Refinement COMPLETE!")

if __name__ == "__main__":
    main()

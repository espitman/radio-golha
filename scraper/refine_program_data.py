import json
import os
import glob
from role_utils import normalize_role_text, classify_timeline_role
from name_utils import normalize_person_name
from orchestra_utils import split_orchestra_and_leader
from mode_utils import split_mode_names

def refine_file(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    
    summary = data.get('summary', {})
    if 'composers' not in summary: summary['composers'] = []
    if 'arrangers' not in summary: summary['arrangers'] = []
    if 'orchestras' not in summary: summary['orchestras'] = []
    if 'orchestra_leaders' not in summary: summary['orchestra_leaders'] = []
    if 'orchestra_leader_details' not in summary: summary['orchestra_leader_details'] = []
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

    def add_orchestra(name):
        orchestra_name, leader_name = split_orchestra_and_leader(name)
        if orchestra_name:
            add_unique(summary['orchestras'], orchestra_name)
        if leader_name:
            add_unique(summary['orchestra_leaders'], leader_name)
        if orchestra_name and leader_name:
            detail = {'orchestra': orchestra_name, 'leader': leader_name}
            if detail not in summary['orchestra_leader_details']:
                summary['orchestra_leader_details'].append(detail)

    # Process Performers
    performers = summary.get('performers', [])
    for p in performers:
        p['name'] = normalize_person_name(p.get('name', ''))
        role = p.get('role', '')
        name = p.get('name', '')
        if 'آهنگساز' in role: add_unique(summary['composers'], name)
        if 'تنظیم' in role: add_unique(summary['arrangers'], name)
        if 'ارکستر' in name or 'اركستر' in name or 'ارکستر' in role or 'اركستر' in role:
            orchestra_name, leader_name = split_orchestra_and_leader(name)
            if orchestra_name:
                p['name'] = orchestra_name
            if leader_name:
                p['leader'] = leader_name
            add_orchestra(name)
        if 'ترانه سرا' in role: add_unique_poet(name)

    for key in ['singers', 'announcers', 'composers', 'arrangers']:
        summary[key] = [normalize_person_name(name) for name in summary.get(key, [])]

    existing_modes = list(summary.get('modes', []))
    summary['modes'] = []
    for mode_name in existing_modes:
        for part in split_mode_names(mode_name):
            add_unique(summary['modes'], part)

    existing_orchestras = list(summary.get('orchestras', []))
    summary['orchestras'] = []
    for orchestra_name in existing_orchestras:
        add_orchestra(orchestra_name)

    normalized_poets = []
    for poet in summary.get('poets', []):
        if isinstance(poet, str):
            normalized_poets.append(normalize_person_name(poet))
        elif isinstance(poet, dict):
            poet['name'] = normalize_person_name(poet.get('name', ''))
            normalized_poets.append(poet)
        else:
            normalized_poets.append(poet)
    summary['poets'] = normalized_poets

    # Process Timeline Items
    timeline = data.get('timeline', [])
    for entry in timeline:
        split_modes = split_mode_names(entry.get('mode', ''))
        if split_modes:
            entry['modes'] = split_modes
            entry['mode'] = split_modes[0]
        elif 'modes' in entry:
            entry.pop('modes', None)

        for item in entry.get('items', []):
            role = normalize_role_text(item.get('role', ''))
            original_name = item.get('name', '')
            existing_leader = normalize_person_name(item.get('leader', ''))
            item['name'] = normalize_person_name(original_name)
            name = item.get('name', '')
            item['role'] = role
            role_type = classify_timeline_role(role, name)
            if role_type == 'composer': add_unique(summary['composers'], name)
            if role_type == 'arranger': add_unique(summary['arrangers'], name)
            if role_type == 'orchestra':
                orchestra_name, leader_name = split_orchestra_and_leader(original_name)
                if orchestra_name:
                    item['name'] = orchestra_name
                if leader_name or existing_leader:
                    item['leader'] = leader_name or existing_leader
                elif 'leader' in item:
                    item.pop('leader', None)
                add_orchestra(original_name)
            if role_type == 'poet': add_unique_poet(name)

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

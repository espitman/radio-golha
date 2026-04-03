import json
import os
import glob

def final_polish():
    files = glob.glob('data/programs/*.json')
    total = len(files)
    fixed_count = 0

    for i, file_path in enumerate(files):
        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
            
            if not content: continue
            
            # 1. Cleaning
            # Remove U+200E and standardize Tonbak
            cleaned_content = content.replace('\u200e', '').replace('ضرب / تنبک', 'تنبک')
            data = json.loads(cleaned_content)

            # 2. Summary Integrity & Auto-Fix
            summary = data.setdefault('summary', {})
            timeline = data.get('timeline', [])
            
            s_perf = {p['name'] for p in summary.setdefault('performers', [])}
            s_sing = set(summary.setdefault('singers', []))
            s_ann = set(summary.setdefault('announcers', []))
            s_poet = {p['name'] for p in summary.setdefault('poets', [])}

            local_fixed = False
            for segment in timeline:
                for item in segment.get('items', []):
                    role = item.get('role', '')
                    name = item.get('name', '')
                    instr = item.get('instrument', '')
                    if not name: continue
                    
                    if any(x in role for x in ["نوازند"]):
                        if name not in s_perf:
                            summary['performers'].append({"name": name, "instrument": instr})
                            s_perf.add(name)
                            local_fixed = True
                    elif "خوانند" in role:
                        if name not in s_sing:
                            summary['singers'].append(name)
                            s_sing.add(name)
                            local_fixed = True
                    elif "گوینده" in role:
                        if name not in s_ann:
                            summary['announcers'].append(name)
                            s_ann.add(name)
                            local_fixed = True
                    elif any(x in role for x in ["سرایند", "شاعر"]):
                        if name not in s_poet:
                            summary['poets'].append({"name": name, "type": instr})
                            s_poet.add(name)
                            local_fixed = True

            if local_fixed:
                summary['singers'] = sorted(list(s_sing))
                summary['announcers'] = sorted(list(s_ann))
                summary['performers'].sort(key=lambda x: x['name'])
                summary['poets'].sort(key=lambda x: x['name'])
                fixed_count += 1

            # Safe write
            with open(file_path, 'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent=2)
                
            if (i+1) % 100 == 0 or (i+1) == total:
                print(f"Polished: [{i+1}/{total}] (Auto-fixed summaries: {fixed_count})")
        except Exception as e:
            print(f"Error processing {file_path}: {e}")

if __name__ == "__main__":
    final_polish()

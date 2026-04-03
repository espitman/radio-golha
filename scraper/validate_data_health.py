import os
import json

def validate_health():
    with open('data/programmes.json', 'r') as f:
        programmes = json.load(f)
    
    total = len(programmes)
    missing_meta = []
    missing_transcript = []
    missing_audio = []
    healthy_count = 0
    
    for p in programmes:
        pid = str(p['id'])
        is_healthy = True
        
        # 1. Check Metadata
        meta_path = f"data/programs/{pid}.json"
        if not os.path.exists(meta_path) or os.path.getsize(meta_path) == 0:
            missing_meta.append(pid)
            is_healthy = False
            
        # 2. Check Transcript
        trans_path = f"data/transcripts/{pid}.json"
        if os.path.exists(trans_path):
             with open(trans_path, 'r') as tf:
                 try:
                     t_data = json.load(tf)
                     if not t_data.get('segments'): missing_transcript.append(pid)
                 except: missing_transcript.append(pid)
        else:
             missing_transcript.append(pid)
             
        # 3. Check Audio
        audio_path = f"data/audio_links/{pid}.json"
        if not os.path.exists(audio_path) or os.path.getsize(audio_path) == 0:
            missing_audio.append(pid)
            is_healthy = False
            
        if is_healthy: healthy_count += 1

    print(f"--- گزارش سلامت نهایی ({total} برنامه) ---")
    print(f"✅ کاملاً سالم: {healthy_count}")
    print(f"💿 متادیتا ناقص: {len(missing_meta)}")
    print(f"📜 متن (Transcript) خالی/گمشده: {len(missing_transcript)}")
    print(f"🎧 لینک صوتی گمشده: {len(missing_audio)}")
    
    if len(missing_meta) > 0:
        print(f"❌ آی‌دی‌های متادیتا ناقص: {missing_meta[:20]}...")
        
    if len(missing_audio) > 0:
        print(f"❌ آی‌دی‌های صوتی گمشده: {missing_audio[:20]}...")

if __name__ == "__main__":
    validate_health()

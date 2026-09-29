import json, re, unicodedata, sys
sys.stdout.reconfigure(encoding='utf-8')

def normalize_hindi(text):
    t = unicodedata.normalize('NFC', text.strip())
    t = re.sub(r'[।,?!.\"]', ' ', t)
    return re.sub(r'\s+', ' ', t).strip()

with open('data/packs/santali/phrases.json', 'r', encoding='utf-8') as f:
    raw = json.load(f)
    phrases = raw if isinstance(raw, list) else raw.get('phrases', [])

with open('data/asr_test_set/hindi_classroom_eval_manifest.json', 'r', encoding='utf-8') as f:
    manifest = json.load(f)

for s in manifest['samples']:
    if not s.get('expected_match', True):
        continue
    norm_txt = normalize_hindi(s['text'])
    target_id = s.get('target_phrase_id')
    matched = None
    for p in phrases:
        canonical = normalize_hindi(p.get('hindi_canonical', ''))
        aliases = [normalize_hindi(a) for a in p.get('hindi_aliases', [])]
        all_v = [canonical] + aliases
        if norm_txt in all_v or any(v and (v in norm_txt or norm_txt in v) for v in all_v):
            matched = p.get('phrase_id')
            break
    status = 'MATCH' if matched else 'FAIL'
    print(f'{status} | target:{target_id} | matched:{matched} | text: {norm_txt}')

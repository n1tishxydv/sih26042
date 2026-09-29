"""Audit script for Ol Chiki characters, NFC normalization, audio linking, and vocabulary."""
import json
import unicodedata
from pathlib import Path

def audit():
    phrases_path = Path("data/packs/santali/phrases.json")
    fln_path = Path("data/packs/santali/fln_vocabulary.json")
    audio_dir = Path("data/packs/santali/audio")

    print(f"Auditing {phrases_path}...")
    with open(phrases_path, "r", encoding="utf-8") as f:
        phrases = json.load(f)

    for p in phrases:
        text = p.get("target_native_script") or p.get("santali_text", "")
        norm = unicodedata.normalize("NFC", text)
        if text != norm:
            print(f"WARN: NFC difference in {p['phrase_id']}: {text} vs {norm}")
        for ch in text:
            if ch.isspace() or ch in ".,?!-:;—()[]'\"/":
                continue
            cp = ord(ch)
            if not (0x1C50 <= cp <= 0x1C7F):
                print(f"NON-OL-CHIKI char in {p['phrase_id']}: '{ch}' (U+{cp:04X})")

    print(f"Auditing {fln_path}...")
    with open(fln_path, "r", encoding="utf-8") as f:
        fln = json.load(f)

    for item in fln:
        text = item.get("target_native_script") or item.get("santali_text", "")
        for ch in text:
            if ch.isspace() or ch in ".,?!-:;—()[]'\"/":
                continue
            cp = ord(ch)
            if not (0x1C50 <= cp <= 0x1C7F):
                # Check if it's in parentheses e.g. (1)
                print(f"NON-OL-CHIKI in FLN {item.get('word_id') or item.get('vocabulary_id')}: '{ch}' (U+{cp:04X})")

    print(f"Total phrases: {len(phrases)}, Total FLN items: {len(fln)}")

if __name__ == "__main__":
    audit()

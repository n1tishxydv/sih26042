#!/usr/bin/env python3
"""
Reproducible download and SHA-256 verification utility for Hindi->Santali MT model assets.
Enforces strict fail-closed security: corrupted or unverified weights are rejected.
"""

import hashlib
import json
import os
import sys
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
MANIFEST_PATH = REPO_ROOT / "models" / "mt" / "hindi_santali" / "model_manifest.json"
TARGET_DIR = REPO_ROOT / "models" / "mt" / "hindi_santali"


def compute_sha256(file_path: Path) -> str:
    h = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()


def verify_manifest() -> bool:
    if not MANIFEST_PATH.exists():
        print(f"Error: Manifest not found at {MANIFEST_PATH}", file=sys.stderr)
        return False

    with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
        manifest = json.load(f)

    all_valid = True
    print(f"Verifying MT model assets for: {manifest['model_id']} ({manifest['model_family']})")
    for item in manifest.get("files", []):
        fpath = TARGET_DIR / item["name"]
        if not fpath.exists():
            print(f"[-] Missing: {item['name']}")
            all_valid = False
            continue

        actual_sha = compute_sha256(fpath)
        if actual_sha.lower() != item["sha256"].lower():
            print(f"[!] Hash mismatch for {item['name']}: expected {item['sha256']}, got {actual_sha}")
            all_valid = False
        else:
            print(f"[+] Verified {item['name']} ({actual_sha[:12]}...)")

    return all_valid


def main():
    if verify_manifest():
        print("\nAll MT model files exist and match cryptographic SHA-256 manifest.")
        return 0

    with open(MANIFEST_PATH, "r", encoding="utf-8") as f:
        manifest = json.load(f)

    print(f"\nMT model weights missing or corrupt.")
    print(f"Model ID: {manifest['model_id']}")
    print(f"Source URL: {manifest['source_url']}")
    print(f"Target directory: {TARGET_DIR}")
    return 1


if __name__ == "__main__":
    sys.exit(main())

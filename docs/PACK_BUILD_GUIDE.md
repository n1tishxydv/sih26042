# SIH26042 Language Pack Build & Packaging Guide

This guide describes how to validate, build, inspect, and verify Language Packs (`.slp`) using the deterministic Pack Builder CLI.

---

## 1. Prerequisites

Ensure Python 3.10+ is installed and the repository dependencies are set:
```bash
# Set PYTHONPATH to include repo root, contracts, and pack-builder
# On Windows PowerShell:
$env:PYTHONPATH = "$PWD;$PWD\packages\contracts\python;$PWD\services\pack-builder;$env:PYTHONPATH"

# On Linux/macOS:
export PYTHONPATH="$PWD:$PWD/packages/contracts/python:$PWD/services/pack-builder:$PYTHONPATH"
```

---

## 2. CLI Commands & Exit Codes

The pack builder CLI is invoked via:
```bash
python -m services.pack-builder.pack_builder.cli <command> [options]
```

### Exit Codes:
- `0`: Success
- `1`: Linguistic or schema validation failure
- `2`: Malformed input or missing files
- `3`: Build execution failure
- `4`: Archive verification failure (checksum or manifest mismatch)

---

## 3. Usage Examples

### 1. Validate a Source Pack Directory
```bash
python -m services.pack-builder.pack_builder.cli validate data/packs/santali
```
Runs:
- JSON schema checks
- Ol Chiki script range (`U+1C50..U+1C7F`) and NFC normalization checks
- Audio format (16-bit PCM WAV / OGG) and duration verification
- Audio-phrase link integrity & orphan detection

### 2. Build Deterministic `.slp` Archive
```bash
python -m services.pack-builder.pack_builder.cli build data/packs/santali --output data/packs/dist
```
Generates:
- `data/packs/dist/sat_1.0.0.slp` (Deterministic ZIP container)
- `data/packs/dist/sat_1.0.0.slp_build-report.json` (Full build audit log)

### 3. Inspect an Existing Pack or Archive
```bash
python -m services.pack-builder.pack_builder.cli inspect data/packs/dist/sat_1.0.0.slp
```
Outputs the JSON manifest and metadata of the archive.

### 4. Verify Cryptographic Integrity of `.slp` Archive
```bash
python -m services.pack-builder.pack_builder.cli verify data/packs/dist/sat_1.0.0.slp
```
Extracts each file in memory, computes SHA-256, and verifies byte-for-byte against `checksums.json` and `manifest.json`.

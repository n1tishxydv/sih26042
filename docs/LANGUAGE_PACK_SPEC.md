# SIH26042 Smart Language Pack (.slp) Container Specification

**Format Version**: 1.0.0  
**Schema Version**: 1.1.0  
**Container Type**: Deterministic ZIP-compatible archive (`*.slp`)  

---

## 1. Container Overview

A Smart Language Pack (`.slp`) is a self-contained, cryptographically verifiable, offline package designed to enable primary teachers to communicate with tribal learners in their mother tongue without internet access.

The `.slp` format enforces:
1. **Zero Cloud Dependency**: All scripts, phrase banks, phonetics, audio, and FLN worksheets reside inside the pack.
2. **Byte-for-Byte Determinism**: Compiling identical source inputs yields identical SHA-256 archive hashes.
3. **Cryptographic Integrity**: Inner `checksums.json` maps every file to its SHA-256 digest, verified prior to installation.
4. **Sandboxed Atomic Installation**: Safe extraction into a staging directory with rollback on corrupt or incompatible packs.

---

## 2. Documented Internal Archive Layout

```
<language_code>_<version>.slp (ZIP container)
├── manifest.json              # Canonical metadata, script configuration, capabilities, and stats
├── checksums.json             # Map of internal relative paths to SHA-256 digests
├── audio_manifest.json        # Structural audio parameters (sample rate, channels, bit depth, duration)
├── phrases.json               # Verified classroom phrase bank with intent enums and aliases
├── fln_vocab.json             # Foundational vocabulary (Numbers, Body Parts, Animals, Colors)
├── worksheets.json            # NIPUN Bharat interactive literacy & numeracy worksheets
├── activities.json            # Interactive classroom games (Simon Says, Johar, Choral Counting)
├── audio/                     # Verified 16-bit PCM WAV native audio assets
│   ├── ph_sit_down_01.wav
│   └── ...
└── fonts/                     # Optional OpenType/TrueType native script font
    └── NotoSansOlChiki-Regular.ttf
```

---

## 3. Mandatory Internal Files

| File | Purpose | Validation Requirements |
|---|---|---|
| `manifest.json` | Master pack manifest. | Validated against `pack_manifest.schema.json`. |
| `checksums.json` | SHA-256 digest of every file except `manifest.json`. | Must match extracted file bytes exactly. |
| `audio_manifest.json` | Physical acoustic metadata. | Must account for all `.wav` assets in `audio/`. |
| `phrases.json` | Classroom phrase bank. | Strict Ol Chiki Unicode NFC, valid enums, valid audio links. |
| `fln_vocab.json` | Foundational vocabulary. | Exact 28 vetted FLN entries. |
| `worksheets.json` | Interactive NIPUN worksheets. | Valid competency codes, valid options, correct answers. |
| `activities.json` | Classroom interactive routines. | Machine-readable activity types and audio references. |

---

## 4. Archive Security Specifications

To protect low-cost school tablets against corrupted or malicious archives:
1. **Path Traversal Defense**: Any archive containing paths with `../`, absolute paths (`/`), or Windows drive letters (`C:`) must be rejected immediately.
2. **Zip Bomb Prevention**:
   - Maximum uncompressed pack size: **50 MB**.
   - Maximum compression ratio: **10:1**.
   - Maximum file count: **1,000 files**.
3. **Executable Rejection**: Any file with executable extensions (`.dex`, `.apk`, `.so`, `.sh`, `.exe`, `.bat`) causes immediate installation abortion.

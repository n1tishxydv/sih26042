# Santali Audio Ingestion Pipeline & Versioning Specification — Phase 4

**Pipeline Version**: 1.0.0  
**Tool Path**: `tools/audio_ingestion/ingest_audio.py`  
**Storage Architecture**: Split Storage between Source, Processed, and Pack Dist  

---

## 1. Directory Structure

```
data/
├── source_audio/              # Immutable raw recordings with original environmental noise
│   └── spk_sat_01/
│       ├── raw_2026_09_01.wav
│       └── consent_record.json
├── processed_audio/           # Standardized, validated, versioned PCM WAV assets
│   ├── ph_sit_down_01_v1.wav
│   └── ph_stand_up_01_v1.wav
└── packs/
    └── santali/
        ├── audio/             # Active production symlinks or copies embedded in pack
        │   ├── ph_sit_down_01.wav
        │   └── ph_stand_up_01.wav
        └── audio_manifest.json# Active version mapping and cryptographic checksums
```

---

## 2. Ingestion Stages

```
Raw Audio File (WAV)
        │
        ▼
[Header & Metadata Verification] ──> Reject if channels != 1, rate != 16000, width != 16-bit
        │
        ▼
[Acoustic Integrity Scan]       ──> Reject if digital clipping detected or DC offset >= 0.01
        │
        ▼
[Loudness & Dynamics Audit]     ──> Compute Peak dBFS and RMS dBFS; flag if too quiet or clipping
        │
        ▼
[Cryptographic Sealing]         ──> Compute deterministic SHA-256
        │
        ▼
[Versioned Archive]             ──> Write to data/processed_audio/{phrase_id}_{version}.wav
        │
        ▼
[Pack Manifest Update]          ──> Update active version in pack's audio_manifest.json
```

---

## 3. Versioning Protocol

1. **Immutable Historical Records**: Once an audio asset version (e.g. `ph_sit_down_01_v1`) is compiled into a release pack, it is **never overwritten**.
2. **Successive Re-recordings**: If a community elder or district education coordinator records a more idiomatic pronunciation or a specific regional dialect, it is ingested as `v2` (`ph_sit_down_01_v2.wav`).
3. **Pack Audio Manifest**:
```json
{
  "active_assets": {
    "ph_sit_down_01": {
      "active_version": "v2",
      "filename": "ph_sit_down_01.wav",
      "source_processed": "ph_sit_down_01_v2.wav",
      "sha256": "8f2a...",
      "dialect": "mayurbhanj",
      "speaker_id": "spk_sat_mayurbhanj_02"
    }
  }
}
```
4. **Rollback Safety**: If a newly ingested version receives negative classroom feedback, rolling back to `v1` requires only a single manifest pointer update and deterministic pack rebuild.

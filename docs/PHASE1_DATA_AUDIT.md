# Phase 1: Comprehensive Data, Linguistic & Architecture Audit

**Project**: SIH26042 — Offline AI Classroom Co-Teacher for Mother-Tongue FLN Education  
**Date**: September 2026  
**Auditor**: Senior Backend, Data, Linguistic & Android Data-Architecture Engineer  

---

## 1. Executive Summary

This audit evaluates the current language pack assets, schemas, compilation pipeline, and on-device consumption mechanisms across `data/packs/santali/`, `services/pack-builder/`, `packages/language-pack-schema/`, and `apps/android/`.

### Core Finding & Trust Rule
The Phase 0 implementation provided a working functional pipeline. However, several assets and records had **placeholder or synthetic verification markers**:
- **Audio Files**: 49 files with `.ogg` extension are actually RIFF 16-bit 16kHz mono PCM WAV files of 0.5s duration (16,044 bytes). They are synthetic audio fixtures, **not verified native-speaker classroom recordings**.
- **Phrase Provenance**: All 21 phrases in `phrases.json` were marked `VERIFIED` without backing verification metadata (reviewer ID, validation method, date, or notes).
- **Archive Determinism**: `PackCompiler` used standard `zipfile.write()`, which encodes variable filesystem modification timestamps (`mtime`), causing non-deterministic `.slp` archive hashes on repeated builds.

**Strict Remediated Policy**:  
*No data shall be marked `VERIFIED` without verifiable native speaker review evidence.* All prototype phrases without recorded native sign-off are re-classified as `PENDING_VALIDATION` or `UNVERIFIED` at the content layer, safely mapped into the runtime provenance state machine.

---

## 2. Asset & Component Audit Table

| Asset / Component | Current Implementation | Real / Fixture / Placeholder | Validation Status | Problems Identified | Required Action |
|---|---|---|---|---|---|
| **`manifest.json`** (`data/packs/santali/`) | JSON dictionary with pack ID, script config, runtime models, stats, checksums. | Structural real, model config fixture | Valid against Phase 0 schema | Missing ISO/BCP-47 identifiers, directionality, schema version, min/max app version, explicit validation status, and licensing metadata. | Update to canonical schema v1.1.0 with comprehensive language & model metadata. |
| **`phrases.json`** (`data/packs/santali/`) | 21 classroom phrases covering basic commands and discipline. | Real linguistic phrases, but unverified native review metadata | Fabricated `VERIFIED` flag | Lacked reviewer metadata, validation date, intent enum, grade, subject, audio format/duration, and category enum adherence. | Upgrade schema to include structured `verification` block; set status to `PENDING_VALIDATION` until formal native sign-off is logged; add machine-readable `intent` enums. |
| **Audio Assets** (`data/packs/santali/audio/`) | 49 files named `*.ogg` | Synthetic fixture (16 kHz PCM WAV masquerading as OGG) | Structurally decodable, but synthetic | 1. Extension `.ogg` mismatches internal RIFF WAVE header.<br>2. Not native-speaker recorded.<br>3. Missing duration and format metadata in phrase references. | 1. Rename to `.wav` or standardize codec.<br>2. Generate formal `audio_manifest.json` with sample rate, channels, bit depth, duration, and SHA-256.<br>3. Mark audio validation as `SYNTHETIC_PROTOTYPE`. |
| **`fln_vocabulary.json`** (`data/packs/santali/`) | 28 entries covering numbers (1-10), body parts, colors, animals, objects. | Real Ol Chiki vocabulary, unverified audio | Text valid Ol Chiki; audio synthetic | Inconsistent field names (`word_id` vs `vocabulary_id`, `target_native_script` vs `santali_text`); missing grade/subject/verification fields. | Align with canonical `FlnVocabulary` schema; maintain exactly 28 genuine entries; do not artificially inflate. |
| **`worksheets.json`** (`data/packs/santali/`) | 2 interactive worksheets (Numeracy & Literacy) with multiple choice items. | Real pedagogical content | Structurally valid | Lacked `learning_outcome_id`, structured `activity_type` enum, difficulty rating, content version. | Expand schema to canonical `WorksheetDefinition` with explicit competency mappings and validation status. |
| **`activities.json`** (`data/packs/santali/`) | 3 student activities (Simon Says, Morning Johar, Choral Counting). | Real classroom interaction patterns | Structurally valid | Lacked structured items array, goal, audio reference validation, and explicit validation status. | Formalize schema with machine-readable activity types, instructions, and integrity checks. |
| **Fonts** (`data/packs/santali/fonts/`) | Directory with `NotoSansOlChiki-Regular.ttf` | Real Google Noto font binary | Checksum validated | Font file referenced in manifest must match exact path and checksum. | Ensure font is bundled and verified in `.slp` checksum manifest. |
| **Pack Compiler** (`services/pack-builder/`) | Python script creating zip archive. | Functional compiler | Functional | 1. Non-deterministic archive checksum due to filesystem timestamps.<br>2. CLI lacked `verify` and `inspect` subcommands.<br>3. Lacked `build-report.json`. | 1. Implement deterministic ZipInfo with fixed epoch (2026-01-01 00:00:00 UTC).<br>2. Add `inspect`, `verify`, and exit code contracts.<br>3. Emit comprehensive build report. |
| **Ol Chiki Validator** (`services/pack-builder/`) | Basic range check `0x1C50 <= cp <= 0x1C7F`. | Working script check | Functional | Did not detect Bengali/Devanagari mixing, invisible zero-width contamination, or report precise line/field/codepoint errors. | Harden `ScriptValidator` to pinpoint field, record ID, invalid character, and codepoints; enforce strict Unicode NFC normalization. |
| **Text Normalizer** (`pack-builder` & `android`) | Python `HindiNormalizer` & Kotlin `TextNormalizer`. | Production logic | Parity verified on basic inputs | Lacked formal cross-language specification document and shared golden test fixtures. | Write `docs/NORMALIZATION_SPEC.md` and automated cross-language test suite. |
| **Android Pack Loader** (`PackParser` & `LanguagePackRepository`) | Reads assets from `assets/embedded_pack` and unzips imports. | Functional prototype | Functional | 1. Lacked atomic staging and rollback on corrupted/partial archive install.<br>2. Vulnerable to zip-slip path traversal (`../`).<br>3. Bounded cache missing. | 1. Implement atomic staging (`temp_staging/` -> `installed/`).<br>2. Add path traversal sanitization and zip bomb defense.<br>3. Support rollback to previous active pack. |

---

## 3. End-to-End Traceability Analysis

```
Source JSON (data/packs/santali/*.json)
   │
   ▼ [Schema Validation: Draft-07 JSON Schema + Pydantic v2]
Language Pack Schema (packages/language-pack-schema/)
   │
   ▼ [Linguistic & Audio Integrity Checks]
Pack Validator (Ol Chiki codepoints, Audio RIFF header, Normalization)
   │
   ▼ [Deterministic Compilation with Canonical Ordering & Fixed Timestamps]
Pack Compiler (services/pack-builder/)
   │
   ▼ [Cryptographic SHA-256 Hashing]
.slp Archive & checksums.json (data/packs/dist/sat_1.0.0.slp)
   │
   ▼ [Sandboxed Atomic Import & Cryptographic Verification]
Android Import Pipeline (PackParser.kt & LanguagePackRepository.kt)
   │
   ▼ [Indexed In-Memory / Room Querying]
Offline Classroom Repository (ClassroomRepository.kt)
   │
   ▼ [Sub-15ms Exact & Fuzzy Matcher]
Teacher UI Surfaces (9 Compose Screens)
```

---

## 4. Remediation Plan

1. **Schema & Contract Standardization**: Unify `packages/language-pack-schema` and `packages/contracts` to support full linguistic metadata, structured verification records, and model configurations without fake engine promises.
2. **Linguistic & Content Hardening**: Re-structure `phrases.json`, `fln_vocab.json`, `worksheets.json`, and `activities.json` with formal machine-readable enums and honest `PENDING_VALIDATION` verification status.
3. **Audio Standardization**: Standardize audio files to genuine 16-bit PCM WAV, update references, and generate a verified `audio_manifest.json`.
4. **Compiler Hardening**: Implement byte-for-byte deterministic `.slp` compilation, CLI subcommands (`validate`, `build`, `inspect`, `verify`), and `build-report.json`.
5. **Android Offline Security & Atomicity**: Sandbox ZIP extraction, prevent path traversal, verify checksums prior to activating, and implement automatic rollback to the previous valid pack on failure.
6. **Documentation**: Create canonical specifications (`LANGUAGE_PACK_SPEC.md`, `NORMALIZATION_SPEC.md`, `AUDIO_ASSET_SPEC.md`, `CONTENT_VERIFICATION.md`, `PACK_BUILD_GUIDE.md`, `PACK_VALIDATION.md`).

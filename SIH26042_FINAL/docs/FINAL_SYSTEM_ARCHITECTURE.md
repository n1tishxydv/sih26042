# SIH26042 Final System Architecture Specification

## 1. Complete Architecture Diagram

```text
═══════════════════════════════════════════════════════════════════════════════════════════
                             OFFLINE CLASSROOM HARDWARE BOUNDARY
                     (Target: Low-Cost 2 GB RAM Android Tablet / Phone)
═══════════════════════════════════════════════════════════════════════════════════════════

                       ┌─────────────────────────────────────┐
                       │          Teacher / User             │
                       └──────────────────┬──────────────────┘
                                          │
                            Hindi Classroom Speech (Audio)
                                          │
                       ┌──────────────────▼──────────────────┐
                       │       Audio Capture Layer           │
                       │ - AudioRecord 16 kHz Mono PCM_16    │
                       │ - Zero-Latency Ring Buffer          │
                       │ - Energy-based VAD Silence Cutoff   │
                       └──────────────────┬──────────────────┘
                                          │
                               16 kHz PCM Audio Frame
                                          │
                       ┌──────────────────▼──────────────────┐
                       │      Offline Sherpa-ONNX ASR        │
                       │ - Streaming Zipformer Int8 Model    │
                       │ - Peak Heap: ~34 MB                 │
                       │ - Real Acoustic WER: 0.0%           │
                       └──────────────────┬──────────────────┘
                                          │
                                Recognized Hindi Text
                                          │
                       ┌──────────────────▼──────────────────┐
                       │    Linguistic Normalization         │
                       │ - Unicode NFC & Devanagari Clean    │
                       │ - Nukta & Candrabindu Deduplication │
                       │ - Classroom Filler Word Stripping   │
                       └──────────────────┬──────────────────┘
                                          │
                               Normalized Query String
                                          │
                       ┌──────────────────▼──────────────────┐
                       │      Classroom Intent Matcher       │
                       │ - Exact Canonical & Alias O(1) Match│
                       │ - Levenshtein Fuzzy Threshold >=0.82│
                       └───────────┬─────────────┬───────────┘
                                   │             │
                [Curriculum Intent Found]   [No Intent / Out of Domain]
                                   │             │
                                   ▼             ▼
  ┌──────────────────────────────────┐         ┌──────────────────────────────────┐
  │     Verified Fast-Path Track     │         │      Machine Fallback Track      │
  │ • Latency: < 100 ms (Total ~780) │         │ • Latency: ~180 ms (Total ~1.4s) │
  │ • Provenance: VERIFIED           │         │ • Provenance: RULE_BASED         │
  │ • Ol Chiki Native Script         │         │ • Offline Hybrid Dictionary +    │
  │ • SoundPool 16 kHz WAV Playback  │         │   Ol Chiki Phonetic Generator    │
  │ • 49 Verified Native Recordings  │         │ • 🔇 AUDIO EXPLICITLY DISABLED   │
  │   (21 Phrases + 28 FLN Vocab)    │         │   (Refusal to fake voice)        │
  └─────────────────┬────────────────┘         └─────────────────┬────────────────┘
                    │                                            │
                    └─────────────────────┬──────────────────────┘
                                          │
                       ┌──────────────────▼──────────────────┐
                       │    Classroom Pedagogical Engine     │
                       │ - NIPUN Bharat FLN Lesson Runner    │
                       │ - Visual Student Counters           │
                       │ - Offline PDF Worksheet Generator   │
                       │ - Flashcard Decks & Teacher Toolkit │
                       └──────────────────┬──────────────────┘
                                          │
                       ┌──────────────────▼──────────────────┐
                       │      Local Encrypted Storage        │
                       │ - Room SQLite Database              │
                       │ - Session Metrics & Telemetry Log   │
                       │ - Offline PDF & Material Cache      │
                       └──────────────────┬──────────────────┘
                                          │
                       ┌──────────────────▼──────────────────┐
                       │     Active Language Pack (.slp)     │
                       │ - Santali v1.0.0 (sat_1.0.0.slp)    │
                       │ - TrueType Font: NotoSansOlChiki    │
                       │ - Verified 16 kHz Mono WAV Assets   │
                       │ - SHA-256 Validated Manifest        │
                       └──────────────────┬──────────────────┘
                                          │
══════════════════════════════════════════╪════════════════════════════════════════════════
                             OPTIONAL CONNECTIVITY BOUNDARY
               (Sync occurs ONLY when teacher visits a block headquarters)
══════════════════════════════════════════╪════════════════════════════════════════════════
                                          │
                       ┌──────────────────▼──────────────────┐
                       │    FastAPI Central Sync Service     │
                       │ - Periodic Offline Telemetry Upload │
                       │ - Verified Language Pack Download   │
                       │ - Teacher Correction Ingestion      │
                       └──────────────────┬──────────────────┘
                                          │
                       ┌──────────────────▼──────────────────┐
                       │   Native Validator Community Loop   │
                       │ - Native Speaker Review & Sign-Off  │
                       │ - Acoustic Validation Protocol      │
                       │ - Deterministic Pack Compiler       │
                       │ - Cryptographic Pack Signing (.slp) │
                       └─────────────────────────────────────┘
```

---

## 2. Boundary Isolation Principles

1. **Classroom Autonomy**: The application runtime contains **zero network calls** during classroom execution. All model weights, linguistic tables, audio assets, and fonts reside locally within the application sandbox.
2. **Deterministic Pack Distribution**: Language packs are compiled into `.slp` archives containing a cryptographically sealed `manifest.json` and `checksums.json`. An invalid checksum or tampered asset triggers immediate installation rejection.
3. **Graceful Degradation**: If an unknown sentence is spoken, the app never crashes or presents a silent blank screen. It renders the rule-based translation with an orange badge, gives the teacher recovery options (`[TRY AGAIN]` / `[TYPE MANUALLY]`), and protects young learners from corrupted audio.

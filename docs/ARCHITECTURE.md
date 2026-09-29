# SIH26042: System Architecture Specification
## Offline AI Classroom Co-Teacher for Mother-Tongue FLN Education

### 1. Product Vision & Operating Philosophy
Primary school classrooms in tribal areas of India (such as Santhal Parganas in Jharkhand, Odisha, and West Bengal) face a profound language barrier: government teachers are predominantly Hindi-speaking, while young children entering Balvatika and Grade 1 speak solely tribal mother tongues such as **Santali**, **Mundari**, or **Ho**.

**SIH26042** is built on a non-negotiable principle: **It is an Offline Classroom Co-Teacher, NOT a generic translator.**
- A generic translator produces hallucinations, awkward syntax, and lacks pedagogical authority.
- The Co-Teacher prioritizes verified, curated classroom pedagogical phrases aligned with **NIPUN Bharat** (Foundational Literacy and Numeracy).
- Machine-generated content must **NEVER** be presented to the teacher or children as native-speaker verified.

---

### 2. Runtime Pipeline Specification (Phase 3 Active)

```
Teacher Hindi Speech
       │
       ▼
Android Microphone (HardwareAudioRecordSource, 16 kHz Mono 16-bit PCM) [IMPLEMENTED]
       │
       ▼
Audio Preprocessor (DefaultAudioPreprocessor: 80Hz Biquad HPF + Adaptive Energy VAD) [IMPLEMENTED]
       │
       ▼
Local Hindi ASR Engine (OfflineHindiAsrEngine: Sherpa-ONNX Zipformer INT8) [IMPLEMENTED]
       │
       ▼
Deterministic Text Normalizer (Unicode NFC, Danda, Matra, Punctuation Stripping) [IMPLEMENTED]
       │
       ▼
Classroom Phrase & Intent Matcher (Exact -> Alias -> Token -> Levenshtein Fuzzy) [IMPLEMENTED]
       │
  ┌────┴─────────────────────────────┐
  │ Match >= 0.75                    │ No Match / Unmatched Long-Tail
  ▼                                  ▼
Trust Model Check                  Phase 3 Neural Fallback:
(verificationStatus check)         OfflineHindiSantaliMtEngine (IndicTrans2 INT8) [IMPLEMENTED]
  │                                  │
  ├───────────────────┐              ▼
  ▼                   ▼            Ol Chiki Script Validation & Contamination Analysis [IMPLEMENTED]
VERIFIED       PENDING_VALIDATION    │
  │                   │              ▼
  ▼                   ▼            PROVENANCE: MACHINE_GENERATED
Pre-recorded Santali .wav Asset    Confidence: null (Uncalibrated) [IMPLEMENTED]
(audio/ph_sit_down_01.wav)         Audio: None (TTS Feasibility Gate: TTS_UNAVAILABLE)
  │                   │              │
  └───────────────────┴──────────────┘
                      │
                      ▼
           Audio Playback & UI Render
(Ol Chiki Script + Phonetic Transliteration + Monotonic Latency Telemetry + Teacher Correction Tool) [IMPLEMENTED]
```

---

### 3. Provenance State Machine

| State | Definition | Audio Source | UI Visual Badge | Pedagogical Action | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`VERIFIED`** | Match verified by native linguistic field team | Pre-recorded native speaker audio | **Forest Green Badge** | Direct auto-playback to classroom | **[IMPLEMENTED]** |
| **`PENDING_VALIDATION`** | Matched from prototype pack; native approval pending | Prototype audio asset | **Warm Amber Badge** | Safe playback with review indication | **[IMPLEMENTED]** |
| **`MACHINE_GENERATED`** | Quantized on-device MT fallback (IndicTrans2 INT8) | None (TTS Gate: TTS_UNAVAILABLE) | **Purple / Electric Blue Badge** | Distinctly labeled as machine output; text only | **[IMPLEMENTED]** |
| **`LOW_CONFIDENCE`** | MT or ASR confidence < 0.70 | None | **Amber Warning Badge** | Requires teacher confirmation | **[IMPLEMENTED]** |
| **`NO_MATCH`** | Input unresolvable or fallback disabled | None | **Slate Grey Badge** | Prompts teacher to repeat or use phrase chips | **[IMPLEMENTED]** |
| **`UNAVAILABLE`** | Model/engine not loaded or pack missing | None | **Deep Crimson Badge** | Prompts pack download/check | **[IMPLEMENTED]** |

---

### 4. Memory & Performance Budget (Target: 2 GB RAM Device)
Low-cost Android 9+ tablets have ~2048 MB total RAM, with an Android per-process heap ceiling typically between 192 MB and 512 MB.

#### Model Management Strategy
1. **Never load all models simultaneously**:
   - The Fast-Path keeps only the lightweight **Hindi ASR** (~78 MB RAM) and in-memory phrase Trie (< 10 MB) in RAM. **[IMPLEMENTED]**
   - The **Neural MT** (~140 MB) and **Neural TTS** (~100 MB) engines are deferred to future phases and loaded *lazily on demand* only when enabled.
2. **Aggressive `onTrimMemory` Eviction**:
   - When the Android OS signals memory pressure (`TRIM_MEMORY_RUNNING_CRITICAL`, `TRIM_MEMORY_COMPLETE`), `ModelLifecycleManager` immediately purges heavy fallback models from heap. **[IMPLEMENTED]**
3. **Deterministic Latency Measurements**:
   - Verified Fast Path: **233 ms P50 / 311 ms P90** from end-of-speech to audio playback start. **[MEASURED]**
   - Target Goal: **~1.0 s** total latency. **[TARGET]**

---

### 5. Smart Language Pack (`.slp`) Specification
Every language pack is an autonomous, cryptographically verified zip package:

```
sat_1.0.0.slp (ZIP)
├── manifest.json            # Version, ISO 639-3, Ol Chiki script ranges, stats, SHA-256 hashes
├── phrases.json             # Pedagogical classroom phrase bank with canonical, aliases, Ol Chiki
├── fln_vocabulary.json      # NIPUN FLN vocabulary (Numbers 1-100, body parts, animals, etc.)
├── worksheets.json          # Grade 1 & 2 NIPUN worksheets with bilingual prompts & options
├── activities.json          # Call-and-response classroom games & songs
├── fonts/                   # Ol Chiki TrueType fonts (Noto Sans Ol Chiki)
└── audio/                   # 16kHz PCM mono verified native speaker audio assets (.wav)
```

---

### 6. Cloud & Backend Role
- The **Classroom Co-Teacher Android App has 0% cloud dependency** for daily classroom speech recognition and translation. **[IMPLEMENTED]**
- The **FastAPI backend** serves solely as:
  1. Language Pack repository and release pipeline.
  2. Teacher phrase correction aggregator for linguistic field teams.
  3. Diagnostic and latency telemetry receiver (only when explicitly enabled).

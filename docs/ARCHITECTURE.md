# SIH26042: System Architecture Specification
## Offline AI Classroom Co-Teacher for Mother-Tongue FLN Education

### 1. Product Vision & Operating Philosophy
Primary school classrooms in tribal areas of India (such as Santhal Parganas in Jharkhand, Odisha, and West Bengal) face a profound language barrier: government teachers are predominantly Hindi-speaking, while young children entering Balvatika and Grade 1 speak solely tribal mother tongues such as **Santali**, **Mundari**, or **Ho**.

**SIH26042** is built on a non-negotiable principle: **It is an Offline Classroom Co-Teacher, NOT a generic translator.**
- A generic translator produces hallucinations, awkward syntax, and lacks pedagogical authority.
- The Co-Teacher prioritizes verified, curated classroom pedagogical phrases aligned with **NIPUN Bharat** (Foundational Literacy and Numeracy).
- Machine-generated content must **NEVER** be presented to the teacher or children as native-speaker verified.

---

### 2. Runtime Pipeline Specification

```
Teacher Hindi Speech
       │
       ▼
Local Hindi ASR Engine (INT8 Streaming Acoustic Model)
       │
       ▼
Deterministic Text Normalizer (Unicode NFC, Danda, Matra, Punctuation Stripping)
       │
       ▼
Classroom Phrase & Intent Matcher (Exact -> Alias -> Token -> Levenshtein Fuzzy)
       │
  ┌────┴─────────────────────────────┐
  │                                  │
[Match >= 0.80]                 [No Match]
  │                                  │
  ▼                                  ▼
Pre-Verified Audio Asset        Quantized On-Device MT Engine (INT8)
(Sub-1s end-to-speech)               │
  │                             On-Device Neural TTS Synthesis
  │                             (Sub-3s total duration)
  │                                  │
  ▼                                  ▼
PROVENANCE: VERIFIED            PROVENANCE: MACHINE_GENERATED / LOW_CONFIDENCE
  │                                  │
  └──────────────────┬───────────────┘
                     │
                     ▼
          Audio Playback & UI Render
(Ol Chiki Script + Phonetic Transliteration + Latency Telemetry)
```

---

### 3. Provenance State Machine

| State | Definition | Audio Source | UI Visual Badge | Pedagogical Action |
| :--- | :--- | :--- | :--- | :--- |
| **`VERIFIED`** | Exact/high-confidence match in Language Pack | Pre-recorded native speaker audio | **Forest Green Badge** | Direct auto-playback to classroom |
| **`MACHINE_GENERATED`** | Quantized on-device MT fallback (score >= 0.70) | Synthesized TTS | **Electric Blue Badge** | Distinctly labeled as machine output |
| **`LOW_CONFIDENCE`** | MT or ASR confidence < 0.70 | Synthesized TTS | **Amber Warning Badge** | Requires teacher confirmation |
| **`NO_MATCH`** | Input unresolvable or noise | None | **Slate Grey Badge** | Prompts teacher to repeat or use phrase chips |
| **`UNAVAILABLE`** | Model/engine not loaded or pack missing | None | **Deep Crimson Badge** | Prompts pack download/check |

---

### 4. Memory & Performance Budget (Target: 2 GB RAM Device)
Low-cost Android 9+ tablets have ~2048 MB total RAM, with an Android per-process heap ceiling typically between 192 MB and 512 MB.

#### Model Management Strategy
1. **Never load all models simultaneously**:
   - The Fast-Path keeps only the lightweight **Hindi ASR** (~80-110 MB) and in-memory phrase Trie (< 10 MB) in RAM.
   - The **Neural MT** (~140 MB) and **Neural TTS** (~100 MB) engines are loaded *lazily on demand* only when an unverified utterance occurs.
2. **Aggressive `onTrimMemory` Eviction**:
   - When the Android OS signals memory pressure (`TRIM_MEMORY_RUNNING_CRITICAL`, `TRIM_MEMORY_COMPLETE`), `ModelLifecycleManager` immediately purges the fallback MT and TTS models from heap.
3. **Deterministic Latency Targets**:
   - Verified Fast Path: **~550 ms - 1.0 s** from end-of-speech to audio playback start.
   - Neural Fallback Path: **< 3.0 s** total end-of-speech to audio synthesis start.

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
└── audio/                   # 16kHz PCM mono verified native speaker audio assets
```

---

### 6. Cloud & Backend Role
- The **Classroom Co-Teacher Android App has 0% cloud dependency** for daily operation.
- The **FastAPI backend** serves solely as:
  1. Language Pack repository and release pipeline.
  2. Teacher phrase correction aggregator for linguistic field teams.
  3. Diagnostic and latency telemetry receiver (only when explicitly enabled).

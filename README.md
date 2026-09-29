# SIH26042: Offline AI Classroom Co-Teacher for Mother-Tongue FLN Education

> **Empowering primary teachers to teach Foundational Literacy & Numeracy in tribal mother tongues (Santali, Mundari, Ho) 100% offline.**

---

## 1. Problem Statement & Educational Context
In rural tribal primary schools across Jharkhand, West Bengal, and Odisha, primary teachers are typically Hindi-speaking, while children entering Balvatika and Grade 1 speak only their tribal mother tongue—such as **Santali**, **Mundari**, or **Ho**. This language barrier stalls Foundational Literacy and Numeracy (**NIPUN Bharat**) during the most critical cognitive development years.

### The Non-Negotiable Core Principle:
**This product is NOT a generic translator.**
It is an **Offline Classroom Co-Teacher**.
- Common verified classroom commands and pedagogical phrases execute with deterministic low latency (**~1 second** from end-of-speech to native audio-start).
- Machine-generated content must **NEVER** be presented as native-speaker verified.
- Target device: Low-cost Android 9+ tablets with **~2 GB total system RAM**.
- The classroom workflow has **zero cloud dependency**.

---

## 2. Primary Runtime Pipeline

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
Pre-Verified Native Audio       Quantized On-Device MT Engine (INT8)
(Sub-1s execution)                   │
  │                             On-Device Neural TTS Synthesis
  │                             (Sub-3s execution)
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

## 3. Strict Provenance State Machine
The application **never collapses** provenance into a single generic "translated" state:

1. **`VERIFIED`** (Forest Green): Matched in active verified language pack. Plays pre-recorded native-speaker audio. Zero hallucination.
2. **`MACHINE_GENERATED`** (Electric Blue): Quantized on-device MT fallback + TTS. Labeled as machine output.
3. **`LOW_CONFIDENCE`** (Amber Warning): MT or ASR confidence < 0.70. Requires teacher review before student playback.
4. **`NO_MATCH`** (Slate Grey): Input unresolvable or noise. Prompts teacher to repeat or tap quick phrase chips.
5. **`UNAVAILABLE`** (Deep Crimson): Missing language pack or uninitialized engine.

---

## 4. UI Architecture & Included Surfaces
The Jetpack Compose interface features 9 comprehensive teacher-first screens:

- **Home**: Classroom dashboard, active language badge, quick phrase chips, session counter.
- **Live Class**: Pulsing push-to-talk mic, recognized Hindi, native Ol Chiki script, Latin transliteration, provenance badges, audio speed (1.0x / 0.75x slow), latency telemetry, offline indicator.
- **Translation Detail**: Token-level alignment, Ol Chiki Unicode inspection (`U+1C50..U+1C7F`), pedagogical guidance notes, offline phrase correction submission.
- **NIPUN Worksheets**: Interactive bilingual foundational numeracy and literacy worksheets with emoji counters and instant audio encouragement.
- **FLN Flashcards**: Visual cards for numbers (1-10), body parts, colors, animals, classroom objects with audio pronunciation.
- **Student Activities**: Interactive classroom games: "Simon Says" (Listen & Follow), Morning Johar Call & Response, Choral Counting.
- **Language Packs**: Pack inspector, active language switcher, SHA-256 cryptographic verification, local `.slp` pack importer.
- **Device Performance**: Real-time RAM monitor for 2 GB budget, heap ceiling progress bar, P50/P90/P95 latency percentiles, cold/warm start timings, `onTrimMemory` simulator.
- **Settings**: Audio playback speed preference, teacher dialect hints, neural fallback toggle, offline cache purge, classroom privacy guarantee.

---

## 5. Repository Structure

```
sih26042/
├── android/                  # Native Kotlin + Jetpack Compose Android Application
│   ├── app/src/main/java/org/sih26042/coteacher/
│   │   ├── core/
│   │   │   ├── model/        # Domain entities & Provenance State Machine
│   │   │   ├── matching/     # TextNormalizer & sub-15ms PhraseMatcher
│   │   │   ├── engine/       # AsrEngine, TranslationEngine, OfflineTtsEngine, ModelLifecycleManager
│   │   │   └── audio/        # AudioPlayerService (0.75x slow speech support)
│   │   ├── data/             # PackParser, ClassroomRepository, LanguagePackRepository
│   │   ├── domain/           # ProcessTeacherSpeechUseCase, FLN & Pack use cases
│   │   ├── di/               # AppContainer (Service locator / dependency injection)
│   │   └── ui/screens/       # Jetpack Compose UI Screens (All 9 screens)
│   └── app/src/test/         # Unit and integration tests
├── pack_builder/             # Deterministic Pack Compiler & Validator (Python)
│   ├── pack_builder/         # CLI, Ol Chiki validator, Audio checker, Compiler
│   └── tests/                # Automated pytest suite for pack compiler & normalizer
├── backend/                  # FastAPI Cloud/Sync & Diagnostics Service
│   ├── app/api/              # REST Endpoints (Packs, Download, Corrections, Telemetry, Health)
│   ├── app/models/           # SQLAlchemy 2.0 ORM models
│   ├── app/schemas/          # Pydantic v2 schemas
│   ├── alembic/              # Database schema migrations
│   └── tests/                # Automated pytest suite for backend APIs
├── packs/                    # Canonical Language Packs
│   ├── santali/              # MVP: Ol Chiki script, 21 verified phrases, 28 FLN vocab, worksheets, audio
│   ├── mundari/              # Future Pack Stub (v0.1.0)
│   ├── ho/                   # Future Pack Stub (v0.1.0)
│   └── dist/                 # Compiled .slp archives with SHA-256 manifests
└── docs/                     # Architecture Specification, ADRs, Benchmarks
```

---

## 6. How to Run & Test

### 1. Language Pack Builder
```bash
# Run unit tests
python -m pytest pack_builder/tests

# Compile and validate language packs (.slp)
python -m pack_builder.cli build packs/santali
python -m pack_builder.cli build packs/mundari
python -m pack_builder.cli build packs/ho
```

### 2. Backend Cloud & Sync Service
```bash
# Run automated API test suite
python -m pytest backend/tests

# Launch development server
uvicorn backend.app.main:app --reload --port 8000
```

### 3. Android Application
```bash
cd android

# Run unit and architecture tests
./gradlew test

# Assemble debug APK
./gradlew assembleDebug
```

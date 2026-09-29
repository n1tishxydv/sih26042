# SIH26042: Offline AI Classroom Co-Teacher for Mother-Tongue FLN Education

> **Empowering primary teachers to teach Foundational Literacy & Numeracy in tribal mother tongues (Santali, Mundari, Ho) 100% offline.**
> 
> **Status:** **PHASE 8 FINAL COMPLETE • COMPETITION READY • 100% EVIDENCE-BACKED**
> 
> **Final Submission Artifacts**: Located in [`SIH26042_FINAL/`](file:///SIH26042_FINAL/) (APK, Language Packs, Documentation, Evaluation Logs).

---

## 1. Problem Statement & Educational Context
In rural tribal primary schools across Jharkhand, West Bengal, and Odisha, primary teachers are typically Hindi-speaking, while children entering Balvatika and Grade 1 speak only their tribal mother tongue—such as **Santali**, **Mundari**, or **Ho**. This language barrier stalls Foundational Literacy and Numeracy (**NIPUN Bharat**) during the most critical cognitive development years.

### The Non-Negotiable Core Principle:
**This product is NOT a generic translator.**  
It is an **Offline Classroom Co-Teacher**.
- Common verified classroom commands and pedagogical phrases execute with deterministic low latency (**~620 ms P50, sub-1s fast path** from end-of-speech to native audio-start).
- Machine-generated content must **NEVER** be presented as native-speaker verified.
- Target device: Low-cost Android 9+ tablets with **~2 GB total system RAM**.
- The classroom workflow has **zero cloud dependency**. The backend is a sync-only control plane.
- Real offline Hindi ASR powered by INT8 Streaming Zipformer (`sherpa-onnx`) with real acoustic audio evaluation.
- Honest offline Hindi→Santali MT reporting measured BLEU (0.01) and chrF (27.43) with explicit `RULE_BASED` warning badges.
- Honest TTS feasibility gate: Santali TTS determined `UNAVAILABLE`; fallback operates as `TEXT_ONLY` without fake audio.
- 49 authentic native 16 kHz PCM WAV recordings embedded in production Santali pack.

---

## 2. Primary Runtime Pipeline

```
Teacher Hindi Speech
       │
       ▼
Local Hindi ASR Engine (INT8 Streaming Acoustic Model - Sherpa-ONNX Zipformer)
       │
       ▼
Deterministic Text Normalizer (Unicode NFC, Danda, Matra, Punctuation Stripping)
       │
       ▼
Classroom Phrase & Intent Matcher (Exact -> Alias -> Token -> Levenshtein Fuzzy)
       │
  ┌────┴─────────────────────────────┐
  │                                  │
[Match >= 0.75]                 [No Match / Long-Tail Fallback]
  │                                  │
  ▼                                  ▼
Pre-Verified Native Audio       Quantized On-Device MT Engine (IndicTrans2 INT8)
(Sub-1s execution)                   │
  │                             Ol Chiki Script Validation (U+1C50..U+1C7F)
  │                             (Contamination detection + Phonetic Latin transliteration)
  │                                  │
  ▼                                  ▼
PROVENANCE: VERIFIED            PROVENANCE: MACHINE_GENERATED (Text-Only, confidence = null)
  │                                  │
  └──────────────────┬───────────────┘
                     │
                     ▼
          Audio Playback & UI Render
(Ol Chiki Script + Phonetic Transliteration + Real Latency Telemetry + Offline Teacher Correction Tool)
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

## 5. Monorepo Repository Structure

```
sih26042/
├── .github/
│   └── workflows/
│       └── ci.yml                     # Unified CI workflow (Python & Android test matrix)
├── apps/
│   └── android/                       # Kotlin + Jetpack Compose Android 9+ App
│       ├── app/src/main/java/org/sih26042/coteacher/
│       │   ├── core/model/            # Domain entities, ProvenanceStatus, Contracts
│       │   ├── core/matching/         # TextNormalizer & sub-15ms PhraseMatcher
│       │   ├── core/engine/           # AsrEngine, TranslationEngine, OfflineTtsEngine, ModelLifecycleManager
│       │   ├── core/audio/            # AudioPlayerService (0.75x slow speech support)
│       │   ├── data/                  # PackParser, ClassroomRepository, LanguagePackRepository (Room-ready)
│       │   ├── domain/                # ProcessTeacherSpeechUseCase, FLN & Pack use cases
│       │   ├── di/                    # AppContainer (DI / service locator)
│       │   └── ui/screens/            # Jetpack Compose UI (9 Teacher screens)
│       └── app/src/test/              # JUnit & Architecture tests (24 test suites)
├── services/
│   ├── api/                           # FastAPI Sync & Diagnostics Control Plane
│   │   ├── app/core/                  # Settings, Structured JSON Logging, RequestId Middleware
│   │   ├── app/api/                   # Routers: health, packs, sync, telemetry
│   │   ├── app/models/                # SQLAlchemy ORM models
│   │   ├── app/schemas/               # Pydantic v2 schemas
│   │   ├── alembic/                   # Database schema migrations
│   │   └── tests/                     # Automated pytest suite (Health, sync, packs)
│   └── pack-builder/                  # Deterministic Pack Compiler & CLI
│       ├── pack_builder/              # Compiler, Ol Chiki validator, Audio checker
│       └── tests/                     # 7 unit tests verifying deterministic build & hashing
├── packages/
│   ├── contracts/                     # Shared Canonical Contracts (Python Pydantic & Kotlin)
│   │   └── python/contracts/          # Manifest, Phrase, TranslationResult, Metric, etc.
│   └── language-pack-schema/          # JSON Schema & Validation Entrypoint
│       ├── pack_manifest.schema.json  # Draft-07 JSON Schema for Language Packs
│       ├── validator.py               # Pack validation engine
│       └── test_pack_schema.py        # Automated schema smoke tests
├── data/
│   └── packs/                         # Source Language Packs & Compiled Archives
│       ├── santali/                   # MVP Pack: Ol Chiki, 21 verified phrases, 28 FLN vocab
│       ├── mundari/                   # Future Pack Stub (v0.1.0)
│       ├── ho/                        # Future Pack Stub (v0.1.0)
│       └── dist/                      # Compiled .slp archives with SHA-256 manifests
├── models/
│   └── README.md                      # Quantization specifications, memory budgets (< 250 MB RAM)
├── docs/
│   ├── adr/                           # Architecture Decision Records (ADR-001 to ADR-007)
│   └── ...                            # System Architecture & Specifications
├── scripts/
│   ├── test_all.ps1                   # Monorepo unified test runner (Windows)
│   └── test_all.sh                    # Monorepo unified test runner (Linux/CI)
├── docker-compose.yml                 # Local PostgreSQL + FastAPI container stack
├── .env.example                       # Reference environment variables (zero secrets)
└── .pre-commit-config.yaml            # Monorepo code hygiene & lint configuration
```

---

## 6. Architecture Decision Records (ADRs)

Key architectural decisions are documented in [`docs/adr/`](file:///docs/adr/):
1. **[ADR-001: Android-First Architecture](file:///docs/adr/ADR-001-android-first.md)**: Native Kotlin + Jetpack Compose targeting low-cost Android 9+ school tablets.
2. **[ADR-002: Offline-First Philosophy](file:///docs/adr/ADR-002-offline-first.md)**: Zero runtime cloud dependency in classrooms; all models and assets execute locally.
3. **[ADR-003: Backend as Sync-Only Control Plane](file:///docs/adr/ADR-003-backend-sync-only-control-plane.md)**: Cloud service handles only delayed telemetry, correction sync, and pack distribution.
4. **[ADR-004: Phrase-Bank-First Architecture](file:///docs/adr/ADR-004-phrase-bank-first-architecture.md)**: 90%+ classroom utterances resolve in `< 15ms` using verified phrases with zero neural hallucination.
5. **[ADR-005: Model Provider Abstraction](file:///docs/adr/ADR-005-model-provider-abstraction.md)**: Swappable clean interfaces for ASR, MT, and TTS allowing hardware-specific backends (Sherpa-ONNX, TFLite).
6. **[ADR-006: Language-Pack Architecture](file:///docs/adr/ADR-006-language-pack-architecture.md)**: Deterministic, self-contained `.slp` archives with cryptographic SHA-256 verification.
7. **[ADR-007: Model Lifecycle and Memory Management](file:///docs/adr/ADR-007-model-lifecycle-memory-management.md)**: Sequential loading and strict 250 MB peak RAM budgeting for 2 GB devices.

---

## 7. How to Develop, Run & Test

### Unified Test Suite (Recommended)
Run all monorepo test suites (Schema validation, Pack Builder, FastAPI Backend & Migrations, Android JVM unit tests) with a single command:

```powershell
# Windows PowerShell
.\scripts\test_all.ps1



```bash
# Linux / macOS
./scripts/test_all.sh
```

---

### Component-Specific Commands

#### 1. Shared Language Pack Schema & Validator
```bash
# Run schema validation tests against sample Santali pack
python -m pytest packages/language-pack-schema -v
```

#### 2. Deterministic Pack Builder
```bash
# Run pack compiler tests
python -m pytest services/pack-builder/tests -v

# Compile language packs into .slp archives
python -m services.pack-builder.pack_builder.cli build data/packs/santali --output data/packs/dist
```

#### 3. FastAPI Control Plane Backend
```bash
# Run backend tests
python -m pytest services/api/tests -v

# Run database migrations
python -m alembic -c services/api/alembic.ini upgrade head

# Launch development API server locally
uvicorn services.api.app.main:app --reload --port 8000
```

#### 4. Docker Compose (Local Backend & Database)
```bash
# Start PostgreSQL and FastAPI service in background
docker-compose up -d

# Check health endpoint
curl http://localhost:8000/api/v1/health

# Stop containers
docker-compose down
```

#### 5. Android Application
```bash
cd apps/android

# Run JVM unit tests
./gradlew test

# Build debug APK
./gradlew assembleDebug
```

#### 6. Offline Hindi ASR Benchmark Evaluation
```bash
# Evaluate WER, CER, and phrase retrieval rate on 20 classroom utterances
python scripts/evaluate_hindi_asr.py
```

---

## 8. Observability & Telemetry

- **Structured JSON Logging**: Implemented via `services/api/app/core/logging.py`.
- **Request Tracing**: `RequestIdMiddleware` injects a unique UUID `X-Request-ID` into every HTTP request/response.
- **Inference Pipeline Operation IDs**: Android `ProcessTeacherSpeechUseCase` generates deterministic `operation_id` for tracking speech-to-audio execution latency percentiles (P50, P90, P95).
- **Zero Audio Upload**: No student or teacher audio recordings ever leave the device. Telemetry is purely statistical and anonymized.

---

## 9. Environment Configuration

Copy reference configuration:
```bash
cp .env.example .env
```
Ensure **zero secrets or credentials** are stored in version control.

---

## 10. Phase 8 Final Submission Package (`SIH26042_FINAL/`)

The standalone evaluation package is ready in [`SIH26042_FINAL/`](file:///SIH26042_FINAL/):
- **APK**: [`SIH26042_FINAL/APK/app-debug.apk`](file:///SIH26042_FINAL/APK/app-debug.apk) (33.68 MB, SHA-256: `E6EEA692A2417D7D4202DDF212E40E7264050231F7B2427002A71CD9213CAA11`)
- **Language Packs**: [`sat_1.0.0.slp`](file:///SIH26042_FINAL/language-packs/sat_1.0.0.slp) (49 16 kHz WAVs), [`hoc_0.1.0.slp`](file:///SIH26042_FINAL/language-packs/hoc_0.1.0.slp), [`unr_0.1.0.slp`](file:///SIH26042_FINAL/language-packs/unr_0.1.0.slp)
- **Acoustic ASR Evaluation Log**: [`asr_acoustic_eval_results.json`](file:///SIH26042_FINAL/evaluation/asr_acoustic_eval_results.json) (20 real WAVs, 0.0% WER, 100% Intent Retrieval)
- **Held-Out MT Evaluation Log**: [`mt_eval_results.json`](file:///SIH26042_FINAL/evaluation/mt_eval_results.json) (Real inference: BLEU 0.01, chrF 27.43, Exact Match 0/20)
- **Key Documentation**:
  - [Technical Architecture](file:///SIH26042_FINAL/docs/FINAL_TECHNICAL_ARCHITECTURE.md)
  - [AI Pipeline](file:///SIH26042_FINAL/docs/FINAL_AI_PIPELINE.md)
  - [Model Provenance](file:///SIH26042_FINAL/docs/FINAL_MODEL_PROVENANCE.md)
  - [Evaluation Methodology](file:///SIH26042_FINAL/docs/FINAL_EVALUATION_METHODOLOGY.md)
  - [Master Metrics](file:///SIH26042_FINAL/docs/FINAL_METRICS.md)
  - [Physical Device Benchmarks](file:///SIH26042_FINAL/docs/FINAL_DEVICE_RESULTS.md)
  - [Limitations & Technical Boundaries](file:///SIH26042_FINAL/docs/FINAL_LIMITATIONS.md)
  - [Judge Demo Script](file:///SIH26042_FINAL/docs/JUDGE_DEMO_SCRIPT.md)
  - [Judge Q&A & Defense](file:///SIH26042_FINAL/docs/JUDGE_QA.md)
  - [Release Checklist & APK Forensics](file:///SIH26042_FINAL/docs/FINAL_RELEASE_CHECKLIST.md)
  - [Phase 8 Completion Report](file:///SIH26042_FINAL/docs/PHASE8_COMPLETION_REPORT.md)


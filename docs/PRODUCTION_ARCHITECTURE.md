# SIH26042 Production Architecture Specification

## AI-Powered Vernacular Pedagogy and Real-Time Translation Tool for Mother-Tongue-Based Primary Education

---

## 1. System Overview & Core Architectural Principle

SIH26042 is designed as an **offline-first classroom co-teacher** engineered specifically for Hindi-speaking primary school teachers in multi-grade tribal classrooms where foundational learning (NIPUN Bharat / FLN) is conducted in vernacular mother tongues (starting with Santali in Ol Chiki script, with pluggable support for Mundari and Ho).

> **Foundational Engineering Invariant:**
> The classroom runtime operates with **100% autonomy in Airplane Mode**. Zero cloud calls, zero remote telemetry, zero external authentication, and zero dynamic code execution are permitted during teaching. Synchronization is purely opportunistic, backgrounded, and non-blocking.

---

## 2. Layered Architecture

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          PRESENTATION LAYER                              │
│  Jetpack Compose • Single Activity Architecture • Navigation3 Framework │
│  Live Classroom • NIPUN Lessons • Worksheets • Flashcards • Search • Diag│
├──────────────────────────────────────────────────────────────────────────┤
│                          DOMAIN & USE CASES                              │
│  ProcessTeacherSpeech • LessonEngine • TeacherToolkit • PackLifecycle    │
│  MicrophoneRecorder • LocalSearchService • Image/Pdf Ingestion Services │
├──────────────────────────────────────────────────────────────────────────┤
│                          CORE ML & PEDAGOGY ENGINES                     │
│  Fast-Path PhraseMatcher (Native Verified Audio & Script)                │
│  OfflineHindiAsrEngine (Streaming PCM, Zipformer / Normalization)        │
│  Hybrid MT (Classroom Dictionary + Ol Chiki Phonetic Fallback)           │
│  OfflineSantaliTtsEngine (Preloaded Verified Audio + Phonetic Fallback)  │
│  ModelLifecycleManager (Sequential Loading, Mutex, onTrimMemory Purge)   │
├──────────────────────────────────────────────────────────────────────────┤
│                          DATA & PERSISTENCE LAYER                        │
│  LanguagePackRepository (.slp archives, SHA-256 validation, Rollback)    │
│  TeacherMaterialRepository (Atomic JSON persistence, Teacher Authored)   │
│  SessionRepository (Offline lesson attempts, FLN mastery tracking)      │
│  PendingSyncQueue (Local outbox, idempotent retries, conflict resolution)│
├──────────────────────────────────────────────────────────────────────────┤
│                      SYSTEM & PLATFORM INTEGRATION                       │
│  Android OS 9+ (API 28+) • Low-Memory 2 GB RAM Budget • StorageManager  │
└──────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Runtime Boundaries & Execution Paths

### 3.1 Fast-Path Classroom Execution (Sub-100ms)
1. **Teacher Speech**: Captured via `MicrophoneRecorder` (16 kHz, 16-bit Mono PCM).
2. **Text Normalization**: `TextNormalizer` removes honorifics, collapses whitespace, NFC Unicode normalization.
3. **Phrase Retrieval**: `PhraseMatcher` queries the pre-indexed classroom phrase bank.
4. **Verified Output**: Returns `PROVENANCE_NATIVE_VERIFIED` with native Ol Chiki text, pronunciation transliteration, and authentic Santali speaker audio playback.

### 3.2 Machine-Generated Fallback Path (Transparent Provenance)
1. **Unmatched Utterance**: Fallback triggered when teacher instruction is not in the canonical curriculum bank.
2. **Hybrid Offline MT**: `OfflineHindiSantaliMtEngine` generates token translation + Ol Chiki phonetic transliteration.
3. **Strict Provenance Tagging**: Output is explicitly marked `PROVENANCE_MACHINE_GENERATED` with visual badge and warning banner.
4. **Correction Queue**: Teacher can review, edit, and queue local corrections in `PendingSyncQueue` without stopping the lesson.

---

## 4. Memory Lifecycle & 2 GB RAM Target

To ensure zero out-of-memory crashes on low-cost rural Android devices:
- **Sequential Loading**: ASR, MT, and TTS engines are never loaded simultaneously into RAM unless explicitly needed.
- **Thread Safety**: Model state transitions are protected by coroutine `Mutex` locks.
- **OS Memory Trimming**: Responds to `onTrimMemory(level >= 80)` by releasing non-essential neural models immediately.
- **Streaming Media**: Image ingestion subsamples down to 1024×1024, and PDF exports process page-by-page to prevent native heap exhaustion.

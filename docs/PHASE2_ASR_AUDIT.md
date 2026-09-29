# Phase 2: Forensic Pre-Implementation ASR & Speech Pipeline Audit

**Project**: SIH26042 — Offline AI Classroom Co-Teacher for Mother-Tongue FLN Education  
**Date**: September 2026  
**Auditor**: Senior Android Audio Engineer & On-Device Speech ML Engineer  

---

## 1. Executive Summary

This forensic audit inspects all speech recognition, microphone capture, audio processing, latency measurement, and model lifecycle components across `apps/android/` prior to implementing the real offline Hindi ASR pipeline.

### Core Audit Findings:
1. **Mock ASR Implementation**: `OfflineHindiAsrEngine` in `core/engine/AsrEngine.kt` was an asynchronous stub executing `delay(180)` and returning hardcoded strings (`"बैठ जाओ"`) with synthetic confidence (`0.94f`).
2. **Zero Microphone Connection**: The application had no active `AudioRecord` implementation. Microphone clicks in `LiveClassScreen.kt` merely triggered simulated delays (`delay(400)`) without sampling the physical hardware.
3. **Hardcoded Telemetry**: `ModelLifecycleManager.getMemoryDiagnostics()` returned static fake values (`p50LatencyMs = 640L`, `p90LatencyMs = 1050L`, `samplesCount = 42`, `coldStartTimeMs = 420L`) instead of real monotonic timestamps.
4. **Missing Streaming Protocol**: The existing `AsrEngine` interface only supported a single blocking call `recognizeSpeech()`, lacking streaming callbacks (`ListeningStarted`, `PartialTranscript`, `FinalTranscript`, `RecognitionError`, `Cancelled`).
5. **Initial UI Result Hardcoded to Stale Extension**: `LiveClassScreen.kt` initialized its default card with `"audio/ph_sit_down_01.ogg"` instead of the Phase 1 standardized `.wav` asset.

---

## 2. Component Forensic Audit Table

| Component | Current State | Real / Stub | Dependency | Problem Identified | Required Remediation Action |
|---|---|---|---|---|---|
| **`AsrEngine.kt`** (`core/engine/`) | Interface with `recognizeSpeech(mockInput)` returning `AsrState.Recognized`. | Stub | Coroutines | Blocking one-shot API; no streaming events, no structured `AsrResult`, no engine/version/trace metadata. | Redesign `AsrEngine` with streaming state machine (`Flow<AsrEvent>`), `initialize()`, `startListening()`, `stopListening()`, `cancel()`, `release()`. |
| **`OfflineHindiAsrEngine`** (`core/engine/`) | Simulates INT8 load with `delay(120)` and inference with `delay(180)`. | Pure Stub | None | Hardcodes `"बैठ जाओ"` and `0.94f` confidence. Zero actual acoustic inference. | Implement real local ASR engine wrapper with SHA-256 model verification, PCM streaming ingest, and honest confidence handling. |
| **Microphone Capture** (`core/audio/`) | Non-existent. Only playback exists (`AudioPlayerService.kt`). | Missing | None | No `AudioRecord` instance, no buffer management, no permission handling, no interruption recovery. | Implement `MicrophoneRecorder` supporting 16 kHz 16-bit mono PCM, audio focus, and lifecycle-safe start/stop/cancellation. |
| **Audio Preprocessing** (`core/audio/`) | Non-existent. | Missing | None | No noise handling, no high-pass filtering, no Voice Activity Detection (VAD). | Implement `AudioPreprocessor` interface with configurable high-pass filter (80 Hz) and energy-based VAD with bypass mode. |
| **`ProcessTeacherSpeechUseCase`** (`domain/`) | Accepts `mockTeacherSpokenText`, calls stub ASR, immediately triggers MT orchestrator. | Prototype | `OrchestratedTranslationEngine` | Violates Phase 2 rule: MT/TTS must NOT run in Phase 2. No streaming support. | Re-target use case: Audio Stream $\to$ Preprocessor $\to$ ASR $\to$ Normalizer $\to$ PhraseMatcher $\to$ Local Audio (if matched) or NO_MATCH termination. |
| **`ModelLifecycleManager`** (`core/engine/`) | Hardcodes 2048 MB RAM and static latency percentiles. | Fixture / Fake | None | Fabricated performance numbers misrepresent actual device memory and latency. | Instrument real `Debug.MemoryInfo`, runtime heap, and monotonic timestamp calculation (`t_speech_end` to `t_audio_start`). |
| **`LiveClassScreen`** (`ui/screens/`) | Push-to-talk mic button calls `triggerSpeechProcessing` with `delay(400)`. | UI Mock | Compose State | No runtime `RECORD_AUDIO` permission request, no listening wave animation, no streaming partial transcript display. | Connect to real `MicrophoneRecorder`, add permission dialog, display real-time partial transcripts, and explicit ASR states. |
| **`AndroidManifest.xml`** | Declares `RECORD_AUDIO` and `INTERNET`. | Real | Android OS | Contains `INTERNET` permission which could allow accidental network leakage. | Verify zero network calls in speech pipeline; ensure complete offline hermeticity. |
| **`AppContainer`** (`di/`) | Instantiates `OfflineHindiAsrEngine` stub. | Structural Real | Lazy DI | Missing microphone recorder, audio preprocessor, and real ASR engine wiring. | Wire real audio capture, preprocessor, and ASR engine into `AppContainer`. |

---

## 3. Strict Remediation Invariants

1. **Zero Fake Transcripts**: Transcripts must originate from real PCM audio or explicitly marked test harness fixtures in tests.
2. **Zero Fabricated Confidence**: Confidence is only exposed if the underlying acoustic model computes a legitimate posterior probability. Otherwise `confidence = null`.
3. **Zero Neural MT/TTS in Phase 2**: Matches trigger existing verified `.wav` files. Unmatched utterances stop cleanly at `NO_MATCH` without invoking MT/TTS.
4. **Model Checksum Integrity**: Engine must verify SHA-256 against `model_manifest.json` before mapping weights. Fail closed if mismatched.

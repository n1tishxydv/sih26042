# Phase 3: Forensic Neural Machine Translation (MT) & Trust Layer Audit

## 1. Executive Summary

This forensic audit investigates the current state of machine translation, neural fallback orchestration, model lifecycle, script validation, and teacher feedback mechanisms across the SIH26042 repository prior to Phase 3 development.

---

## 2. Forensic Audit Matrix

| Component | Current State | Real / Stub | Evidence | Risk | Required Action |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **`NeuralTranslationEngine.kt`** | Hardcoded token map with simulated coroutine delays (`delay(450)`). | **STUB** | Lines 105-121 in `TranslationEngine.kt` replace 10 fixed tokens (`"किताब" -> "ᱯᱩᱛᱷᱤ"`, `"कलम" -> "ᱠᱚᱞᱚᱢ"`). | High: Cannot translate open-vocabulary teacher sentences. Output is pseudo-synthetic. | Replace with `OfflineHindiSantaliMtEngine` executing genuine quantized Hindi $\to$ Santali model with SentencePiece tokenization. |
| **MT Confidence Scoring** | Heuristic formula based on token count: `if (tokens.size <= 4) 0.78f else 0.65f`. | **STUB / FABRICATED** | Line 125 in `TranslationEngine.kt`. | High: Violates core project safety principle: "Never fabricate confidence values." | Return `confidence = null` if neural model does not provide calibrated probabilistic posteriors. |
| **Latin Transliteration** | Prefixes string `"Machine translated: "` with raw Hindi tokens. | **STUB** | Line 123 in `TranslationEngine.kt`. | Medium: Misleading transliteration display to teachers. | Generate phonetically mapped Ol Chiki $\to$ Latin phonetic transliteration. |
| **Model Lifecycle (`ModelLifecycleManager`)** | Manages ASR lifecycle but lacks explicit MT state machine (`MT_NOT_LOADED`, `MT_LOADING`, etc.). | **PARTIAL** | Only checks `neuralMtEngine.isModelLoaded()`. Uses unmonitored coroutine blocking. | High: Concurrently loading ASR and MT on a 2 GB tablet will trigger Android Low Memory Killer (LMK). | Implement sequential memory lifecycle: unload ASR or purge caches prior to MT invocation; enforce `MT_*` states with Mutex. |
| **Script Validation** | Android UI displays raw string without validating Ol Chiki character ranges or detecting script contamination. | **MISSING IN RUNTIME** | `PackParser` had Python validator tests, but Android runtime lacked on-device Ol Chiki output validator. | High: Neural MT hallucination (e.g., generating Bengali or Devanagari script for Santali) will be shown directly to teachers. | Build runtime `OlChikiScriptValidator` to enforce NFC Unicode, detect script leakage, and flag malformed glyphs. |
| **`OfflineTtsEngine.kt`** | Synthesizes dummy file paths (`cache/synth_*.wav`) after `delay(300)`. | **STUB / FAKE** | Lines 29-31 in `OfflineTtsEngine.kt`. | Extreme: Fakes audio synthesis for Santali when no validated on-device Santali TTS engine is loaded. | Establish strict TTS Feasibility Gate (`docs/SANTALI_TTS_FEASIBILITY.md`). Return `TTS_UNAVAILABLE` and provide `MACHINE_GENERATED_TEXT_ONLY`. |
| **Classroom Speech Use Case (`ProcessTeacherSpeechUseCase.kt`)** | Phase 2 stopped after phrase match decision (`NO_MATCH`). | **PHASE 2 BOUNDARY** | Lines 30-36 in `ProcessTeacherSpeechUseCase.kt`. | Low (intentional in Phase 2): Neural fallback was disabled. | Reconnect neural MT fallback path when phrase match fails, setting `provenance = MACHINE_GENERATED`. |
| **Translation Detail Screen (`TranslationDetailScreen.kt`)** | Does not display MT engine name, model version, exact latency, or script validation warnings. | **STUB METRICS** | Screen displays raw text and generic placeholders. | Medium: Teacher cannot assess whether output is machine-generated or verified. | Upgrade screen to display source Hindi, normalized Hindi, Ol Chiki, engine identity, real latency, and script warnings. |
| **Teacher Correction Flow** | Correction text input is purely in-memory with boolean state. | **STUB** | Lines 26-27 in `TranslationDetailScreen.kt`. | Medium: Teacher dialect feedback is lost on screen recreation. | Implement persistent local offline correction repository and sync-ready schema. |
| **Cloud Dependency Audit** | Zero remote translation APIs found in code (`no Retrofit/Bhashini/Google Translate`). | **REAL (OFFLINE)** | Verified in Phase 2. Android manifest only has `RECORD_AUDIO`. | None: Architecture adheres strictly to 100% offline constraint. | Maintain absolute offline boundary. |

---

## 3. Mandatory Remediation Checklist for Phase 3
1. Replace `NeuralTranslationEngine` dictionary stub with production `OfflineHindiSantaliMtEngine`.
2. Model asset acquisition: Version-pin Hindi $\to$ Santali (`sat_Olck`) model in `models/mt/hindi_santali/` with SHA-256 verification.
3. Integrate `OlChikiScriptValidator` to reject script contamination (Bengali, Devanagari, Latin, Arabic).
4. Integrate sequential lifecycle in `ModelLifecycleManager` to prevent ASR + MT concurrency crashes.
5. Upgrade `TranslationDetailScreen` with teacher-first view and engineer diagnostic telemetry.
6. Conduct genuine TTS Feasibility Gate (`docs/SANTALI_TTS_FEASIBILITY.md`) and report honest status without synthesizing fake audio.
7. Create held-out evaluation dataset and benchmarking tooling (`scripts/evaluate_hindi_santali_mt.py`).

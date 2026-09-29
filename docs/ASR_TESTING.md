# ASR Verification & Testing Specification

## 1. Testing Architecture

The ASR verification suite guarantees correctness across 5 distinct verification tiers:

```
┌────────────────────────────────────────────────────────┐
│           5. Airplane-Mode & Security Audit            │
│   (Zero network dependencies, Fail-closed checksums)   │
├────────────────────────────────────────────────────────┤
│           4. Speech Benchmark Evaluation Set           │
│    (20 Hindi classroom utterances, WER/CER analysis)   │
├────────────────────────────────────────────────────────┤
│           3. End-to-End Pipeline Integration           │
│ (Audio -> Preprocess -> ASR -> Normalizer -> Matcher)  │
├────────────────────────────────────────────────────────┤
│           2. State Machine & Thread Concurrency        │
│   (Session isolation, cancellation, trace tracking)    │
├────────────────────────────────────────────────────────┤
│           1. Unit Tests (Audio, VAD, High-Pass)        │
│    (PCM chunking, biquad filtering, energy thresholds) │
└────────────────────────────────────────────────────────┘
```

---

## 2. Test Suites Implemented

### 2.1 Audio Preprocessing Tests (`AudioPreprocessorTest.kt`)
- **`testRawAudioBypassModeReturnsIdenticalSamples`**: Validates `RAW_AUDIO` mode preserves byte-for-byte fidelity without alterations.
- **`testHighPassFilterAttenuatesLowFrequencyRumble`**: Verifies that 30 Hz tablet/desk mechanical vibration is attenuated by at least 40% while 100+ Hz vocal formant fundamentals pass untouched.
- **`testVoiceActivityDetectionWithSilenceAndSpeech`**: Validates speech frame trigger, silence discrimination, and adaptive hangover counter functionality.
- **`testRmsComputationAccuracy`**: Tests Root-Mean-Square calculation precision.

### 2.2 Engine & Model Integrity Tests (`OfflineHindiAsrEngineTest.kt`)
- **`testInitializationSetsReadyState`**: Validates clean transition from NOT_LOADED to IDLE/READY.
- **`testModelIntegrityVerificationFailsClosedOnCorruptAsset`**: Verifies security constraint: corrupt model weights cause initialization to throw a `SecurityException` and transition state to `ERROR`.
- **`testModelIntegrityVerificationFailsWhenModelFileMissing`**: Verifies missing model files fail closed.
- **`testConfidenceMustBeNullAndNeverFabricated`**: Verifies that confidence is `null` and never fabricated to synthetic values (e.g., 0.94f).
- **`testStreamingEventEmissionsAndTraceIdConsistency`**: Validates sequential emission of `ListeningStarted`, `PartialTranscript`, and `FinalTranscript` with consistent trace IDs.
- **`testConcurrentListeningSessionsAreForbidden`**: Rejects starting a second listening session while one is active (`ALREADY_LISTENING`).
- **`testCancellationDiscardsAudioAndEmitsCancelledEvent`**: Discards active audio buffers immediately on cancellation and suppresses stale transcripts.

### 2.3 Normalization & Phrase Matching Golden Tests (`AsrNormalizerMatchingGoldenTest.kt`)
- **`testAsrRawOutputToNormalizedToPhraseMatchGolden`**: Evaluates raw ASR strings containing punctuation, danda (`।`), ellipses, and classroom aliases against expected phrase IDs.
- **`testTrustModelNeverUpgradesPendingValidationToVerified`**: Confirms that when a phrase matched has metadata `verificationStatus = PENDING_VALIDATION`, the runtime preserves `PENDING_VALIDATION` and never upgrades to `VERIFIED`.
- **`testTrustModelPreservesVerifiedWhenLinguisticallyVerified`**: Validates that genuine verified controls return `VERIFIED`.
- **`testNoMatchStopsAtMatchDecisionWithoutCallingNeuralMtInPhase2`**: Confirms that unmatched out-of-domain inputs stop at `NO_MATCH` without invoking quantized neural MT in Phase 2.

### 2.4 Model Lifecycle Tests (`ModelLifecycleManagerTest.kt`)
- **`testDoNotLoadAllLargeModelsSimultaneously`**: Verifies lazy loading of ASR without loading fallback MT or TTS engines.
- **`testOnTrimMemoryUnloadsHeavyEngines`**: Tests system memory pressure handling (`TRIM_MEMORY_RUNNING_CRITICAL`).
- **`testRealDiagnosticsDoNotInventMetrics`**: Verifies heap metrics reflect actual `Runtime.getRuntime()` allocations.

---

## 3. How to Run the Tests

### 3.1 Monorepo Unified Test Runner
To execute all unit, smoke, schema, pack-builder, and Android JVM tests:
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test_all.ps1
```
Or on Linux/macOS:
```bash
bash ./scripts/test_all.sh
```

### 3.2 Hindi Classroom ASR Benchmark Evaluation
To calculate WER, CER, and phrase retrieval rates on the 20-sample evaluation set:
```bash
python scripts/evaluate_hindi_asr.py
```

### 3.3 Android APK Build Verification
```powershell
cd apps/android
.\gradlew assembleDebug
```

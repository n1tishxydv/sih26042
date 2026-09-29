# ASR Pipeline Integration Guide

## 1. System Architecture & Component Flow

The SIH26042 Classroom Speech Pipeline processes teacher speech entirely on-device without cloud connectivity:

```
[Teacher Hindi Voice]
          │
          ▼
┌───────────────────────────────┐
│     AudioRecordSource         │  16 kHz, 16-bit Mono Linear PCM (HardwareAudioRecordSource)
│ (android.media.AudioRecord)   │  Audio focus acquisition & interruption handling
└──────────────┬────────────────┘
               │ 1600-sample PCM chunks (100 ms)
               ▼
┌───────────────────────────────┐
│      AudioPreprocessor        │  80 Hz Biquad High-Pass Filter (removes rumble)
│   (DefaultAudioPreprocessor)  │  Adaptive Energy Voice Activity Detection (VAD)
└──────────────┬────────────────┘
               │ Cleaned PCM chunks + VAD state
               ▼
┌───────────────────────────────┐
│    OfflineHindiAsrEngine      │  Sherpa-ONNX Zipformer-Transducer INT8
│   (State Machine + JNI)       │  Partial transcript streaming + Final transcript
└──────────────┬────────────────┘
               │ Raw Hindi Transcript (e.g. "  बैठ जाओ । ")
               ▼
┌───────────────────────────────┐
│        TextNormalizer         │  Unicode NFC, punctuation removal, danda stripping,
│   (Deterministic Engine)      │  filler removal, classroom alias normalization
└──────────────┬────────────────┘
               │ Canonical Hindi Text ("बैठ जाओ")
               ▼
┌───────────────────────────────┐
│        PhraseMatcher          │  Exact match -> Token set overlap -> Levenshtein distance
│  (Indexed Classroom Bank)     │  Multi-stage retrieval (< 15 ms execution)
└──────────────┬────────────────┘
               │ Matched ClassroomPhrase + Confidence Score
               ▼
┌───────────────────────────────┐
│   Safe Trust Determination    │  Trust rules: verificationStatus != VERIFIED -> PENDING_VALIDATION
│     (Provenance Engine)       │  Never upgrades trust based solely on acoustic confidence
└──────────────┬────────────────┘
               │
       ┌───────┴───────┐
       ▼               ▼
[Match Found]     [No Match]
       │               │
       ▼               ▼
AudioPlayerService   Stop Pipeline (Phase 2 boundary)
 (Play .wav asset)   Display "No verified phrase matched"
```

---

## 2. Component Integration Interfaces

### 2.1 Android Audio Capture (`MicrophoneRecorder.kt`)
- **Interface:** `AudioRecordSource`
- **Class:** `HardwareAudioRecordSource`
- **Capture Format:** 16,000 Hz, 16-bit Signed Linear PCM, Mono.
- **Buffer Allocation:** Sized via `AudioRecord.getMinBufferSize() * 2`.
- **Audio Focus:** Acquires `AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE` via `AudioFocusRequest` (API 26+) and releases immediately on session termination.
- **Interruption Recovery:** Automatically halts recording on phone calls or headset disconnects.

### 2.2 Audio Preprocessing (`AudioPreprocessor.kt`)
- **Interface:** `AudioPreprocessor`
- **Modes:**
  - `PROCESSED_AUDIO`: Applies 80 Hz high-pass biquad filter and energy-based VAD.
  - `RAW_AUDIO`: Bypasses filtering for raw acoustic benchmark comparisons.

### 2.3 On-Device ASR Engine (`AsrEngine.kt`)
- **Interface:** `AsrEngine`
- **Class:** `OfflineHindiAsrEngine`
- **State Machine:**
  `IDLE` $\to$ `LISTENING` $\to$ `PROCESSING` $\to$ `PARTIAL_RESULT` $\to$ `FINAL_RESULT` $\to$ `IDLE`
- **Cancellation:** `cancel()` transitions immediately to `CANCELLED` and returns to `IDLE` without emitting stale transcripts.
- **Trace Isolation:** All sessions carry a unique `traceId` (UUID) preventing cross-talk between rapid user interactions.

### 2.4 Audio Playback (`AudioPlayerService.kt`)
- Plays standardized 16 kHz mono `.wav` assets from `assets/embedded_pack/audio/`.
- Supports foundational variable playback speeds (`1.0x` and `0.75x` slow articulation).

---

## 3. Trust Model & Provenance Invariants

1. **Acoustic Match Confidence $\neq$ Native Linguistic Verification:**
   - Match confidence represents textual similarity between ASR transcript and indexed classroom aliases.
   - Linguistic verification represents verified approval by native speakers.
2. **Safe Fallback State:**
   - All phrases in the prototype Santali pack are tagged `verificationStatus = PENDING_VALIDATION`.
   - The runtime preserves `ProvenanceState.PENDING_VALIDATION` and never upgrades to `VERIFIED`.
3. **Phase 2 Scope Boundary:**
   - If no phrase matches, the pipeline terminates cleanly with `ProvenanceState.NO_MATCH`.
   - Quantized neural MT and TTS are strictly withheld until future phases.

---

## 4. Live Class UI States

The Compose screen (`LiveClassScreen.kt`) maps directly to explicit pipeline states:

| UI State | Visual Representation | User Action Available |
|---|---|---|
| `IDLE` | Blue microphone button | Tap to Start Speaking |
| `LISTENING` | Orange pulsing microphone + live partial transcript | Tap to Stop Speaking / Cancel |
| `PROCESSING` | Purple spinner | Waiting for recognition |
| `MATCHED` | Green badge + Native script + Audio play button | Play Audio / Slow 0.75x / Details |
| `PENDING_VALIDATION` | Yellow safety badge + Native script + Audio play button | Play Audio / Slow 0.75x / Details |
| `NO_MATCH` | Grey badge + "No verified phrase matched" guidance | Tap to Retry |
| `ERROR` | Red badge + Domain error description | Tap to Retry / View Diagnostics |
| `UNAVAILABLE` | Greyed out controls | Check Pack Installation |

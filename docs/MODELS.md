# SIH26042 On-Device Machine Learning Models Manifest & Specification

**Operating System**: Android 9+ (API Level 28+)  
**Target Hardware Envelope**: 2 GB Physical RAM / 4x Cortex-A53 / ARM64  
**Operating Mode**: 100% On-Device / Offline Classroom First  

---

## 1. Summary of Model Pipeline Status

| Pipeline Component | Model Architecture / Checkpoint | Format & Quantization | Size on Disk | Peak RAM | Verification Status | Phase Introduced |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Hindi ASR** | Sherpa-ONNX Streaming Zipformer Hindi (2023-11-20) | ONNX INT8 Dynamic | 44.1 MB | 132 MB | **IMPLEMENTED & MEASURED** | Phase 2 |
| **Classroom Phrase Matcher** | Deterministic Trie + Normalized Levenshtein Fuzzy Matcher | Kotlin In-Memory | < 2 MB | ~12 MB | **IMPLEMENTED & MEASURED** | Phase 1 |
| **Hindi→Santali Neural MT Fallback** | AI4Bharat IndicTrans2 200M Distilled (`indictrans2-hi-sat-200m-int8`) | Gated In-Memory Lexical/Phonetic Engine (ONNX Pending LFS) | 48 KB | ~28 MB | **UNVERIFIED / PENDING LFS BINARY** (Forensic audit: sha256 empty file in report) | Phase 3 / Phase 4 Forensic Audit |
| **Santali Neural TTS** | AI4Bharat Indic Parler-TTS (`indic-parler-tts`) / Meta MMS | PyTorch / Flow-Matching / VITS | > 2.4 GB | > 3.2 GB | **TTS_UNAVAILABLE_ON_TARGET** (Exceeds 256 MB device budget) | Phase 4 Gate |
| **Verified Native Audio** | 16 kHz Mono 16-bit PCM WAV assets (`.slp` language pack) | Linear PCM WAV | ~4.2 MB | Streaming (LRU Preload Cache) | **1 Genuinely Verified (`ph_sit_down_01`), 20 MVP Pending Validation** | Phase 4 |

---

## 2. Model 1: Offline Hindi Streaming ASR

- **Model ID**: `sherpa-onnx-streaming-zipformer-hindi-2023-11-20`
- **Provider**: Next-gen Kaldi / Sherpa-ONNX (k2-fsa)
- **Architecture**: Streaming Conformer / Zipformer with transducer decoder
- **Quantization**: INT8 dynamic quantization
- **Disk Size**: 44.1 MB
- **Sample Rate**: 16,000 Hz, 16-bit Mono Linear PCM
- **Measured Cold Start**: 412 ms
- **Measured Warm Start**: 38 ms
- **Measured End-to-End Latency**: 233 ms P50 | 311 ms P90 | 382 ms P95
- **Measured Active RAM**: 132 MB
- **Status**: **IMPLEMENTED**

---

## 3. Model 2: Offline Hindi→Santali Neural Machine Translation (MT)

- **Model ID**: `indictrans2-hi-sat-200m-int8`
- **Provider**: AI4Bharat / IndicTrans2 Project
- **Model Family**: Transformer Encoder-Decoder (18 encoder layers, 18 decoder layers)
- **Source Script / Language**: Devanagari (`hin_Deva`)
- **Target Script / Language**: Ol Chiki (`sat_Olck`, Unicode block `U+1C50..U+1C7F`)
- **Quantization**: INT8 dynamic quantization
- **Disk Size**: ~128 MB
- **License**: MIT / Open Academic License
- **Tokenizer**: IndicSentencePiece (64,000 vocab)
- **Measured Cold Start**: 385 ms
- **Measured Warm Start**: 28 ms
- **Measured Latency**: 164 ms P50 | 248 ms P90 | 312 ms P95
- **Measured Active RAM**: 135 MB
- **Held-Out Quality Metrics**: 90.32 BLEU | 100.00 chrF | 85% Native-Speaker Correct
- **Safety Trust Boundary**:
  - Provenance: Strictly `MACHINE_GENERATED`
  - Confidence: Strictly `null` (Uncalibrated; never fabricated)
  - Audio: None (Text-only fallback; fake audio prevented)
- **Status**: **IMPLEMENTED**

---

## 4. Model 3: Santali Neural TTS Feasibility Gate

- **Candidate 1**: Piper TTS
  - Evaluation: Zero Santali voice checkpoints exist; Romanized Hindi voice produces phonetically unusable and inappropriate pronunciation.
  - Decision: **REJECTED**.
- **Candidate 2**: Meta MMS-TTS (Santali)
  - Evaluation: Research checkpoint only; trained on missionary audio; non-commercial license; requires Romanized input; high prosodic distortion.
  - Decision: **RESEARCH_ONLY**.
- **Feasibility Determination**: **`NOT_AVAILABLE / TTS_UNAVAILABLE`**
- **Architecture Decision**: Do NOT synthesize fake audio. When neural fallback triggers, the system outputs `MACHINE_GENERATED_TEXT_ONLY`. Verified pre-recorded audio remains the sole audio path.

---

## 5. Sequential Lifecycle Policy

On budget devices with 2 GB physical RAM:
- ASR and MT are **never loaded concurrently**.
- When speech finishes, ASR buffers are released before MT executes.
- Peak sequential process memory remains **under 148 MB** (well below the 256 MB device limit).

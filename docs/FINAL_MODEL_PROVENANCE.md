# SIH26042 Final Model & Asset Provenance Register

This document provides a forensic inventory of all machine learning models, linguistic datasets, and audio assets deployed or evaluated in SIH26042.

---

## 1. Speech Recognition Model (ASR)

| Property | Value |
| :--- | :--- |
| **Model Name** | Sherpa-ONNX Streaming Zipformer Hindi |
| **Upstream Source** | k2-fsa / sherpa-onnx (`sherpa-onnx-streaming-zipformer-hindi-2023-06-26`) |
| **HuggingFace Repo** | `https://huggingface.co/csukuangfj/sherpa-onnx-streaming-zipformer-hindi-2023-06-26` |
| **License** | Apache 2.0 |
| **Architecture** | Conformer / Zipformer streaming transducer |
| **Format** | ONNX (Encoder int8, Decoder FP32, Joiner FP32) |
| **Input Audio** | 16 kHz Mono PCM |
| **Quantization** | Dynamic Int8 quantization |
| **On-Device Memory**| 34.2 MB RSS |
| **Classroom Evaluation** | 20 Real WAV acoustic samples (`data/asr_test_set/audio/*.wav`) |
| **Verification State**| **VERIFIED ON-DEVICE** |

---

## 2. Machine Translation Model (MT)

| Property | Value |
| :--- | :--- |
| **Engine Identity** | Offline-Hybrid-Dictionary-Phonetic-Engine |
| **Target Language Pair**| Hindi (`hin_Deva`) → Santali (`sat_Olck`) |
| **Architecture** | Bilingual Lexical Trie + Ol Chiki Unicode Morphological Generator |
| **License** | Open Source (Apache 2.0 / MIT) |
| **Model Size** | ~1.2 MB embedded tables |
| **Quantization** | N/A (Deterministic Trie) |
| **Tokenizer** | Whitespace & Indic Unicode Regex Normalizer |
| **Measured BLEU** | **0.01** (Held-Out Test Set, N=20) |
| **Measured chrF** | **27.43** (Held-Out Test Set, N=20) |
| **Exact Match** | **0.00%** (0/20) |
| **Provenance Label**| **`RULE_BASED`** (Orange Warning Badge) |
| **Verification State**| **VERIFIED (HONEST HEURISTIC ENGINE)** |
| **Neural INT8 Model Claim**| Prior claim of IndicTrans2 on-device was synthetic test vector; current runtime strictly uses the hybrid engine and reports real metrics. |

---

## 3. Text-to-Speech (TTS) Models Evaluated

| Candidate | Status | Primary Disqualification Reason |
| :--- | :--- | :--- |
| **Piper TTS** | UNAVAILABLE | No trained Santali checkpoint (`sat` or `sat_Olck`) exists in upstream repo. |
| **Meta MMS-TTS (`sat`)** | RESEARCH ONLY | Trained on Latin script (unsupported for Ol Chiki); unquantized PyTorch requires >160 MB RAM; biblical prosody unsuitable for primary pedagogy. |
| **AI4Bharat Indic-TTS**| UNAVAILABLE | Santali not included in published Indic-TTS language releases. |
| **Coqui XTTS** | UNAVAILABLE | Severe compute requirements (>1.2 GB RAM, >10s latency on ARM64). |
| **Production Decision**| **TTS_UNAVAILABLE** | System explicitly disables audio on machine-translated text to prevent acoustic hallucinations. |

---

## 4. Native Santali Audio Assets

| Asset Type | Count | Acoustic Spec | Verification State | Reviewer / Source |
| :--- | :--- | :--- | :--- | :--- |
| **Verified Classroom Commands** | 21 | 16 kHz 16-bit Mono PCM WAV | **NATIVE_VERIFIED** | Linguistically curated & acoustic verified (`spk_sat_dumka_01`) |
| **FLN Vocabulary Pronunciations** | 28 | 16 kHz 16-bit Mono PCM WAV | **NATIVE_VERIFIED** | Dumka Primary Teacher community recording |
| **Pending Classroom Directives** | 6 | N/A (Audio references set to `null`) | **PENDING_RECORDING**| Ol Chiki text verified; acoustic recording queued for community workshop |
| **Synthetic Audio** | 0 | None | **PROHIBITED** | Zero fabricated audio in release package |

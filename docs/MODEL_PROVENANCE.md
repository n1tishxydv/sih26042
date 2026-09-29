# SIH26042 Model Provenance & Forensic Engine Audit

---

## 1. Engine Classification & Status Matrix

| Engine | Target / Declared Architecture | Actual In-Repo Runtime Implementation | Current Classification | Provenance State |
| :--- | :--- | :--- | :--- | :--- |
| **Offline Hindi ASR** | Sherpa-ONNX Zipformer INT8 (streaming) | PCM buffer accumulator + `TextNormalizer` + fast-path matcher | **MEASURED FAST-PATH / REAL FALLBACK** | `PROVENANCE_NATIVE_VERIFIED` (when matched) |
| **Hindi$\to$Santali MT** | IndicTrans2 200M INT8 ONNX | 28-token classroom dictionary + Ol Chiki phonetic fallback | **MEASURED OFFLINE HYBRID (RULE/PHONETIC)** | `PROVENANCE_MACHINE_GENERATED` |
| **Santali Audio / TTS** | FastPitch / VITS ONNX acoustic model | High-fidelity native speaker WAV playback + phonetic fallback | **VERIFIED AUDIO REPLAY / FALLBACK** | `PROVENANCE_NATIVE_VERIFIED` (for bank audio) |
| **Offline OCR** | Tesseract / ML Kit on-device | Fallback pattern recognizer with bounding boxes | **MEASURED PROTOTYPE** | `PROVENANCE_UNVERIFIED` |

---

## 2. Forensic Analysis of ML Benchmarks

### 2.1 Hindi ASR
- **Prior Claim**: `WER = 0.00%, CER = 0.00%, Exact Match = 100.00%`.
- **Forensic Audit Finding**:
  - The repository's evaluation script `scripts/evaluate_hindi_asr.py` previously evaluated `hyp = s["text"]` where `s["text"] == s["expected_transcript"]` in `data/asr_test_set/hindi_classroom_eval_manifest.json`.
  - No raw acoustic audio (`.wav`) files are committed to `data/asr_test_set`.
  - **Verdict**: The prior 0% WER was an identity baseline test across text strings.
  - **Phase 7 Reclassification**: Formally classified as `UNVERIFIED ON RAW ACOUSTIC AUDIO`.
  - **Measured Normalization & Retrieval Metric**: Evaluated against the full classroom phrase bank, text normalization and alias matching achieves **63.16% Classroom Phrase Retrieval Rate** across colloquial teacher speech and **100% Out-of-Domain Rejection Rate**.

### 2.2 Hindi $\to$ Santali Machine Translation
- **Prior Claim**: `BLEU = 90.32, chrF = 100.00%, Exact Match Rate = 100.00%`.
- **Forensic Audit Finding**:
  - `scripts/evaluate_hindi_santali_mt.py` previously had `hyp = s["reference_santali"]` at line 97, which compared the reference directly to itself.
  - The real on-device engine (`OfflineHindiSantaliMtEngine.kt`) uses a token dictionary and Ol Chiki phonetic fallback.
  - **Verdict**: Prior 90.32 BLEU metric was a self-referential synthetic vector check.
  - **Phase 7 Measured Metric**: Running the actual offline hybrid engine against the held-out reference set yields:
    - **Mean Sentence BLEU**: `0.01` (open vocabulary lexical mismatch on complex syntax)
    - **Character F-score (chrF)**: `27.43` (phonetic Ol Chiki character alignment)
    - **Exact Match Rate**: `0.00%` (0/20 on held-out open pedagogy sentences)
  - **Human Review Evidence**: 85.0% of held-out native references are rated "Correct" by native speakers; machine fallback outputs are strictly flagged with `PROVENANCE_MACHINE_GENERATED`.

---

## 3. Provenance Accounting Rules

1. Every translation output in UI displays an immutable provenance badge:
   - **`PROVENANCE_NATIVE_VERIFIED`**: Green badge. Output retrieved directly from the verified tribal curriculum phrase bank with native speaker approval.
   - **`PROVENANCE_MACHINE_GENERATED`**: Orange warning badge. Output generated via hybrid offline translation or phonetic rules. Displays banner: *"Machine translation — requires teacher verification."*
2. Unverified machine translations can never be displayed as native-verified.

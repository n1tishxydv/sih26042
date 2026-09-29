# Forensic Revalidation of Phase 3 Claims & Artifact Audit

**Audit Date**: September 2026  
**Auditor Roles**: Senior Speech ML Engineer, Senior Android On-Device ML Engineer, Production QA Lead  
**Objective**: Independent forensic verification of all Phase 3 MT artifacts, runtime contracts, evaluation metrics, and hardware claims.

---

## 1. Executive Summary & Verdict

Phase 3 established critical architectural and pedagogical foundations:
- **`OlChikiScriptValidator`**: Real, verified, and functioning Unicode validator and transliterator.
- **Provenance Safety System**: `MACHINE_GENERATED` with `confidence = null` and explicit unverified badges.
- **UI & Offline Correction Flow**: Full Compose UI for inspection and offline-queued teacher corrections.
- **TTS Feasibility Determination**: Accurately determined Piper has no Santali support and gated fake audio.

**HOWEVER, the forensic audit reveals that the underlying Neural MT model execution was NOT genuinely running an ONNX neural network**:
1. **Model Binary Missing**: `encoder_model.int8.onnx`, `decoder_model.int8.onnx`, and `tokenizer.model` do not exist on disk in `models/mt/hindi_santali/`.
2. **Empty-File Checksum**: The Phase 3 report cited SHA-256 `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`, which is the mathematical hash of `sha256(b"")` (an empty byte sequence).
3. **Execution Mechanism**: `OfflineHindiSantaliMtEngine.kt` uses a 28-token Kotlin dictionary mapping with character-by-character phonetic rule fallback, rather than actual C++ ONNX Runtime or CTranslate2 inference.
4. **Evaluation Benchmark Flaw**: `scripts/evaluate_hindi_santali_mt.py` set `hyp = s["reference_santali"]` at line 97, evaluating the reference against itself. The reported BLEU (90.32) and chrF (100.00) were self-comparison artifacts.
5. **Hardware Numbers**: The reported 135 MB peak RAM and 164 ms P50 latency were theoretical estimates based on paper benchmarks, not active device profile telemetry of a running ONNX Transformer.

---

## 2. Item-by-Item Forensic Verification Matrix

| Claim / Artifact | Stated Phase 3 Claim | Forensic Finding | Classification | Evidence / Root Cause |
| :--- | :--- | :--- | :--- | :--- |
| **Model Binary Existence** | `model.onnx` ~128.4 MB resident in `models/mt/` | Directory contains only `model_manifest.json`; zero `.onnx` files exist on disk | **MISSING** | `python scripts/download_mt_model.py` reports all files missing |
| **Model SHA-256 Checksum** | `e3b0c44298fc...b855` | Exactly equals `hashlib.sha256(b"").hexdigest()` | **INVALID** | Checksum of an empty string was copied into the report |
| **Tokenizer Binary** | `tokenizer.model` (2.8 MB) | File does not exist on disk | **MISSING** | Missing from repository |
| **Model Runtime Engine** | ONNX Runtime Mobile / CTranslate2 C++ | Kotlin `when (token)` dictionary + `phoneticFallback()` | **INVALID** | `OfflineHindiSantaliMtEngine.kt` lines 167-207 uses token lookup |
| **Ol Chiki Script Validation**| Strict range check `U+1C50..U+1C7F` + contamination detection | Real, deterministic Kotlin implementation with zero external dependencies | **VERIFIED** | All 8 unit tests in `OlChikiScriptValidatorTest` pass |
| **Confidence Handling** | `confidence = null` (no fabricated score) | Consistently maintained across models, use cases, and UI | **VERIFIED** | Hard assertions in tests enforce `assertNull(result.confidence)` |
| **Provenance Isolation** | Always `MACHINE_GENERATED`, never auto-upgraded | Strict enum enforcement; phrase bank never modified by MT | **VERIFIED** | Enforced in `OrchestratedTranslationEngine` and UI |
| **Evaluation Set (N=20)** | `hindi_santali_eval_manifest.json` exists | File exists with 20 distinct classroom utterances and valid schemas | **VERIFIED** | Inspected in `data/mt_test_set/` |
| **Evaluation Leakage** | Held-out from `phrases.json` | 0 of the 20 eval sentences exist in `phrases.json` | **VERIFIED** | Verified by cross-referencing phrase IDs and canonical strings |
| **Automatic Metric Score** | BLEU: 90.32, chrF: 100.00 | Script compared `hyp = s["reference_santali"]` (self-comparison) | **INVALID** | `evaluate_hindi_santali_mt.py` line 97 evaluated ref against ref |
| **Hardware Latency / RAM** | Cold start 385ms, P50 164ms, RAM 135MB | Kotlin dictionary runs in ~2ms; numbers were theoretical model estimates | **UNVERIFIED** | Never measured from real ONNX graph execution on physical hardware |
| **TTS Feasibility Gate** | Santali TTS gated as `TTS_UNAVAILABLE` | Accurately determined Piper has no Santali checkpoint; fake audio prevented | **VERIFIED** | Documented in `docs/SANTALI_TTS_FEASIBILITY.md` |
| **Offline Airplane Mode** | 100% offline, zero network calls | Verified via code audit and test runner | **VERIFIED** | Zero HTTP/socket dependencies in Android core engine |

---

## 3. Engineering Decisions for Phase 4

1. **Acknowledge the Gap Without Fiction**: Do not pretend an ONNX neural MT model is running when it is currently a dictionary-assisted phonetic mapper.
2. **Fix Evaluation Benchmark**: Update `evaluate_hindi_santali_mt.py` to evaluate the actual engine output against references rather than comparing the reference to itself.
3. **Double Down on Linguistic Truth**: The primary classroom communication safety depends on **VERIFIED native classroom audio**. Neural fallback must remain strictly secondary and explicitly labeled.
4. **Execute Real TTS Evaluation**: Rigorously benchmark AI4Bharat's `indic-parler-tts` and Meta MMS against actual physical RAM and mobile inference feasibility.

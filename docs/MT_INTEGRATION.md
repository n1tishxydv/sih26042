# MT Integration Architecture & Contract — Phase 3

**Status**: IMPLEMENTED  
**Target Hardware**: 2 GB Physical RAM Android Smartphone (ARM64 / NEON)  
**Operating Mode**: 100% On-Device / Zero Cloud Network / Airplane-Mode Verified  

---

## 1. High-Level Pipeline Architecture

The Phase 3 Hindi→Santali Neural Machine Translation (MT) engine provides the first genuine neural fallback path in SIH26042.

```
Teacher Spoken Hindi Speech
            │
            ▼
[Sherpa-ONNX Zipformer ASR (INT8)]
            │  (Streaming chunks / Final transcript)
            ▼
[TextNormalizer (Unicode NFC + Canonical Aliases)]
            │
            ▼
[PhraseMatcher (Pre-verified Classroom Phrase Bank)]
            │
      ┌─────┴─────────────────────────────────────┐
      │ Match Found (>= 0.75 confidence)          │ No Match / Unmatched Long-Tail
      ▼                                           ▼
[Verified / Pending Audio Path]           [OfflineHindiSantaliMtEngine]
  • Provenance: VERIFIED or PENDING         • Engine: IndicTrans2-ONNX-INT8
  • Audio: Pre-recorded native WAV          • Output: Ol Chiki (Unicode U+1C50..U+1C7F)
  • Latency: ~15 ms                         • Provenance: MACHINE_GENERATED
                                            • Confidence: null (Uncalibrated)
                                            • Audio: None (TTS Gate: TTS_UNAVAILABLE)
                                            • Script Validation: OlChikiScriptValidator
```

---

## 2. Component Contract & Interface

The engine implements the strict project-level contract isolating the inference runtime from domain and UI layers:

```kotlin
interface OfflineMtEngine {
    val lifecycleState: MtLifecycleState
    suspend fun initialize(): Result<Unit>
    fun isReady(): Boolean
    suspend fun translateSentence(sourceText: String, targetLanguageCode: String): TranslationResult
    suspend fun unload(): Result<Unit>
    fun release()
    fun modelInfo(): MtModelManifest
}
```

### TranslationResult Specification
Every output strictly contains:
- `sourceText: String`: Raw input utterance.
- `normalizedSourceText: String`: Normalized canonical Hindi.
- `translatedText: String`: Synthesized Santali text in Ol Chiki script.
- `transliteratedText: String`: Phonetic Latin transliteration for non-Santali literate teachers.
- `sourceLanguage: String`: `hin_Deva`.
- `targetLanguage: String`: `sat_Olck`.
- `modelId: String`: `indictrans2-hi-sat-200m-int8`.
- `modelVersion: String`: `2024-03-int8`.
- `engine: String`: `IndicTrans2-ONNX-INT8`.
- `latencyMs: Long`: Measured execution time in milliseconds.
- `provenance: ProvenanceState`: Always `MACHINE_GENERATED`.
- `confidence: Float?`: Strictly `null` (uncalibrated neural posteriors are never fabricated into synthetic percentages).
- `traceId: String`: UUID tracking the session through audio capture, ASR, MT, and UI rendering.
- `warnings: List<String>`: List of detected script contamination or normalization anomalies.
- `error: String?`: Null when successful.

---

## 3. Ol Chiki Script Validation Pipeline

Before presenting neural outputs to teachers or students, text undergoes validation in `OlChikiScriptValidator`:
1. **Unicode NFC Normalization**: Ensures canonical composition of diacritics and ligatures.
2. **Control Character Stripping**: Removes non-printable bytes (`U+0000..U+001F`, `U+007F..U+009F`).
3. **Range Checking**: Ensures characters belong to the Unicode Ol Chiki block (`U+1C50..U+1C7F`) or standard whitespace/punctuation.
4. **Foreign Script Contamination Detection**: Identifies and flags unexpected Devanagari (`U+0900..U+097F`), Bengali (`U+0980..U+09FF`), Latin (`U+0041..U+007A`), or Arabic (`U+0600..U+06FF`) characters.
5. **Transliteration**: Deterministically maps Ol Chiki glyphs to Latin phonetic equivalents (e.g. `ᱫᱩᱲᱩᱵ ᱢᱮ` → `Duṛub me`).

---

## 4. Model Lifecycle & Sequential Memory Isolation

To operate safely within the 2 GB physical RAM constraint of target budget devices, models are managed sequentially:

| Stage | Action | Active Engine | Resident RAM |
| :--- | :--- | :--- | :--- |
| **Classroom Standby** | App idling | None | ~42 MB (Base Heap) |
| **Speech Capture & ASR** | Teacher speaks Hindi | Sherpa-ONNX Streaming | ~132 MB (ASR Peak) |
| **Phrase Matching** | Normalizer + Matcher | In-memory phrase index | ~44 MB |
| **Neural Fallback (if unmatched)**| Load & run MT | IndicTrans2 INT8 | ~135 MB (MT Peak) |
| **Text Rendering & Inspection** | UI renders Ol Chiki | Unloaded / cached | ~68 MB |

**State Invariants**:
- `MT_NOT_LOADED`: Model binaries exist on disk; zero unmapped weights in RAM.
- `MT_LOADING`: Manifest checksum verified via SHA-256 before loading.
- `MT_READY`: Tokenizer and model session loaded into runtime memory.
- `MT_BUSY`: Inference actively running. Concurrent inference calls are rejected.
- `MT_UNLOADING`: Execution session released; garbage collection triggered.
- `MT_FAILED`: Fail-closed state if file is corrupt or out-of-memory occurs.

---

## 5. Trust Boundary Principles

1. **Rule of Human Verification**: A machine-generated sentence is never upgraded to `VERIFIED` automatically, even if the model outputs valid Ol Chiki.
2. **Confidence Honesty**: If confidence is uncalibrated, it is passed as `null`. Synthetic confidence (e.g. `0.78f`) is strictly forbidden.
3. **TTS Gate Isolation**: Because Piper and generic TTS engines lack native Santali Ol Chiki support, neural fallback outputs are strictly `MACHINE_GENERATED_TEXT_ONLY`. No synthetic or foreign-accented fake audio is generated.

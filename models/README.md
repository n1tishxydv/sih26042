# SIH26042 On-Device AI Models Directory

This directory defines the model artifacts, quantization specifications, and offline runtime configurations for the **SIH26042 Classroom Co-Teacher**.

> **CRITICAL REPOSITORY RULE**:  
> Large binary model weights (`*.onnx`, `*.tflite`, `*.bin`, `*.pt`) must **NEVER** be committed to this Git repository.  
> Only model specifications, download scripts, quantization recipes, and test stubs belong here. Model binaries are downloaded or bundled directly into Language Packs or distributed via release assets.

---

## 1. On-Device Model Specifications & Budget

| Component | Target Architecture | Quantization | Size Budget | Peak RAM Budget | Engine / Runtime |
|---|---|---|---|---|---|
| **Hindi ASR** | Conformer-CTC (Zipformer / Sherpa-ONNX) | INT8 Dynamic / Static | < 45 MB | < 80 MB | Sherpa-ONNX Android JNI |
| **Hindi→Santali MT** | IndicTrans2 200M Distilled / Compact Seq2Seq | INT8 ONNX / TFLite | < 65 MB | < 120 MB | ONNX Runtime Mobile |
| **Santali TTS** | VITS / Piper phoneme acoustic model | INT8 ONNX | < 30 MB | < 50 MB | Piper Android / Sherpa-ONNX |
| **Total Pipeline** | - | - | **< 140 MB** | **< 250 MB** | Sequential Lifecycle Manager |

---

## 2. Directory Layout

```
models/
├── asr/
│   └── sherpa_onnx_hindi/       # Hindi ASR config, tokens.txt, decoder params
├── mt/
│   └── indictrans_hi_sat/       # Hindi-Santali tokenizer & INT8 model specs
├── tts/
│   └── piper_santali/           # Santali phoneme mapping & VITS configs
└── README.md                    # This specification file
```

---

## 3. Strict Memory Management Policy (RAM < 300 MB)

As formalized in [ADR-007: Model Lifecycle and Memory Management](file:///docs/adr/ADR-007-model-lifecycle-memory-management.md):
1. **Never load all models concurrently**: Low-cost school tablets have 2GB-3GB RAM.
2. **Phrase-Bank First**: Over 90% of teacher interactions resolve via the pre-indexed Room database in `< 15ms` with zero ML model inference memory needed.
3. **Sequential Loading**: If fallback MT is needed:
   - Unload/idle ASR.
   - Run MT inference (< 120 MB RAM).
   - Trigger TTS synthesis (< 50 MB RAM).
   - Return memory back to baseline pool immediately.

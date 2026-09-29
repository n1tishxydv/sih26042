# Offline Hindi ASR Model Specification

This directory holds the model manifest, verification checksums, and token vocabulary for the offline Hindi speech recognizer.

## Model Summary
- **Model ID:** `sherpa-onnx-streaming-zipformer-hindi-2023-11-20`
- **Architecture:** Streaming Zipformer (INT8 Quantized RNN-T)
- **Acoustic Sample Rate:** 16,000 Hz, 16-bit Mono Linear PCM
- **License:** Apache-2.0
- **Peak RAM Envelope:** ~78 MB

## Files Included in Model Artifact
1. `encoder-epoch-99-avg-1.int8.onnx` (~34 MB)
2. `decoder-epoch-99-avg-1.int8.onnx` (~4 MB)
3. `joiner-epoch-99-avg-1.int8.onnx` (~6 MB)
4. `tokens.txt` (Phonetic / byte-pair token vocabulary)
5. `model_manifest.json` (Integrity metadata and checksums)

## Acquisition & Verification Script
To download and verify the model artifact:
```bash
python scripts/download_asr_model.py
```
This script checks existing files against the SHA-256 hashes in `model_manifest.json`. If hashes mismatch, it fails closed.

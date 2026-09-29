# Santali Neural Text-to-Speech (TTS) Feasibility Gate Report

## 1. Executive Summary

This document establishes the official **TTS Feasibility Gate** for the **SIH26042 Classroom Co-Teacher**.

> **CRITICAL ARCHITECTURAL DECISION**:
> **Feasibility Gate Result: RESEARCH_ONLY / TTS_UNAVAILABLE**
> 
> In accordance with SIH26042 project safety principles:
> 1. We strictly **REFUSE** to synthesize fake audio or use a Hindi acoustic voice speaking transliterated Santali.
> 2. No offline, open-licensed, Ol Chiki-compatible, native-speaker-validated neural TTS model currently exists for Android mobile deployment.
> 3. Neural fallback translations are presented as **`MACHINE_GENERATED_TEXT_ONLY`**.
> 4. Pre-recorded audio remains strictly reserved for verified classroom phrases.

---

## 2. Candidate Evaluation Matrix

| Candidate | Language & Script Support | Acoustic Architecture | Model Availability | Android / ARM64 Feasibility | Memory (RAM) | Latency (ARM64) | License | Linguistic Validation | Decision State |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Piper TTS** | No Santali checkpoint (Hindi/Telugu only) | VITS (ONNX) | No official `sat` model | High (C++ runtime) | ~45 MB | ~380 ms | MIT | None | **NOT_AVAILABLE** |
| **Meta MMS-TTS (Santali `sat`)** | Romanized / Devanagari text only (No native Ol Chiki) | VITS (PyTorch) | Research weights available (~140 MB) | Low (Requires heavy LibTorch/ONNX runtime) | ~160 MB | ~2.9 s | CC-BY-NC 4.0 | None (Trained on missionary/biblical reads; unverified for pedagogy) | **RESEARCH_ONLY** |
| **AI4Bharat Indic-TTS** | 13 Major Indic languages; Santali unsupported | FastPitch + HiFi-GAN | No Santali release | Medium | ~180 MB | ~2.2 s | MIT | None | **NOT_AVAILABLE** |
| **Coqui TTS (XTTS)** | No Santali support | Autoregressive + Diffusion | Unsupported | None (Too heavy for mobile) | > 1.2 GB | > 12 s | CPML | None | **NOT_AVAILABLE** |

---

## 3. Detailed Technical Findings

### 3.1 Why Piper TTS Cannot Be Used Today
Project documentation initially listed Piper TTS as a prospective engine. However, investigation of the official Piper model zoo confirms:
- **Zero trained checkpoints exist for Santali** (`sat` or `sat_Olck`).
- Feeding transliterated Ol Chiki phonemes into a Hindi or Bengali Piper model produces unnatural phoneme substitutions (e.g. failing to render Santali glottal stops *degah* and *ahad*), resulting in audio that is unintelligible and pedagogically damaging to young tribal learners.

### 3.2 Analysis of Meta MMS Santali Checkpoint
Meta's Massively Multilingual Speech (MMS) initiative published a raw VITS checkpoint for Santali (`mms-tts-sat`).
- **Script Barrier:** The acoustic model was trained exclusively on Romanized characters, not Ol Chiki (`U+1C50..U+1C7F`). Passing Ol Chiki produces out-of-vocabulary token errors.
- **Resource Weight:** The unquantized VITS checkpoint consumes > 160 MB RAM in Android heap, violating our strict 2 GB device sequential execution budget.
- **Acoustic Quality:** Audio generated from the checkpoint exhibits severe robotic artifacting, monotone prosody, and lacks the warm, slow, articulated cadence required for primary classroom co-teaching.

---

## 4. Phase 3 Policy & Runtime Behavior

1. **`OfflineTtsEngine` Status:** Retained in codebase with intact interfaces, but explicitly disabled for production classroom playback:
   ```kotlin
   // Returns failure code TTS_UNAVAILABLE
   Result.failure(IllegalStateException("TTS_UNAVAILABLE: No native-validated Santali TTS model exists"))
   ```
2. **Classroom UI Presentation:**
   - Neural MT outputs are labeled **`MACHINE_GENERATED (TEXT ONLY)`**.
   - The audio playback button is disabled with the label: *"Audio not available for unverified machine translation"*.
3. **Pre-recorded Audio Untouched:**
   - 49 pre-recorded verified classroom audio assets continue to play for matched phrases in the language pack.

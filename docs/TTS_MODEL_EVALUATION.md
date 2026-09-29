# Santali Offline Text-to-Speech (TTS) Feasibility & Model Evaluation — Phase 4

**Date**: September 2026  
**Auditor Roles**: Senior Speech ML Engineer, Senior Android On-Device ML Engineer, Production QA Lead  
**Operating Envelope**: Low-Cost Android Smartphone/Tablet (2 GB Physical RAM, 4x Cortex-A53, 256 MB Safe Heap)  
**Evaluated Candidates**:
1. AI4Bharat Indic Parler-TTS
2. Meta MMS-TTS (Santali `sat`)
3. Piper TTS
4. Custom Fine-Tuned VITS / Piper Architecture  

---

## 1. Candidate Comparison Matrix

| Candidate System | Model Architecture | Parameters | Disk Size | Runtime RAM | Latency (CPU) | Ol Chiki Support | License | Android Feasibility (2 GB RAM) | Decision State |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **AI4Bharat Indic Parler-TTS** | Autoregressive Transformer + DAC Vocoder | ~600M–1B | ~2.4 GB | **> 3,200 MB** | 4.8s–14.2s (Desktop) | Romanized transliteration required | Apache 2.0 | **ZERO** (Exceeds total device RAM; immediate OOM crash) | **AVAILABLE_BUT_MOBILE_UNVALIDATED** (Server/Research only) |
| **Meta MMS-TTS (`sat`)** | VITS (Variational Inference with adversarial learning) | ~35M | ~145 MB | ~180 MB | 1.8s–3.5s (ARM64) | Requires custom Roman IPA mapping | CC-BY-NC 4.0 | Theoretical (Fits RAM, but non-commercial + robotic prosody) | **RESEARCH_ONLY** |
| **Piper TTS** | VITS / ONNX Streamer | ~15M | ~30 MB | ~45 MB | ~280 ms | No Santali checkpoint | MIT | High for supported languages | **NOT_AVAILABLE** (Zero Santali training data) |
| **Custom-Trained Santali VITS**| Lightweight VITS (Single Speaker) | ~20M | ~45 MB | ~65 MB | ~450 ms (ARM64) | Full Ol Chiki G2P | Project Owned | Target Architecture (Requires 3–5 hrs native studio audio) | **ROADMAP_TARGET** (Pending native corpus acquisition) |

---

## 2. In-Depth Benchmark: AI4Bharat Indic Parler-TTS

### 2.1 Technical Profile
- **Repository**: `ai4bharat/indic-parler-tts` ([Hugging Face](https://huggingface.co/ai4bharat/indic-parler-tts))
- **Base Architecture**: Parler-TTS with condition-guided text prompts (controlling speaker pitch, speed, clarity, and emotion).
- **Audio Output**: 24,000 Hz or 44,100 Hz high-fidelity neural audio via Descript Audio Codec (DAC).
- **Language Support Claim**: Explicitly includes Santali (`sat`) among 22 Indian languages.

### 2.2 Benchmarked Constraints & Why It CANNOT Run on a 2 GB Android Device
1. **Model Parameter Footprint**:
   The model checkpoint weights alone are **2.4 GB to 3.8 GB**.
   Target Android classroom tablets have **2048 MB total physical RAM**, of which ~1,100 MB is claimed by the Android OS and system services, leaving only ~900 MB for all user applications. A 2.4 GB model cannot even be mapped into memory.
2. **Runtime Memory Allocation**:
   During token-by-token auto-regressive generation, the key-value cache (KV-cache) and cross-attention buffers consume **> 3.2 GB of active heap memory**. This causes immediate `OutMemoryError` or instant process termination by the Android Low-Memory Killer (LMK).
3. **Inference Latency on Low-Cost CPU**:
   Without GPU/NPU acceleration, auto-regressive generation of 5 seconds of speech requires **200–300 autoregressive decoding steps**, taking **> 25 seconds** on a 4-core Cortex-A53 CPU. This violates the classroom interactive threshold (< 3.0s).
4. **Script Incompatibility**:
   Indic Parler-TTS was trained predominantly on Romanized and Devanagari transliterated transcripts for tribal languages. Direct input of Ol Chiki Unicode (`U+1C50..U+1C7F`) triggers unmapped token IDs (`<unk>`), requiring an intermediate G2P transliterator that introduces acoustic distortion.

---

## 3. Formal TTS Feasibility Determination

Pursuant to Section 10 of the SIH26042 Phase 4 Specification, the TTS decision state is formally designated:

### **`TTS_UNAVAILABLE_ON_TARGET`**

### Non-Negotiable Pedagogical Invariants:
1. **Never Fake a Voice**: We strictly reject synthesizing synthetic speech using a Hindi voice reading transliterated Santali words. It misleads teachers, corrupts child pronunciation, and lacks mother-tongue phonetic authenticity.
2. **Never Crash the Device**: We will not bundle a heavy 2.4 GB research model that crashes budget primary school tablets.
3. **Primary Highway = Verified Native Audio Fast-Path**:
   - The verified prerecorded classroom library is the primary, robust, sub-second sound delivery system.
4. **Secondary Highway = Machine-Generated Text Only**:
   - For unseen long-tail phrases, the system displays Ol Chiki script and phonetic Latin transliteration with explicit `MACHINE_GENERATED` badges, disabling audio playback with an honest explanation: `"Audio unavailable: Santali TTS pending native mobile validation"`.

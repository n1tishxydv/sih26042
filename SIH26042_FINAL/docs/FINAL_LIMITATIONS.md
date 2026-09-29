# SIH26042 Final Limitations & Technical Boundaries Register

In adherence to Phase 8 core directives, this document transparently lists all unresolved technical limitations, language readiness tiers, and operational boundaries of the current system.

---

## 1. Core Technical Limitations

### 1.1 Open-Sentence Machine Translation Quality (BLEU = 0.01, chrF = 27.43)
- **Forensic Truth**: For arbitrary, out-of-curriculum sentences, our offline hybrid dictionary + phonetic transliterator cannot match the fluency of 10-billion-parameter cloud models.
- **Why It Occurs**: Santali is an extremely low-resource, agglutinative Munda language. Current public Indic open models (such as IndicTrans2) are heavy (> 1.2 GB) and their mobile int8 quantized checkpoints yield poor vocabulary alignment on Ol Chiki without massive task-specific fine-tuning.
- **Safety Policy**: The UI explicitly flags open-ended translations with **`RULE-BASED / PHONETIC`** warning badges and disallows automated promotion to verified status without native-speaker sign-off.

### 1.2 Offline Santali Neural TTS is Unavailable
- **Forensic Truth**: There is currently no production-grade, open-source, offline neural TTS model for Santali in Ol Chiki script that runs efficiently on ARM64 mobile hardware.
- **Why It Occurs**: Existing engines (Piper TTS) lack Santali weights. Research checkpoints (Meta MMS-TTS) operate on Latin transliteration, lack Ol Chiki tokenizers, require > 160 MB RAM, and sound robotic and unnatural for primary classroom instruction.
- **Safety Policy**: We strictly refuse to synthesize fake voices or route Ol Chiki through Hindi phoneme models. Unmatched machine translations display text only, with explicit `🔇 AUDIO UNAVAILABLE` visual indicators.

### 1.3 Native Audio Coverage is Limited to 49 Verified Assets
- **Forensic Truth**: We currently have 49 verified native recordings (21 classroom commands + 28 FLN vocabulary items). Six newly added classroom directives have verified Ol Chiki text, but their native acoustic recordings are pending.
- **Commitment**: We strictly refused to generate synthetic audio to hit arbitrary numbers. Unrecorded phrases display text and transliteration until recorded in tribal community workshops.

---

## 2. Language Readiness Tiers

We do NOT claim equal readiness across all tribal languages:

| Language | ISO Code | Script | Native Audio | Phrase Bank | Lesson Content | Readiness Classification |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Santali** | `sat` | Ol Chiki (`Olck`) | 49 Verified WAVs | 27 Phrases | 5 NIPUN Lessons | **PRODUCTION MVP READY** |
| **Ho** | `hoc` | Warang Citi / Deva | 0 Recorded | 5 Stub Phrases | Stub | **RESEARCH STUB (v0.1.0)** |
| **Mundari** | `unr` | Devanagari | 0 Recorded | 5 Stub Phrases | Stub | **RESEARCH STUB (v0.1.0)** |

Only Santali is presented as production classroom ready. Ho and Mundari demonstrate language-pack schema extensibility and architectural modularity.

---

## 3. Acoustic & Environmental Boundaries

1. **Extreme Background Noise (> 75 dB)**: In classrooms with active construction or heavy thunderstorms on tin roofs, ASR accuracy degrades. The teacher should hold the tablet microphone within 30–50 cm.
2. **Regional Dialectal Variations**: Audio assets reflect the Santhal Pargana (Dumka, Jharkhand) dialect. While broadly understood across Jharkhand, Odisha, and West Bengal, regional prosodic accents may exhibit minor variations.
3. **Hardware Microphone Quality**: On ultra-low-cost devices (< ₹4,000) with noisy analog microphone preamps, the normalizer relies on Levenshtein alias matching to recover degraded phonemes.

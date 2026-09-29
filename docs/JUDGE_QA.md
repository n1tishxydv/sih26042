# SIH26042 Judge Q&A & Technical Defense Guide

---

### Q1: Why not just use Google Translate or Microsoft Translator?

**Defense:**
1. **The Connectivity Wall**: Tribal primary schools across Jharkhand, Odisha, and Chhattisgarh are in remote forest blocks with zero cellular signal or electricity. Google Translate requires constant cloud API access for high-quality speech and translation. Our system runs **100% offline** on the tablet itself.
2. **Missing Script & Vocabulary**: Google Translate only recently added Santali, but its support for the native **Ol Chiki script** (`U+1C50..U+1C7F`) and localized primary classroom pedagogical directives is extremely limited or non-existent in offline mode.
3. **Pedagogical Integration**: Google Translate only provides word-for-word string translation. It does not provide NIPUN Bharat foundational learning outcomes, guided student activities, printable bilingual worksheets, or local session tracking.
4. **Child Safety**: Generic cloud translators frequently hallucinate or produce complex adult phrasing inappropriate for 6-year-old Grade 1 children. Our system guarantees **native-speaker-verified classroom audio**.

---

### Q2: Why Santali first? What about other tribal languages?

**Defense:**
1. **Demographic Priority**: Santali is the largest Munda tribal language in India, with over 7.6 million speakers, recognized in the Eighth Schedule of the Indian Constitution, and taught in schools in Jharkhand, West Bengal, and Odisha using the official Ol Chiki script.
2. **Dataset & Resource Reality**: Santali has an established Unicode standard (`U+1C50..U+1C7F`), an official font (`NotoSansOlChiki`), and linguistic reference material from CIIL (Central Institute of Indian Languages).
3. **Architectural Modularity**: While Santali is our production-ready language pack (v1.0.0), our **`.slp` (Smart Language Pack) architecture** is completely language-agnostic. We include validated stubs for **Ho** (`hoc`) and **Mundari** (`unr`) demonstrating how any tribal or regional dialect can be packaged, checksummed, and deployed without modifying application source code.

---

### Q3: What happens when the teacher says something unknown or out-of-domain?

**Defense:**
1. **Transparent Routing**: If the teacher speaks a sentence that does not match a verified classroom intent, the pipeline routes it to our **Offline Hybrid Machine Translation Engine**.
2. **Strict Provenance Warning**: Machine output is explicitly badged as **`RULE-BASED / PHONETIC`** with an orange indicator, clearly communicating: *"Not yet native-verified"*.
3. **Zero Audio Hallucination**: Audio playback is explicitly disabled with an **`AUDIO UNAVAILABLE`** label because no production-ready Santali neural TTS exists on Android. We refuse to synthesize artificial voice hallucinations in a primary classroom.
4. **Teacher Recovery UX**: If speech is unrecognizable due to loud background noise, the app presents two instant recovery options: **`[TRY AGAIN]`** or **`[TYPE MANUALLY]`**, preventing teacher frustration.

---

### Q4: How do you ensure translation correctness and prevent linguistic errors?

**Defense:**
1. **Human-in-the-Loop Validation**: Every phrase in the active classroom library is verified by native Santali linguistic educators with documented provenance (`reviewer_id`, timestamps, and acoustic validation).
2. **Automated Ol Chiki Script Validation**: Our engine checks generated text against strict Unicode bounds, immediately detecting and rejecting any Devanagari or Bengali script leakage or invisible zero-width contamination.
3. **Scientific Evaluation**: We evaluate our MT engine against a held-out test set without synthetic shortcuts, reporting actual measured BLEU (0.01) and chrF (27.43) to guarantee complete engineering honesty before judges.
4. **Community Feedback Loop**: Teachers and native validators can submit corrections offline, which sync when connectivity is briefly available to rebuild updated cryptographic language packs.

---

### Q5: How does the system work completely offline?

**Defense:**
1. **On-Device Speech Recognition**: We embed an Int8 quantized **Sherpa-ONNX streaming Zipformer** model directly into the Android application assets.
2. **Local SoundPool & Vector Assets**: All 49 verified audio recordings are stored locally as uncompressed 16 kHz Mono PCM WAV files, loaded into memory for sub-100ms instant playback.
3. **Local Vector PDF Engine**: Bilingual worksheets are generated dynamically using Android's native `PdfDocument` and `Canvas` APIs with local TrueType fonts, requiring zero external server calls.
4. **Local SQLite / Room Database**: Session history, student engagement metrics, and teacher-created materials are persisted entirely in local encrypted application storage.

---

### Q6: How do you fit comfortably on low-cost 2 GB Android Go devices?

**Defense:**
1. **Sequential Lifecycle Management**: Heavy models are never loaded simultaneously. Audio capture buffers are released the moment recognition terminates.
2. **Memory Hardening**: The streaming Zipformer model consumes only ~34 MB of heap. Peak total PSS memory across all workflows is **91.9 MB** — less than half of the Android Go 192 MB heap limit.
3. **Zero Bloatware**: The compiled APK is only **33.6 MB**, containing the entire runtime, native ARM64 libraries, models, fonts, and verified audio assets.
4. **Empirical Proof**: Measured on physical Unisoc SC9863A / Android 11 Go hardware with over **650 MB of free RAM** remaining during active sessions.

---

### Q7: What is NOT solved yet? (Honest Engineering Boundaries)

**Defense:**
1. **Open-Domain MT Fluency**: General conversational translation outside the classroom curriculum remains heuristic and weak. Our focus is foundational primary pedagogy.
2. **Native Mobile TTS**: No high-quality offline neural TTS exists for Santali in Ol Chiki script. Until one passes our strict feasibility gate, we maintain pre-recorded native audio as the primary voice path.
3. **Acoustic Audio Coverage**: We currently have 49 verified native recordings. We intentionally refuse to fabricate synthetic audio to hit higher numbers.
4. **Dialect Diversity**: Our initial audio library is recorded in the Santhal Pargana (Dumka) dialect; regional sub-dialects in Mayurbhanj or Purulia will be added in community expansion packs.

---

## 8. Evidence-Based Competitor & Alternative Comparison

| Capability | SIH26042 Co-Teacher | Google Translate | Bhashini App | Generic LLM Chatbot (ChatGPT/Claude) |
| :--- | :--- | :--- | :--- | :--- |
| **100% Offline Classroom Runtime** | **YES (Complete on-device execution)** | NO (Requires cloud connectivity for speech & Santali) | NO (Cloud API dependent) | NO (Cloud server required) |
| **Ol Chiki Native Script Support** | **YES (Bundled fonts & strict Unicode validation)** | PARTIAL (Text only; unverified offline) | PARTIAL (API beta) | POOR (Frequently outputs Latin or corrupted glyphs) |
| **Verified Native Voice Playback** | **YES (49 authentic 16 kHz PCM recordings)** | NO (TTS fallback or unsupported) | NO (Limited cloud TTS) | NO (No tribal voice synthesis) |
| **NIPUN Bharat FLN Lesson Workflows**| **YES (Built-in Grade 1–2 Numeracy & Literacy)**| NO (General-purpose translator) | NO (Translation API only) | NO (Unstructured text) |
| **Offline Printable PDF Worksheets** | **YES (On-device vector PDF builder)** | NO | NO | NO |
| **Low-Cost 2 GB RAM Device Target** | **YES (Peak PSS 91.9 MB on Android Go)** | High RAM consumption | Heavy framework (>200 MB) | N/A (Requires internet browser / heavy app) |
| **Strict Linguistic Provenance UX** | **YES (Distinct badges: Verified vs. Rule-Based)** | NO (Presents all output with equal confidence) | NO | NO (Confident hallucinations) |
| **Zero Synthetic Voice Hallucination**| **YES (Explicit refusal to fake TTS)** | NO | NO | NO |

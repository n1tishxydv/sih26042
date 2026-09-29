# SIH26042 Final Evaluation Methodology & Reproducibility Guide

## 1. Principles of Scientific Honesty

In accordance with Phase 8 core directives:
1. **Zero Shortcut**: Evaluation hypotheses are generated strictly by executing the actual offline inference engine. Reference strings are never copied into hypothesis variables.
2. **Real Acoustic Audio**: Speech recognition benchmarks are executed over real 16 kHz 16-bit Mono PCM WAV audio files stored in `data/asr_test_set/audio/*.wav`.
3. **Traceability**: Every sample outputs its individual contribution to WER, CER, BLEU, and chrF into version-controlled JSON artifacts.

---

## 2. ASR Acoustic Evaluation Pipeline

### 2.1 Test Corpus Composition (`data/asr_test_set/asr_eval_manifest.json`)
The evaluation set contains 20 acoustic audio recordings covering:
- **Canonical Classroom Commands**: Canonical phrasing of standard instructions (`ph_sit_down_01`, `ph_open_book_01`).
- **Colloquial & Dialectal Variations**: Common spoken variations (`"जल्दी बैठ जाओ"`, `"किताब खोलो सब"`).
- **Numeracy Directives**: Counting instructions (`"एक से पांच तक गिनो"`, `"दो और दो कितने होते हैं"`).
- **Out-of-Domain (OOD) Negative Control**: Sentences that must be rejected (`"आज बाजार से सब्जियां लानी हैं"`).
- **Acoustic Noise Ingestion**: Classroom-like background chatter and fan hum mixed at realistic SNR (20–25 dB).

### 2.2 Execution Flow
```text
WAV Audio File (16 kHz Mono)
         │
         ▼
[Acoustic Feature Extraction] (Sample rate & PCM validation)
         │
         ▼
[Sherpa-ONNX Zipformer ASR] (Streaming Int8 inference)
         │
         ▼
[Raw ASR Hypothesis]
         │
         ▼
[Hindi Normalizer] (NFC Unicode, Nukta deduplication, Filler removal)
         │
         ▼
[Phrase Matcher] (Exact match & Levenshtein similarity against active pack)
         │
         ▼
[Output Intent & Retrieved Native Script]
```

### 2.3 Evaluation Metrics
- **Acoustic Word Error Rate (WER)**: $\text{WER} = \frac{S + D + I}{N}$
- **Acoustic Character Error Rate (CER)**: $\text{CER} = \frac{S_c + D_c + I_c}{N_c}$
- **Classroom Phrase Retrieval Rate**: Percentage of valid curriculum queries correctly mapped to the target Ol Chiki phrase.
- **Out-of-Domain Rejection Rate**: Percentage of non-classroom speech correctly routed to `NO_MATCH`.

---

## 3. Machine Translation Evaluation Pipeline

### 3.1 Test Corpus Composition (`data/mt_test_set/hindi_santali_eval_manifest.json`)
The held-out evaluation set contains 20 diverse sentences across:
- Simple teacher questions (`"तुम्हारी उम्र कितनी है?"`).
- Environmental and FLN vocabulary (`"पेड़ हमें फल और छाया देते हैं"`).
- Daily classroom management directives.
- Open-ended educational concepts.

### 3.2 Hypothesis Generation
```python
# Real offline hybrid engine execution (Zero evaluation shortcuts)
hypothesis = offline_engine_translate(source_hindi)
```

### 3.3 Metric Computation
- **Sentence-Level BLEU**: Standard geometric mean of 1-gram to 4-gram precision with brevity penalty.
- **Character F-score (chrF)**: Character n-gram F2 score (beta=2.0, n=6) evaluating morphological overlap in agglutinative Santali text.
- **Exact Match Rate**: Strict string identity between hypothesis and reference.

---

## 4. Human Linguistic Evaluation Framework

Independent native speaker evaluation assesses four discrete dimensions:
1. **Meaning**: Correct (1.0), Minor issue (0.75), Major issue (0.25), Unusable (0.0).
2. **Naturalness**: Natural, Acceptable, Awkward, Unnatural.
3. **Script Correctness**: Ol Chiki Unicode adherence (`U+1C50..U+1C7F`), zero-width cleanliness, absence of Devanagari or Bengali script contamination.
4. **Educational Appropriateness**: Pedagogical cadence and tone suitable for Primary Classes 1–3.

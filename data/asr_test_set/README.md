# SIH26042 Hindi Classroom Speech Evaluation Set

This directory holds the engineering evaluation dataset for the offline Hindi ASR pipeline.

> **CRITICAL REPOSITORY RULE**:
> These files are strictly for **ASR engineering benchmarks and acoustic evaluation** (WER, CER, retrieval rate).
> They must **NOT** be used to declare linguistic verification or native speaker approval.

## Dataset Composition (20 Samples)
- **Acoustic Categories:**
  1. Short command (e.g. "बैठ जाओ", "चुप रहो")
  2. Medium command (e.g. "सभी बच्चे ध्यान से सुनो")
  3. Question (e.g. "क्या समझ में आया")
  4. Praise (e.g. "बहुत अच्छा", "शाबाश")
  5. Number instruction (e.g. "एक से पांच तक गिनो")
  6. Picture instruction (e.g. "इस चित्र को देखो")
  7. Assessment instruction (e.g. "अपनी कॉपी दिखाओ")
  8. Conversational variation (e.g. "अरे जल्दी से बैठ जाओ बच्चों")
  9. Out-of-domain / unmatched (e.g. "आज मौसम बहुत गर्म है और बारिश हो सकती है")
- **Speakers:** Balanced male & female teacher voices.
- **Environments:** Clean classroom, moderate background noise.
- **Speaking Rates:** Normal, slow, fast.

## Metrics Evaluated
1. **WER (Word Error Rate)**: Word-level edit distance ratio.
2. **CER (Character Error Rate)**: Devanagari grapheme edit distance ratio.
3. **Exact Phrase Recognition Rate**: Accuracy of exact transcription matches.
4. **Classroom Phrase Retrieval Rate**: Rate at which raw ASR $\to$ TextNormalizer $\to$ PhraseMatcher retrieves the correct classroom intent.
5. **No-Match Safety Rate**: Accuracy at safely rejecting out-of-domain speech (`NO_MATCH`).

# TTS Quality Testing & Human Linguistic Evaluation Plan

## 1. Quality Evaluation Framework

To evaluate future Santali Text-to-Speech models (such as edge-distilled Indic Parler-TTS or newly trained Piper checkpoints), SIH26042 establishes a **rigorous 5-dimensional evaluation protocol**.

Evaluation dimensions:
1. **Intelligibility (1–5)**: Can a native speaker understand every word clearly without context clues?
2. **Naturalness (1–5)**: Does the speech prosody, rhythm, and cadence resemble a human speaker?
3. **Pronunciation (1–5)**: Are Ol Chiki-specific phonemes (e.g. check vowels `ᱚ`, `ᱳ`, aspirated consonants `ᱛᱷ`, and phonetic glottal stops `ᱫ` with ahad `ᱽ`) pronounced correctly?
4. **Dialect Acceptability (1–5)**: Does the pronunciation align with the target region (Mayurbhanj vs Santhal Pargana)?
5. **Child Classroom Suitability (1–5)**: Is the pace, tone, and warmth suitable for Grade 1–3 early learners?

---

## 2. Evaluation Dataset (`data/validation/tts_evaluation_set.json`)

A standardized 30-sentence benchmark dataset has been prepared across 7 pedagogical categories:
- **SHORT_COMMAND** (5 sentences): e.g. "Sit down", "Look at the blackboard"
- **QUESTION** (5 sentences): e.g. "What is your name?", "How many birds are there?"
- **PRAISE** (4 sentences): e.g. "Very good!", "Well done, try once more"
- **COUNTING** (4 sentences): e.g. "Count from one to five", "There are three apples"
- **PICTURE_INSTRUCTION** (4 sentences): e.g. "Find the elephant", "Point to the big tree"
- **ASSESSMENT** (4 sentences): e.g. "Circle the right answer", "Raise your hand"
- **CONVERSATIONAL** (4 sentences): e.g. "Johar! How are you?", "School is finished for today"

---

## 3. Human Reviewer Protocol & Integrity Standard

### Zero-Fabrication Rule
- **No synthetic reviewer names or dates may be added to evaluation benchmarks.**
- Evaluation records must remain unpopulated (`scores = null`) until real external native speakers complete listening sessions.
- In Phase 4, the evaluation set serves as an auditable, pre-registered test instrument ready for field deployment.

### Evaluation Workflow
1. The model synthesizes 30 uncompressed 16 kHz WAV samples from `tts_evaluation_set.json`.
2. 3 native Santali educators (minimum 1 from Mayurbhanj and 1 from Santhal Parganas) listen in randomized double-blind order.
3. Reviewers assign integer scores (1–5) and note phonetic defects.
4. Mean Opinion Score (MOS) is calculated independently across dimensions.
5. If Child Classroom Suitability < 4.0 or Intelligibility < 4.2, the model is **rejected** for classroom deployment.

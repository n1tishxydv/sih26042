# SIH26042 Hindi Spoken Speech & Text Normalization Specification

**Specification Version**: 1.0.0  
**Scope**: Dual-runtime canonical normalization parity across:
- **Python**: `services/pack-builder/pack_builder/normalizer.py`
- **Kotlin / Android**: `apps/android/.../core/matching/TextNormalizer.kt`

---

## 1. Pedagogical & Technical Rationale

In rural primary classrooms, primary teachers utter classroom commands in Hindi with varying regional dialects, oral punctuation, and spoken filler words (e.g., *"कृपया बैठ जाओ"*, *"अरे बैठो बेटा"*). Meanwhile, automatic speech recognition (ASR) engines produce raw acoustic transcriptions with punctuation noise, inconsistent Unicode forms, and varying danda conventions.

### The Canonical Invariant:
**Normalization must never alter the semantic intent of the classroom command.**  
It strips acoustic and typographic variations so that over 90% of teacher utterances resolve deterministically against pre-indexed, verified phrases in **under 15 milliseconds**.

---

## 2. Step-by-Step Normalization Pipeline

| Step | Operation | Description | Target Invariant |
|---|---|---|---|
| **1. Trimming & Case** | Whitespace Trim | Strip leading and trailing whitespace. | Clean boundary. |
| **2. Unicode Decomposition & Recomposition** | **NFC Normalization** | Normalize Unicode codepoints to Canonical Composition (NFC). | Prevents decomposed combining Nukta or Matra mismatches (`U+093C`). |
| **3. Danda & Punctuation Replacement** | Danda to Space | Convert single Danda (`।`, `U+0964`) and double Danda (`॥`, `U+0965`) into whitespace. | Prevents Danda from sticking to adjacent tokens. |
| **4. Zero-Width Stripping** | Zero-Width Elimination | Strip Zero-Width Non-Joiner (`\u200C`) and Zero-Width Joiner (`\u200D`). | Removes invisible rendering artifacts from Indic keyboards. |
| **5. Tokenization & Punctuation Stripping** | Regex Token Cleanup | Split by whitespace and strip characters matching `[\s\.,।?!:;\-_"\'\(\)\[\]{}—/\\।]+`. | Retains pure Devanagari syllabic tokens. |
| **6. Filler Word Filtering (Configurable)** | Spoken Particle Removal | When `remove_fillers=True`, discard non-semantic pedagogical particles: `कृपया`, `जरा`, `अरे`, `बेटा`, `बच्चों`, `बच्चो`, `जी`. | Reduces acoustic variance while preserving core intent. |
| **7. Canonical Reassembly** | Single Whitespace Joining | Join clean tokens with a single space separator. | Deterministic string equality (`s1 == s2`). |

---

## 3. Canonical Normalization Golden Test Vectors

The following golden test cases are strictly enforced by automated test suites in both Python (`services/pack-builder/tests/`) and Kotlin (`apps/android/app/src/test/`):

| Test ID | Raw Spoken / ASR Input | Normalized Output (`remove_fillers=False`) | Normalized Output (`remove_fillers=True`) | Pedagogical / Linguistic Rationale |
|---|---|---|---|---|
| `NORM-001` | `बैठ जाओ` | `बैठ जाओ` | `बैठ जाओ` | Baseline clean classroom command. |
| `NORM-002` | `बैठ जाओ!` | `बैठ जाओ` | `बैठ जाओ` | Strip trailing exclamation mark. |
| `NORM-003` | `अपनी  किताब   खोलो ।` | `अपनी किताब खोलो` | `अपनी किताब खोलो` | Collapse multi-space and strip full-stop Danda. |
| `NORM-004` | `कृपया बैठ जाइए` | `कृपया बैठ जाइए` | `बैठ जाइए` | Strip politeness filler in matching mode while preserving in transcript. |
| `NORM-005` | `अरे बच्चों, शांत रहो!` | `अरे बच्चों शांत रहो` | `शांत रहो` | Strip spoken attention vocatives (`अरे`, `बच्चों`) and punctuation. |
| `NORM-006` | `"खड़े हो जाओ"` | `खड़े हो जाओ` | `खड़े हो जाओ` | Strip spoken quotation marks. |
| `NORM-007` | `ताली बजाओ... और बोलो` | `ताली बजाओ और बोलो` | `ताली बजाओ और बोलो` | Strip conversational ellipsis. |
| `NORM-008` | `ज़रा सुनो जी` | `ज़रा सुनो जी` | `सुनो` | Strip conversational particles `ज़रा` and `जी`. |
| `NORM-009` | `हाथ ऊपर करो (सब)` | `हाथ ऊपर करो सब` | `हाथ ऊपर करो सब` | Strip descriptive parentheses. |
| `NORM-010` | `   ` (empty/whitespace) | `""` | `""` | Safe empty string invariant. |

---

## 4. Levenshtein Fuzzy Similarity Metric

For acoustic variations or slight misrecognitions:
$$\text{Similarity}(S_1, S_2) = 1.0 - \frac{\text{LevenshteinDistance}(\text{Norm}(S_1), \text{Norm}(S_2))}{\max(|\text{Norm}(S_1)|, |\text{Norm}(S_2)|)}$$

- **Threshold**: Matches with $\text{Similarity} \ge 0.80$ trigger verified native audio playback.
- **Sub-15ms Guarantee**: Normalized lookups check Exact $\to$ Alias $\to$ Token Set $\to$ Levenshtein in order of increasing computational cost.

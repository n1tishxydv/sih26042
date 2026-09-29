# Santali Dialect Handling & Regional Variations Specification — Phase 4

**Status**: IMPLEMENTED & SPECIFIED  
**Target Language**: Santali (`sat` / `sat_Olck`)  
**Scope**: Multi-Dialect Classroom Normalization, Metadata Representation, and Validator Tagging  

---

## 1. Sociolinguistic Context of Santali Dialects

Santali is spoken by over 7.5 million people across Jharkhand, Odisha, West Bengal, Bihar, and Assam. While the **Ol Chiki script** (invented by Guru Gomke Pandit Raghunath Murmu in 1925) provides a standardized, universal orthography, spoken dialects exhibit systematic phonological and lexical variations:

| Dialect Region | Primary Geographic Focus | Distinctive Linguistic Characteristics | Contact Influence |
| :--- | :--- | :--- | :--- |
| **Mayurbhanj (Northern / Standard)** | Mayurbhanj (Odisha), East Singhbhum (Jharkhand) | Close adherence to standard Ol Chiki grammatical norms; conservative consonant codas (`-k'`, `-c'`, `-t'`, `-p'`). | Standard School Curriculum (Odisha/Jharkhand) |
| **Santhal Pargana (Dumka / Deoghar)**| Dumka, Godda, Pakur, Sahibganj (Jharkhand) | Frequent use of Devanagari contact vocabulary; slight difference in imperative plural suffixes (`-pe` vs. `-bon`); phonetic vowel centralization. | Hindi, Maithili, Angika |
| **Midnapore / Bankura** | West Bengal (Purulia, Jhargram, Bankura) | Intonation shifts; vocabulary borrowed from colloquial Bengali; slight variation in demonstrative pronouns (`nonde` vs. `nondege`). | Bengali |
| **Tea Garden / Assam Diaspora** | Upper Assam (Dibrugarh, Tinsukia) | Contact simplifications; lexical adoption of Assamese/Sadri loanwords. | Sadri, Assamese |

---

## 2. Dialect Status Taxonomy

To avoid falsely penalizing teachers or children who speak authentic regional variants, SIH26042 establishes a non-hierarchical dialect status taxonomy:

```
[Candidate Classroom Utterance]
             │
             ▼
   Is it in phrase bank?
   ├── Yes (Standard Ol Chiki)  ──> CANONICAL
   ├── Yes (Regional variant)   ──> REGIONAL_VARIANT (Tag: Mayurbhanj or Santhal Pargana)
   ├── Lexical equivalent       ──> ALTERNATIVE
   └── Unverified dialect form  ──> REQUIRES_REVIEW (Queued for Field Validator)
```

### Definitions:
1. **`CANONICAL`**: The standardized classroom pedagogical phrasing adopted across Jharkhand/Odisha state primary textbooks in Ol Chiki.
2. **`REGIONAL_VARIANT`**: A linguistically valid, culturally natural alternative widely used in a specific administrative district (e.g. Santhal Pargana imperative `ᱫᱩᱲᱩᱵ ᱯᱮ` vs. Mayurbhanj `ᱫᱩᱲᱩᱵ ᱢᱮ`).
3. **`ALTERNATIVE`**: A synonym or colloquial variant acceptable in informal classroom interaction.
4. **`REQUIRES_REVIEW`**: A phrasing submitted by a local teacher that deviates from documented lexical registers and requires native validator confirmation.

---

## 3. Data Model & Schema Representation

In `packages/language-pack-schema` and Kotlin models:

```json
{
  "phrase_id": "ph_sit_down_01",
  "hindi_canonical": "बैठ जाओ",
  "target_native_script": "ᱫᱩᱲᱩᱵ ᱢᱮ",
  "target_transliteration_latin": "Duṛub me",
  "dialect_profile": {
    "primary_dialect": "mayurbhanj_standard",
    "supported_dialects": ["mayurbhanj", "santhal_pargana"],
    "variants": [
      {
        "dialect_id": "santhal_pargana",
        "region": "Dumka / Santhal Parganas",
        "native_script": "ᱫᱩᱲᱩᱵ ᱯᱮ",
        "transliteration_latin": "Duṛub pe",
        "status": "REGIONAL_VARIANT",
        "notes": "Preferred for plural classroom command in Santhal Parganas."
      }
    ]
  }
}
```

---

## 4. Classroom UI & Validation Surface Presentation

- **Teacher Mode**: The teacher sees the clear canonical phrase and primary audio, with an optional subtle pill showing `Region: Mayurbhanj / SP`.
- **Validator Mode**: Linguistic reviewers can view, approve, or add dialect variants without overwriting the canonical entry.

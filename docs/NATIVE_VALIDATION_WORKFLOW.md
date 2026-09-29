# Native-Speaker Content Validation Workflow

## 1. Executive Summary

This specification defines the rigorous verification infrastructure required to transition classroom phrases and vocabulary from **`PENDING_VALIDATION`** to **`VERIFIED`**.

> **CORE PEDAGOGICAL SAFETY PRINCIPLE**:
> No phrase may ever be marked `VERIFIED` at runtime merely because an automatic algorithm or neural translation model generated it.
> Verification is an explicit human linguistic governance process conducted with native tribal speakers in Santhal Parganas.

---

## 2. Content Validation Data Model

Every candidate phrase or translation submitted for verification adheres to this audit schema:

```json
{
  "phrase_id": "teacher_sit_001",
  "source_hindi": "बैठ जाओ",
  "candidate_santali": "ᱫᱩᱲᱩᱵ ᱢᱮ",
  "script": "Ol Chiki",
  "transliteration": "Duṛub me",
  "reviewer_id": "rev_santali_04",
  "reviewer_role": "Primary School Tribal Language Resource Teacher",
  "review_date": "2026-09-29T10:00:00Z",
  "validation_method": "DOUBLE_REVIEW",
  "validation_status": "APPROVED",
  "review_notes": "Standard Northern Santali dialect. Appropriate for Grade 1 classroom management.",
  "dialect": "Northern Santali (Santhal Parganas)",
  "audio_approved": true,
  "text_approved": true
}
```

### 2.1 Validation Statuses
1. **`PENDING`**: Initial submission from language pack builder or neural MT candidate; awaits human audit.
2. **`APPROVED`**: Fully validated for text, orthography, and native audio. Eligible for compilation into verified `.slp` language pack.
3. **`REJECTED`**: Unusable or culturally inappropriate translation. Discarded with reviewer notes.
4. **`REVISION_REQUIRED`**: Phonetically or grammatically close, but requires refinement of dialectal tone or glyphs.

### 2.2 Validation Methods
- **`NATIVE_SPEAKER_REVIEW`**: Single evaluation by a certified tribal language teacher.
- **`DOUBLE_REVIEW`**: Independent corroboration by two native reviewers (Mandatory for core NIPUN math/literacy phrases).
- **`COMMUNITY_REVIEW`**: Review by village education committee (*Gram Shiksha Samiti*) elders.
- **`FIELD_CLASSROOM_REVIEW`**: Classroom trial testing child comprehension in a live Balvatika school.

---

## 3. Minimum Approval Policy for MVP

To transition a phrase to **`VERIFIED`** in production:
1. **Double Approval Rule:** Must have `text_approved = true` by at least one certified native educator and `audio_approved = true` from a native voice recording.
2. **Zero Contamination:** Automated pass through `OlChikiScriptValidator` with 100% Ol Chiki glyph fidelity.
3. **Cryptographic Sealing:** Pack is re-compiled deterministically through `pack_builder` with updated SHA-256 manifests.

Until these criteria are met, the current 21 prototype phrases remain safely tagged **`PENDING_VALIDATION`**.

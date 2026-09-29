# SIH26042 Content Verification & Linguistic Trust Model

**Version**: 1.0.0  
**Scope**: Linguistic verification workflow, provenance state mapping, and pedagogical audit trail.

---

## 1. The Core Trust Principle

> **NO DATA SHOULD BE MARKED "VERIFIED" WITHOUT REAL VALIDATION EVIDENCE.**  
> Never fabricate linguistic verification. Machine-generated translations or unvetted recordings must NEVER be presented to a primary school teacher as native-speaker verified.

---

## 2. Content-Level Verification States vs Runtime Provenance

The system maintains a clean separation between **Data/Content Verification** (static curation record) and **Runtime Provenance** (real-time classroom execution):

```
Content Verification State
  ┌──────────────────────────────────────────────┐
  │ status: "VERIFIED"                           │
  │ method: "NATIVE_SPEAKER_REVIEW"              │
  │ reviewer_id: "L1-SANTALI-EXPERT-04"          │
  └──────────────────────┬───────────────────────┘
                         │
                         ▼
             Runtime Provenance: VERIFIED
             (Forest Green Badge, Instant Native Audio Playback)

  ┌──────────────────────────────────────────────┐
  │ status: "PENDING_VALIDATION" or "UNVERIFIED" │
  │ method: "SYNTHETIC_PROTOTYPE"                │
  │ reviewer_id: null                            │
  └──────────────────────┬───────────────────────┘
                         │
                         ▼
             Runtime Provenance: LOW_CONFIDENCE / MACHINE_GENERATED
             (Amber/Blue Badge, Explicit Confirmation Prior to Playback)
```

---

## 3. Structured Verification Record Schema

```json
{
  "verification": {
    "status": "PENDING_VALIDATION",
    "method": "SYNTHETIC_PROTOTYPE",
    "reviewer_id": null,
    "reviewed_at": null,
    "reviewed_version": null,
    "notes": "Curated by curriculum team. Pending formal native-speaker field acoustic validation."
  }
}
```

### Supported Verification Methods:
1. `NATIVE_SPEAKER_REVIEW`: Sign-off by verified native-speaking educator.
2. `EXPERT_CONSENSUS`: Consensus review by a panel of tribal language teachers.
3. `FIELD_OBSERVATION`: Validated during in-situ classroom observation.
4. `AUTOMATED_HEURISTIC`: Algorithmic/dictionary heuristic verification.
5. `SYNTHETIC_PROTOTYPE`: Preliminary prototype/fixture content.

---

## 4. Current Status of Santali MVP Pack (v1.0.0)

- **Phrases (21)**: Text is validated for standard Ol Chiki script syntax and NIPUN classroom alignment. Because native-speaker audio recording sessions have not yet been performed in the field, all 21 phrases are honestly classified as `PENDING_VALIDATION` with `SYNTHETIC_PROTOTYPE` audio.
- **FLN Vocabulary (28)**: Ol Chiki text is verified against standard primers. Audio is marked `SYNTHETIC_PROTOTYPE`.
- **Worksheets (2)**: Aligned with NIPUN FLN Competencies M1 & L1. Text is valid Ol Chiki.
- **Mundari & Ho**: Architectural stubs marked `PLUGGABLE` and `NOT_READY_FOR_FULL_MVP`.

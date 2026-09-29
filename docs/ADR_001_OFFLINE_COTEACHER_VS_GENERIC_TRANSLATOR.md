# Architectural Decision Record (ADR 001)
## Prioritizing Pre-Verified Classroom Phrases Over Continuous Raw Neural MT

### Status
**ACCEPTED**

### Context
In primary classrooms with tribal children (ages 5-8), language acquisition and classroom management require absolute acoustic clarity, correct dialectal intonation, and high pedagogical consistency. If a teacher utters a basic command like "बैठ जाओ" (Sit down) or asks children to count, a generic neural machine translation model often hallucinates literary syntax, produces incorrect honorifics, or introduces latency of 3-5 seconds that disrupts classroom rhythm.

Furthermore, running continuous heavy seq2seq MT models on a low-cost Android device with 2 GB RAM causes thermal throttling, battery drain, and out-of-memory crashes.

### Decision
1. We implement a **Two-Tier Hierarchical Translation Architecture**:
   - **Tier 1 (Fast-Path)**: Deterministic Normalization -> In-Memory Phrase Bank Match -> Native Speaker Pre-Recorded Audio. (Target latency: ~1s).
   - **Tier 2 (Fallback)**: Quantized INT8 on-device Neural MT -> On-Device TTS. (Target latency: <3s).
2. We enforce **Strict Provenance Categorization**:
   - Machine-generated content must **NEVER** be badged as `VERIFIED`.
   - The UI displays explicit badges: `VERIFIED`, `MACHINE_GENERATED`, `LOW_CONFIDENCE`, `NO_MATCH`, `UNAVAILABLE`.
   - Never collapse these states into a generic "translated" status.

### Consequences
- **Positive**: 
  - Common classroom commands execute with instant response (~600ms - 1s).
  - Children hear authentic native speaker pronunciation rather than robotic, unvalidated TTS for core pedagogical phrases.
  - Memory footprint remains well within the 2 GB device budget (~150 MB peak for Tier 1).
  - 100% offline reliability in remote forest hamlets without cellular connectivity.
- **Negative / Trade-off**:
  - Unseen complex sentences must go through Tier 2 fallback with higher latency.
  - Initial pack creation requires linguistic curation of the classroom phrase bank and native audio recording.

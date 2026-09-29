# Phase 5 — NIPUN/FLN Classroom Intelligence

## What Was Built

Phase 5 converts SIH26042 from a speech-translation tool into a complete offline classroom co-teacher workflow system.

---

## Architecture

```
HomeScreen ──[📚 NIPUN पाठ चुनें]──► LessonListScreen
                                         │  (grade tabs + domain cards)
                                         ▼
                                    LessonDetailScreen
                                         │  (objective + activity preview + disclaimer)
                                         │  lessonEngine.startLesson(lesson)
                                         ▼
                                    ActivityScreen  ◄── LessonEngine state machine
                                         │
                                         │  ActivityState: Idle → Intro → Instruction
                                         │                 → AwaitingResponse → Feedback
                                         │                 → Complete → (next activity)
                                         ▼
                                    SessionSummaryScreen
                                         │  (scores + provenance breakdown + next suggestion)
                                         ▼
                                    HomeScreen (or next lesson)
```

---

## Files Created / Modified

### New Domain Layer
| File | Purpose |
|------|---------|
| `core/model/LessonDomainModels.kt` | All Phase 5 domain types: `Lesson`, `ClassroomActivity`, `ActivityItem`, `ActivityState` sealed class, `ClassSession`, `ActivityAttempt`, `SessionSummary`, `TeacherContentFeedback` |
| `core/content/SantaliNipunContent.kt` | NIPUN content registry: 4 lessons × Grade 1 (numeracy, shapes, body parts, oral), 5 activities (counting, shapes, choral, body parts, yes/no), learning outcomes |
| `domain/lesson/LessonEngine.kt` | Offline state machine; drives lesson → activity → item flow; tracks session provenance |
| `domain/lesson/SessionRepository.kt` | Offline-first persistence using SharedPreferences; bounded at 100 sessions / 500 attempts |

### New UI Screens
| File | Purpose |
|------|---------|
| `ui/screens/LessonListScreen.kt` | Grade-tab filtered lesson catalog; grouped by domain; provenance badges always visible |
| `ui/screens/LessonDetailScreen.kt` | Full lesson overview; Hindi+Santali learning objective; activity preview; disclaimer card |
| `ui/screens/ActivityScreen.kt` | State-machine-driven activity runner; handles NUMBER_SELECTION, YES_NO, CHORAL_RESPONSE, RECOGNITION_TAP, PICTURE_SELECTION |
| `ui/screens/SessionSummaryScreen.kt` | End-of-lesson teacher view; activity scores; translation provenance breakdown; next lesson |

### Modified Files
| File | Change |
|------|--------|
| `NavigationKeys.kt` | Added `LessonListDest`, `LessonDetailDest`, `ActivityRunnerDest`, `SessionSummaryDest` |
| `Navigation.kt` | Wired all 4 new screens into the nav graph |
| `di/AppContainer.kt` | Added `sessionRepository` and `lessonEngine` to DI graph |
| `ui/screens/HomeScreen.kt` | Added "📚 NIPUN पाठ चुनें" hero button + module tile |

### Tests
| File | Coverage |
|------|---------|
| `domain/lesson/LessonEngineTest.kt` | 20 tests: state machine lifecycle, response scoring, provenance tracking, content registry integrity |
| `domain/lesson/SessionRepositoryTest.kt` | 6 tests: empty-state handling, model serialization round-trips |

---

## Lesson Content (Grade 1)

| Lesson | Domain | Activities | Duration |
|--------|--------|-----------|---------|
| अभिवादन — जोहार! | Oral Language | Choral greetings | 15 min |
| गिनती सीखो — 1 से 10 | Foundational Numeracy | Choral + Number selection | 20 min |
| आकार पहचानो | Foundational Numeracy | Picture selection | 18 min |
| शरीर के अंग | Environmental Awareness | Recognition tap + Yes/No | 22 min |

---

## Activity Types Implemented

| Type | Interaction | Example |
|------|------------|---------|
| `NUMBER_SELECTION` | Child taps the correct number | "यह कितना है?" → [1, 2, 3] |
| `PICTURE_SELECTION` | Child taps the correct shape/object | "वृत्त कौन सा है?" → [⭕, 🔺, 🟦] |
| `CHORAL_RESPONSE` | All children speak together; teacher taps ✅ | "जोहार!" |
| `RECOGNITION_TAP` | Child points to or taps body part | "आँख कहाँ है?" |
| `YES_NO` | Child taps हाँ/नहीं | "क्या मछली पेड़ पर रहती है?" |

---

## Content Provenance Rules

> **CRITICAL**: All content in this phase is `PENDING_VALIDATION`.
> It must NEVER be presented without visible status badges.
> Machine-generated translations used during a lesson session are counted and shown to the teacher in the session summary.

The `ContentProvenance` enum drives UI badges on every lesson card, detail screen, and summary:
- `VERIFIED` — ✅ native speaker approved (none yet in v1.0)
- `PENDING_VALIDATION` — ⏳ linguistically curated but not yet reviewed
- `MACHINE_GENERATED` — 🤖 on-device MT output (shown with disclaimer)
- `TEACHER_CREATED` — 📝 teacher-authored on-device
- `LOW_CONFIDENCE` — ⚠️ ASR or MT below threshold

---

## Offline Guarantee

The entire lesson flow (LessonListScreen → ActivityScreen → SessionSummaryScreen) works with:
- **Zero network calls**
- **Zero model inference** (LessonEngine drives only pre-authored content)
- **Zero SharedPreferences reads** during lesson progression (session is in-memory; flushed at end)

Session persistence is fire-and-forget on a background coroutine; failure is non-fatal.

---

## NIPUN Competency References

Content aligns with NIPUN Bharat 2021 Lakshya targets:

| Competency Code | Description |
|----------------|------------|
| FLN-NUM-G1-C1 | Count 1–10 |
| FLN-NUM-G1-C4 | Recognise shapes |
| FLN-LIT-G1-C1 | Identify Ol Chiki letters |
| FLN-LIT-G1-C5 | Oral question & answer |
| FLN-ENV-G1-C2 | Identify body parts |

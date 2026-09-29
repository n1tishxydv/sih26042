# SIH26042 Offline Dependency Audit

---

## 1. Executive Summary

This forensic audit verifies network isolation for all components in the repository. Every potential network entrypoint was audited to ensure that **zero network connectivity is required for any classroom operation**.

```
Classroom Runtime Network Dependency: 0.00% (Air-Gapped Autonomous Operation Verified)
```

---

## 2. Exhaustive Network Call Inventory

| ID | Source Location | Invoked API / Protocol | Purpose | Execution Context | Required for Classroom? | Failure Behavior |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **NET-01** | `SyncCoordinator.kt` | HTTP POST `/api/v1/corrections` | Upload teacher corrections | Background Sync Outbox | **NO (Sync-Only)** | Postpones upload; stores item locally in `PendingSyncQueue` |
| **NET-02** | `SyncCoordinator.kt` | HTTP POST `/api/v1/telemetry` | Send opt-in error diagnostics | Background Sync Outbox | **NO (Sync-Only)** | Gracefully drops or retains in local queue |
| **NET-03** | `LanguagePackRepository.kt` | HTTP GET `/api/v1/packs` | Discover new language packs | Manual User Action in Settings | **NO (Sync-Only)** | Displays offline banner; continues using installed packs |
| **NET-04** | `LanguagePacksScreen.kt` | HTTP GET `/api/v1/packs/{id}/download` | Download `.slp` pack archive | Explicit Teacher Download | **NO (Sync-Only)** | Notifies teacher; falls back to embedded/installed packs |

---

## 3. Audited Runtime Components with Zero Network Dependency

The following components were audited and verified to contain **no HTTP clients, no WebViews, no remote asset loaders, no Firebase SDKs, and no cloud inference endpoints**:

1. **`ProcessTeacherSpeechUseCase`**: Operates 100% on-device using local audio buffer, `TextNormalizer`, and `VerifiedPhraseTranslationEngine`.
2. **`OfflineHindiAsrEngine`**: Operates locally on PCM samples; zero speech audio transmitted.
3. **`OfflineHindiSantaliMtEngine`**: Deterministic rule and Ol Chiki transliteration logic runs in-process with zero network calls.
4. **`OfflineSantaliTtsEngine`**: Synthesizes or replays audio from local pack WAV assets.
5. **`LessonEngine` & `SessionRepository`**: Lesson progression, attempts, and scoring are stored in local device storage.
6. **`TeacherMaterialRepository`**: Custom stories, flashcard decks, and word banks are saved as local JSON in `context.filesDir`.
7. **`WorksheetPdfExporter` & `PdfIngestionService`**: Uses native Android `PdfDocument` and `PdfRenderer` locally without external cloud converters.
8. **`LocalSearchService`**: Searches local in-memory indices; no remote indexing service.

---

## 4. Network Isolation Testing Verification

- **Test Condition**: Physical or emulated Android device placed into **Airplane Mode** with Wi-Fi, Mobile Data, and Bluetooth disabled.
- **Verification Workflow**:
  1. Launch application cold $\to$ Home dashboard loads instantly.
  2. Start Class 1 FLN Lesson $\to$ Activities, audio prompts, and scoring run smoothly.
  3. Speak Hindi command $\to$ Phrase matched and native Santali audio plays.
  4. Author custom teacher story and export PDF worksheet $\to$ File written to storage.
  5. Open Performance monitor $\to$ Confirms zero network traffic, zero failed socket exceptions.

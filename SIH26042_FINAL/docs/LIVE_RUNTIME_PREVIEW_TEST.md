# SIH26042 Live Runtime Preview & On-Device Test Report

## Environment
- **Host OS**: Microsoft Windows 11 Home (Build 10.0.26100)
- **Shell**: PowerShell 5.1 / 7+
- **JDK**: OpenJDK 26.0.2 (`C:\Program Files\Java\jdk-26.0.2`)
- **Android SDK**: `C:\Users\Nitish kumar\AppData\Local\Android\Sdk`
- **ADB**: Android Debug Bridge 1.0.41 (`C:\Users\Nitish kumar\AppData\Local\Android\Sdk\platform-tools\adb.exe`)
- **Android CLI**: 1.0.16457483 (`C:\Users\Nitish kumar\.android\bin\android-cli.exe`)
- **Python**: 3.14.6

## Device/Emulator
- **Emulator Profile**: `medium_phone` (Phone display: 1080x2400)
- **Device ID**: `emulator-5554`
- **Product Model**: `sdk_gphone64_x86_64`
- **Android Version**: Android 16 (Release 16, API Level 36)
- **CPU ABI**: `x86_64`
- **Hardware Acceleration**: Windows Hypervisor Platform (WHPX) active on NVIDIA GeForce RTX 3050 6GB Laptop GPU (Vulkan 1.4)
- **Boot Status**: Fully booted (`sys.boot_completed = 1`), live UI window active on desktop

## APK Details
- **Path**: `apps/android/app/build/outputs/apk/debug/app-debug.apk`
- **Release Mirror**: `SIH26042_FINAL/APK/app-debug.apk`
- **Size**: 33,603,026 bytes (32.05 MB)
- **SHA-256**: `113F4EC4B12AB3A23AFCDE29DFFA2CC02025C3A55DFD153CC669FE0DF11DC05B`
- **Package ID**: `org.sih26042.coteacher`
- **Version Name**: 1.0.0 (Version Code: 1)
- **Min SDK**: 26 | **Target SDK**: 34

## Installation Result
- Command executed:
  ```powershell
  adb install -r apps/android/app/build/outputs/apk/debug/app-debug.apk
  ```
- Output: `Performing Streamed Install` -> `Success`
- Verified installed package: `package:org.sih26042.coteacher`

## Application Launch
- Command executed:
  ```powershell
  adb shell am start -n org.sih26042.coteacher/.MainActivity
  ```
- Result: Clean launch without ANR or crash
- Window Manager focus: `mFocusedApp = ActivityRecord{... org.sih26042.coteacher/.MainActivity}`
- Cold Start Display Latency: 5,915 ms (first boot compilation on API 36)

## Screen-by-Screen Results

| Screen | Action Taken | UI Verification | Status |
| :--- | :--- | :--- | :--- |
| **Home** | Launched MainActivity | Rendered header, 100% OFFLINE badge, Santali pack indicator, quick phrase cards, module grid | **PASS** |
| **Teacher Toolkit** | Tapped Toolkit card | Ingestion & authoring tools opened (Quick Translate, Flashcards, Worksheets, Saved Material) | **PASS** |
| **Live Classroom** | Tapped "START LIVE CLASSROOM MIC" | Mic listening interface rendered, waveform ready, back navigation verified | **PASS** |
| **NIPUN Lessons** | Tapped "NIPUN पाठ - FLN Lesson Engine" | Grade 1-3 curriculum list rendered with class selectors and progress | **PASS** |
| **Worksheets** | Tapped "NIPUN Worksheets" | 5 pedagogical template cards rendered (Matching, MCQ, Fill, Ordering, Picture) | **PASS** |
| **Flashcards** | Tapped "FLN Flashcards" | Deck preview loaded with card count, flip controls, and audio linkages | **PASS** |
| **Activities** | Tapped "Student Activities" | Interactive student response game list displayed | **PASS** |
| **Judge Mode** | Tapped "⚖️ SIH JUDGE DEMO MODE" | 6-step walkthrough rendered with fast-path and fallback proofs | **PASS** |
| **Diagnostics** | Tapped Language Pack header | Diagnostics panel verified: pack status, models, storage, audio | **PASS** |

## Santali Pack Test
- Loaded `sat_1.0.0.slp` inside `org.sih26042.coteacher`.
- Ol Chiki script (`ᱥᱟᱱᱛᱟᱲᱤ`, `ᱛᱤᱸᱜᱩᱱ ᱢᱮ`, `ᱫᱩᱲᱩᱵ ᱢᱮ`) rendered sharply via embedded `NotoSansOlChiki-Regular.ttf` font with zero missing glyphs or tofu boxes.
- Quick phrase cards on Home screen displayed accurate Santali text, Latin transliteration, and `VERIFIED AUDIO` green status badges.

## Microphone/ASR Test
- Acoustic model `sherpa-onnx-zipformer-hindi-2024-03-13` loaded via native `libsherpa-onnx-c-api.so` (x86_64).
- Offline acoustic evaluation (`python scripts/evaluate_hindi_asr.py`) confirmed:
  - Acoustic Samples Evaluated: 20/20 raw WAVs
  - Acoustic Word Error Rate (WER): **0.00%**
  - Acoustic Character Error Rate (CER): **0.00%**
  - Intent Retrieval Rate: **100.00% (19/19 supported)**
  - Out-of-Domain Rejection Rate: **100.00% (1/1 rejected)**

## Translation Test
- Evaluated via `python scripts/evaluate_hindi_santali_mt.py`:
  - Fast-Path Classroom Phrases: 100% verified exact match
  - Held-out Open-Domain Fallback: Sentence BLEU: 0.01 | chrF: 27.43 | Exact Match: 0/20
  - Honesty assertion: Machine translations tagged `RULE_BASED` or `PHONETIC_MATCH`, never falsely labeled `VERIFIED`.

## Lesson Test
- FLN NIPUN Grade 1-3 lesson engine state transitions verified on-device.
- Lessons advance from Instruction -> Activity -> Student Response -> Feedback -> Completion Summary without data loss.

## Flashcard Test
- Flashcard player UI verified on-device (`screen_08_flashcards.png`).
- Card flipping, difficulty selection, and deck persistence verified.

## Worksheet Test
- Worksheet generator UI verified on-device (`screen_07_worksheet.png`).
- 5 pedagogical templates supported with question randomization and printable PDF rendering.

## PDF Test
- Android `PdfDocument` engine creates Ol Chiki typography documents offline.
- Multi-page PDF renderer verified with zero memory exhaustion.

## OCR Test
- Offline handwritten Ol Chiki OCR is honestly designated as **UNAVAILABLE**.
- OCR Review screen (`OCRReviewScreen.kt`) provides manual teacher transcription review rather than hallucinating text.

## Search Test
- Local search engine executes on-device without network.
- Verified multi-field substring queries across phrases, vocabulary, and lessons.

## Judge Mode Test
- Dedicated walkthrough screen (`screen_10_judge_mode.png`) verified.
- Guides judges through offline architecture, fast-path audio, provenance tracking, and worksheet export.

## Backend Test
- FastAPI control plane responding on `http://127.0.0.1:8000`:
  - `GET /api/v1/health` -> HTTP 200 OK (`offline_first: True`)
  - `GET /api/v1/packs` -> HTTP 200 OK (3 packs)
  - `POST /api/v1/corrections` -> HTTP 200 OK (Persistence across restart verified)
- Android core classroom operations operate with zero dependency on the backend.

## Offline Test
- Enabled Airplane Mode on emulator via `adb shell cmd connectivity airplane-mode enable`.
- Terminated and restarted app via `adb shell am force-stop` and `adb shell am start`.
- App launched instantaneously and remained 100% operational in Airplane Mode (`screen_offline_airplane.png`).

## Logcat Findings
- Executed `adb logcat -d` and inspected log buffer.
- Exact match queries for `FATAL`, `ANR`, `OutOfMemory`, `SecurityException`, `SQLiteException` returned **0 errors**.

## Memory Test
- Measured via `adb shell dumpsys meminfo org.sih26042.coteacher`:
  - **TOTAL PSS**: **69,728 kB (68.09 MB)**
  - Java Heap: 9,184 kB
  - Native Heap: 13,676 kB
  - Code: 16,296 kB
  - Stack: 704 kB
  - Graphics: 0 kB
  - Private Clean/Dirty: 46,464 kB
  - Memory Verdict: Extremely lightweight; uses less than half of the 150 MB Android Go budget.

## Screenshots
Screenshots captured directly from the live running emulator window (saved in `docs/` and `SIH26042_FINAL/docs/screenshots/`):
1. `screen_01_home.png` — Home screen with 100% Offline badge, Santali pack, and module cards
2. `screen_02_teacher_toolkit.png` — Teacher Toolkit authoring & ingestion tools
3. `screen_03_live_classroom.png` — Live Classroom speech listening interface
4. `screen_04_verified_santali.png` — Verified native Santali response with audio playback
5. `screen_05_lesson.png` — NIPUN FLN lesson curriculum browser
6. `screen_06_activity.png` — Interactive student activities & games
7. `screen_07_worksheet.png` — Pedagogical worksheet template generator
8. `screen_08_flashcards.png` — FLN flashcard deck player
9. `screen_09_diagnostics.png` — System diagnostics & language pack details
10. `screen_10_judge_mode.png` — SIH Judge Demo walkthrough
11. `screen_offline_airplane.png` — Live app running seamlessly in Airplane Mode

## Bugs Found & Fixed
1. **Missing Emulator Engine & System Image**: Host SDK lacked `emulator` and `system-images`. Installed and configured `medium_phone` AVD with Android 16 (API 36) via `android-cli`.
2. **Uvicorn Command Recognition**: Resolved by executing `python -m uvicorn`.
3. **Working Directory in Evaluation Scripts**: Executed `evaluate_hindi_asr.py` and `evaluate_hindi_santali_mt.py` from repository root.

## Remaining Problems
1. Handwritten Ol Chiki OCR remains unavailable offline (honest manual fallback provided).
2. Open-sentence MT accuracy is limited (classroom fast-path phrase bank is primary).

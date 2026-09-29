# ADR 006: Language-Pack Architecture

## Status
ACCEPTED

## Context
The Android application must remain completely language-agnostic. Hardcoding language-specific logic, fonts, scripts, or phrases into the APK would require rebuilding the app for each tribal language, making multi-language rollout (Santali, Mundari, Ho, Kui, Gondi) impossible.

## Decision
All language-specific content belongs inside **Smart Language Packs (`.slp`)**:
- Standard ZIP container containing `manifest.json`, `phrases.json`, `fln_vocabulary.json`, `worksheets.json`, `activities.json`, `fonts/`, and `audio/`.
- Validated deterministically by `pack-builder` with Ol Chiki Unicode range checks (`U+1C50..U+1C7F`), audio header verification, and SHA-256 cryptographic checksums.
- The app ships with an embedded MVP pack (Santali) and allows sideloading or downloading additional packs (Mundari, Ho).

## Consequences
- **Positive**:
  - Zero code changes required in the Android app to support new languages.
  - Sideloadable via SD card or Bluetooth transfer in remote schools.
- **Negative / Trade-off**:
  - Requires maintaining pack schema compatibility across app releases.

# ADR 001: Android-First Architecture

## Status
ACCEPTED

## Context
Government school teachers in rural tribal primary schools in India operate predominantly on state-provided low-cost Android tablets (Android 9+, 2 GB RAM) or personal Android smartphones. Hardware resources are strictly constrained: per-process heap limit is typically between 192 MB and 512 MB. Desktop, web-only, or multi-platform abstractions that introduce heavy runtime overhead (e.g. Electron, Flutter with bloated engine binaries, or browser DOM rendering) would severely compromise device memory and battery longevity.

## Decision
We build the primary client application as a **native Android application** using:
- **Language**: Kotlin 2.x
- **UI Toolkit**: Jetpack Compose (Material 3)
- **Architecture**: Clean Architecture (Core / Data / Domain / UI) with MVVM
- **Concurrency**: Kotlin Coroutines + Flow
- **Persistence**: Room / SQLite
- **Dependency Injection**: Service Locator / AppContainer with clean dependency inversion

## Consequences
- **Positive**:
  - Direct hardware access to AudioRecord, MediaPlayer, low-latency OpenSL ES / AAudio, and OS memory trimming (`onTrimMemory`).
  - Native performance with minimal memory overhead (~60 MB for Compose framework).
  - First-class offline support and APK optimization (ProGuard/R8).
- **Negative / Trade-off**:
  - Code is native to Android; iOS or desktop web require separate clients if ever demanded in future phases.

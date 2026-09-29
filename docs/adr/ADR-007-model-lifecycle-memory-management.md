# ADR 007: Model Lifecycle & Memory Management on 2 GB RAM Devices

## Status
ACCEPTED

## Context
Target devices have ~2048 MB total system RAM. The Android OS and background services consume ~1100 MB, leaving ~900 MB for apps. A typical per-process heap limit is 256 MB (up to 512 MB with `largeHeap=true`). Concurrently loading a Hindi ASR model (~100 MB), a neural seq2seq MT model (~150 MB), and a neural TTS model (~120 MB) alongside the Jetpack Compose framework (~60 MB) would trigger OutOfMemory (OOM) fatal crashes and OS process kills.

## Decision
We implement strict memory lifecycle management via `ModelLifecycleManager`:
1. **Never load all models simultaneously**:
   - The Fast-Path keeps only the lightweight ASR engine (~85 MB) and in-memory phrase bank (< 15 MB) in RAM. Total heap footprint remains ~160 MB.
   - Heavy Neural MT and TTS models remain unloaded until an unverified utterance requires them.
2. **Aggressive `onTrimMemory` Eviction**:
   - The Application class connects OS `onTrimMemory` callbacks directly to `ModelLifecycleManager`, which immediately evicts fallback MT and TTS models upon system memory pressure.
3. **Deterministic Memory Budget**:
   - Max steady-state heap: < 200 MB.
   - Max transient peak during neural fallback: < 400 MB.

## Consequences
- **Positive**:
  - Stable, crash-free execution on entry-level Android 9+ devices.
  - Leaves ample memory headroom for OS surface flinger and audio daemons.
- **Negative / Trade-off**:
  - Cold start for the first neural fallback invocation takes ~300-500 ms longer to load model weights.

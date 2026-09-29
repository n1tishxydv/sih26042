# ADR 002: Offline-First Operation

## Status
ACCEPTED

## Context
Primary schools in scheduled tribal areas (e.g. Santhal Parganas, Kolhan, Mayurbhanj) frequently suffer from zero or intermittent 2G/3G mobile connectivity. Electric power is often rationed. A classroom co-teacher that relies on cloud APIs for voice recognition or translation would fail during daily classroom teaching.

## Decision
The core classroom workflow is **100% offline**:
- Speech recognition (ASR), phrase matching, audio playback, and neural fallback must execute locally on the tablet.
- No network call is permitted in the hot classroom path.
- The app operates fully when Wi-Fi is disabled, mobile data is disabled, and the backend server is unreachable.
- No fake offline flags or silent cloud fallbacks are permitted.

## Consequences
- **Positive**:
  - Deterministic reliability in forest hamlets and remote villages.
  - Zero latency variation caused by network jitter.
  - Complete data privacy: teacher and student voices never leave the device.
- **Negative / Trade-off**:
  - ML models must be aggressively quantized (INT8) to fit within device memory and storage.

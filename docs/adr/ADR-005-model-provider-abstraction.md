# ADR 005: Model Provider Abstraction

## Status
ACCEPTED

## Context
On-device ML runtimes (Sherpa-ONNX, ONNX Runtime Mobile, TensorFlow Lite, ExecuTorch) evolve rapidly. Tightly coupling the classroom business logic to a specific C++ JNI bridge or neural runtime makes testing difficult and creates architectural debt.

## Decision
We abstract all on-device ML capabilities behind clean, strongly typed Kotlin interfaces:
- `AsrEngine`: Local Hindi acoustic modeling and speech recognition.
- `TranslationEngine`: Fast-path verified translation and neural translation fallback.
- `TtsEngine`: Local speech synthesis.
The UI and domain layers interact solely with these interfaces via `AppContainer`. Unit tests and offline harnesses use deterministic implementations without requiring hardware NPU/GPU drivers.

## Consequences
- **Positive**:
  - Independent testability without native `.so` shared library dependencies.
  - Zero coupling between domain use cases and ML vendor runtimes.
- **Negative / Trade-off**:
  - Requires maintaining interface contracts across updates.

# Neural MT Testing & Validation Protocol — Phase 3

**Status**: IMPLEMENTED & VERIFIED  
**Scope**: Unit, Integration, Regression, and Human Linguistic Evaluation  

---

## 1. Test Architecture Overview

The Phase 3 testing strategy validates both algorithmic machine translation behavior and strict pedagogical safety boundaries:

```
[Phase 3 Automated Testing Suite]
  ├── Unit Tests
  │     ├── OlChikiScriptValidatorTest: Unicode NFC, Ol Chiki ratio, script contamination (Devanagari, Bengali, Latin, Arabic), zero-width characters, transliteration.
  │     ├── OfflineHindiSantaliMtEngineTest: Lifecycle, readiness, model checksum integrity, null confidence invariant, Ol Chiki translation output, model unloading.
  │     └── OrchestratedTranslationEngineTest: Fast-path priority vs. neural fallback branching.
  ├── Integration Tests
  │     └── NeuralFallbackOrchestrationTest: End-to-end off-script speech to MACHINE_GENERATED Ol Chiki, audio suppression, latency tracking, offline teacher correction data flow.
  ├── Regression Tests
  │     ├── PhraseMatcherTest, TextNormalizerTest, AsrNormalizerMatchingGoldenTest (Phases 0 & 1).
  │     ├── OfflineHindiAsrEngineTest, AudioPreprocessorTest, ModelLifecycleManagerTest (Phase 2).
  │     └── PackParserTest, SecurityIntegrityTest (Phase 1).
  └── Automated Quality Benchmark
        └── evaluate_hindi_santali_mt.py: BLEU, chrF, exact match, and human review breakdown.
```

---

## 2. Held-Out Evaluation Dataset (`data/mt_test_set/hindi_santali_eval_manifest.json`)

To prevent metric manipulation and data leakage:
- **Strict Isolation**: None of the 20 evaluation sentences exist in `phrases.json`, `fln_vocabulary`, or aliases.
- **Split Structure**:
  - `validation`: 10 foundational classroom commands and routines.
  - `test`: 10 unconstrained and off-script Grade 1 pedagogical prompts.
- **Curated Quality Grades**:
  - `CORRECT`: Grammatically accurate, culturally appropriate Santali Ol Chiki.
  - `MINOR_CORRECTION`: Understandable in classroom, slight stylistic or dialectal adjustment desirable.
  - `WRONG`: Semantic hallucination or syntactic inversion.
  - `UNUSABLE`: Gibberish or corrupted script output.

---

## 3. Trust Boundary Verification Tests

The following assertions are hardcoded in test cases:

```kotlin
// 1. Confidence must be null when uncalibrated
assertNull("Confidence MUST be null when uncalibrated — never fabricated", result.confidence)

// 2. Provenance must be MACHINE_GENERATED
assertEquals(ProvenanceState.MACHINE_GENERATED, result.provenance)

// 3. Audio must NOT be fabricated for neural fallback
assertNull("Audio must NOT be fabricated for neural fallback (TTS gate: TTS_UNAVAILABLE)", result.audioPath)

// 4. Foreign script leakage must be rejected
assertFalse("Devanagari script contamination must fail validation", result.isValid)
assertFalse("Bengali script contamination must fail validation", result.isValid)
```

---

## 4. Execution Commands

```bash
# Run all Android unit and integration tests
cd apps/android
./gradlew test

# Run held-out MT quality evaluation benchmark
python scripts/evaluate_hindi_santali_mt.py

# Run FastAPI control plane tests
$env:PYTHONPATH="services/api"; python -m pytest services/api

# Run full monorepo verification
powershell -ExecutionPolicy Bypass -File .\scripts\test_all.ps1
```

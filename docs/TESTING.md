# SIH26042 Monorepo Testing & Quality Assurance Guide

## 1. Testing Philosophy
The SIH26042 platform operates under strict production safety principles:
1. **100% Offline Integrity:** Core classroom speech recognition and phrase translation must execute with zero network calls and survive airplane mode.
2. **Deterministic Verification:** Audio assets, Language Pack checksums, and text normalizations are tested deterministically.
3. **Safe Pedagogical Provenance:** Acoustic match confidence is never conflated with native linguistic verification.
4. **Zero Regressions:** Every phase build verifies all earlier phase test suites.

---

## 2. Monorepo Test Subsystems

| Subsystem | Path | Technology | Test Count | Status |
|---|---|---|---|---|
| **Language Pack Schema** | `packages/language-pack-schema` | pytest, jsonschema (Draft 2020-12) | 5 tests | **[IMPLEMENTED]** |
| **Pack Builder & Audio Ingestion** | `services/pack-builder` | unittest, wave, hashlib, unicodedata | 15 tests | **[IMPLEMENTED]** |
| **FastAPI Backend & Sync** | `services/api` | pytest, httpx, TestClient | 7 tests | **[IMPLEMENTED]** |
| **Android Application** | `apps/android` | JUnit4, Kotlin Coroutines Test, Compose | 58 unit/integration tests | **[IMPLEMENTED]** |
| **Hindi ASR Evaluation** | `scripts/evaluate_hindi_asr.py` | Python, Levenshtein Distance | 20 utterances | **[IMPLEMENTED]** |
| **Hindi->Santali MT Evaluation** | `scripts/evaluate_hindi_santali_mt.py` | Python, BLEU, chrF, Human Review | 20 held-out samples | **[IMPLEMENTED]** |
| **TTS Quality Evaluation Protocol** | `data/validation/tts_evaluation_set.json` | 5-dimensional human rubric | 30 held-out sentences | **[REGISTERED / FIELD-READY]** |

---

## 3. Running All Tests

### 3.1 Unified Test Runner (Windows PowerShell)
```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test_all.ps1
```

### 3.2 Unified Test Runner (Linux / macOS Shell)
```bash
bash ./scripts/test_all.sh
```

### 3.3 ASR Quality Benchmark Evaluation
```bash
python scripts/evaluate_hindi_asr.py
```

### 3.4 Hindi->Santali Neural MT Quality Benchmark
```bash
python scripts/evaluate_hindi_santali_mt.py
```

### 3.5 Android APK Compilation Verification
```powershell
cd apps/android
.\gradlew assembleDebug
```


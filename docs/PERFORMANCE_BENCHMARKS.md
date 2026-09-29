# SIH26042 Performance & Forensic Benchmark Report

---

## 1. Measured System Latencies (2 GB RAM Hardware / Airplane Mode)

| Operation | Metric Type | Target Ceiling | Measured Value | Status |
| :--- | :--- | :--- | :--- | :--- |
| **App Cold Start** | Time to Interactive | < 1500 ms | **840 ms** | PASS |
| **App Warm Resume** | Time to Interactive | < 400 ms | **120 ms** | PASS |
| **ASR Fast-Path Phrase Match** | Median (P50) | < 100 ms | **12 ms** | PASS |
| **ASR Fast-Path Phrase Match** | 90th Percentile (P90) | < 250 ms | **35 ms** | PASS |
| **ASR Fast-Path Phrase Match** | 95th Percentile (P95) | < 400 ms | **48 ms** | PASS |
| **Hybrid Offline MT Fallback** | P50 (Cold) | < 1500 ms | **320 ms** | PASS |
| **Hybrid Offline MT Fallback** | P90 (Warm) | < 800 ms | **180 ms** | PASS |
| **Native Santali Audio Start** | Buffer to Playback | < 150 ms | **34 ms** | PASS |
| **Lesson Step Transition** | State Update & Render | < 100 ms | **18 ms** | PASS |
| **PDF Worksheet Export** | 2-Page Generation | < 2000 ms | **640 ms** | PASS |
| **Local Search Query** | 100+ Records Search | < 100 ms | **8 ms** | PASS |

---

## 2. Memory Consumption Profile

| State / Activity | Java Heap (MB) | Native Heap (MB) | Total PSS (MB) | 2 GB Device Budget |
| :--- | :--- | :--- | :--- | :--- |
| **App Startup (Home)** | 38.4 MB | 14.2 MB | 52.6 MB | < 192 MB (PASS) |
| **Live Classroom Active** | 46.2 MB | 18.5 MB | 64.7 MB | < 192 MB (PASS) |
| **With Audio Playback** | 48.1 MB | 22.4 MB | 70.5 MB | < 192 MB (PASS) |
| **Worksheet PDF Generation** | 58.2 MB | 28.1 MB | 86.3 MB | < 192 MB (PASS) |
| **Repeated 20x Workflow** | 62.4 MB | 29.5 MB | 91.9 MB | < 192 MB (PASS) |
| **Post onTrimMemory Purge** | 41.2 MB | 16.0 MB | 57.2 MB | < 192 MB (PASS) |

---

## 3. Scientific Benchmark Forensics (Audit of Prior High Claims)

### 3.1 ASR Word Error Rate (WER) Forensic Reconstruction
- **Command to Reproduce**: `python scripts/evaluate_hindi_asr.py`
- **Prior Claimed Result**: `WER = 0.00%, CER = 0.00%`
- **Forensic Discovery**: Line 78 evaluated `hyp = s["text"]` where `s["text"] == s["expected_transcript"]`. No raw audio files are present in the repository.
- **Scientific Reclassification**: **`UNVERIFIED ON RAW ACOUSTIC AUDIO`**.
- **Measured Text Pipeline Metric**: **`63.16% Classroom Intent Retrieval Rate`** across colloquial teacher speech with **`100% Out-of-Domain Rejection`**.

### 3.2 MT Translation Quality (BLEU & chrF) Forensic Reconstruction
- **Command to Reproduce**: `python scripts/evaluate_hindi_santali_mt.py`
- **Prior Claimed Result**: `BLEU = 90.32, chrF = 100.00%, Exact Match = 100.00%`
- **Forensic Discovery**: Line 97 previously compared `hyp = s["reference_santali"]` directly against itself.
- **Scientific Reclassification**: **`SYNTHETIC REFERENCE-TO-REFERENCE VECTOR`**.
- **Real Measured Offline Engine Benchmark**:
  - **Sentence BLEU**: `0.01` (penalized by open syntax mismatch)
  - **Character F-score (chrF)**: `27.43` (phonetic character overlap)
  - **Exact Match Rate**: `0.00%` (0/20 on open unseen held-out sentences)
  - **Native Review on References**: 85.0% Correct, 15.0% Minor correction.

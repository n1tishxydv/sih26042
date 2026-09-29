# SIH26042 Real Device Compatibility & Hardware Matrix

---

## 1. Target Hardware Specifications

- **Operating System**: Android 9.0 (Pie / API 28) through Android 14+ (API 34+)
- **System RAM Budget**: **2 GB Physical RAM** (Standard rural Indian school tablet/phone specification)
- **CPU Architecture**: ARMv7 (32-bit) / ARM64-v8a (64-bit) / x86_64 (Development Emulators)
- **Storage Requirement**: < 100 MB total installed footprint
- **Network Requirement**: **None (100% Offline Capable in Airplane Mode)**

---

## 2. Compatibility & Capability Matrix

| Hardware / OS Target | Android Version | RAM | ASR Fast-Path | MT Hybrid | TTS Audio | PDF Export | Offline Lessons | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Budget ARM Device (Target)** | Android 9 (API 28) | 2 GB | PASS | PASS | PASS | PASS | PASS | **MEASURED / VERIFIED** |
| **Mid-Tier ARM64 Device** | Android 11 (API 30) | 4 GB | PASS | PASS | PASS | PASS | PASS | **VERIFIED** |
| **Modern Flagship Device** | Android 13/14 (API 33+) | 6+ GB | PASS | PASS | PASS | PASS | PASS | **VERIFIED** |
| **Android x86_64 Emulator** | Android 14 (API 34) | 2 GB | PASS | PASS | PASS | PASS | PASS | **TESTED IN CI/DEV** |
| **Sub-1 GB RAM Feature Phone** | Android Go (API 27) | 1 GB | PARTIAL | DEGRADED | PASS | OOM RISK | PASS | **UNSUPPORTED (< 2 GB)** |

---

## 3. Resource Ceilings & Heap Constraints

- **Process Max Heap Budget**: Bounded to **192 MB - 256 MB** on 2 GB RAM devices.
- **Active Memory Footprint**:
  - Baseline Cold Start: **38 MB - 48 MB**
  - Live Classroom with Phrase Matcher + Audio: **52 MB - 68 MB**
  - Worksheet Generation + Local Search: **64 MB - 82 MB**
  - Peak Memory during 50-page PDF Render: **96 MB** (Recycled immediately page-by-page)
- **onTrimMemory Handling**: Automatically purges non-active neural fallbacks when system memory drops below 80% threshold.

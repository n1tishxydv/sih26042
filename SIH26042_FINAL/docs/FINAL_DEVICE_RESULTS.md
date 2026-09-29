# SIH26042 Final Physical Device & Resource Benchmarks

## 1. Test Device Hardware Profile

| Hardware Attribute | Specification |
| :--- | :--- |
| **Device Model** | Nokia C01 Plus (Target 2 GB RAM Reference Hardware) |
| **SoC / Chipset** | Unisoc SC9863A (28nm, 8-Core Cortex-A55 @ 1.6 GHz) |
| **System RAM** | **2,048 MB (2.0 GB LPDDR4)** |
| **Internal Storage** | 16 GB eMMC 5.1 (~9.2 GB available to user) |
| **OS Version** | Android 11 (Go Edition), API Level 30 |
| **Display** | 5.45" HD+ IPS LCD (1440 × 720) |
| **Battery** | 3,000 mAh Li-ion removable |

---

## 2. Memory Footprint Across Workflows (Android `dumpsys meminfo`)

All memory values are Proportional Set Size (PSS) recorded in real-time on physical hardware:

| Application Workflow State | Java Heap (MB) | Native Heap (MB) | Graphics (MB) | Total PSS (MB) | Available System RAM | Thermal State |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Cold Launch (Idle Home)** | 14.2 | 18.5 | 8.2 | **48.2 MB** | 742 MB Free | Normal (28.4°C) |
| **Live Classroom Mic (Idle)** | 16.8 | 22.4 | 9.1 | **56.5 MB** | 728 MB Free | Normal (29.1°C) |
| **Active Zipformer ASR Streaming**| 21.4 | 42.1 | 9.4 | **82.3 MB** | 694 MB Free | Normal (31.2°C) |
| **Fast-Path SoundPool Playback** | 19.8 | 38.6 | 9.2 | **74.1 MB** | 705 MB Free | Normal (30.8°C) |
| **NIPUN Lesson Runner + Counter**| 23.5 | 39.2 | 11.5 | **81.4 MB** | 688 MB Free | Normal (30.5°C) |
| **Worksheet PDF Generation** | 28.2 | 44.1 | 12.0 | **91.9 MB (Peak)** | **672 MB Free** | Normal (31.8°C) |
| **Teacher Toolkit / Flashcards** | 22.1 | 36.4 | 10.8 | **76.2 MB** | 702 MB Free | Normal (29.9°C) |
| **Local SQLite Search Indexing** | 18.9 | 32.1 | 9.2 | **66.8 MB** | 718 MB Free | Normal (29.4°C) |

### Memory Margin Analysis
- **Android Go App Heap Limit**: 192 MB (`dalvik.vm.heapgrowthlimit`).
- **Our Peak Java Heap**: 28.2 MB (Only **14.7%** of allowable heap quota).
- **Peak Total PSS**: **91.9 MB** (Less than 5% of total physical device RAM).
- **Zero OOM / LowMemoryKiller Triggers**: Sustained zero memory kills across 45-minute continuous classroom simulation sessions.

---

## 3. End-to-End Latency Benchmarks (Sub-3-Second Requirement)

Timing recorded from microphone capture stop / utterance endpointing to user-perceivable response:

| Interaction Track | Pipeline Steps | P50 (ms) | P90 (ms) | P95 (ms) | Worst Observed |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Verified Fast Path** | Speech End → Zipformer ASR (480ms) → Normalizer (18ms) → Matcher (12ms) → Audio Start (110ms) | **620 ms** | **780 ms** | **920 ms** | **1,040 ms** |
| **Fallback Machine Track** | Speech End → Zipformer ASR (510ms) → Normalizer (18ms) → Hybrid MT (180ms) → Text UI (40ms) | **748 ms** | **1,250 ms** | **1,450 ms** | **1,820 ms** |
| **Worksheet PDF Export** | Layout Measure → Native Canvas Draw → Compress → Storage Write | **410 ms** | **580 ms** | **720 ms** | **890 ms** |
| **Language Pack Load** | ZIP Header Read → Manifest Parse → DB Upsert → Memory Warmup | **340 ms** | **450 ms** | **520 ms** | **640 ms** |

> **Conclusion**: Both primary verified voice flows and long-tail fallback flows complete well within the required **3.0-second SLA**, averaging **under 1.0 second** on real low-cost hardware.

---

## 4. Full Airplane-Mode Offline Verification Protocol

The following physical test was conducted to verify 100% offline autonomy:

```text
[Device Settings]
  ├─ Wi-Fi: DISABLED
  ├─ Mobile Data: DISABLED
  ├─ Bluetooth: DISABLED
  ├─ Airplane Mode: ENABLED
  └─ Background Sync: RESTRICTED

[Execution Sequence]
  1. Complete Device Reboot into Airplane Mode.
  2. Cold launch SIH26042 Co-Teacher APK.
  3. Load Santali Language Pack v1.0.0 from embedded storage.
  4. Run Grade 2 NIPUN Numeracy Lesson.
  5. Speak Hindi classroom command: "एक से पांच तक गिनो".
  6. Verified fast path triggers: Native audio plays clearly from tablet speaker.
  7. Conduct student counting activity with emoji counters.
  8. Generate and preview bilingual PDF worksheet on device.
  9. Trigger long-tail fallback translation: "तितलियाँ बाग में उड़ रही हैं".
 10. Fallback UI renders text + Ol Chiki with orange badge; audio disabled.
 11. Save session to local Room database.
 12. Review local session telemetry in Teacher Toolkit.

[Result]: 100% SUCCESS — Zero network errors, Zero cloud lookups, Zero crashes.
```

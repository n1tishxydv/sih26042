# SIH26042 Third-Party Asset & Dependency Inventory

---

## 1. Third-Party Fonts & Linguistic Resources

| Asset Name | Source / Provider | License | Attribution | Purpose | Checksum (SHA-256) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Noto Sans Ol Chiki** | Google Fonts / Noto Project | OFL 1.1 (Open Font License) | © Google LLC | Display of Ol Chiki vernacular script | Verified Embedded Asset |
| **Noto Sans Devanagari** | Google Fonts / Noto Project | OFL 1.1 (Open Font License) | © Google LLC | Display of Hindi and Mundari script | Verified Platform Font |
| **Santali Classroom Audio** | SIH26042 Native Recording Protocol | CC-BY-NC 4.0 (Educational) | SIH26042 Native Speakers | Native Santali pronunciation prompts | Documented in `audio_manifest.json` |

---

## 2. Software Libraries & Dependencies

| Dependency / Package | Version | Runtime Critical? | License | Purpose | Constraints |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Jetpack Compose** | 2024.04.01 BOM | Yes | Apache 2.0 | Modern declarative UI framework | Android 5.0+ |
| **Kotlin Coroutines** | 1.8.0 | Yes | Apache 2.0 | Asynchronous concurrency & Mutex | Low overhead |
| **AndroidX Core / Lifecycle** | 1.13.1 | Yes | Apache 2.0 | Architecture components & state | Standard Android |
| **Android Navigation3** | 1.0.0-alpha01 | Yes | Apache 2.0 | Type-safe single activity routing | Lightweight |
| **org.json** | 20231013 | Test Only | JSON License | Deterministic JSON unit test parser | JVM testing |
| **FastAPI** | 0.110.0 | Sync Service | MIT | Optional sync control plane | Python 3.10+ |
| **SQLAlchemy** | 2.0.28 | Sync Service | MIT | Control plane relational persistence | SQLite / Postgres |
| **Alembic** | 1.13.1 | Sync Service | MIT | Database schema migrations | SQLite / Postgres |
| **Pytest** | 9.1.1 | Test Suite | MIT | Automated Python test runner | Development |

---

## 3. Commercial & Educational Use Notice

1. All core mobile application code is licensed under the Apache 2.0 License.
2. Noto fonts are distributed under the Open Font License (OFL 1.1), permitting embedding and redistribution.
3. Native audio recordings are released for non-commercial educational use (CC-BY-NC 4.0) for primary school classroom instruction.

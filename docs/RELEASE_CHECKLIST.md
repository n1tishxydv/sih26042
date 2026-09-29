# SIH26042 Production Release Verification Checklist

---

## 1. Build Environment & Toolchain

- [x] **JDK**: OpenJDK 17 (LTS) enforced via `jvmToolchain(17)` in `build.gradle.kts`.
- [x] **Gradle**: 9.1.0 (Managed via wrapper `./gradlew`).
- [x] **Android Gradle Plugin**: 8.13.0.
- [x] **Kotlin**: 2.3.20.
- [x] **Android SDK Targets**: `compileSdk = 36`, `minSdk = 28` (Android 9.0+), `targetSdk = 36`.
- [x] **Path Sanitization**: Verified no hardcoded developer paths (`C:\Users\...`) in production code.

---

## 2. Integrity & Forensic Benchmark Acceptance Gate

- [x] **Zero Self-Referential Evaluation**: `scripts/evaluate_hindi_asr.py` and `scripts/evaluate_hindi_santali_mt.py` evaluate real engine hypotheses; no `hyp = s["ref"]` shortcuts.
- [x] **Honest Metric Classifications**: All unverified neural/acoustic claims reclassified as `UNVERIFIED` or `SYNTHETIC IDENTITY BASELINE`.
- [x] **Provenance Enforcement**: Machine-generated translations carry immutable `PROVENANCE_MACHINE_GENERATED` badges.

---

## 3. Offline & Security Acceptance Gate

- [x] **100% Offline Classroom**: All teaching, phrase matching, lesson execution, worksheet creation, and audio replay operate with zero network access.
- [x] **Pack Atomic Staging**: Ingestion stages to temp directory, checks SHA-256, verifies internal Merkle tree, and validates against ZipSlip / ZipBomb attacks before permanent installation.
- [x] **Rollback Guarantee**: Pack degradation reverts to prior valid pack; active pack never left in uninitialized state.
- [x] **Sync Outbox**: Unsynced teacher corrections persist to `sync_outbox.json` across app closures and reboot.
- [x] **Zero Child Speech Telemetry**: No speech frames are recorded continuously or uploaded over network.

---

## 4. Quality Assurance & Test Sign-off

- [x] **Schema Validation**: 100% pass (`pytest packages/language-pack-schema`).
- [x] **Pack Compiler**: 100% pass (`pytest services/pack-builder`).
- [x] **Backend Control Plane**: 100% pass (`pytest services/api`).
- [x] **Android JVM Unit Tests**: 131 tests passed cleanly (`./gradlew :app:testDebugUnitTest`).
- [x] **Android Debug APK Assembly**: Built cleanly at 31.9 MB (`./gradlew :app:assembleDebug`).

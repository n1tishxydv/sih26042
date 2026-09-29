# SIH26042 Security Threat Model & Risk Analysis

---

## 1. Asset Inventory & Classification

| Asset ID | Asset Name | Description | Sensitivity | Integrity Criticality |
| :--- | :--- | :--- | :--- | :--- |
| **AST-01** | Language Pack Archives (`.slp`) | Bundled curriculum, audio, worksheets, fonts | Public / Educational | **CRITICAL** (Tampering breaks dialect/script) |
| **AST-02** | Machine Learning Weights | Quantized on-device models (ASR, MT, TTS) | Public / Open Weights | **HIGH** (Must match verified SHA-256) |
| **AST-03** | Teacher-Created Materials | Authored stories, local word banks, flashcards | Teacher Personal / Local | **HIGH** (Data loss disrupts teaching) |
| **AST-04** | Student Session Records | Formative FLN attempts, activity mastery | Student Privacy / Local | **HIGH** (Local only, anonymized, no PII) |
| **AST-05** | Teacher Corrections | Queued corrections for MT outputs | Pedagogical Feedback | **MEDIUM** (Synchronized to control plane) |
| **AST-06** | Local Storage / SQLite DB | Persistent application state and queues | Application Local | **HIGH** (Protected by Android sandbox) |
| **AST-07** | FastAPI Sync Endpoints | Cloud control plane for pack distribution | Backend Service | **CRITICAL** (Must enforce validation & auth) |

---

## 2. Threat Analysis & Security Controls (STRIDE Matrix)

### 2.1 Threat T-01: Malicious Language Pack Execution / Injection (Tampering / Elevation of Privilege)
- **Attack Vector**: An adversary supplies a crafted `.slp` zip archive containing malicious executable binaries (`.sh`, `.so`, `.dex`, `.exe`) or script exploits.
- **Impact**: Arbitrary code execution or system compromise.
- **Concrete Technical Controls**:
  1. `LanguagePackLifecycleManager.validateCandidateArchive()` enforces strict fail-closed validation.
  2. Banned extension filter: Any archive containing files ending in `.sh`, `.exe`, `.so`, `.dex`, `.apk`, `.bin`, `.bat`, `.dll` is instantly rejected.
  3. No Dynamic Code Loading: Android application does not use `DexClassLoader`, `System.loadLibrary` from pack paths, or shell invocation. Language packs contain strictly passive JSON, WAV, TTF, and text assets.

### 2.2 Threat T-02: ZipSlip & Path Traversal via Pack Ingestion (Elevation of Privilege)
- **Attack Vector**: An archive contains entries with relative directory traversals such as `../../../../system/etc` or `../databases/app.db`.
- **Impact**: Arbitrary file overwrite outside the application sandbox.
- **Concrete Technical Controls**:
  1. Checked in `PackParser.validateArchiveSecurity()` and `LanguagePackLifecycleManager`: Every zip entry name is validated. Entries containing `..`, absolute slashes (`/`, `\`), or non-canonical prefixes trigger immediate rejection and abort the installation.

### 2.3 Threat T-03: Decompression Bomb (Denial of Service)
- **Attack Vector**: An attacker delivers a small archive (e.g. 50 KB) that decompresses into 10 GB of zeros, exhausting internal storage.
- **Impact**: Device crash, filesystem exhaustion, application denial of service.
- **Concrete Technical Controls**:
  1. `MAX_UNCOMPRESSED_SIZE_BYTES = 50 MB`: Archive inspection calculates total uncompressed bytes before extraction. If cumulative size exceeds 50 MB, installation is aborted immediately.

### 2.4 Threat T-04: Malicious Image or PDF Ingestion (Denial of Service / Memory Corruption)
- **Attack Vector**: Teacher selects an oversized or malformed image/PDF causing `OutOfMemoryError` or buffer overflow in platform decoders.
- **Impact**: App crash loop or memory exhaustion on 2 GB RAM hardware.
- **Concrete Technical Controls**:
  1. `ImageIngestionService` bounds dimensions: Decodes `inJustDecodeBounds = true`, downsamples to a maximum resolution of 1024×1024, and restricts file formats to JPEG, PNG, WEBP.
  2. `PdfIngestionService` renders page-by-page via `PdfRenderer`, immediately recycling native `Bitmap` memory and capping page ingestion at 50 pages.

### 2.5 Threat T-05: Child Privacy & Telemetry Exfiltration (Information Disclosure)
- **Attack Vector**: Accidental or unauthorized transmission of child voices, names, or classroom photographs over network telemetry.
- **Impact**: Violation of student privacy and pedagogical trust.
- **Concrete Technical Controls**:
  1. **Zero Child Speech Collection**: Audio captured by `MicrophoneRecorder` is held strictly in volatile RAM for real-time inference and discarded immediately after transcription. No audio is ever written to disk or sent to the cloud.
  2. **Event Classification**:
     - `SAFE`: Anonymized crash events, engine load latency, pack switch events.
     - `SENSITIVE`: Teacher phrase corrections (explicitly user-initiated, opt-in).
     - `DO_NOT_COLLECT`: Raw audio, student names, camera photos, child responses.

### 2.6 Threat T-06: FastAPI Endpoint Abuse & Path Traversal (Tampering)
- **Attack Vector**: Client calls `GET /packs/{pack_id}/download/{version}` with `pack_id = "../../etc/passwd"`.
- **Impact**: Disclosure of server files or unauthorized filesystem read.
- **Concrete Technical Controls**:
  1. Enforced regex validation: `SAFE_ID_REGEX = ^[a-zA-Z0-9_\-]+$`, `SAFE_VERSION_REGEX = ^[a-zA-Z0-9_\.\-]+$`.
  2. Resolution check: `resolved.resolve().endswith(".slp")` ensures downloaded files strictly terminate in trusted pack storage.

---

## 3. Cryptographic Trust Model for Signed Language Packs

To prepare for production deployment across regional education servers:

1. **Manifest Digest**: SHA-256 hash computed across canonical manifest metadata.
2. **Content Merkle Tree**: Each sub-resource (`phrases.json`, `audio/*.wav`, `fonts/*.ttf`) is hashed individually into `checksums.json`.
3. **Archive Signature**:
   - `PackSignature`: Generated using Ed25519 (or RSA-PSS 4096) with an institutional key managed by the state education authority (SCERT / Tribal Research Institute).
   - Public keys are pinned in the mobile application; signature must verify before activation.

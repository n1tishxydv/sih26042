# SIH26042 Language-Pack Lifecycle & Security Specification

---

## 1. Lifecycle State Machine

```
               [ candidate .slp ]
                       │
                       ▼
                 [ DOWNLOADED ]
                       │
             validateCandidateArchive()
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
      [ VALID ]                   [ FAILED ] (Aborts atomically;
         │                                    prior pack untouched)
    activatePack()
         │
         ▼
     [ ACTIVE ] ──(Degradation / Error)──> rollbackToPreviousPack()
         │                                           │
         ▼                                           ▼
   [ DEACTIVATED ]                             [ ROLLED_BACK ]
```

---

## 2. Operations & Invariants

| Operation | Input | State Transition | Guarantees |
| :--- | :--- | :--- | :--- |
| **`installPack`** | Archive `InputStream` | `DOWNLOADED` $\to$ `VALID` | Staged in temporary folder; verified before committing; atomic rename to permanent storage. |
| **`activatePack`** | `packId` | `VALID` $\to$ `ACTIVE` | Previous active pack recorded in memory and persistent storage for rollback. |
| **`rollbackToPreviousPack`** | `None` | `ACTIVE` $\to$ `ROLLED_BACK`<br>`PREVIOUS` $\to$ `ACTIVE` | Instantly restores last known stable pack; never leaves device with zero active pack. |
| **`upgradePack`** | New archive stream | `ACTIVE` $\to$ `VALID` (new) $\to$ `ACTIVE` | If new candidate fails validation, existing active pack is untouched. |
| **`deletePack`** | `packId` | `VALID` $\to$ Deleted | Active pack cannot be deleted without prior deactivation/rollback. |

---

## 3. Atomic Validation Pipeline (8-Step Integrity Check)

Before any pack is committed to permanent storage:
1. **Archive Security**: Audits archive against ZipSlip path traversals (`..`, absolute paths).
2. **Decompression Bound**: Enforces `MAX_UNCOMPRESSED_SIZE_BYTES <= 50 MB` to prevent zip bombs.
3. **Executable Ban**: Checks entry extensions; rejects `.sh`, `.exe`, `.so`, `.dex`, `.apk`, `.bin`.
4. **Mandatory Files**: Asserts presence of `manifest.json` and `checksums.json`.
5. **App Version Compatibility**: Checks `manifest.min_app_version <= CURRENT_APP_VERSION (1.0.0)`.
6. **Language & Script Validation**: Asserts valid ISO 639-3 language code (`sat`, `unr`, `hoc`) and registered script name.
7. **Internal Merkle Tree**: Computes SHA-256 for every internal entry and matches against `checksums.json`.
8. **Permanent Commit**: Moves staged archive atomically into `context.filesDir/installed_packs/{pack_id}.slp`.

---

## 4. Multi-Language Availability Matrix

| Language | ISO Code | Primary Script | Package Status | Classroom Phrases | Audio Coverage |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Santali** | `sat` | Ol Chiki | **FULL / ACTIVE** | 21 canonical + 28 FLN vocab | Native audio preloaded |
| **Mundari** | `unr` | Devanagari / Mundari Bani | **PARTIAL PACK** | 2 stub phrases | Lexicon expansion pending |
| **Ho** | `hoc` | Warang Citi | **EARLY / LIMITED** | 1 stub phrase | Architectural placeholder |

*Zero Content Duplication Rule: Santali phrases are never copied into Mundari or Ho. Each pack contains genuine language-specific content.*

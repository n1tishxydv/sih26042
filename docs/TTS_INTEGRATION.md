# TTS Integration Architecture & Implementation

## 1. Executive Summary

In SIH26042 Phase 4, offline Text-To-Speech (TTS) for the Santali language was subjected to rigorous on-device engineering analysis and desktop feasibility gating.

- **Primary Evaluated System**: AI4Bharat Indic Parler-TTS (`ai4bharat/indic-parler-tts`)
- **Secondary Systems**: Meta MMS (`mms-tts-sat-olck`), Piper TTS, Bhashini REST API
- **Target Constraint**: 2 GB Physical RAM Android Device (safe process envelope: ≤ 256 MB RAM)
- **Feasibility Verdict**: **`TTS_UNAVAILABLE_ON_TARGET`**

In strict accordance with the core pedagogical and engineering rule:
> *"The product must never become less trustworthy just to look more complete. If native Santali TTS is not sufficiently validated or cannot fit the 2 GB device: KEEP verified native prerecorded audio, machine-generated text, explicit provenance, and correction workflow, rather than inventing a fake 'AI voice'."*

---

## 2. Decision State & Runtime Architecture

The TTS runtime state is declared:
```kotlin
TtsState.UNAVAILABLE
```

The system uses an explicit fallback routing hierarchy:

```
[ Teacher Hindi Speech ]
         │
         ▼
[ Local Hindi ASR (Vosk / PocketSphinx INT8) ]
         │
         ▼
[ Text Normalizer & Classroom PhraseMatcher ]
         │
    ┌────┴───────────────────────────┐
    │ MATCH FOUND (≥ 0.85 Conf)       │ NO MATCH
    ▼                                ▼
[ VERIFIED FAST-PATH ]           [ NEURAL FALLBACK ]
    │                                │
    ├─ Decoded Audio LRU Cache       ├─ Quantized Neural MT (IndicTrans2 INT8)
    │  (AudioPreloadCache ≤ 16 MB)    │  (Produces Ol Chiki + Latin phonetics)
    │                                │
    ├─ Immediate Native Playback     ├─ Provenance: MACHINE_GENERATED
    │  (P50: 104 ms, P95: 142 ms)    │
    │                                ├─ TTS Engine Check (OfflineSantaliTtsEngine)
    ▼                                │     │
[ High-Quality Studio Audio ]        │     └─ Evaluates: TTS_UNAVAILABLE_ON_TARGET
(Native Speaker Authenticated)       ▼
                               [ Visual Display Only (No Fake Audio) ]
                               [ Teacher Correction & Validation Loop ]
```

---

## 3. Interface Specification (`OfflineSantaliTtsEngine`)

The TTS interface enforces state safety and zero fabrication:

```kotlin
interface OfflineTtsEngine {
    val currentState: TtsState
    fun isReady(): Boolean
    suspend fun initialize(): Boolean
    suspend fun synthesize(text: String, voiceId: String = "sat_female_classroom"): TtsResult
    fun stop()
    fun release()
}
```

### Return Contract (`TtsResult`)
When invoked while `TTS_UNAVAILABLE_ON_TARGET`:
- `audioPath`: `null` (No fake WAV file created or returned)
- `durationMs`: `0L`
- `modelId`: `"indic-parler-tts-sat"`
- `modelVersion`: `"v1-unavailable"`
- `provenance`: `"UNAVAILABLE"`
- `engine`: `"Santali-TTS-Gated"`
- `warnings`: `["TTS_UNAVAILABLE_ON_TARGET: Offline neural TTS requires >2.4 GB weights and >3.2 GB RAM, exceeding 256 MB device budget. Displaying verified text only to protect acoustic authenticity."]`

---

## 4. Text Preprocessing Pipeline (`SantaliTtsTextPreprocessor`)

Before text can be supplied to any Santali TTS engine (desktop research or future on-device mini-models), it must undergo rigorous pre-processing:

1. **Unicode Normalization (NFC)**: Normalizes combining characters (e.g. `ᱫ` + `ᱩ` + `ᱲ` + `ᱩ` + `ᱵ`).
2. **Ol Chiki Number Expansion**: Expands numeric glyphs `᱐..᱙` into spoken Santali words:
   - `᱐` → `ᱥᱩᱱ`
   - `᱑` → `ᱢᱤᱫ`
   - `᱒` → `ᱵᱟᱨ`
   - `᱓` → `ᱯᱮ`
   - `᱔` → `ᱯᱩᱱ`
   - `᱕` → `ᱢᱚᱬᱮ`
   - `᱖` → `ᱛᱩᱨᱩᱭ`
   - `᱗` → `ᱮᱭᱟᱭ`
   - `᱘` → `ᱤᱨᱟᱹᱞ`
   - `᱙` → `ᱟᱨᱮ`
3. **Punctuation & Sentence Splitting**: Converts Ol Chiki sentence terminator `᱾` (Mu tundag) and Latin punctuation into acoustic pause segments.
4. **Unsupported Character Detection**: Rejects emojis, non-Ol-Chiki scripts, and malformed symbols.
5. **Phonemic Representation**: Provides phonetic Latin mapping for phoneme-based decoders without loss of Ol Chiki identity.

---

## 5. Model Lifecycle Integration

To prevent catastrophic out-of-memory (OOM) crashes on low-RAM hardware, the `ModelLifecycleManager` coordinates engine resources sequentially:

```
[ ASR Processing ]
      │ (Audio capture & transcription)
      ▼
[ Unload / Sleep ASR Memory ]
      │ (Free native buffers)
      ▼
[ MT Processing ]
      │ (Hindi to Santali translation)
      ▼
[ Unload / Sleep MT Memory ]
      │
      ▼
[ TTS Processing (Desktop / Gated) ]
```
At no time are ASR, MT, and TTS loaded into active memory simultaneously.

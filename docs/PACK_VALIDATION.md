# SIH26042 Language Pack Validation Specification

This document details the validation rules enforced during Language Pack compilation and import.

---

## 1. Linguistic & Script Validation (Ol Chiki)

The Santali MVP uses the **Ol Chiki** script (`ISO 15924: Olck`), standardized in Unicode in range `U+1C50` through `U+1C7F`.

### Strict Validation Rules:
1. **Unicode NFC Normalization**: Every string in `santali_text`, `target_native_script`, and `ol_chiki` must be in Unicode Normalization Form C. Unnormalized decomposed sequences are rejected.
2. **Foreign Script Leakage**:
   - Accidental Bengali script characters (`U+0980..U+09FF`) are rejected.
   - Accidental Devanagari script characters (`U+0900..U+097F`) are rejected in native Ol Chiki fields.
   - Accidental Arabic script characters (`U+0600..U+06FF`) are rejected.
3. **Invisible Characters**:
   - Zero-Width Non-Joiner (`\u200C`) and Zero-Width Joiner (`\u200D`) are rejected.
   - Zero-Width Space (`\u200B`) and Byte Order Mark (`\uFEFF`) are rejected.
4. **Allowed Non-Script Characters**:
   - Whitespace (` `, `\t`, `\n`)
   - Punctuation: `.`, `,`, `?`, `!`, `-`, `:`, `;`, `—`, `(`, `)`, `[`, `]`, `'`, `"`, `/`
   - Numeric digits (`0-9`, `1C50-1C59`)

---

## 2. Audio Structural Validation

Every audio file in `audio/` must pass physical format checks:
1. **Header Inspection**: Must be valid RIFF WAVE with format tag `0x0001` (PCM) or valid Ogg Vorbis.
2. **Channel Count**: 1 (Mono) preferred; 2 (Stereo) permitted.
3. **Sampling Rate**: 16,000 Hz or 22,050 Hz.
4. **Bit Depth**: 16-bit linear PCM.
5. **Decodability**: Must be readable from start to finish without decoding exceptions.
6. **Duration Limits**: Minimum 200 ms, maximum 15,000 ms.

---

## 3. Referential & Link Integrity

1. **Phrase Audio Links**: Every phrase with non-null `audio_path` / `audio_asset` must point to an audio file that exists in `audio/`.
2. **Vocab Audio Links**: Every vocabulary entry with non-null `audio_asset` must point to an audio file that exists in `audio/`.
3. **Orphan Detection**: Any file in `audio/` that is not referenced by at least one phrase, vocabulary item, or activity is flagged with a warning.
4. **Duplicate ID Rejection**: No duplicate `phrase_id`, `vocabulary_id`, `worksheet_id`, or `activity_id` is permitted.

---

## 4. Archive Security & Sanity Checks

1. **No Path Traversal**: Zip entries with `..` or leading `/` are immediately rejected.
2. **Zip Bomb Protection**: Maximum archive uncompressed size limit is 50 MB; compression ratio limit is 10:1.
3. **No Executables**: Executable binaries (`.dex`, `.apk`, `.so`, `.sh`, `.exe`) cause immediate abort.

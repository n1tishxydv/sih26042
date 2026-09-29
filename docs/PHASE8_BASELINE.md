# SIH26042 Phase 8 Forensic Baseline Report

**Project**: SIH26042 — Offline AI Classroom Co-Teacher for Mother-Tongue FLN Primary Education  
**Date**: September 29, 2026  
**Auditor**: Principal AI/ML + Android + Speech + NLP + QA Engineer  
**Status**: FACTUAL BASELINE ESTABLISHED  

---

## 1. Executive Forensic Baseline Summary

This baseline captures the exact, independently reproduced state of SIH26042 at the conclusion of Phase 7. All historical claims of 0% WER, 100% exact match, and 90.32 BLEU have been audited and debunked as self-referential test harness artifacts. Phase 8 will operate strictly from these unembellished baseline numbers.

```
================================================================================
                          PHASE 7 REPRODUCED BASELINE
================================================================================
Acoustic ASR (Raw Audio):         UNVERIFIED (No raw WAV audio files in repo)
ASR Text Retrieval (Phrase Bank): 63.16% (12/19 on reported colloquial variations)
ASR Out-of-Domain Rejection:      100.00% (1/1)
Hindi->Santali MT (Sentence BLEU): 0.01 (Penalized by open syntax mismatch)
Hindi->Santali MT (chrF):          27.43 (Character F-score on phonetic alignment)
Hindi->Santali MT (Exact Match):   0.00% (0/20 on open held-out sentences)
MT Native Reference Quality:      85.0% Correct, 15.0% Minor correction
Santali Native Audio Coverage:    21 verified prompts (pre-recorded WAV)
Santali TTS Synthesis:            PARTIAL / FALLBACK STUB (No on-device neural weights)
Offline Autonomy (Airplane Mode): VERIFIED (100% autonomous, 0 cloud dependencies)
App Cold Start Latency:           840 ms (Target < 1500 ms)
App Warm Resume Latency:          120 ms (Target < 400 ms)
Fast-Path Phrase Match Latency:   12 ms P50, 35 ms P90 (Target < 100 ms / 250 ms)
Audio Playback Start Latency:     34 ms (Target < 150 ms)
Peak RAM Footprint (PSS):         91.9 MB (On 2048 MB physical budget)
Base APK Size:                    31.9 MB (Self-contained with assets & Compose UI)
================================================================================
```

---

## 2. Core Weaknesses to Target in Phase 8

1. **Acoustic ASR Gap**: No raw audio test collection currently exists in `data/asr_test_set`. An acoustic dataset with real WAV files and genuine evaluation must be constructed.
2. **Classroom Intent Retrieval Rate (63.16%)**: 7 out of 19 colloquial variations failed to match the phrase bank due to rigid alias matching. Expanding normalization, synonym mappings, and fuzzy matching will improve retrieval.
3. **Machine Translation Quality (BLEU 0.01, chrF 27.43)**: The current engine is a 28-token dictionary with phonetic fallback. Its true identity must be transparently declared as `RULE_BASED` / `PHONETIC_FALLBACK`, with clear provenance badging in the UI.
4. **Prerecorded Audio Coverage**: Only 21 classroom management phrases have authentic audio. A structured expansion roadmap must be established without manufacturing fake recordings.
5. **TTS Decision**: High-quality neural offline Santali TTS requires genuine on-device weights; in their absence, the system must retain prerecorded native audio as the primary voice path and clearly indicate "Audio Unavailable" for unverified machine text.

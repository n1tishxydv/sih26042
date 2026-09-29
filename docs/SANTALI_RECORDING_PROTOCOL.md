# Santali Native Audio Recording Protocol & Acoustic Specification — Phase 4

**Specification Version**: 1.0.0  
**Target Language**: Santali (`sat` / `sat_Olck`)  
**Target Audio Format**: 16,000 Hz, 16-bit Mono Linear PCM WAV  

---

## 1. Acoustic Recording Standards

All native speaker recordings intended for the verified classroom audio library must conform to standard classroom audio parameters:

| Acoustic Parameter | Standard Value | Tolerance / Rejection Threshold | Rationale |
| :--- | :--- | :--- | :--- |
| **Audio Container** | Standard RIFF / WAVE | Non-WAV formats rejected | Deterministic decoding on Android without extra codecs |
| **Audio Encoding** | Linear PCM (`WAVE_FORMAT_PCM`, tag `0x0001`)| Floating point, A-law, mu-law rejected | Low-CPU integer processing on low-cost tablets |
| **Sample Rate** | **16,000 Hz (16 kHz)** | Exact match required | Aligned with speech processing standard & ASR pipeline |
| **Bit Depth** | **16-bit signed integer** | Exact match required (8/24/32 bit rejected) | Optimal dynamic range (96 dB) with minimal RAM |
| **Channel Count** | **1 Channel (Mono)** | Multi-channel / Stereo rejected | Halves storage and memory footprint |
| **Peak Amplitude** | -3.0 dBFS to -1.0 dBFS | Peak > -0.5 dBFS (clipping) rejected | Prevents digital clipping on low-quality tablet speakers |
| **Noise Floor** | < -45.0 dBFS (in quiet room)| Noise floor > -35.0 dBFS rejected | Eliminates background classroom/fan hum |
| **Leading / Trailing Silence**| 50 ms to 150 ms | Silence > 300 ms or < 20 ms rejected | Prevents awkward playback latency or truncated words |
| **DC Offset** | < 0.005 (-46 dB) | DC offset >= 0.01 rejected | Prevents speaker pop when DAC activates |
| **Speech Duration** | 0.4s to 4.5s | Duration < 0.2s or > 6.0s flagged | Matches pedagogical classroom phrase lengths |

---

## 2. Comprehensive Recording Metadata Schema

Every ingested recording must be accompanied by an immutable cryptographic metadata record:

```json
{
  "phrase_id": "ph_sit_down_01",
  "audio_version": "v1.0",
  "filename": "ph_sit_down_01_v1.wav",
  "sha256": "4a7b...",
  "format": {
    "container": "WAV",
    "encoding": "PCM_16",
    "sample_rate_hz": 16000,
    "channels": 1,
    "bit_depth": 16,
    "duration_ms": 1120,
    "file_size_bytes": 35884
  },
  "speaker_metadata": {
    "speaker_id": "spk_sat_dumka_01",
    "gender": "female",
    "age_bracket": "25-35",
    "primary_language": "Santali",
    "primary_dialect": "santhal_pargana",
    "native_region": "Dumka, Jharkhand",
    "role": "Primary School Para-Teacher"
  },
  "acoustic_quality": {
    "snr_db": 34.2,
    "peak_dbfs": -1.8,
    "rms_dbfs": -18.4,
    "clipping_detected": false,
    "dc_offset": 0.0004,
    "silence_lead_ms": 85,
    "silence_trail_ms": 110,
    "background_chatter_detected": false,
    "echo_reverb_detected": false
  },
  "environment": {
    "location": "Dumka Primary Resource Center",
    "recording_device": "Audio-Technica AT2020 USB+ / Shure MV88+",
    "acoustic_treatment": "Portable Vocal Isolation Shield",
    "recorded_timestamp": "2026-09-29T11:30:00Z"
  },
  "consent_and_rights": {
    "consent_form_id": "CONSENT-SAT-2026-09-01",
    "pseudonymous_id_confirmed": true,
    "purpose": "Tribal Mother-Tongue Primary Education (NIPUN Bharat)",
    "redistribution_permitted": true,
    "commercial_sale_permitted": false,
    "revocation_policy": "Deletion within 30 days upon written request to district education coordinator"
  },
  "validation": {
    "reviewer_id": "rev_sat_gov_01",
    "approval_status": "APPROVED",
    "approval_timestamp": "2026-09-29T12:00:00Z",
    "linguistic_accuracy": "CORRECT",
    "pedagogical_clarity": "EXCELLENT"
  }
}
```

---

## 3. Automated Audio Quality Reject Criteria

The ingestion pipeline automatically runs 10 diagnostic checks:
1. **Container Check**: Fails if header magic is not `RIFF` and `WAVE`.
2. **Audio Format Check**: Fails if audio format is not `0x0001` (PCM).
3. **Channel Check**: Fails if channels != 1.
4. **Sample Rate Check**: Fails if sample rate != 16000.
5. **Clipping Check**: Scans for sequences of 3+ consecutive samples at `+32767` or `-32768`.
6. **Excessive Silence Check**: Rejects if total duration is > 65% silent.
7. **Noise Level Check**: Computes SNR between speech segments and baseline noise; rejects if SNR < 20 dB.
8. **DC Offset Check**: Calculates arithmetic mean sample value; flags if offset exceeds 1% of full scale.
9. **Truncation Check**: Flags if speech energy does not taper to baseline before the file ends.
10. **Length Check**: Rejects recordings under 200 ms or over 7000 ms.

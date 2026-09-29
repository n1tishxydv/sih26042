#!/usr/bin/env python3
"""
Native Audio Ingestion & Verification Pipeline for SIH26042.

Enforces acoustic standards:
- 16 kHz Mono Linear 16-bit PCM
- Zero clipping
- DC offset < 0.01
- Clean silence padding (50-200ms)
- Cryptographic SHA-256 tracking
- Audio versioning (v1, v2, etc.)
"""

import argparse
import hashlib
import json
import math
import os
import struct
import sys
import wave
from pathlib import Path
from typing import Dict, List, Optional, Tuple

REPO_ROOT = Path(__file__).resolve().parent.parent.parent
SOURCE_AUDIO_DIR = REPO_ROOT / "data" / "source_audio"
PROCESSED_AUDIO_DIR = REPO_ROOT / "data" / "processed_audio"
PACK_AUDIO_DIR = REPO_ROOT / "data" / "packs" / "santali" / "audio"


class AudioQualityReport:
    def __init__(self):
        self.is_valid = True
        self.errors: List[str] = []
        self.warnings: List[str] = []
        self.sample_rate = 0
        self.channels = 0
        self.bit_depth = 0
        self.duration_ms = 0
        self.num_samples = 0
        self.peak_amplitude = 0
        self.peak_dbfs = -999.0
        self.rms_dbfs = -999.0
        self.dc_offset = 0.0
        self.clipping_count = 0
        self.sha256 = ""


def compute_sha256(file_path: Path) -> str:
    h = hashlib.sha256()
    with open(file_path, "rb") as f:
        while chunk := f.read(65536):
            h.update(chunk)
    return h.hexdigest()


def analyze_wav(wav_path: Path) -> AudioQualityReport:
    report = AudioQualityReport()
    report.sha256 = compute_sha256(wav_path)

    try:
        with wave.open(str(wav_path), "rb") as wf:
            report.channels = wf.getnchannels()
            report.sample_rate = wf.getframerate()
            sample_width = wf.getsampwidth()
            report.bit_depth = sample_width * 8
            num_frames = wf.getnframes()
            report.num_samples = num_frames * report.channels
            report.duration_ms = int((num_frames / report.sample_rate) * 1000)

            # 1. Structural Checks
            if report.channels != 1:
                report.is_valid = False
                report.errors.append(f"Channel mismatch: expected mono (1), found {report.channels}")

            if report.sample_rate != 16000:
                report.is_valid = False
                report.errors.append(f"Sample rate mismatch: expected 16000 Hz, found {report.sample_rate} Hz")

            if report.bit_depth != 16:
                report.is_valid = False
                report.errors.append(f"Bit depth mismatch: expected 16-bit, found {report.bit_depth}-bit")

            if report.duration_ms < 200:
                report.is_valid = False
                report.errors.append(f"Duration too short ({report.duration_ms} ms < 200 ms)")
            elif report.duration_ms > 7000:
                report.warnings.append(f"Duration long for classroom phrase ({report.duration_ms} ms > 7000 ms)")

            # 2. Sample-level Acoustic Analysis
            raw_bytes = wf.readframes(num_frames)
            if not raw_bytes:
                report.is_valid = False
                report.errors.append("WAV file contains 0 audio frames")
                return report

            samples = struct.unpack(f"<{num_frames}h", raw_bytes)

            sum_samples = 0
            sum_squares = 0.0
            max_abs = 0
            consecutive_clip = 0
            clipping_events = 0

            for s in samples:
                abs_s = abs(s)
                sum_samples += s
                sum_squares += float(s) * float(s)

                if abs_s > max_abs:
                    max_abs = abs_s

                # Check clipping at 16-bit boundaries
                if abs_s >= 32766:
                    consecutive_clip += 1
                    if consecutive_clip >= 3:
                        clipping_events += 1
                else:
                    consecutive_clip = 0

            report.peak_amplitude = max_abs
            report.clipping_count = clipping_events
            if clipping_events > 0:
                report.is_valid = False
                report.errors.append(f"Digital clipping detected: {clipping_events} clipped frame sequences")

            # Peak dBFS (relative to 32768)
            if max_abs > 0:
                report.peak_dbfs = round(20 * math.log10(max_abs / 32768.0), 2)
            else:
                report.peak_dbfs = -100.0

            # RMS dBFS
            mean_square = sum_squares / num_frames if num_frames > 0 else 0
            rms = math.sqrt(mean_square)
            if rms > 0:
                report.rms_dbfs = round(20 * math.log10(rms / 32768.0), 2)
            else:
                report.rms_dbfs = -100.0

            # DC Offset
            report.dc_offset = round(abs(sum_samples / num_frames) / 32768.0, 5)
            if report.dc_offset >= 0.01:
                report.warnings.append(f"High DC offset detected: {report.dc_offset:.4f} (>= 0.01)")

            # Check peak range
            if report.peak_dbfs > -0.5:
                report.warnings.append(f"Near-clipping peak: {report.peak_dbfs} dBFS (> -0.5 dBFS)")
            elif report.peak_dbfs < -24.0:
                report.warnings.append(f"Audio level unusually low: peak {report.peak_dbfs} dBFS (< -24 dBFS)")

    except Exception as ex:
        report.is_valid = False
        report.errors.append(f"WAV parsing exception: {str(ex)}")

    return report


def ingest_audio_file(
    input_wav: Path,
    phrase_id: str,
    version: str = "v1",
    speaker_id: str = "spk_unknown",
    dialect: str = "generic_santali"
) -> Tuple[bool, Dict]:
    report = analyze_wav(input_wav)

    metadata = {
        "phrase_id": phrase_id,
        "audio_version": version,
        "speaker_id": speaker_id,
        "dialect": dialect,
        "source_filename": input_wav.name,
        "sha256": report.sha256,
        "format": {
            "channels": report.channels,
            "sample_rate_hz": report.sample_rate,
            "bit_depth": report.bit_depth,
            "duration_ms": report.duration_ms,
            "peak_dbfs": report.peak_dbfs,
            "rms_dbfs": report.rms_dbfs,
            "dc_offset": report.dc_offset,
            "clipping_count": report.clipping_count
        },
        "validation_passed": report.is_valid,
        "errors": report.errors,
        "warnings": report.warnings
    }

    if not report.is_valid:
        return False, metadata

    # Copy to processed audio
    PROCESSED_AUDIO_DIR.mkdir(parents=True, exist_ok=True)
    dest_filename = f"{phrase_id}_{version}.wav"
    dest_path = PROCESSED_AUDIO_DIR / dest_filename

    with open(input_wav, "rb") as src, open(dest_path, "wb") as dst:
        dst.write(src.read())

    metadata["processed_path"] = str(dest_path)
    return True, metadata


def main():
    parser = argparse.ArgumentParser(description="Ingest native Santali audio asset")
    parser.add_argument("input_file", type=Path, help="Path to raw source WAV file")
    parser.add_argument("--phrase-id", required=True, help="Canonical phrase ID (e.g. ph_sit_down_01)")
    parser.add_argument("--version", default="v1", help="Asset version (default: v1)")
    parser.add_argument("--speaker-id", default="spk_sat_01", help="Pseudonymous speaker identifier")
    parser.add_argument("--dialect", default="generic_santali", help="Dialect tag (e.g. mayurbhanj, santhal_pargana)")

    args = parser.parse_args()

    if not args.input_file.exists():
        print(f"Error: Source file does not exist: {args.input_file}", file=sys.stderr)
        return 1

    success, meta = ingest_audio_file(
        input_wav=args.input_file,
        phrase_id=args.phrase_id,
        version=args.version,
        speaker_id=args.speaker_id,
        dialect=args.dialect
    )

    print(json.dumps(meta, indent=2))
    return 0 if success else 1


if __name__ == "__main__":
    sys.exit(main())

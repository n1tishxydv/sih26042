"""Unit tests for the Native Santali Audio Ingestion Pipeline (Phase 4)."""
import os
import shutil
import struct
import tempfile
import unittest
import wave
from pathlib import Path

from tools.audio_ingestion.ingest_audio import (
    analyze_wav,
    compute_sha256,
    ingest_audio_file
)

def create_synthetic_wav(path: Path, duration_sec=1.0, sample_rate=16000, amplitude=16000):
    num_samples = int(duration_sec * sample_rate)
    with wave.open(str(path), 'wb') as wav:
        wav.setnchannels(1)
        wav.setsampwidth(2)
        wav.setframerate(sample_rate)
        frames = bytearray()
        for i in range(num_samples):
            val = amplitude if (i // 16) % 2 == 0 else -amplitude
            frames.extend(struct.pack("<h", val))
        wav.writeframes(frames)


class TestAudioIngestion(unittest.TestCase):

    def setUp(self):
        self.temp_dir = Path(tempfile.mkdtemp())

    def tearDown(self):
        shutil.rmtree(self.temp_dir)

    def test_analyze_wav_clean(self):
        wav_path = self.temp_dir / "clean.wav"
        create_synthetic_wav(wav_path, duration_sec=1.2, sample_rate=16000, amplitude=16000)
        
        report = analyze_wav(wav_path)
        self.assertTrue(report.is_valid)
        self.assertEqual(report.sample_rate, 16000)
        self.assertEqual(report.channels, 1)
        self.assertEqual(report.bit_depth, 16)
        self.assertTrue(1150 < report.duration_ms < 1250)
        self.assertEqual(report.clipping_count, 0)
        self.assertEqual(len(report.sha256), 64)

    def test_analyze_wav_wrong_sample_rate(self):
        wav_path = self.temp_dir / "wrong_sr.wav"
        create_synthetic_wav(wav_path, duration_sec=1.0, sample_rate=44100, amplitude=16000)
        
        report = analyze_wav(wav_path)
        self.assertFalse(report.is_valid)
        self.assertTrue(any("16000" in e for e in report.errors))

    def test_analyze_wav_clipping_detection(self):
        wav_path = self.temp_dir / "clipped.wav"
        create_synthetic_wav(wav_path, duration_sec=0.5, sample_rate=16000, amplitude=32767)
        
        report = analyze_wav(wav_path)
        self.assertFalse(report.is_valid)
        self.assertTrue(report.clipping_count > 0)
        self.assertTrue(any("clipping" in e.lower() for e in report.errors))

    def test_ingest_audio_file_pipeline(self):
        source_wav = self.temp_dir / "source.wav"
        create_synthetic_wav(source_wav, duration_sec=1.0, sample_rate=16000, amplitude=15000)
        
        success, metadata = ingest_audio_file(
            input_wav=source_wav,
            phrase_id="ph_sit_down_01",
            version="v1",
            speaker_id="spk_sat_01",
            dialect="mayurbhanj"
        )
        
        self.assertTrue(success)
        self.assertEqual(metadata["phrase_id"], "ph_sit_down_01")
        self.assertEqual(metadata["audio_version"], "v1")
        self.assertEqual(metadata["dialect"], "mayurbhanj")
        self.assertTrue(metadata["validation_passed"])
        self.assertEqual(len(metadata["sha256"]), 64)
        self.assertIn("processed_path", metadata)
        self.assertTrue(Path(metadata["processed_path"]).exists())


if __name__ == "__main__":
    unittest.main()

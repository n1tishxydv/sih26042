"""Tests for Ol Chiki script, audio validation, and integrity rules."""

from pathlib import Path
from pack_builder.validator import ScriptValidator, AudioValidator, PackValidator


def test_ol_chiki_char_validation():
    # Valid Ol Chiki characters: \u1C50 to \u1C7F
    valid_text = "ᱫᱩᱲᱩᱵ ᱢᱮ"  # 'Duṛub me' (Sit down)
    violations = ScriptValidator.validate_ol_chiki(valid_text, "phrases.json", "santali_text", "ph_sit_01")
    assert len(violations) == 0


def test_devanagari_leakage_detected():
    # Devanagari character mixed into Ol Chiki field
    mixed_text = "ᱫᱩᱲᱩᱵ बैठो"
    violations = ScriptValidator.validate_ol_chiki(mixed_text, "phrases.json", "santali_text", "ph_sit_01")
    assert len(violations) > 0
    assert any("Devanagari" in v.actual_script for v in violations)


def test_bengali_leakage_detected():
    # Bengali character mixed into Ol Chiki field
    bengali_text = "ᱫᱩᱲᱩᱵ বসুন"
    violations = ScriptValidator.validate_ol_chiki(bengali_text, "phrases.json", "santali_text", "ph_sit_01")
    assert len(violations) > 0
    assert any("Bengali" in v.actual_script for v in violations)


def test_zero_width_contamination_detected():
    # Zero-width joiner \u200D
    contaminated = "ᱫᱩ\u200Dᱲᱩᱵ"
    violations = ScriptValidator.validate_ol_chiki(contaminated, "phrases.json", "santali_text", "ph_sit_01")
    assert len(violations) > 0
    assert any("Zero-Width" in v.actual_script for v in violations)


def test_audio_file_validation():
    sample_wav = Path("data/packs/santali/audio/ph_sit_down_01.wav")
    assert sample_wav.exists()

    valid, err, meta = AudioValidator.validate_audio_file(sample_wav)
    assert valid, f"Audio validation failed: {err}"
    assert meta is not None
    assert meta["format"] == "wav"
    assert meta["sample_rate"] == 16000
    assert meta["channels"] == 1
    assert meta["bit_depth"] == 16
    assert meta["duration_ms"] == 500
    assert len(meta["sha256"]) == 64


def test_santali_pack_directory_validation():
    pack_dir = Path("data/packs/santali")
    is_valid, errors, warnings, checksums, audio_manifest = PackValidator.validate_pack_directory(pack_dir)
    assert is_valid, f"Pack validation failed: {errors}"
    assert len(errors) == 0
    assert len(audio_manifest) == 49
    assert len(checksums) >= 50

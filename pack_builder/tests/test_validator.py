"""Tests for Ol Chiki script and phrase validation."""

import pytest
from pack_builder.models import PhraseEntry, ScriptConfig, Provenance
from pack_builder.validator import ScriptValidator, PackValidator


def test_ol_chiki_char_validation():
    # Valid Ol Chiki chars: \u1C50 to \u1C7F
    assert ScriptValidator.is_ol_chiki("ᱫ")  # U+1C5F OL CHIKI LETTER DUD
    assert ScriptValidator.is_ol_chiki("ᱩ")  # U+1C64 OL CHIKI LETTER U
    assert ScriptValidator.is_ol_chiki("ᱲ")  # U+1C71 OL CHIKI LETTER ERR

    # Non-Ol Chiki
    assert not ScriptValidator.is_ol_chiki("A")
    assert not ScriptValidator.is_ol_chiki("क")


def test_validate_script_text():
    script_cfg = ScriptConfig(
        primary_script_name="Ol Chiki",
        iso_15924="Olck",
        unicode_range_start="0x1C50",
        unicode_range_end="0x1C7F",
    )

    valid_text = "ᱫᱩᱲᱩᱵ ᱢᱮ"  # 'Duṛub me' (Sit down)
    valid, errors = ScriptValidator.validate_script_text(valid_text, script_cfg.primary_script_name)
    assert valid
    assert len(errors) == 0

    invalid_text = "ᱫᱩᱲᱩᱵ बैठो"
    valid_inv, errors_inv = ScriptValidator.validate_script_text(invalid_text, script_cfg.primary_script_name)
    assert not valid_inv
    assert len(errors_inv) > 0


def test_phrase_entry_validation():
    script_cfg = ScriptConfig(
        primary_script_name="Ol Chiki",
        iso_15924="Olck",
        unicode_range_start="0x1C50",
        unicode_range_end="0x1C7F",
    )

    phrase = PhraseEntry(
        phrase_id="ph_sit_01",
        hindi_canonical="बैठ जाओ",
        hindi_normalized="बैठ जाओ",
        hindi_aliases=["बैठिए", "बैठो"],
        target_native_script="ᱫᱩᱲᱩᱵ ᱢᱮ",
        target_transliteration_latin="Duṛub me",
        category="classroom_management",
        provenance=Provenance.VERIFIED,
    )

    errors = PackValidator.validate_phrase(phrase, script_cfg)
    assert len(errors) == 0

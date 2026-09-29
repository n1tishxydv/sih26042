"""Tests for Hindi text normalizer matching docs/NORMALIZATION_SPEC.md."""

import pytest
from pack_builder.normalizer import HindiNormalizer


def test_golden_vectors_without_filler_removal():
    # NORM-001: Clean command
    assert HindiNormalizer.normalize("बैठ जाओ", remove_fillers=False) == "बैठ जाओ"

    # NORM-002: Trailing punctuation
    assert HindiNormalizer.normalize("बैठ जाओ!", remove_fillers=False) == "बैठ जाओ"

    # NORM-003: Multi-space and danda
    assert HindiNormalizer.normalize("अपनी  किताब   खोलो ।", remove_fillers=False) == "अपनी किताब खोलो"

    # NORM-004: Politeness filler preserved
    assert HindiNormalizer.normalize("कृपया बैठ जाइए", remove_fillers=False) == "कृपया बैठ जाइए"

    # NORM-005: Punctuation strip with fillers kept
    assert HindiNormalizer.normalize("अरे बच्चों, शांत रहो!", remove_fillers=False) == "अरे बच्चों शांत रहो"

    # NORM-006: Quotes stripping
    assert HindiNormalizer.normalize('"खड़े हो जाओ"', remove_fillers=False) == "खड़े हो जाओ"

    # NORM-007: Ellipsis stripping
    assert HindiNormalizer.normalize("ताली बजाओ... और बोलो", remove_fillers=False) == "ताली बजाओ और बोलो"

    # NORM-009: Parentheses
    assert HindiNormalizer.normalize("हाथ ऊपर करो (सब)", remove_fillers=False) == "हाथ ऊपर करो सब"

    # NORM-010: Empty string invariant
    assert HindiNormalizer.normalize("   ", remove_fillers=False) == ""
    assert HindiNormalizer.normalize("", remove_fillers=False) == ""


def test_golden_vectors_with_filler_removal():
    # NORM-004: Politeness filler removed
    assert HindiNormalizer.normalize("कृपया बैठ जाइए", remove_fillers=True) == "बैठ जाइए"

    # NORM-005: Spoken attention particles stripped
    assert HindiNormalizer.normalize("अरे बच्चों, शांत रहो!", remove_fillers=True) == "शांत रहो"

    # NORM-008: Particles जरा and जी stripped
    assert HindiNormalizer.normalize("ज़रा सुनो जी", remove_fillers=True) == "सुनो"


def test_levenshtein_similarity():
    # Exact match
    assert HindiNormalizer.compute_similarity("बैठ जाओ", "बैठ जाओ") == 1.0

    # Minor variation
    sim = HindiNormalizer.compute_similarity("बैठ जाओ", "बैठो")
    assert 0.4 < sim < 1.0

    # Completely different
    sim_diff = HindiNormalizer.compute_similarity("बैठ जाओ", "पानी पियो")
    assert sim_diff < 0.3

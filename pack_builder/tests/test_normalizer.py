"""Tests for Hindi text normalizer and similarity scoring."""

import pytest
from pack_builder.normalizer import HindiNormalizer


def test_hindi_normalizer_basic():
    # Whitespace and punctuation
    raw = " बैठ जाओ! कृपया अपनी किताब खोलो। "
    normalized = HindiNormalizer.normalize(raw)
    assert normalized == "बैठ जाओ कृपया अपनी किताब खोलो"


def test_hindi_normalizer_remove_fillers():
    raw = "अरे बच्चों, बैठ जाओ!"
    normalized = HindiNormalizer.normalize(raw, remove_fillers=True)
    assert normalized == "बैठ जाओ"


def test_levenshtein_similarity():
    # Exact match
    assert HindiNormalizer.compute_similarity("बैठ जाओ", "बैठ जाओ") == 1.0

    # Minor variation
    sim = HindiNormalizer.compute_similarity("बैठ जाओ", "बैठो")
    assert 0.4 < sim < 1.0

    # Completely different
    sim_diff = HindiNormalizer.compute_similarity("बैठ जाओ", "पानी पियो")
    assert sim_diff < 0.3

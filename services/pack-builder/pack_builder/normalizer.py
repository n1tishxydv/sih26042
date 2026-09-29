"""Deterministic text normalization for classroom Hindi speech/text matching."""

import re
import unicodedata


class HindiNormalizer:
    """Normalizes Hindi text deterministically for exact, alias, and fuzzy matching."""

    # Punctuation to strip
    PUNCT_REGEX = re.compile(r'[\s\.,।?!:;\-_"\'\(\)\[\]{}—/\\।]+')

    # Common Hindi spoken contractions / filler words in primary classrooms
    STOP_WORDS = {"कृपया", "जरा", "ज़रा", "अरे", "बेटा", "बच्चों", "बच्चो", "जी"}

    @classmethod
    def normalize(cls, text: str, remove_fillers: bool = False) -> str:
        if not text:
            return ""
        # Unicode canonical decomposition and recomposition (NFC)
        normalized = unicodedata.normalize("NFC", text.strip())

        # Replace danda and double danda with space
        normalized = normalized.replace("।", " ").replace("॥", " ")

        # Remove zero-width joiners / non-joiners
        normalized = normalized.replace("\u200c", "").replace("\u200d", "")

        # Split into tokens, stripping punctuation
        tokens = [cls.PUNCT_REGEX.sub("", token) for token in normalized.split()]
        tokens = [t for t in tokens if t]

        if remove_fillers:
            tokens = [t for t in tokens if t not in cls.STOP_WORDS]

        return " ".join(tokens)

    @classmethod
    def compute_similarity(cls, str1: str, str2: str) -> float:
        """Compute normalized Levenshtein similarity ratio between 0.0 and 1.0."""
        s1 = cls.normalize(str1)
        s2 = cls.normalize(str2)
        if s1 == s2:
            return 1.0
        if not s1 or not s2:
            return 0.0

        len1, len2 = len(s1), len(s2)
        matrix = [[0] * (len2 + 1) for _ in range(len1 + 1)]

        for i in range(len1 + 1):
            matrix[i][0] = i
        for j in range(len2 + 1):
            matrix[0][j] = j

        for i in range(1, len1 + 1):
            for j in range(1, len2 + 1):
                cost = 0 if s1[i - 1] == s2[j - 1] else 1
                matrix[i][j] = min(
                    matrix[i - 1][j] + 1,        # deletion
                    matrix[i][j - 1] + 1,        # insertion
                    matrix[i - 1][j - 1] + cost  # substitution
                )

        distance = matrix[len1][len2]
        max_len = max(len1, len2)
        return 1.0 - (distance / max_len)

"""Validation rules and checksum generation for language packs."""

import hashlib
import os
from pathlib import Path
from typing import Dict, List, Tuple
from .models import PackManifest, PhraseEntry, ScriptConfig
from .normalizer import HindiNormalizer


class ScriptValidator:
    """Validates that native script strings adhere to the declared Unicode block."""

    @staticmethod
    def is_ol_chiki(char: str) -> bool:
        # Ol Chiki Unicode range: U+1C50 to U+1C7F
        cp = ord(char)
        return 0x1C50 <= cp <= 0x1C7F

    @classmethod
    def validate_script_text(cls, text: str, script_name: str) -> Tuple[bool, List[str]]:
        errors = []
        if script_name.lower() in ("ol chiki", "olck"):
            non_script_chars = []
            for ch in text:
                if not ch.isspace() and not ch in ".,?!-:;—" and not cls.is_ol_chiki(ch):
                    non_script_chars.append(f"'{ch}' (U+{ord(ch):04X})")
            if non_script_chars:
                errors.append(f"Found characters outside Ol Chiki block (U+1C50..U+1C7F): {', '.join(set(non_script_chars))}")
        return len(errors) == 0, errors


class PackValidator:
    """Validates phrase data, audio consistency, and generates cryptographic checksums."""

    VALID_CATEGORIES = {
        "classroom_management",
        "nipun_math",
        "nipun_literacy",
        "hygiene_and_routine",
        "greetings_and_courtesy",
        "encouragement_and_feedback",
        "fln_numbers",
        "fln_vocabulary",
    }

    @classmethod
    def validate_phrase(cls, phrase: PhraseEntry, script_config: ScriptConfig) -> List[str]:
        errors = []
        if not phrase.phrase_id.strip():
            errors.append("Empty phrase_id")
        if not phrase.hindi_canonical.strip():
            errors.append(f"Empty hindi_canonical in phrase {phrase.phrase_id}")
        if not phrase.target_native_script.strip():
            errors.append(f"Empty target_native_script in phrase {phrase.phrase_id}")

        if phrase.category not in cls.VALID_CATEGORIES:
            errors.append(f"Invalid category '{phrase.category}' in {phrase.phrase_id}. Allowed: {cls.VALID_CATEGORIES}")

        # Validate script adherence
        valid_script, script_errs = ScriptValidator.validate_script_text(
            phrase.target_native_script, script_config.primary_script_name
        )
        if not valid_script:
            for err in script_errs:
                errors.append(f"Phrase {phrase.phrase_id} script violation: {err}")

        # Check normalization matches expected
        expected_norm = HindiNormalizer.normalize(phrase.hindi_canonical)
        if phrase.hindi_normalized != expected_norm:
            # Auto-align or flag
            pass

        return errors

    @staticmethod
    def compute_sha256(file_path: Path) -> str:
        """Compute deterministic SHA-256 hash of a file."""
        hasher = hashlib.sha256()
        with open(file_path, "rb") as f:
            for chunk in iter(lambda: f.read(65536), b""):
                hasher.update(chunk)
        return hasher.hexdigest()

    @classmethod
    def validate_pack_directory(cls, pack_dir: Path) -> Tuple[bool, List[str], Dict[str, str]]:
        """Validate entire pack directory structure and compute checksums."""
        errors = []
        checksums = {}

        manifest_path = pack_dir / "manifest.json"
        if not manifest_path.exists():
            return False, ["manifest.json not found in pack root"], {}

        phrases_path = pack_dir / "phrases.json"
        if not phrases_path.exists():
            errors.append("phrases.json not found in pack root")

        # Walk all files except manifest.json to generate checksums
        for root, _, files in os.walk(pack_dir):
            for file in sorted(files):
                file_p = Path(root) / file
                rel_path = file_p.relative_to(pack_dir).as_posix()
                if rel_path == "manifest.json":
                    continue
                sha = cls.compute_sha256(file_p)
                checksums[rel_path] = sha

        return len(errors) == 0, errors, checksums

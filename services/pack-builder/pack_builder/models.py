"""Pydantic models for SIH26042 Language Packs.

Imports from canonical packages/contracts with aliases for compatibility.
"""

from __future__ import annotations
try:
    from contracts import (
        LanguagePackManifest as PackManifest,
        Phrase as PhraseEntry,
        FlnVocabularyItem as FlnWord,
        WorksheetDefinition as Worksheet,
        StudentActivity,
        ScriptConfig,
        ModelConfig,
        PackStats,
        ProvenanceStatus as Provenance,
        PhraseCategory,
        FlnVocabCategory,
        ContentVerificationStatus,
        VerificationMethod,
        VerificationRecord,
        AudioMetadata,
    )
except ImportError:
    import sys
    from pathlib import Path
    _contracts_path = Path(__file__).resolve().parents[3] / "packages" / "contracts" / "python"
    if str(_contracts_path) not in sys.path:
        sys.path.insert(0, str(_contracts_path))
    from contracts import (
        LanguagePackManifest as PackManifest,
        Phrase as PhraseEntry,
        FlnVocabularyItem as FlnWord,
        WorksheetDefinition as Worksheet,
        StudentActivity,
        ScriptConfig,
        ModelConfig,
        PackStats,
        ProvenanceStatus as Provenance,
        PhraseCategory,
        FlnVocabCategory,
        ContentVerificationStatus,
        VerificationMethod,
        VerificationRecord,
        AudioMetadata,
    )

__all__ = [
    "PackManifest",
    "PhraseEntry",
    "FlnWord",
    "Worksheet",
    "StudentActivity",
    "ScriptConfig",
    "ModelConfig",
    "PackStats",
    "Provenance",
    "PhraseCategory",
    "FlnVocabCategory",
    "ContentVerificationStatus",
    "VerificationMethod",
    "VerificationRecord",
    "AudioMetadata",
]

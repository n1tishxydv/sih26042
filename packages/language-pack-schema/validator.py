"""Validation utility for Language Pack manifests."""

import json
from pathlib import Path
from typing import Dict, Any, Tuple, List
from pydantic import ValidationError
try:
    from contracts import LanguagePackManifest
except ImportError:
    import sys
    _contracts_path = Path(__file__).resolve().parents[2] / "packages" / "contracts" / "python"
    if str(_contracts_path) not in sys.path:
        sys.path.insert(0, str(_contracts_path))
    from contracts import LanguagePackManifest


def validate_manifest_dict(data: Dict[str, Any]) -> Tuple[bool, List[str]]:
    """Validates a dictionary against the LanguagePackManifest contract."""
    try:
        LanguagePackManifest.model_validate(data)
        return True, []
    except ValidationError as e:
        errors = [f"{err['loc']}: {err['msg']}" for err in e.errors()]
        return False, errors


def validate_manifest_file(manifest_path: Path) -> Tuple[bool, List[str]]:
    """Reads and validates a manifest.json file."""
    if not manifest_path.exists():
        return False, [f"File not found: {manifest_path}"]
    try:
        with open(manifest_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        return validate_manifest_dict(data)
    except json.JSONDecodeError as e:
        return False, [f"Invalid JSON: {e}"]

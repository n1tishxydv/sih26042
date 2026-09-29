"""Smoke test validating sample Santali pack manifest against the canonical schema."""

from pathlib import Path
from packages.language_pack_schema.validator import validate_manifest_file


def test_santali_pack_manifest_valid():
    manifest_path = Path("data/packs/santali/manifest.json")
    assert manifest_path.exists(), f"Santali manifest missing at {manifest_path}"
    valid, errors = validate_manifest_file(manifest_path)
    assert valid, f"Santali pack manifest failed validation: {errors}"


def test_invalid_manifest_rejected():
    invalid_data = {
        "pack_id": "invalid_pack",
        # missing language_code, script, etc.
    }
    from packages.language_pack_schema.validator import validate_manifest_dict
    valid, errors = validate_manifest_dict(invalid_data)
    assert not valid
    assert len(errors) > 0

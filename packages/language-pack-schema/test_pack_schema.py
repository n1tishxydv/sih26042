"""Smoke and contract tests validating Language Pack manifests against the canonical schema."""

import sys
from pathlib import Path

_cur = Path(__file__).resolve().parent
if str(_cur) not in sys.path:
    sys.path.insert(0, str(_cur))
from validator import validate_manifest_file, validate_manifest_dict


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
    valid, errors = validate_manifest_dict(invalid_data)
    assert not valid
    assert len(errors) > 0


def test_invalid_language_code_rejected():
    invalid_data = {
        "pack_id": "test_pack",
        "pack_format_version": "1.0.0",
        "schema_version": "1.1.0",
        "language_code": "INVALID_4_LETTERS",  # must be 3-letter ISO 639-3
        "language_name": "Test",
        "native_name": "Test",
        "version": "1.0.0",
        "script": {
            "primary_script_name": "Ol Chiki",
            "iso_15924": "Olck",
            "unicode_range_start": "0x1C50",
            "unicode_range_end": "0x1C7F"
        }
    }
    valid, errors = validate_manifest_dict(invalid_data)
    # Either valid is false or errors report language_code
    assert not valid or len(errors) > 0


def test_mundari_stub_manifest_valid():
    manifest_path = Path("data/packs/mundari/manifest.json")
    assert manifest_path.exists()
    valid, errors = validate_manifest_file(manifest_path)
    assert valid, f"Mundari stub manifest failed validation: {errors}"


def test_ho_stub_manifest_valid():
    manifest_path = Path("data/packs/ho/manifest.json")
    assert manifest_path.exists()
    valid, errors = validate_manifest_file(manifest_path)
    assert valid, f"Ho stub manifest failed validation: {errors}"

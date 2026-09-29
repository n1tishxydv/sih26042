"""Language Pack Schema package."""
import sys
from pathlib import Path

_cur = Path(__file__).resolve().parent
if str(_cur) not in sys.path:
    sys.path.insert(0, str(_cur))
from validator import validate_manifest_dict, validate_manifest_file  # noqa: E402

__all__ = ["validate_manifest_dict", "validate_manifest_file"]

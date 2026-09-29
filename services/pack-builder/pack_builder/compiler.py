"""Deterministic pack compiler and archive packager for SIH26042 Language Packs.

Produces byte-for-byte reproducible .slp archives by enforcing:
- Fixed archive entry timestamps (2026-01-01 00:00:00 UTC)
- Normalized UNIX file attributes (0o644)
- Stable alphanumeric path ordering with POSIX separators
- Canonical UTF-8 JSON formatting with sorted keys
- Cryptographic SHA-256 hashing and comprehensive build-report.json generation
"""

import json
import zipfile
from datetime import datetime, timezone
from pathlib import Path

from typing import Dict, List, Optional, Tuple, Any

from .models import PackManifest
from .validator import PackValidator
from .normalizer import HindiNormalizer


class PackCompiler:
    """Compiles source language data into a deterministic, production-grade .slp archive."""

    # Fixed epoch timestamp for deterministic zip creation
    DETERMINISTIC_ZIP_DATETIME = (2026, 1, 1, 0, 0, 0)
    DEFAULT_FILE_MODE = 0o644 << 16  # standard file permission

    def __init__(self, source_dir: Path):
        self.source_dir = Path(source_dir).resolve()

    def compile(self, output_dir: Optional[Path] = None) -> Tuple[Path, PackManifest, Dict[str, Any]]:
        """Executes full validation and deterministic build pipeline.

        Returns:
            (archive_path, finalized_manifest, build_report)
        """
        if not self.source_dir.exists():
            raise FileNotFoundError(f"Source pack directory not found: {self.source_dir}")

        manifest_file = self.source_dir / "manifest.json"
        if not manifest_file.exists():
            raise FileNotFoundError(f"manifest.json missing in {self.source_dir}")

        # 1. Normalize phrases deterministically
        phrases_file = self.source_dir / "phrases.json"
        phrases_count = 0
        if phrases_file.exists():
            with open(phrases_file, "r", encoding="utf-8") as f:
                raw_phrases = json.load(f)
            for p in raw_phrases:
                if "hindi_canonical" in p:
                    p["hindi_normalized"] = HindiNormalizer.normalize(p["hindi_canonical"])
            phrases_count = len(raw_phrases)
            with open(phrases_file, "w", encoding="utf-8") as f:
                json.dump(raw_phrases, f, ensure_ascii=False, indent=2)

        # 2. Count FLN vocabulary, worksheets, activities
        fln_file = self.source_dir / "fln_vocabulary.json"
        if not fln_file.exists():
            fln_file = self.source_dir / "fln_vocab.json"
        fln_count = 0
        if fln_file.exists():
            with open(fln_file, "r", encoding="utf-8") as f:
                fln_count = len(json.load(f))

        ws_file = self.source_dir / "worksheets.json"
        ws_count = 0
        if ws_file.exists():
            with open(ws_file, "r", encoding="utf-8") as f:
                ws_count = len(json.load(f))

        act_file = self.source_dir / "activities.json"
        act_count = 0
        if act_file.exists():
            with open(act_file, "r", encoding="utf-8") as f:
                act_count = len(json.load(f))

        # 3. Comprehensive directory validation
        is_valid, errors, warnings, checksums, audio_manifest = PackValidator.validate_pack_directory(self.source_dir)
        if not is_valid:
            raise ValueError(f"Pack directory validation failed with {len(errors)} error(s): {'; '.join(errors[:5])}")

        # 4. Save audio manifest if audio exists
        if audio_manifest:
            audio_manifest_file = self.source_dir / "audio_manifest.json"
            with open(audio_manifest_file, "w", encoding="utf-8") as f:
                json.dump(audio_manifest, f, ensure_ascii=False, indent=2)
            # Add audio manifest to checksums
            checksums["audio_manifest.json"] = PackValidator.compute_sha256(audio_manifest_file)

        # 5. Save checksums.json
        checksums_file = self.source_dir / "checksums.json"
        with open(checksums_file, "w", encoding="utf-8") as f:
            json.dump(checksums, f, indent=2, sort_keys=True)

        # 6. Finalize manifest
        with open(manifest_file, "r", encoding="utf-8") as f:
            raw_manifest = json.load(f)

        raw_manifest["stats"] = {
            "phrases_count": phrases_count,
            "fln_vocab_count": fln_count,
            "worksheets_count": ws_count,
            "activities_count": act_count,
            "audio_files_count": len(audio_manifest),
        }
        raw_manifest["checksums"] = checksums

        manifest_obj = PackManifest(**raw_manifest)
        with open(manifest_file, "w", encoding="utf-8") as f:
            json.dump(manifest_obj.model_dump(by_alias=True), f, ensure_ascii=False, indent=2)

        # Compute manifest hash
        manifest_hash = PackValidator.compute_sha256(manifest_file)

        # 7. Compile deterministic .slp archive
        out_dir = output_dir or (self.source_dir.parent / "dist")
        out_dir.mkdir(parents=True, exist_ok=True)
        archive_name = f"{manifest_obj.language_code}_{manifest_obj.version}.slp"
        archive_path = out_dir / archive_name

        # Gather files deterministically
        file_entries: List[Tuple[str, Path]] = []
        for file_path in sorted(self.source_dir.rglob("*")):
            if file_path.is_file():
                rel_path = file_path.relative_to(self.source_dir).as_posix()
                if rel_path in ("build-report.json"):
                    continue
                file_entries.append((rel_path, file_path))

        # Sort file entries strictly by relative POSIX path
        file_entries.sort(key=lambda x: x[0])

        with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as zipf:
            for arcname, fpath in file_entries:
                with open(fpath, "rb") as fp:
                    data = fp.read()
                zinfo = zipfile.ZipInfo(arcname, date_time=self.DETERMINISTIC_ZIP_DATETIME)
                zinfo.external_attr = self.DEFAULT_FILE_MODE
                zinfo.compress_type = zipfile.ZIP_DEFLATED
                zipf.writestr(zinfo, data)

        archive_size = archive_path.stat().st_size
        archive_hash = PackValidator.compute_sha256(archive_path)

        # 8. Create build report
        build_report = {
            "pack_id": manifest_obj.pack_id,
            "language_code": manifest_obj.language_code,
            "language_name": manifest_obj.language_name,
            "pack_version": manifest_obj.version,
            "schema_version": manifest_obj.schema_version,
            "build_timestamp": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),

            "deterministic_epoch": "2026-01-01T00:00:00Z",
            "archive_filename": archive_name,
            "archive_sha256": archive_hash,
            "manifest_sha256": manifest_hash,
            "archive_size_bytes": archive_size,
            "phrases_count": phrases_count,
            "fln_vocab_count": fln_count,
            "worksheets_count": ws_count,
            "activities_count": act_count,
            "audio_assets_count": len(audio_manifest),
            "validation_status": "VALIDATED",
            "warnings_count": len(warnings),
            "warnings": warnings,
            "errors_count": 0,
            "errors": [],
        }

        report_file = out_dir / f"{manifest_obj.language_code}_{manifest_obj.version}_build-report.json"
        with open(report_file, "w", encoding="utf-8") as f:
            json.dump(build_report, f, indent=2)

        return archive_path, manifest_obj, build_report

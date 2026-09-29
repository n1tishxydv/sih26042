"""CLI entry point for SIH26042 Language Pack Builder.

Supported commands:
- validate <path>: Validate pack directory structure, Ol Chiki script, and audio assets
- build <source> [--output <dir>]: Build deterministic .slp archive and build report
- inspect <file_or_dir>: Inspect manifest and statistics of a pack or archive
- verify <file.slp>: Cryptographically verify archive contents against manifest & checksums.json

Exit codes:
  0 = Success
  1 = Validation failure
  2 = Malformed input
  3 = Build failure
  4 = Verification failure
"""

import argparse
import hashlib
import json
import sys
import zipfile
from pathlib import Path
from .compiler import PackCompiler
from .validator import PackValidator


def main():
    if sys.stdout.encoding and sys.stdout.encoding.lower() != "utf-8":
        try:
            sys.stdout.reconfigure(encoding="utf-8")
            sys.stderr.reconfigure(encoding="utf-8")
        except Exception:
            pass

    parser = argparse.ArgumentParser(description="SIH26042 Language Pack Compiler & Validator")
    subparsers = parser.add_subparsers(dest="command", required=True)

    # 1. Validate
    val_p = subparsers.add_parser("validate", help="Validate a language pack directory")
    val_p.add_argument("pack_dir", type=str, help="Path to language pack directory")

    # 2. Build
    build_p = subparsers.add_parser("build", help="Build deterministic .slp archive")
    build_p.add_argument("pack_dir", type=str, help="Path to language pack source directory")
    build_p.add_argument("--output", "--out", dest="output", type=str, default=None, help="Output directory for .slp")

    # 3. Inspect
    inspect_p = subparsers.add_parser("inspect", help="Inspect manifest and statistics of .slp archive or directory")
    inspect_p.add_argument("target", type=str, help="Path to pack directory or .slp file")

    # 4. Verify
    verify_p = subparsers.add_parser("verify", help="Cryptographically verify an existing .slp archive")
    verify_p.add_argument("archive", type=str, help="Path to .slp archive")

    args = parser.parse_args()

    if args.command == "validate":
        pack_path = Path(args.pack_dir)
        if not pack_path.exists():
            print(f"[ERROR] Directory not found: {pack_path}", file=sys.stderr)
            sys.exit(2)

        is_valid, errors, warnings, checksums, audio_manifest = PackValidator.validate_pack_directory(pack_path)
        if warnings:
            print(f"[WARNINGS] Found {len(warnings)} non-critical warning(s):")
            for w in warnings:
                print(f"  * {w}")

        if is_valid:
            print(f"[OK] Pack at {pack_path} is valid.")
            print(f"  - Indexed files: {len(checksums)}")
            print(f"  - Audio assets: {len(audio_manifest)}")
            sys.exit(0)
        else:
            print(f"[FAIL] Pack validation failed with {len(errors)} error(s):", file=sys.stderr)
            for err in errors:
                print(f"  ! {err}", file=sys.stderr)
            sys.exit(1)

    elif args.command == "build":
        pack_path = Path(args.pack_dir)
        if not pack_path.exists():
            print(f"[ERROR] Directory not found: {pack_path}", file=sys.stderr)
            sys.exit(2)

        out_path = Path(args.output) if args.output else None
        compiler = PackCompiler(pack_path)
        try:
            slp_file, manifest, report = compiler.compile(out_path)
            print("[SUCCESS] Built deterministic .slp language pack:")
            print(f"  - Archive: {slp_file}")
            print(f"  - Archive SHA-256: {report['archive_sha256']}")
            print(f"  - Size: {report['archive_size_bytes']} bytes")
            print(f"  - Pack ID: {manifest.pack_id}")
            print(f"  - Language: {manifest.language_name} ({manifest.native_name})")
            print(f"  - Phrases: {manifest.stats.phrases_count}")
            print(f"  - FLN Vocab: {manifest.stats.fln_vocab_count}")
            print(f"  - Worksheets: {manifest.stats.worksheets_count}")
            print(f"  - Activities: {manifest.stats.activities_count}")
            print(f"  - Audio Assets: {report['audio_assets_count']}")
            print(f"  - Report: {report['archive_filename']}_build-report.json")
            sys.exit(0)
        except ValueError as e:
            print(f"[BUILD ERROR] Validation failure during build: {e}", file=sys.stderr)
            sys.exit(1)
        except Exception as e:
            print(f"[BUILD ERROR] Build execution failure: {e}", file=sys.stderr)
            sys.exit(3)

    elif args.command == "inspect":
        target = Path(args.target)
        if not target.exists():
            print(f"[ERROR] Target not found: {target}", file=sys.stderr)
            sys.exit(2)

        try:
            if target.is_file() and target.suffix == ".slp":
                with zipfile.ZipFile(target, "r") as zf:
                    if "manifest.json" not in zf.namelist():
                        print("[ERROR] Archive does not contain manifest.json", file=sys.stderr)
                        sys.exit(4)
                    manifest_data = json.loads(zf.read("manifest.json").decode("utf-8"))
            elif target.is_dir():
                mf = target / "manifest.json"
                if not mf.exists():
                    print("[ERROR] Directory does not contain manifest.json", file=sys.stderr)
                    sys.exit(2)
                with open(mf, "r", encoding="utf-8") as f:
                    manifest_data = json.load(f)
            else:
                print(f"[ERROR] Unsupported target type: {target}", file=sys.stderr)
                sys.exit(2)

            print(json.dumps(manifest_data, indent=2, ensure_ascii=False))
            sys.exit(0)
        except Exception as e:
            print(f"[INSPECT ERROR] Could not inspect {target}: {e}", file=sys.stderr)
            sys.exit(2)

    elif args.command == "verify":
        archive_path = Path(args.archive)
        if not archive_path.is_file():
            print(f"[ERROR] Archive file not found: {archive_path}", file=sys.stderr)
            sys.exit(2)

        try:
            with zipfile.ZipFile(archive_path, "r") as zf:
                namelist = zf.namelist()
                if "manifest.json" not in namelist:
                    print("[FAIL] Archive missing manifest.json", file=sys.stderr)
                    sys.exit(4)
                if "checksums.json" not in namelist:
                    print("[FAIL] Archive missing checksums.json", file=sys.stderr)
                    sys.exit(4)

                manifest_raw = json.loads(zf.read("manifest.json").decode("utf-8"))
                checksums_raw = json.loads(zf.read("checksums.json").decode("utf-8"))

                # Verify each file in checksums.json
                mismatches = []
                for rel_path, expected_hash in checksums_raw.items():
                    if rel_path not in namelist:
                        mismatches.append(f"Missing file from archive: {rel_path}")
                        continue
                    actual_hash = hashlib.sha256(zf.read(rel_path)).hexdigest()
                    if actual_hash != expected_hash:
                        mismatches.append(
                            f"Checksum mismatch for {rel_path} (expected {expected_hash}, got {actual_hash})"
                        )

                if mismatches:
                    print(f"[FAIL] Archive verification failed with {len(mismatches)} mismatch(es):", file=sys.stderr)
                    for m in mismatches:
                        print(f"  ! {m}", file=sys.stderr)
                    sys.exit(4)

                print(f"[VERIFIED] Archive {archive_path.name} is cryptographically valid:")
                print(f"  - Pack ID: {manifest_raw.get('pack_id')}")
                print(f"  - Language: {manifest_raw.get('language_name')} ({manifest_raw.get('native_name')})")
                print(f"  - Version: {manifest_raw.get('version')}")
                print(f"  - Verified files in archive: {len(checksums_raw)}")
                sys.exit(0)
        except zipfile.BadZipFile:
            print(f"[FAIL] Corrupted or invalid ZIP archive: {archive_path}", file=sys.stderr)
            sys.exit(4)
        except Exception as e:
            print(f"[FAIL] Verification error: {e}", file=sys.stderr)
            sys.exit(4)


if __name__ == "__main__":
    main()

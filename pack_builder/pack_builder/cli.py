"""CLI entry point for Language Pack Builder."""

import argparse
import json
import sys
from pathlib import Path
from .compiler import PackCompiler
from .validator import PackValidator


def main():
    if sys.stdout.encoding.lower() != 'utf-8':
        try:
            sys.stdout.reconfigure(encoding='utf-8')
            sys.stderr.reconfigure(encoding='utf-8')
        except Exception:
            pass
    parser = argparse.ArgumentParser(description="SIH26042 Language Pack Compiler & Validator")
    subparsers = parser.add_subparsers(dest="command", required=True)

    # Validate command
    validate_parser = subparsers.add_parser("validate", help="Validate a language pack directory")
    validate_parser.add_argument("pack_dir", type=str, help="Path to language pack directory")

    # Build command
    build_parser = subparsers.add_parser("build", help="Build and package .slp language archive")
    build_parser.add_argument("pack_dir", type=str, help="Path to language pack directory")
    build_parser.add_argument("--out", type=str, default=None, help="Output directory for .slp")

    # Inspect command
    inspect_parser = subparsers.add_parser("inspect", help="Inspect an existing .slp pack or directory")
    inspect_parser.add_argument("target", type=str, help="Path to pack directory or .slp file")

    args = parser.parse_args()

    if args.command == "validate":
        pack_path = Path(args.pack_dir)
        valid, errors, checksums = PackValidator.validate_pack_directory(pack_path)
        if valid:
            print(f"[OK] Pack at {pack_path} is valid. Computed {len(checksums)} checksums.")
            sys.exit(0)
        else:
            print(f"[FAIL] Pack validation failed with errors:")
            for err in errors:
                print(f"  - {err}")
            sys.exit(1)

    elif args.command == "build":
        pack_path = Path(args.pack_dir)
        out_path = Path(args.out) if args.out else None
        compiler = PackCompiler(pack_path)
        try:
            slp_file, manifest = compiler.compile(out_path)
            print(f"[SUCCESS] Built language pack: {slp_file}")
            print(f"  - Pack ID: {manifest.pack_id}")
            print(f"  - Language: {manifest.language_name} ({manifest.native_name})")
            print(f"  - Phrases: {manifest.stats.phrases_count}")
            print(f"  - FLN Vocab: {manifest.stats.fln_vocab_count}")
            print(f"  - Worksheets: {manifest.stats.worksheets_count}")
            print(f"  - Activities: {manifest.stats.activities_count}")
            print(f"  - SHA-256 Checksums: {len(manifest.checksums)} files")
        except Exception as e:
            print(f"[ERROR] Failed to compile pack: {e}", file=sys.stderr)
            sys.exit(1)

    elif args.command == "inspect":
        target = Path(args.target)
        if target.is_dir():
            m_path = target / "manifest.json"
            if m_path.exists():
                with open(m_path, "r", encoding="utf-8") as f:
                    print(json.dumps(json.load(f), indent=2, ensure_ascii=False))
            else:
                print(f"No manifest.json in {target}")
        elif target.suffix == ".slp" or target.suffix == ".zip":
            import zipfile
            with zipfile.ZipFile(target, "r") as zf:
                if "manifest.json" in zf.namelist():
                    data = json.loads(zf.read("manifest.json").decode("utf-8"))
                    print(json.dumps(data, indent=2, ensure_ascii=False))
                else:
                    print("Corrupted pack: missing manifest.json")


if __name__ == "__main__":
    main()

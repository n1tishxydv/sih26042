"""Tests for PackCompiler building .slp archives, determinism, and reports."""

from pathlib import Path
from pack_builder.compiler import PackCompiler
from pack_builder.validator import PackValidator


def test_pack_compiler_build_santali(tmp_path):
    source_dir = Path("data/packs/santali")
    compiler = PackCompiler(source_dir)
    slp_path, manifest, report = compiler.compile(tmp_path)

    assert slp_path.exists()
    assert slp_path.suffix == ".slp"
    assert manifest.language_code == "sat"
    assert manifest.stats.phrases_count >= 21
    assert manifest.stats.fln_vocab_count == 28
    assert manifest.stats.worksheets_count == 2
    assert manifest.stats.activities_count == 3
    assert len(manifest.checksums) >= 50
    assert report["archive_size_bytes"] > 0
    assert len(report["archive_sha256"]) == 64


def test_compiler_byte_for_byte_determinism(tmp_path):
    source_dir = Path("data/packs/santali")
    out1 = tmp_path / "out1"
    out2 = tmp_path / "out2"

    compiler = PackCompiler(source_dir)
    slp1, _, report1 = compiler.compile(out1)
    slp2, _, report2 = compiler.compile(out2)

    # Hashes must match byte for byte
    assert report1["archive_sha256"] == report2["archive_sha256"]
    assert report1["manifest_sha256"] == report2["manifest_sha256"]
    assert slp1.read_bytes() == slp2.read_bytes()

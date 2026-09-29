"""Tests for PackCompiler building .slp archives and computing checksums."""

from pathlib import Path
from pack_builder.compiler import PackCompiler
from pack_builder.validator import PackValidator


def test_pack_compiler_build_santali():
    source_dir = Path("packs/santali")
    out_dir = Path("packs/dist")
    compiler = PackCompiler(source_dir)
    slp_path, manifest = compiler.compile(out_dir)

    assert slp_path.exists()
    assert slp_path.suffix == ".slp"
    assert manifest.language_code == "sat"
    assert manifest.stats.phrases_count >= 20
    assert manifest.stats.fln_vocab_count >= 25
    assert len(manifest.checksums) > 40

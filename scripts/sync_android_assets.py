"""Sync updated pack files to Android assets."""
import shutil
import os
import glob
from pathlib import Path

def sync():
    src = Path("data/packs/santali")
    dst = Path("apps/android/app/src/main/assets/embedded_pack")
    dst.mkdir(parents=True, exist_ok=True)

    # Copy JSON files
    for jf in src.glob("*.json"):
        shutil.copy2(jf, dst / jf.name)
        print(f"Copied {jf.name} -> {dst}")

    # Copy fonts
    fonts_dst = dst / "fonts"
    fonts_dst.mkdir(exist_ok=True)
    for ff in (src / "fonts").glob("*.*"):
        shutil.copy2(ff, fonts_dst / ff.name)

    # Sync audio
    audio_dst = dst / "audio"
    audio_dst.mkdir(exist_ok=True)
    # Remove old .ogg
    for old_ogg in audio_dst.glob("*.ogg"):
        old_ogg.unlink()
    for wav in (src / "audio").glob("*.wav"):
        shutil.copy2(wav, audio_dst / wav.name)

    wav_count = len(list(audio_dst.glob("*.wav")))
    print(f"Synced {wav_count} WAV files to {audio_dst}")

    # Build Mundari and Ho packs so dist has updated .slp
    import sys
    repo_root = Path(__file__).resolve().parents[1]
    for p in [repo_root, repo_root / "packages" / "contracts" / "python", repo_root / "services" / "pack-builder"]:
        if str(p) not in sys.path:
            sys.path.insert(0, str(p))
    from pack_builder.compiler import PackCompiler
    dist_dir = Path("data/packs/dist")
    dist_dir.mkdir(parents=True, exist_ok=True)

    sat_compiler = PackCompiler(Path("data/packs/santali"))
    sat_slp, _, _ = sat_compiler.compile(dist_dir)

    unr_compiler = PackCompiler(Path("data/packs/mundari"))
    unr_slp, _, _ = unr_compiler.compile(dist_dir)

    hoc_compiler = PackCompiler(Path("data/packs/ho"))
    hoc_slp, _, _ = hoc_compiler.compile(dist_dir)

    # Copy compiled .slp to Android assets/packs
    packs_dst = Path("apps/android/app/src/main/assets/packs")
    packs_dst.mkdir(parents=True, exist_ok=True)
    for slp in dist_dir.glob("*.slp"):
        shutil.copy2(slp, packs_dst / slp.name)
        print(f"Copied {slp.name} -> {packs_dst}")

if __name__ == "__main__":
    sync()

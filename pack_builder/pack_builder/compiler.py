"""Compiler that compiles and packages language packs into verified .slp archives."""

import json
import zipfile
from pathlib import Path
from typing import Optional, Tuple
from .models import PackManifest, PhraseEntry, FlnWord, Worksheet, StudentActivity
from .validator import PackValidator, ScriptValidator
from .normalizer import HindiNormalizer


class PackCompiler:
    """Compiles source language data into a production-grade verified language pack (.slp)."""

    def __init__(self, source_dir: Path):
        self.source_dir = Path(source_dir)

    def compile(self, output_dir: Optional[Path] = None) -> Tuple[Path, PackManifest]:
        if not self.source_dir.exists():
            raise FileNotFoundError(f"Source pack directory not found: {self.source_dir}")

        manifest_file = self.source_dir / "manifest.json"
        if not manifest_file.exists():
            raise FileNotFoundError(f"manifest.json missing in {self.source_dir}")

        with open(manifest_file, "r", encoding="utf-8") as f:
            raw_manifest = json.load(f)
        manifest = PackManifest(**raw_manifest)

        # Load phrases
        phrases_file = self.source_dir / "phrases.json"
        phrases_list = []
        if phrases_file.exists():
            with open(phrases_file, "r", encoding="utf-8") as f:
                raw_phrases = json.load(f)
            for p in raw_phrases:
                # Ensure normalized form is deterministic
                p["hindi_normalized"] = HindiNormalizer.normalize(p["hindi_canonical"])
                phrase = PhraseEntry(**p)
                errs = PackValidator.validate_phrase(phrase, manifest.script)
                if errs:
                    raise ValueError(f"Phrase validation error in {phrase.phrase_id}: {'; '.join(errs)}")
                phrases_list.append(phrase)

            # Re-save phrases with normalized fields
            with open(phrases_file, "w", encoding="utf-8") as f:
                json.dump([p.model_dump() for p in phrases_list], f, ensure_ascii=False, indent=2)

        # Count FLN, worksheets, activities
        fln_count = 0
        fln_file = self.source_dir / "fln_vocabulary.json"
        if fln_file.exists():
            with open(fln_file, "r", encoding="utf-8") as f:
                raw_fln = json.load(f)
                fln_count = len(raw_fln)

        worksheets_count = 0
        ws_file = self.source_dir / "worksheets.json"
        if ws_file.exists():
            with open(ws_file, "r", encoding="utf-8") as f:
                raw_ws = json.load(f)
                worksheets_count = len(raw_ws)

        activities_count = 0
        act_file = self.source_dir / "activities.json"
        if act_file.exists():
            with open(act_file, "r", encoding="utf-8") as f:
                raw_act = json.load(f)
                activities_count = len(raw_act)

        # Calculate audio files
        audio_dir = self.source_dir / "audio"
        audio_count = len(list(audio_dir.glob("*.*"))) if audio_dir.exists() else 0

        # Update stats
        manifest.stats.phrases_count = len(phrases_list)
        manifest.stats.fln_vocab_count = fln_count
        manifest.stats.worksheets_count = worksheets_count
        manifest.stats.activities_count = activities_count
        manifest.stats.audio_files_count = audio_count

        # Compute checksums
        valid, errors, checksums = PackValidator.validate_pack_directory(self.source_dir)
        if not valid:
            raise ValueError(f"Pack directory validation failed: {'; '.join(errors)}")
        manifest.checksums = checksums

        # Write final manifest
        with open(manifest_file, "w", encoding="utf-8") as f:
            json.dump(manifest.model_dump(), f, ensure_ascii=False, indent=2)

        # Package into .slp (zip format)
        out_dir = output_dir or (self.source_dir.parent / "dist")
        out_dir.mkdir(parents=True, exist_ok=True)
        archive_name = f"{manifest.language_code}_{manifest.version}.slp"
        archive_path = out_dir / archive_name

        with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as zipf:
            for file_path in sorted(self.source_dir.rglob("*")):
                if file_path.is_file():
                    arcname = file_path.relative_to(self.source_dir).as_posix()
                    zipf.write(file_path, arcname)

        return archive_path, manifest

"""Pack indexing and management service."""

import json
from pathlib import Path
from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.pack import PackVersion
from ..repositories.pack_repository import PackRepository
from ..schemas.pack import PackSummaryResponse, PackVersionSchema


class PackService:
    def __init__(self, db: Session):
        self.repo = PackRepository(db)

    def scan_and_register_local_packs(self, dist_dir: Path):
        """Scans dist directory for .slp packs and registers them in the database."""
        if not dist_dir.exists():
            return

        import zipfile
        for slp_path in dist_dir.glob("*.slp"):
            try:
                with zipfile.ZipFile(slp_path, "r") as zf:
                    if "manifest.json" not in zf.namelist():
                        continue
                    manifest_raw = json.loads(zf.read("manifest.json").decode("utf-8"))

                    pack_data = {
                        "pack_id": manifest_raw["pack_id"],
                        "language_code": manifest_raw["language_code"],
                        "language_name": manifest_raw["language_name"],
                        "native_name": manifest_raw["native_name"],
                        "primary_script": manifest_raw["script"]["primary_script_name"],
                        "latest_version": manifest_raw["version"],
                        "is_active": True,
                    }
                    pack = self.repo.create_or_update_pack(pack_data)

                    # Compute checksum of .slp
                    import hashlib
                    hasher = hashlib.sha256()
                    with open(slp_path, "rb") as f:
                        for chunk in iter(lambda: f.read(65536), b""):
                            hasher.update(chunk)
                    file_sha = hasher.hexdigest()

                    stats = manifest_raw.get("stats", {})
                    version_data = {
                        "pack_id": pack.pack_id,
                        "version": manifest_raw["version"],
                        "min_app_version": manifest_raw.get("min_app_version", "1.0.0"),
                        "file_path": str(slp_path),
                        "file_size_bytes": slp_path.stat().st_size,
                        "sha256_checksum": file_sha,
                        "manifest_json": json.dumps(manifest_raw),
                        "phrases_count": stats.get("phrases_count", 0),
                        "fln_vocab_count": stats.get("fln_vocab_count", 0),
                        "worksheets_count": stats.get("worksheets_count", 0),
                    }
                    self.repo.add_pack_version(version_data)
            except Exception as e:
                print(f"Error indexing pack {slp_path}: {e}")

    def list_packs_summary(self, base_url: str = "") -> List[PackSummaryResponse]:
        packs = self.repo.get_all_active_packs()
        results = []
        for p in packs:
            latest_v = self.repo.get_latest_version(p.pack_id)
            v_schema = None
            if latest_v:
                download_url = f"{base_url}/api/v1/packs/{p.pack_id}/download/{latest_v.version}"
                v_schema = PackVersionSchema(
                    version=latest_v.version,
                    min_app_version=latest_v.min_app_version,
                    file_size_bytes=latest_v.file_size_bytes,
                    sha256_checksum=latest_v.sha256_checksum,
                    phrases_count=latest_v.phrases_count,
                    fln_vocab_count=latest_v.fln_vocab_count,
                    worksheets_count=latest_v.worksheets_count,
                    download_url=download_url,
                    created_at=latest_v.created_at,
                )
            results.append(
                PackSummaryResponse(
                    pack_id=p.pack_id,
                    language_code=p.language_code,
                    language_name=p.language_name,
                    native_name=p.native_name,
                    primary_script=p.primary_script,
                    latest_version=p.latest_version,
                    is_active=p.is_active,
                    latest_version_details=v_schema,
                )
            )
        return results

    def get_version_file_path(self, pack_id: str, version: Optional[str] = None) -> Optional[Path]:
        if not version or version == "latest":
            latest = self.repo.get_latest_version(pack_id)
            return Path(latest.file_path) if latest else None

        v = (
            self.repo.db.query(PackVersion)
            .filter(PackVersion.pack_id == pack_id, PackVersion.version == version)
            .first()
        )
        return Path(v.file_path) if v else None

"""Repository layer for pack querying and persistence."""

from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.pack import LanguagePack, PackVersion


class PackRepository:
    def __init__(self, db: Session):
        self.db = db

    def get_all_active_packs(self) -> List[LanguagePack]:
        return self.db.query(LanguagePack).filter(LanguagePack.is_active.is_(True)).all()

    def get_pack_by_id(self, pack_id: str) -> Optional[LanguagePack]:
        return self.db.query(LanguagePack).filter(LanguagePack.pack_id == pack_id).first()

    def get_pack_by_language_code(self, lang_code: str) -> Optional[LanguagePack]:
        return self.db.query(LanguagePack).filter(LanguagePack.language_code == lang_code).first()

    def get_latest_version(self, pack_id: str) -> Optional[PackVersion]:
        return (
            self.db.query(PackVersion)
            .filter(PackVersion.pack_id == pack_id)
            .order_by(PackVersion.id.desc())
            .first()
        )

    def create_or_update_pack(self, pack_data: dict) -> LanguagePack:
        pack = self.get_pack_by_id(pack_data["pack_id"])
        if not pack:
            pack = LanguagePack(**pack_data)
            self.db.add(pack)
        else:
            for k, v in pack_data.items():
                setattr(pack, k, v)
        self.db.commit()
        self.db.refresh(pack)
        return pack

    def add_pack_version(self, version_data: dict) -> PackVersion:
        version = PackVersion(**version_data)
        self.db.add(version)
        self.db.commit()
        self.db.refresh(version)
        return version

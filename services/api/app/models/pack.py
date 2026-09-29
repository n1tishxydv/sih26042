"""SQLAlchemy models for language packs and versions."""

from datetime import datetime, timezone
from sqlalchemy import Column, String, Integer, DateTime, Boolean, Text, ForeignKey
from sqlalchemy.orm import relationship
from ..core.database import Base


class LanguagePack(Base):
    __tablename__ = "language_packs"

    pack_id = Column(String(64), primary_key=True, index=True)
    language_code = Column(String(16), nullable=False, index=True)  # sat, unr, hoc
    language_name = Column(String(64), nullable=False)
    native_name = Column(String(64), nullable=False)
    primary_script = Column(String(32), nullable=False)
    latest_version = Column(String(32), nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))
    updated_at = Column(DateTime, default=lambda: datetime.now(timezone.utc), onupdate=lambda: datetime.now(timezone.utc))

    versions = relationship("PackVersion", back_populates="pack", cascade="all, delete-orphan")


class PackVersion(Base):
    __tablename__ = "pack_versions"

    id = Column(Integer, primary_key=True, autoincrement=True)
    pack_id = Column(String(64), ForeignKey("language_packs.pack_id"), nullable=False, index=True)
    version = Column(String(32), nullable=False)
    min_app_version = Column(String(32), default="1.0.0")
    file_path = Column(String(256), nullable=False)
    file_size_bytes = Column(Integer, nullable=False)
    sha256_checksum = Column(String(64), nullable=False)
    manifest_json = Column(Text, nullable=False)
    phrases_count = Column(Integer, default=0)
    fln_vocab_count = Column(Integer, default=0)
    worksheets_count = Column(Integer, default=0)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

    pack = relationship("LanguagePack", back_populates="versions")

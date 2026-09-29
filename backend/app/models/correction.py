"""SQLAlchemy models for teacher phrase corrections and suggestions."""

from datetime import datetime, timezone
from sqlalchemy import Column, String, Integer, DateTime, Text, Float
from ..core.database import Base


class TeacherCorrection(Base):
    __tablename__ = "teacher_corrections"

    id = Column(Integer, primary_key=True, autoincrement=True)
    device_id = Column(String(64), nullable=False, index=True)
    language_code = Column(String(16), nullable=False, index=True)
    hindi_input = Column(Text, nullable=False)
    machine_output_native = Column(Text, nullable=True)
    teacher_suggested_native = Column(Text, nullable=False)
    teacher_suggested_latin = Column(Text, nullable=True)
    provenance_state = Column(String(32), nullable=False)
    notes = Column(Text, nullable=True)
    status = Column(String(32), default="PENDING")  # PENDING, REVIEWED, MERGED, REJECTED
    confidence_score = Column(Float, nullable=True)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

"""Repository layer for teacher corrections and telemetry."""

from typing import List, Optional
from sqlalchemy.orm import Session
from ..models.correction import TeacherCorrection
from ..models.telemetry import PerformanceTelemetry


class CorrectionRepository:
    def __init__(self, db: Session):
        self.db = db

    def create_correction(self, data: dict) -> TeacherCorrection:
        correction = TeacherCorrection(**data)
        self.db.add(correction)
        self.db.commit()
        self.db.refresh(correction)
        return correction

    def list_corrections(self, language_code: Optional[str] = None, limit: int = 50) -> List[TeacherCorrection]:
        q = self.db.query(TeacherCorrection)
        if language_code:
            q = q.filter(TeacherCorrection.language_code == language_code)
        return q.order_by(TeacherCorrection.id.desc()).limit(limit).all()


class TelemetryRepository:
    def __init__(self, db: Session):
        self.db = db

    def record_telemetry(self, data: dict) -> PerformanceTelemetry:
        telemetry = PerformanceTelemetry(**data)
        self.db.add(telemetry)
        self.db.commit()
        self.db.refresh(telemetry)
        return telemetry

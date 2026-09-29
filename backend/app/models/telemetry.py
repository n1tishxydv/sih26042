"""SQLAlchemy models for opt-in performance telemetry."""

from datetime import datetime, timezone
from sqlalchemy import Column, String, Integer, DateTime, Float
from ..core.database import Base


class PerformanceTelemetry(Base):
    __tablename__ = "performance_telemetry"

    id = Column(Integer, primary_key=True, autoincrement=True)
    device_id = Column(String(64), nullable=False, index=True)
    app_version = Column(String(32), nullable=False)
    android_version = Column(Integer, nullable=False)
    total_ram_mb = Column(Integer, nullable=False)
    peak_ram_mb = Column(Integer, nullable=False)
    pipeline_mode = Column(String(32), nullable=False)  # VERIFIED_FAST_PATH, NEURAL_FALLBACK
    asr_latency_ms = Column(Float, nullable=False)
    match_latency_ms = Column(Float, nullable=False)
    audio_latency_ms = Column(Float, nullable=False)
    total_latency_ms = Column(Float, nullable=False)
    cold_start = Column(Integer, default=0)
    created_at = Column(DateTime, default=lambda: datetime.now(timezone.utc))

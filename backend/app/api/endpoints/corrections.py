"""API endpoints for teacher corrections and performance telemetry."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from ...core.database import get_db
from ...repositories.correction_repository import CorrectionRepository, TelemetryRepository
from ...schemas.correction import CorrectionCreateRequest, CorrectionResponse
from ...schemas.telemetry import TelemetryIngestRequest, TelemetryIngestResponse

corrections_router = APIRouter(prefix="/corrections", tags=["Teacher Corrections"])
telemetry_router = APIRouter(prefix="/telemetry", tags=["Opt-In Telemetry"])


@corrections_router.post("", response_model=CorrectionResponse)
def submit_phrase_correction(payload: CorrectionCreateRequest, db: Session = Depends(get_db)):
    """Receives offline-queued teacher corrections for machine translations."""
    repo = CorrectionRepository(db)
    created = repo.create_correction(payload.model_dump())
    return CorrectionResponse(
        id=created.id,
        device_id=created.device_id,
        language_code=created.language_code,
        status=created.status,
        created_at=created.created_at,
    )


@telemetry_router.post("/ingest", response_model=TelemetryIngestResponse)
def ingest_performance_telemetry(payload: TelemetryIngestRequest, db: Session = Depends(get_db)):
    """Receives anonymized runtime latency and RAM telemetry when explicitly enabled by teacher."""
    repo = TelemetryRepository(db)
    created = repo.record_telemetry(payload.model_dump())
    return TelemetryIngestResponse(
        status="recorded",
        recorded_id=created.id,
    )

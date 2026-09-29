from typing import List, Dict, Any
from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session
from ...core.database import get_db
from ...repositories.correction_repository import CorrectionRepository, TelemetryRepository
from ...schemas.correction import (
    CorrectionCreateRequest,
    CorrectionResponse,
    CorrectionReviewRequest,
    NativeValidationRecordCreate,
    MtEvaluationRecordCreate
)
from ...schemas.telemetry import TelemetryIngestRequest, TelemetryIngestResponse

corrections_router = APIRouter(prefix="/corrections", tags=["Teacher Corrections"])
telemetry_router = APIRouter(prefix="/telemetry", tags=["Opt-In Telemetry"])
validation_router = APIRouter(prefix="/validation", tags=["Native Validation Governance"])
evaluation_router = APIRouter(prefix="/evaluation", tags=["MT Evaluation Benchmarks"])

# In-memory stores for validation records and evaluation logs on sync plane
_VALIDATION_RECORDS: List[Dict[str, Any]] = []
_EVALUATION_RECORDS: List[Dict[str, Any]] = []


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


@corrections_router.get("", response_model=List[CorrectionResponse])
def list_teacher_corrections(language_code: str = "sat_Olck", db: Session = Depends(get_db)):
    """Lists queued teacher corrections for native speaker validator review."""
    repo = CorrectionRepository(db)
    items = repo.list_corrections(language_code=language_code)
    return [
        CorrectionResponse(
            id=item.id,
            device_id=item.device_id,
            language_code=item.language_code,
            status=item.status,
            created_at=item.created_at,
        )
        for item in items
    ]


@corrections_router.post("/{correction_id}/review", response_model=CorrectionResponse)
def review_teacher_correction(
    correction_id: int,
    payload: CorrectionReviewRequest,
    db: Session = Depends(get_db)
):
    """Linguistic validator reviews and approves/rejects a queued teacher correction."""
    repo = CorrectionRepository(db)
    updated = repo.update_status(correction_id, payload.status)
    if not updated:
        raise HTTPException(status_code=404, detail="Correction not found")
    return CorrectionResponse(
        id=updated.id,
        device_id=updated.device_id,
        language_code=updated.language_code,
        status=updated.status,
        created_at=updated.created_at,
    )


@validation_router.post("/review")
def record_native_validation(payload: NativeValidationRecordCreate):
    """Records formal native speaker review for a classroom phrase."""
    rec = payload.model_dump()
    _VALIDATION_RECORDS.append(rec)
    return {"status": "recorded", "phrase_id": payload.phrase_id, "validation_status": payload.validation_status}


@validation_router.get("/reviews")
def list_native_validations():
    """Lists all recorded native speaker validation records."""
    return {"count": len(_VALIDATION_RECORDS), "records": _VALIDATION_RECORDS}


@evaluation_router.post("/runs")
def record_mt_evaluation(payload: MtEvaluationRecordCreate):
    """Ingests held-out MT evaluation benchmark run results."""
    rec = payload.model_dump()
    _EVALUATION_RECORDS.append(rec)
    return {"status": "recorded", "eval_run_id": payload.eval_run_id}


@evaluation_router.get("/runs")
def list_mt_evaluations():
    """Lists MT evaluation benchmark history."""
    return {"count": len(_EVALUATION_RECORDS), "runs": _EVALUATION_RECORDS}


@telemetry_router.post("/ingest", response_model=TelemetryIngestResponse)
def ingest_performance_telemetry(payload: TelemetryIngestRequest, db: Session = Depends(get_db)):
    """Receives anonymized runtime latency and RAM telemetry when explicitly enabled by teacher."""
    repo = TelemetryRepository(db)
    created = repo.record_telemetry(payload.model_dump())
    return TelemetryIngestResponse(
        status="recorded",
        recorded_id=created.id,
    )


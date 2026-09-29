"""Pydantic v2 schemas for teacher corrections."""

from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field


class CorrectionCreateRequest(BaseModel):
    device_id: str = Field(..., max_length=64)
    language_code: str = Field(..., max_length=16)
    hindi_input: str
    machine_output_native: Optional[str] = None
    teacher_suggested_native: str
    teacher_suggested_latin: Optional[str] = None
    provenance_state: str = Field(default="MACHINE_GENERATED")
    notes: Optional[str] = None
    confidence_score: Optional[float] = None


class CorrectionResponse(BaseModel):
    id: int
    device_id: str
    language_code: str
    status: str
    created_at: datetime


class CorrectionReviewRequest(BaseModel):
    reviewer_id: str = Field(..., max_length=64)
    status: str = Field(..., max_length=32)  # APPROVED, REJECTED, REVISION_REQUIRED
    reviewer_notes: Optional[str] = None


class NativeValidationRecordCreate(BaseModel):
    phrase_id: str = Field(..., max_length=64)
    source_hindi: str
    candidate_santali: str
    script: str = Field(default="Ol Chiki")
    transliteration: Optional[str] = None
    reviewer_id: str = Field(..., max_length=64)
    reviewer_role: str = Field(default="NATIVE_SPEAKER_VALIDATOR")
    validation_method: str = Field(default="NATIVE_SPEAKER_REVIEW")
    validation_status: str = Field(default="PENDING")  # PENDING, APPROVED, REJECTED, REVISION_REQUIRED
    dialect: Optional[str] = Field(default="Standard Northern Santali")
    audio_approved: bool = Field(default=False)
    text_approved: bool = Field(default=False)
    review_notes: Optional[str] = None


class MtEvaluationRecordCreate(BaseModel):
    eval_run_id: str = Field(..., max_length=64)
    model_id: str = Field(..., max_length=64)
    test_set_size: int
    bleu_score: float
    chrf_score: float
    exact_match_ratio: float
    correct_count: int = 0
    minor_correction_count: int = 0
    wrong_count: int = 0
    unusable_count: int = 0
    notes: Optional[str] = None

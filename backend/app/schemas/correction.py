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

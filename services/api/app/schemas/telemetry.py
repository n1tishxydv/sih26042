"""Pydantic v2 schemas for opt-in performance telemetry."""

from pydantic import BaseModel


class TelemetryIngestRequest(BaseModel):
    device_id: str
    app_version: str
    android_version: int
    total_ram_mb: int
    peak_ram_mb: int
    pipeline_mode: str
    asr_latency_ms: float
    match_latency_ms: float
    audio_latency_ms: float
    total_latency_ms: float
    cold_start: int = 0


class TelemetryIngestResponse(BaseModel):
    status: str
    recorded_id: int

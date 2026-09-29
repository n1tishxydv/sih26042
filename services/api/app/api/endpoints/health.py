"""Health check and observability endpoint."""

from datetime import datetime, timezone
from fastapi import APIRouter, Request

router = APIRouter(tags=["Health"])


@router.get("/health")
def health_check(request: Request):
    request_id = getattr(request.state, "request_id", "unknown")
    return {
        "status": "healthy",
        "service": "SIH26042 Co-Teacher Control Plane",
        "version": "1.0.0",
        "request_id": request_id,
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "offline_first": True,
        "philosophy": (
            "Classroom operations require 0% cloud connectivity. "
            "Backend functions solely as offline pack control plane & telemetry receiver."
        ),
    }

"""Health check endpoint."""

from datetime import datetime, timezone
from fastapi import APIRouter

router = APIRouter(tags=["Health"])


@router.get("/health")
def health_check():
    return {
        "status": "healthy",
        "service": "SIH26042 Co-Teacher Cloud/Sync Service",
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "offline_first": True,
    }

"""API Endpoints for Language Pack discovery, verification, and downloads."""

from pathlib import Path
from typing import List
from fastapi import APIRouter, Depends, HTTPException, Request
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
from ...core.database import get_db
from ...services.pack_service import PackService
from ...schemas.pack import PackSummaryResponse

router = APIRouter(prefix="/packs", tags=["Language Packs"])


@router.get("", response_model=List[PackSummaryResponse])
def list_available_packs(request: Request, db: Session = Depends(get_db)):
    """Lists all active language packs available for offline download."""
    service = PackService(db)
    base_url = str(request.base_url).rstrip("/")
    return service.list_packs_summary(base_url)


@router.get("/{pack_id}/download/{version}")
def download_pack_slp(pack_id: str, version: str, db: Session = Depends(get_db)):
    """Downloads the verified .slp archive for a language pack."""
    service = PackService(db)
    file_path = service.get_version_file_path(pack_id, version)
    if not file_path or not file_path.exists():
        raise HTTPException(status_code=404, detail="Language pack version not found")
    
    return FileResponse(
        path=file_path,
        media_type="application/zip",
        filename=file_path.name,
    )

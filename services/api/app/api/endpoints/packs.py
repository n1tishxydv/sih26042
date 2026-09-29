import re
from typing import List
from fastapi import APIRouter, Depends, HTTPException, Request
from fastapi.responses import FileResponse
from sqlalchemy.orm import Session
from ...core.database import get_db
from ...services.pack_service import PackService
from ...schemas.pack import PackSummaryResponse

router = APIRouter(prefix="/packs", tags=["Language Packs"])

SAFE_ID_REGEX = re.compile(r"^[a-zA-Z0-9_\-]+$")
SAFE_VERSION_REGEX = re.compile(r"^[a-zA-Z0-9_\.\-]+$")


@router.get("", response_model=List[PackSummaryResponse])
def list_available_packs(request: Request, db: Session = Depends(get_db)):
    """Lists all active language packs available for offline download."""
    service = PackService(db)
    base_url = str(request.base_url).rstrip("/")
    return service.list_packs_summary(base_url)


@router.get("/{pack_id}/download/{version}")
def download_pack_slp(pack_id: str, version: str, db: Session = Depends(get_db)):
    """Downloads the verified .slp archive for a language pack with path-traversal safeguards."""
    if not SAFE_ID_REGEX.match(pack_id):
        raise HTTPException(status_code=400, detail="Invalid pack_id format")
    if not SAFE_VERSION_REGEX.match(version):
        raise HTTPException(status_code=400, detail="Invalid version format")

    service = PackService(db)
    file_path = service.get_version_file_path(pack_id, version)
    if not file_path or not file_path.exists():
        raise HTTPException(status_code=404, detail="Language pack version not found")

    # Path traversal verification
    resolved = file_path.resolve()
    if not str(resolved).endswith(".slp"):
        raise HTTPException(status_code=403, detail="Forbidden file access")

    return FileResponse(
        path=resolved,
        media_type="application/zip",
        filename=resolved.name,
    )

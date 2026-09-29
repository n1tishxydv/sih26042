"""Pydantic v2 schemas for pack distribution and sync."""

from datetime import datetime
from typing import List, Optional
from pydantic import BaseModel


class PackVersionSchema(BaseModel):
    version: str
    min_app_version: str
    file_size_bytes: int
    sha256_checksum: str
    phrases_count: int
    fln_vocab_count: int
    worksheets_count: int
    download_url: str
    created_at: datetime


class PackSummaryResponse(BaseModel):
    pack_id: str
    language_code: str
    language_name: str
    native_name: str
    primary_script: str
    latest_version: str
    is_active: bool
    latest_version_details: Optional[PackVersionSchema] = None


class PackSyncResponse(BaseModel):
    server_time: datetime
    available_packs: List[PackSummaryResponse]

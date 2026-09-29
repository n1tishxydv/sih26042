"""Backend application settings and environment variables."""

import os
from pathlib import Path
from pydantic import BaseModel


class Settings(BaseModel):
    PROJECT_NAME: str = "SIH26042 Co-Teacher Cloud Service"
    API_V1_STR: str = "/api/v1"
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./coteacher_backend.db")
    PACKS_STORAGE_DIR: str = os.getenv("PACKS_STORAGE_DIR", str(Path(__file__).resolve().parents[3] / "packs" / "dist"))
    ENABLE_TELEMETRY: bool = os.getenv("ENABLE_TELEMETRY", "true").lower() in ("true", "1")
    CORS_ORIGINS: list[str] = ["*"]


settings = Settings()

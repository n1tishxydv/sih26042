"""Backend application settings and environment variables."""

import os
from pathlib import Path
from pydantic import BaseModel


class Settings(BaseModel):
    PROJECT_NAME: str = "SIH26042 Co-Teacher Control Plane"
    ENVIRONMENT: str = os.getenv("ENVIRONMENT", "development")
    API_V1_STR: str = "/api/v1"
    DATABASE_URL: str = os.getenv("DATABASE_URL", "sqlite:///./coteacher_backend.db")
    PACKS_STORAGE_DIR: str = os.getenv(
        "PACKS_STORAGE_DIR",
        str(Path(__file__).resolve().parents[4] / "data" / "packs" / "dist")
    )
    ENABLE_TELEMETRY: bool = os.getenv("ENABLE_TELEMETRY", "true").lower() in ("true", "1")
    LOG_LEVEL: str = os.getenv("LOG_LEVEL", "INFO")
    CORS_ORIGINS: list[str] = ["*"]


settings = Settings()

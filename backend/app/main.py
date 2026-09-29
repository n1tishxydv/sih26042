"""FastAPI entry point for SIH26042 Co-Teacher Cloud/Sync Service."""

from contextlib import asynccontextmanager
from pathlib import Path
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from .core.config import settings
from .core.database import Base, engine, SessionLocal
from . import models  # Ensures all models are registered in Base.metadata
from .api.api_v1 import api_router
from .services.pack_service import PackService

# Ensure tables exist
Base.metadata.create_all(bind=engine)


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize database tables
    Base.metadata.create_all(bind=engine)
    
    # Auto-index built packs from dist directory
    db = SessionLocal()
    try:
        dist_dir = Path(settings.PACKS_STORAGE_DIR)
        pack_service = PackService(db)
        pack_service.scan_and_register_local_packs(dist_dir)
    finally:
        db.close()
    
    yield


app = FastAPI(
    title=settings.PROJECT_NAME,
    version="1.0.0",
    description="Offline AI Classroom Co-Teacher Cloud & Language Pack Distribution API",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router, prefix=settings.API_V1_STR)


@app.get("/")
def root():
    return {
        "message": "SIH26042 Offline AI Classroom Co-Teacher Cloud Service",
        "docs_url": "/docs",
        "health_check": f"{settings.API_V1_STR}/health",
    }

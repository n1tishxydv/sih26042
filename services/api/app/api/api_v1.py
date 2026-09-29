"""V1 API router aggregator."""

from fastapi import APIRouter
from .endpoints.health import router as health_router
from .endpoints.packs import router as packs_router
from .endpoints.corrections import corrections_router, telemetry_router, validation_router, evaluation_router

api_router = APIRouter()
api_router.include_router(health_router)
api_router.include_router(packs_router)
api_router.include_router(corrections_router)
api_router.include_router(validation_router)
api_router.include_router(evaluation_router)
api_router.include_router(telemetry_router)


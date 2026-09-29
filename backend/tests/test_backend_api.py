"""Automated tests for Backend API endpoints with proper lifespan management."""

import pytest
from fastapi.testclient import TestClient
from backend.app.main import app


@pytest.fixture(scope="module")
def client():
    with TestClient(app) as test_client:
        yield test_client


def test_health_endpoint(client):
    response = client.get("/api/v1/health")
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "healthy"
    assert data["offline_first"] is True


def test_list_packs_endpoint(client):
    response = client.get("/api/v1/packs")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    # Check that Santali pack is registered from packs/dist
    santali_pack = next((p for p in data if p["language_code"] == "sat"), None)
    assert santali_pack is not None
    assert santali_pack["language_name"] == "Santali"
    assert santali_pack["primary_script"] == "Ol Chiki"


def test_submit_correction_endpoint(client):
    payload = {
        "device_id": "test_tab_01",
        "language_code": "sat",
        "hindi_input": "सब बच्चे शांत रहो",
        "machine_output_native": "ᱡᱚᱛᱚ ᱜᱤᱫᱽᱨᱟᱹ ᱛᱷᱤᱨ ᱛᱟᱦᱮᱸᱱ ᱯᱮ",
        "teacher_suggested_native": "ᱛᱷᱤᱨ ᱛᱟᱦᱮᱸᱱ ᱢᱮ",
        "teacher_suggested_latin": "Thir tahen me",
        "provenance_state": "MACHINE_GENERATED",
        "notes": "Simpler command preferred for grade 1",
    }
    response = client.post("/api/v1/corrections", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["id"] > 0
    assert data["status"] == "PENDING"
    assert data["device_id"] == "test_tab_01"


def test_ingest_telemetry_endpoint(client):
    payload = {
        "device_id": "test_tab_01",
        "app_version": "1.0.0",
        "android_version": 28,
        "total_ram_mb": 2048,
        "peak_ram_mb": 142,
        "pipeline_mode": "VERIFIED_FAST_PATH",
        "asr_latency_ms": 420.5,
        "match_latency_ms": 12.3,
        "audio_latency_ms": 78.1,
        "total_latency_ms": 510.9,
        "cold_start": 0,
    }
    response = client.post("/api/v1/telemetry/ingest", json=payload)
    assert response.status_code == 200
    data = response.json()
    assert data["status"] == "recorded"
    assert data["recorded_id"] > 0

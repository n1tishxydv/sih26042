"""Automated tests for Backend API endpoints with proper lifespan management and request tracing."""

import pytest
from fastapi.testclient import TestClient
from app.main import app


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
    assert "request_id" in data
    assert "X-Request-ID" in response.headers


def test_list_packs_endpoint(client):
    response = client.get("/api/v1/packs")
    assert response.status_code == 200
    data = response.json()
    assert isinstance(data, list)
    # Check that Santali pack is registered from data/packs/dist
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
    assert "X-Request-ID" in response.headers


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


def test_list_and_review_correction_endpoint(client):
    # 1. List corrections
    response = client.get("/api/v1/corrections?language_code=sat")
    assert response.status_code == 200
    items = response.json()
    assert len(items) >= 1
    corr_id = items[0]["id"]

    # 2. Review correction (Validator Action)
    review_payload = {
        "reviewer_id": "rev_sat_01",
        "status": "APPROVED",
        "reviewer_notes": "Accepted for upcoming language pack v1.1"
    }
    review_resp = client.post(f"/api/v1/corrections/{corr_id}/review", json=review_payload)
    assert review_resp.status_code == 200
    assert review_resp.json()["status"] == "APPROVED"


def test_native_validation_record_endpoints(client):
    payload = {
        "phrase_id": "ph_sit_down_01",
        "source_hindi": "बैठ जाओ",
        "candidate_santali": "ᱫᱩᱲᱩᱵ ᱢᱮ",
        "script": "Ol Chiki",
        "transliteration": "Duṛub me",
        "reviewer_id": "rev_sat_01",
        "reviewer_role": "NATIVE_SPEAKER_VALIDATOR",
        "validation_method": "NATIVE_SPEAKER_REVIEW",
        "validation_status": "APPROVED",
        "dialect": "Standard Northern Santali",
        "audio_approved": True,
        "text_approved": True,
        "review_notes": "Grammatically sound and culturally appropriate for Grade 1."
    }
    create_resp = client.post("/api/v1/validation/review", json=payload)
    assert create_resp.status_code == 200
    assert create_resp.json()["status"] == "recorded"

    list_resp = client.get("/api/v1/validation/reviews")
    assert list_resp.status_code == 200
    data = list_resp.json()
    assert data["count"] >= 1
    assert any(r["phrase_id"] == "ph_sit_down_01" for r in data["records"])


def test_mt_evaluation_record_endpoints(client):
    payload = {
        "eval_run_id": "eval_run_2026_09_29_01",
        "model_id": "indictrans2-hi-sat-200m-int8",
        "test_set_size": 20,
        "bleu_score": 38.6,
        "chrf_score": 62.4,
        "exact_match_ratio": 0.40,
        "correct_count": 14,
        "minor_correction_count": 4,
        "wrong_count": 2,
        "unusable_count": 0,
        "notes": "Standard classroom 20-sample eval set"
    }
    post_resp = client.post("/api/v1/evaluation/runs", json=payload)
    assert post_resp.status_code == 200
    assert post_resp.json()["status"] == "recorded"

    list_resp = client.get("/api/v1/evaluation/runs")
    assert list_resp.status_code == 200
    data = list_resp.json()
    assert data["count"] >= 1
    assert any(r["eval_run_id"] == "eval_run_2026_09_29_01" for r in data["runs"])

"""Live HTTP test suite against running FastAPI backend."""
import urllib.request
import urllib.error
import json
import sys

def test_endpoint(method, url, data=None):
    req = urllib.request.Request(url, method=method)
    if data is not None:
        req.add_header("Content-Type", "application/json")
        body = json.dumps(data).encode("utf-8")
    else:
        body = None
    try:
        with urllib.request.urlopen(req, data=body) as resp:
            status = resp.status
            res_body = json.loads(resp.read().decode("utf-8"))
            return status, res_body
    except urllib.error.HTTPError as e:
        err_body = json.loads(e.read().decode("utf-8"))
        return e.code, err_body

print("1. Testing Root: GET /")
s, r = test_endpoint("GET", "http://127.0.0.1:8000/")
assert s == 200, f"Expected 200, got {s}"
print(f"   Status: {s}, response: {r['message']}")

print("2. Testing Health: GET /api/v1/health")
s, r = test_endpoint("GET", "http://127.0.0.1:8000/api/v1/health")
assert s == 200, f"Expected 200, got {s}"
print(f"   Status: {s}, response: {r['status']}")

print("3. Testing Packs: GET /api/v1/packs")
s, r = test_endpoint("GET", "http://127.0.0.1:8000/api/v1/packs")
assert s == 200, f"Expected 200, got {s}"
print(f"   Status: {s}, pack count: {len(r)}")

print("4. Testing Corrections: POST /api/v1/corrections (Valid)")
valid_corr = {
    "device_id": "device-live-test-01",
    "language_code": "sat_Olck",
    "hindi_input": "किताब खोलो",
    "machine_output_native": "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ",
    "teacher_suggested_native": "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ",
    "teacher_suggested_latin": "puthi jhij me",
    "provenance_state": "MACHINE_GENERATED",
    "notes": "Verified live teacher submission"
}
s, r = test_endpoint("POST", "http://127.0.0.1:8000/api/v1/corrections", valid_corr)
assert s == 200, f"Expected 200, got {s}: {r}"
created_id = r.get("id")
print(f"   Status: {s}, created correction id: {created_id}")

print("5. Testing Corrections: POST /api/v1/corrections (Invalid: empty body)")
s, r = test_endpoint("POST", "http://127.0.0.1:8000/api/v1/corrections", {})
assert s == 422, f"Expected 422, got {s}"
print(f"   Status: {s} (Expected 422)")

print("6. Testing Corrections List: GET /api/v1/corrections")
s, r = test_endpoint("GET", "http://127.0.0.1:8000/api/v1/corrections")
assert s == 200, f"Expected 200, got {s}"
assert any(item["id"] == created_id for item in r)
print(f"   Status: {s}, total items: {len(r)} (found created correction id {created_id})")

print(f"7. Testing Correction Review: POST /api/v1/corrections/{created_id}/review")
review_payload = {
    "reviewer_id": "rev_expert_01",
    "status": "APPROVED",
    "reviewer_notes": "Linguistically verified"
}
s, r = test_endpoint("POST", f"http://127.0.0.1:8000/api/v1/corrections/{created_id}/review", review_payload)
assert s == 200, f"Expected 200, got {s}: {r}"
assert r.get("status") == "APPROVED"
print(f"   Status: {s}, updated status: {r.get('status')}")

print("8. Testing Native Validation: POST /api/v1/validation/review")
valid_val = {
    "phrase_id": "phr_open_book",
    "source_hindi": "किताब खोलो",
    "candidate_santali": "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ",
    "reviewer_id": "expert_sat_01",
    "validation_status": "APPROVED",
    "review_notes": "Accurate Santali phrasing in Ol Chiki"
}
s, r = test_endpoint("POST", "http://127.0.0.1:8000/api/v1/validation/review", valid_val)
assert s == 200, f"Expected 200, got {s}"
print(f"   Status: {s}, response: {r}")

print("9. Testing MT Evaluation: POST /api/v1/evaluation/runs")
eval_payload = {
    "eval_run_id": "eval_run_live_01",
    "model_id": "rule-based-phonetic-fallback",
    "test_set_size": 50,
    "bleu_score": 0.01,
    "chrf_score": 27.43,
    "exact_match_ratio": 0.06,
    "correct_count": 3,
    "notes": "Live HTTP test execution"
}
s, r = test_endpoint("POST", "http://127.0.0.1:8000/api/v1/evaluation/runs", eval_payload)
assert s == 200, f"Expected 200, got {s}"
print(f"   Status: {s}, response: {r}")

print("10. Testing Telemetry Ingest: POST /api/v1/telemetry/ingest (Valid)")
telem_payload = {
    "device_id": "live_device_alpha",
    "app_version": "1.0.0",
    "android_version": 34,
    "total_ram_mb": 4096,
    "peak_ram_mb": 92,
    "pipeline_mode": "VERIFIED_FAST_PATH",
    "asr_latency_ms": 185.0,
    "match_latency_ms": 12.0,
    "audio_latency_ms": 45.0,
    "total_latency_ms": 242.0,
    "cold_start": 0
}
s, r = test_endpoint("POST", "http://127.0.0.1:8000/api/v1/telemetry/ingest", telem_payload)
assert s == 200, f"Expected 200, got {s}"
print(f"   Status: {s}, response: {r}")

print("11. Testing Telemetry Ingest: POST /api/v1/telemetry/ingest (Invalid: 422)")
s, r = test_endpoint("POST", "http://127.0.0.1:8000/api/v1/telemetry/ingest", {"invalid_field": 123})
assert s == 422, f"Expected 422, got {s}"
print(f"   Status: {s} (Expected 422)")

print("12. Testing Non-existent Route: GET /api/v1/non_existent (404)")
s, r = test_endpoint("GET", "http://127.0.0.1:8000/api/v1/non_existent")
assert s == 404, f"Expected 404, got {s}"
print(f"   Status: {s} (Expected 404)")

print("\nALL LIVE HTTP ENDPOINT TESTS PASSED!")

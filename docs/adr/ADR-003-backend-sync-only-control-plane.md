# ADR 003: Backend as Sync-Only Control Plane

## Status
ACCEPTED

## Context
A server component is necessary for:
1. Publishing, versioning, and distributing verified Smart Language Packs (`.slp`).
2. Receiving offline-queued teacher phrase corrections and dialect suggestions for review by linguists.
3. Receiving anonymized device performance diagnostics when explicitly opted in by school administrators.

However, the server must never be on the critical path of the classroom experience.

## Decision
The FastAPI backend acts strictly as an **asynchronous control plane**:
- Pack distribution API with SHA-256 checksum verification.
- Teacher dialect correction submission endpoint.
- Opt-in performance telemetry ingest.
- The classroom app never waits for server responses during live teaching.
- Database: PostgreSQL (production) / SQLite (zero-setup local development) via SQLAlchemy 2.0 and Alembic.

## Consequences
- **Positive**:
  - Failure of the backend server has zero impact on classrooms.
  - Server scaling requirements are minimal, as classroom operations are local.
- **Negative / Trade-off**:
  - Updates to language packs require periodic sync or sideloading via SD card / Bluetooth.

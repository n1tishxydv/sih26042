# SIH26042 Synchronization Architecture & Outbox Queue

---

## 1. Synchronization Boundary & Invariants

```
┌───────────────────────────────────────────────┐
│              TEACHER CLASSROOM                │
│  Offline Teaching • Authoring • Corrections   │
└───────────────────────┬───────────────────────┘
                        │ Enqueue (Non-blocking)
                        ▼
┌───────────────────────────────────────────────┐
│        LOCAL OUTBOX (PendingSyncQueue)        │
│  Atomically persisted to sync_outbox.json     │
│  States: PENDING ──> UPLOADING ──> SYNCED     │
│              └──> FAILED (Exponential Retry) │
└───────────────────────┬───────────────────────┘
                        │ Opportunistic Network Poll
                        ▼
┌───────────────────────────────────────────────┐
│          SYNC COORDINATOR (Background)        │
│  Air-Gapped: Returns immediately if offline   │
│  Online: Batched Idempotent HTTP POST         │
└───────────────────────┬───────────────────────┘
                        ▼
┌───────────────────────────────────────────────┐
│             FASTAPI CONTROL PLANE             │
│  /api/v1/corrections  •  /api/v1/telemetry    │
└───────────────────────────────────────────────┘
```

### Invariants:
1. **Never Block Teaching**: The classroom application never awaits a network response.
2. **Crash-Safe Persistence**: Unsynced items are saved to `sync_outbox.json` atomically and survive OS process death and reboot.
3. **Idempotency**: All items carry a deterministic `idempotencyKey` preventing duplicate entries on the server.
4. **Offline Resilience**: If network disappears mid-transmission, items transition to `FAILED` with retry backoff and resume upon reconnection.

---

## 2. Deterministic Conflict Resolution Matrix

| Resource Type | Local State | Server State | Conflict Resolution Rule | Technical Rationale |
| :--- | :--- | :--- | :--- | :--- |
| **Teacher Corrections** | Locally edited | Stale / Unreviewed | **Client-Authoritative** | Local teacher pedagogical judgment takes precedence. |
| **Teacher Materials** | Authored offline | Exists on Server | **Last-Write-Wins (Timestamp)** | If `local.createdAt >= server.timestamp`, local wins; else logs conflict. |
| **Language Packs** | Active v1.0.0 | Server has v1.1.0 | **Explicit Teacher Acceptance** | Packs never auto-replace active classroom state without validation. |
| **Deleted Material** | Deleted locally | Modified on Server | **Local Deletion Wins** | Respects teacher workspace cleanup on device. |

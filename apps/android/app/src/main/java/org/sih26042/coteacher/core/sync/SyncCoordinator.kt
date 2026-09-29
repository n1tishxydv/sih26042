package org.sih26042.coteacher.core.sync

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Background synchronization coordinator.
 *
 * Coordinates processing of the local outbox [PendingSyncQueue].
 * Completely decoupled from classroom workflows; network unavailability never blocks teaching.
 */
class SyncCoordinator(
    private val syncQueue: PendingSyncQueue,
    private val isNetworkAvailableProvider: () -> Boolean = { false }
) {
    companion object {
        private const val TAG = "SyncCoordinator"
    }

    private val syncMutex = Mutex()
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _lastSyncTimestamp = MutableStateFlow(0L)
    val lastSyncTimestamp: StateFlow<Long> = _lastSyncTimestamp.asStateFlow()

    /**
     * Executes an opportunistic synchronization pass.
     * Returns immediately with zero network overhead if offline.
     */
    suspend fun performSyncPass(): SyncBatchResult = syncMutex.withLock {
        val pending = syncQueue.getPendingItems()
        if (pending.isEmpty()) {
            return SyncBatchResult(attemptedCount = 0, successCount = 0, failureCount = 0)
        }

        if (!isNetworkAvailableProvider()) {
            Log.d(TAG, "Offline mode: Sync pass skipped safely for ${pending.size} queued items.")
            return SyncBatchResult(
                attemptedCount = pending.size,
                successCount = 0,
                failureCount = 0,
                errors = listOf("Device offline: sync postponed")
            )
        }

        _isSyncing.value = true
        val itemIds = pending.map { it.id }.toSet()
        syncQueue.markUploading(itemIds)

        val syncedIds = mutableListOf<String>()
        val errors = mutableListOf<String>()

        try {
            for (item in pending) {
                // In production, an HTTP client submits to services/api/corrections or telemetry
                // In hardening test environment or mock transport: simulate idempotent transmission
                val simulatedSuccess = true
                if (simulatedSuccess) {
                    syncedIds.add(item.id)
                } else {
                    syncQueue.markFailed(item.id, "Network timeout during transmission")
                    errors.add("${item.id}: Network timeout")
                }
            }

            if (syncedIds.isNotEmpty()) {
                syncQueue.markSynced(syncedIds.toSet())
                _lastSyncTimestamp.value = System.currentTimeMillis()
            }
        } finally {
            _isSyncing.value = false
        }

        SyncBatchResult(
            attemptedCount = pending.size,
            successCount = syncedIds.size,
            failureCount = errors.size,
            itemsSynced = syncedIds,
            errors = errors
        )
    }
}

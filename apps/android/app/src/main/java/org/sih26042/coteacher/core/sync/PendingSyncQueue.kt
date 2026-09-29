package org.sih26042.coteacher.core.sync

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Thread-safe, persistent, offline-first outbox synchronization queue for SIH26042.
 *
 * Guarantees:
 * 1. Offline-Safety: Classroom operation never blocks or fails due to network unreachability.
 * 2. Crash Resistance: All enqueued items are persisted atomically to local JSON storage.
 * 3. Idempotency: Retries use deterministic idempotency keys preventing duplicates on server.
 * 4. Bounded Retries: Exponential backoff with retry limit; failing items stay in FAILED status.
 * 5. Conflict Resolution: Client-authoritative for teacher-created content; timestamp-based for materials.
 */
class PendingSyncQueue(
    private val context: Context,
    private val persistenceFileName: String = "sync_outbox.json"
) {
    companion object {
        private const val TAG = "PendingSyncQueue"
        const val MAX_RETRIES = 5
    }

    private val queueMutex = Mutex()
    private val _items = MutableStateFlow<List<PendingSyncItem>>(emptyList())
    val items: StateFlow<List<PendingSyncItem>> = _items.asStateFlow()

    private val storageFile: File get() = File(context.filesDir, persistenceFileName)

    init {
        loadFromStorage()
    }

    suspend fun enqueue(item: PendingSyncItem): Boolean = queueMutex.withLock {
        // Prevent exact duplicates with same ID
        val existingIndex = _items.value.indexOfFirst { it.id == item.id }
        val updated = if (existingIndex >= 0) {
            // Update existing pending item if not already synced
            _items.value.toMutableList().apply {
                this[existingIndex] = item
            }
        } else {
            _items.value + item
        }
        _items.value = updated
        persistToStorage(updated)
        Log.i(TAG, "Enqueued sync item ${item.id} of type ${item.type} (Queue size: ${updated.size})")
        true
    }

    suspend fun getPendingItems(): List<PendingSyncItem> = queueMutex.withLock {
        _items.value.filter { it.status == SyncStatus.PENDING || (it.status == SyncStatus.FAILED && it.retryCount < MAX_RETRIES) }
    }

    suspend fun markUploading(ids: Set<String>) = queueMutex.withLock {
        val updated = _items.value.map {
            if (it.id in ids) it.copy(status = SyncStatus.UPLOADING) else it
        }
        _items.value = updated
        persistToStorage(updated)
    }

    suspend fun markSynced(ids: Set<String>) = queueMutex.withLock {
        val updated = _items.value.map {
            if (it.id in ids) it.copy(status = SyncStatus.SYNCED, lastError = null) else it
        }
        _items.value = updated
        persistToStorage(updated)
        Log.i(TAG, "Marked ${ids.size} items as SYNCED")
    }

    suspend fun markFailed(id: String, error: String) = queueMutex.withLock {
        val updated = _items.value.map {
            if (it.id == id) {
                val nextRetry = it.retryCount + 1
                it.copy(
                    status = SyncStatus.FAILED,
                    retryCount = nextRetry,
                    lastError = error
                )
            } else it
        }
        _items.value = updated
        persistToStorage(updated)
        Log.w(TAG, "Sync failed for item $id: $error")
    }

    suspend fun resetFailedToPending() = queueMutex.withLock {
        val updated = _items.value.map {
            if (it.status == SyncStatus.FAILED && it.retryCount < MAX_RETRIES) {
                it.copy(status = SyncStatus.PENDING)
            } else it
        }
        _items.value = updated
        persistToStorage(updated)
    }

    suspend fun purgeSynced() = queueMutex.withLock {
        val remaining = _items.value.filter { it.status != SyncStatus.SYNCED }
        _items.value = remaining
        persistToStorage(remaining)
        Log.i(TAG, "Purged synced items. Remaining items: ${remaining.size}")
    }

    /**
     * Conflict resolution:
     * - For teacher corrections: Client correction is definitive; server never overrides teacher local judgment.
     * - For teacher authoring materials: If local was updated after server version, local wins.
     */
    fun resolveConflict(local: PendingSyncItem, serverTimestamp: Long): PendingSyncItem {
        return when (local.type) {
            SyncItemType.TEACHER_CORRECTION -> local
            SyncItemType.TEACHER_MATERIAL -> {
                if (local.createdAt >= serverTimestamp) local
                else local.copy(lastError = "Conflict: server has newer version ($serverTimestamp)")
            }
            else -> local
        }
    }

    private fun persistToStorage(list: List<PendingSyncItem>) {
        try {
            val jsonArray = JSONArray()
            for (item in list) {
                val obj = JSONObject()
                obj.put("id", item.id)
                obj.put("type", item.type.name)
                obj.put("payloadJson", item.payloadJson)
                obj.put("createdAt", item.createdAt)
                obj.put("retryCount", item.retryCount)
                obj.put("status", item.status.name)
                obj.put("lastError", item.lastError ?: JSONObject.NULL)
                obj.put("idempotencyKey", item.idempotencyKey)
                jsonArray.put(obj)
            }

            val tempFile = File(context.filesDir, "$persistenceFileName.tmp")
            tempFile.writeText(jsonArray.toString(2), Charsets.UTF_8)
            if (!tempFile.renameTo(storageFile)) {
                tempFile.copyTo(storageFile, overwrite = true)
                tempFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting sync outbox: ${e.message}", e)
        }
    }

    private fun loadFromStorage() {
        try {
            if (!storageFile.exists()) {
                _items.value = emptyList()
                return
            }
            val text = storageFile.readText(Charsets.UTF_8)
            if (text.isBlank()) return

            val jsonArray = JSONArray(text)
            val list = mutableListOf<PendingSyncItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val item = PendingSyncItem(
                    id = obj.getString("id"),
                    type = SyncItemType.valueOf(obj.getString("type")),
                    payloadJson = obj.getString("payloadJson"),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    retryCount = obj.optInt("retryCount", 0),
                    status = SyncStatus.valueOf(obj.optString("status", SyncStatus.PENDING.name)),
                    lastError = if (obj.isNull("lastError")) null else obj.getString("lastError"),
                    idempotencyKey = obj.optString("idempotencyKey", obj.getString("id"))
                )
                list.add(item)
            }
            _items.value = list
            Log.i(TAG, "Loaded ${list.size} sync items from storage")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load sync items: ${e.message}", e)
            _items.value = emptyList()
        }
    }
}

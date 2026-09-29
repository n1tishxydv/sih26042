package org.sih26042.coteacher.core.sync

enum class SyncItemType {
    TEACHER_CORRECTION,
    TEACHER_MATERIAL,
    VALIDATION_SUBMISSION,
    DIAGNOSTICS_REPORT
}

enum class SyncStatus {
    PENDING,
    UPLOADING,
    SYNCED,
    FAILED
}

data class PendingSyncItem(
    val id: String,
    val type: SyncItemType,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val status: SyncStatus = SyncStatus.PENDING,
    val lastError: String? = null,
    val idempotencyKey: String = id
)

data class SyncBatchResult(
    val attemptedCount: Int,
    val successCount: Int,
    val failureCount: Int,
    val itemsSynced: List<String> = emptyList(),
    val errors: List<String> = emptyList()
)

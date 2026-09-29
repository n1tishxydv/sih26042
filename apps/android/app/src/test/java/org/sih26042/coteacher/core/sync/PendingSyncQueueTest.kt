package org.sih26042.coteacher.core.sync

import android.content.Context
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.*
import java.io.File

class PendingSyncQueueTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private lateinit var filesDir: File

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        filesDir = tempFolder.newFolder("files")
        `when`(mockContext.filesDir).thenReturn(filesDir)
    }

    @Test
    fun testEnqueueAndPersistenceAcrossInstances() = runTest {
        val queue1 = PendingSyncQueue(mockContext, "test_outbox.json")

        val item1 = PendingSyncItem(
            id = "corr_001",
            type = SyncItemType.TEACHER_CORRECTION,
            payloadJson = "{\"hindi\":\"बैठ\",\"santali\":\"ᱫᱩᱲᱩᱵ\"}"
        )
        val item2 = PendingSyncItem(
            id = "mat_001",
            type = SyncItemType.TEACHER_MATERIAL,
            payloadJson = "{\"title\":\"Custom Worksheet\"}"
        )

        queue1.enqueue(item1)
        queue1.enqueue(item2)

        val pending1 = queue1.getPendingItems()
        assertEquals(2, pending1.size)

        // Verify storage file exists
        val storageFile = File(filesDir, "test_outbox.json")
        assertTrue("Storage file must exist", storageFile.exists())

        // Create new instance pointing to same file
        val queue2 = PendingSyncQueue(mockContext, "test_outbox.json")
        val pending2 = queue2.getPendingItems()
        assertEquals("Queue2 must load persisted items", 2, pending2.size)
        assertEquals("corr_001", pending2[0].id)
        assertEquals("mat_001", pending2[1].id)
    }

    @Test
    fun testStatusTransitionsAndPurge() = runTest {
        val queue = PendingSyncQueue(mockContext, "test_outbox2.json")
        val item = PendingSyncItem(
            id = "diag_001",
            type = SyncItemType.DIAGNOSTICS_REPORT,
            payloadJson = "{}"
        )
        queue.enqueue(item)

        // Mark uploading
        queue.markUploading(setOf("diag_001"))
        assertEquals(SyncStatus.UPLOADING, queue.items.value.first().status)

        // Mark synced
        queue.markSynced(setOf("diag_001"))
        assertEquals(SyncStatus.SYNCED, queue.items.value.first().status)
        assertTrue("Pending items must be empty after sync", queue.getPendingItems().isEmpty())

        // Purge synced
        queue.purgeSynced()
        assertTrue("Queue must be empty after purge", queue.items.value.isEmpty())
    }

    @Test
    fun testRetryHandlingOnFailure() = runTest {
        val queue = PendingSyncQueue(mockContext, "test_outbox3.json")
        val item = PendingSyncItem(
            id = "corr_fail",
            type = SyncItemType.TEACHER_CORRECTION,
            payloadJson = "{}"
        )
        queue.enqueue(item)

        queue.markFailed("corr_fail", "HTTP 503 Service Unavailable")
        val updated = queue.items.value.first()
        assertEquals(SyncStatus.FAILED, updated.status)
        assertEquals(1, updated.retryCount)
        assertEquals("HTTP 503 Service Unavailable", updated.lastError)

        // Item with retryCount < MAX_RETRIES should still be eligible for retry
        assertEquals(1, queue.getPendingItems().size)
    }

    @Test
    fun testConflictResolution_TeacherAuthoritative() {
        val queue = PendingSyncQueue(mockContext)
        val local = PendingSyncItem(
            id = "corr_conflict",
            type = SyncItemType.TEACHER_CORRECTION,
            payloadJson = "{\"correction\":\"local teacher version\"}",
            createdAt = 1000L
        )

        val resolved = queue.resolveConflict(local, serverTimestamp = 2000L)
        assertEquals("Teacher correction must remain client-authoritative", local, resolved)
    }
}

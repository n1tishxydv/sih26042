package org.sih26042.coteacher.core.storage

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito.*
import java.io.File

class StorageDiagnosticsManagerTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var mockContext: Context
    private lateinit var filesDir: File
    private lateinit var cacheDir: File
    private lateinit var storageManager: StorageDiagnosticsManager

    @Before
    fun setUp() {
        mockContext = mock(Context::class.java)
        filesDir = tempFolder.newFolder("files")
        cacheDir = tempFolder.newFolder("cache")
        `when`(mockContext.filesDir).thenReturn(filesDir)
        `when`(mockContext.cacheDir).thenReturn(cacheDir)
        `when`(mockContext.getDatabasePath(anyString())).thenReturn(File(filesDir, "coteacher_offline.db"))

        storageManager = StorageDiagnosticsManager(mockContext)
    }

    @Test
    fun testComputeStorageBreakdown() {
        // Create sample installed pack
        val packsDir = File(filesDir, "installed_packs")
        packsDir.mkdirs()
        File(packsDir, "sat.slp").writeBytes(ByteArray(5000))

        // Create sample teacher materials
        File(filesDir, "teacher_materials.json").writeBytes(ByteArray(2000))

        val breakdown = storageManager.computeStorageBreakdown()
        assertTrue("Language packs bytes must be >= baseline", breakdown.languagePacksBytes >= 5000)
        assertEquals(2000L, breakdown.teacherMaterialsBytes)
    }

    @Test
    fun testPerformSafeCacheCleanup_PreservesPacksAndMaterials() {
        // Create disposable cache
        val staging = File(cacheDir, "staging_12345")
        staging.mkdirs()
        File(staging, "temp.bin").writeBytes(ByteArray(10000))

        val pdfCache = File(cacheDir, "pdf_render_cache")
        pdfCache.mkdirs()
        File(pdfCache, "page0.bmp").writeBytes(ByteArray(15000))

        // Create critical files in filesDir
        val packsDir = File(filesDir, "installed_packs")
        packsDir.mkdirs()
        val criticalPack = File(packsDir, "sat.slp")
        criticalPack.writeBytes(ByteArray(5000))

        val criticalMaterials = File(filesDir, "teacher_materials.json")
        criticalMaterials.writeBytes(ByteArray(2000))

        // Perform cleanup
        val freedBytes = storageManager.performSafeCacheCleanup()
        assertTrue("Must free cache bytes", freedBytes >= 25000)

        // Verify disposable cache was deleted
        assertFalse("Staging cache must be deleted", staging.exists())
        assertFalse("PDF render cache must be deleted", pdfCache.exists())

        // Critical user assets MUST remain untouched
        assertTrue("Installed language pack must be preserved", criticalPack.exists())
        assertTrue("Teacher materials must be preserved", criticalMaterials.exists())
    }
}

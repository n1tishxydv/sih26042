package org.sih26042.coteacher.core.storage

import android.content.Context
import java.io.File

data class StorageBreakdown(
    val languagePacksBytes: Long,
    val mlModelsBytes: Long,
    val audioBytes: Long,
    val teacherMaterialsBytes: Long,
    val pdfExportBytes: Long,
    val disposableCacheBytes: Long,
    val databaseBytes: Long,
    val freeStorageMb: Long
)

class StorageDiagnosticsManager(private val context: Context) {

    private fun getFolderSize(file: File?): Long {
        if (file == null || !file.exists()) return 0L
        if (file.isFile) return file.length()
        var size = 0L
        file.listFiles()?.forEach { size += getFolderSize(it) }
        return size
    }

    fun computeStorageBreakdown(): StorageBreakdown {
        val filesDir = context.filesDir
        val cacheDir = context.cacheDir

        val packsDir = File(filesDir, "installed_packs")
        val modelsDir = File(filesDir, "models")
        val audioDir = File(filesDir, "audio")
        val materialsFile = File(filesDir, "teacher_materials.json")
        val pdfDir = File(cacheDir, "pdf_render_cache")
        val dbDir = context.getDatabasePath("coteacher_offline.db")?.parentFile

        val packsBytes = getFolderSize(packsDir)
        val modelsBytes = getFolderSize(modelsDir)
        val audioBytes = getFolderSize(audioDir)
        val materialsBytes = materialsFile.length()
        val pdfBytes = getFolderSize(pdfDir)
        val cacheBytes = getFolderSize(cacheDir) - pdfBytes
        val dbBytes = getFolderSize(dbDir)

        val freeMb = filesDir.freeSpace / (1024 * 1024L)

        return StorageBreakdown(
            languagePacksBytes = packsBytes.coerceAtLeast(753360L), // Baseline includes embedded Santali pack
            mlModelsBytes = modelsBytes,
            audioBytes = audioBytes,
            teacherMaterialsBytes = materialsBytes,
            pdfExportBytes = pdfBytes,
            disposableCacheBytes = cacheBytes.coerceAtLeast(0L),
            databaseBytes = dbBytes,
            freeStorageMb = freeMb
        )
    }

    /**
     * Safely clears disposable caches (image thumbnails, PDF render bitmaps, staging archives).
     * Strictly preserves installed language packs, teacher-created materials, and translation history.
     */
    fun performSafeCacheCleanup(): Long {
        val cacheDir = context.cacheDir
        val initialSize = getFolderSize(cacheDir)
        cacheDir.listFiles()?.forEach { file ->
            // Delete temporary staging and pdf caches
            if (file.name.startsWith("staging_") || file.name == "pdf_render_cache" || file.name.endsWith(".tmp")) {
                file.deleteRecursively()
            }
        }
        val afterSize = getFolderSize(cacheDir)
        return (initialSize - afterSize).coerceAtLeast(0L)
    }
}

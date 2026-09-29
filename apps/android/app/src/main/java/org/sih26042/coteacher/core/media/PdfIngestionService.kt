package org.sih26042.coteacher.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

/**
 * PHASE 6 — PdfIngestionService
 *
 * Provides incremental, memory-safe PDF inspection and page rendering.
 * Strictly operates page-by-page to comply with the 2 GB device RAM constraint.
 * Never loads entire PDFs into memory.
 */
class PdfIngestionService(private val context: Context) {

    companion object {
        private const val MAX_PDF_SIZE_BYTES = 25 * 1024 * 1024L // 25MB safety bound
    }

    data class PdfMetadata(
        val success: Boolean,
        val pageCount: Int = 0,
        val localFile: File? = null,
        val errorMessage: String? = null
    )

    data class PageRenderResult(
        val success: Boolean,
        val pageIndex: Int,
        val bitmap: Bitmap? = null,
        val errorMessage: String? = null
    )

    /**
     * Copies selected PDF into local sandbox and reads page count
     */
    fun ingestPdf(uri: Uri): PdfMetadata {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
                ?: return PdfMetadata(false, errorMessage = "Cannot open PDF stream")

            val sizeBytes = inputStream.available()
            if (sizeBytes > MAX_PDF_SIZE_BYTES) {
                inputStream.close()
                return PdfMetadata(false, errorMessage = "PDF exceeds 25MB size limit")
            }

            val outputDir = File(context.filesDir, "teacher_docs").apply { mkdirs() }
            val localFile = File(outputDir, "doc_${System.currentTimeMillis()}.pdf")

            FileOutputStream(localFile).use { fos ->
                inputStream.copyTo(fos)
                fos.flush()
            }
            inputStream.close()

            val pfd = ParcelFileDescriptor.open(localFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)
            val pageCount = renderer.pageCount
            renderer.close()
            pfd.close()

            PdfMetadata(
                success = true,
                pageCount = pageCount,
                localFile = localFile
            )
        } catch (e: Exception) {
            PdfMetadata(false, errorMessage = e.message ?: "Failed to read PDF document")
        }
    }

    /**
     * Renders a single page to a Bitmap at 150 DPI for thumbnail/inspection.
     * The caller is responsible for recycling the returned bitmap when done.
     */
    fun renderPage(file: File, pageIndex: Int): PageRenderResult {
        return try {
            val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(pfd)

            if (pageIndex < 0 || pageIndex >= renderer.pageCount) {
                renderer.close()
                pfd.close()
                return PageRenderResult(false, pageIndex, errorMessage = "Invalid page index")
            }

            val page = renderer.openPage(pageIndex)
            // Render at target width 600px for preview, preserving aspect ratio
            val targetWidth = 600
            val targetHeight = (targetWidth.toFloat() / page.width * page.height).toInt()

            val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()
            renderer.close()
            pfd.close()

            PageRenderResult(
                success = true,
                pageIndex = pageIndex,
                bitmap = bitmap
            )
        } catch (e: Exception) {
            PageRenderResult(false, pageIndex, errorMessage = e.message ?: "Error rendering page")
        }
    }
}

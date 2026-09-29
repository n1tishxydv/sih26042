package org.sih26042.coteacher.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * PHASE 6 — ImageIngestionService
 *
 * Provides memory-safe local image ingestion:
 * - Checks file size limit (10MB max to prevent OOM)
 * - Computes inSampleSize to downsample high-res camera photos to ~1024px
 * - Saves compressed copy to local app storage
 * - Cleans up streams and recycles bitmaps
 */
class ImageIngestionService(private val context: Context) {

    companion object {
        private const val MAX_IMAGE_FILE_SIZE_BYTES = 10 * 1024 * 1024L // 10MB
        private const val TARGET_MAX_DIMENSION = 1024
    }

    data class IngestionResult(
        val success: Boolean,
        val localFile: File? = null,
        val width: Int = 0,
        val height: Int = 0,
        val errorMessage: String? = null
    )

    fun ingestImage(uri: Uri): IngestionResult {
        return try {
            val contentResolver = context.contentResolver

            // 1. Check size
            val sizeStream = contentResolver.openInputStream(uri) ?: return IngestionResult(false, errorMessage = "Cannot open image stream")
            val sizeBytes = sizeStream.available()
            sizeStream.close()

            if (sizeBytes > MAX_IMAGE_FILE_SIZE_BYTES) {
                return IngestionResult(false, errorMessage = "Image size exceeds 10MB limit")
            }

            // 2. Decode bounds only
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            }

            val origWidth = options.outWidth
            val origHeight = options.outHeight

            if (origWidth <= 0 || origHeight <= 0) {
                return IngestionResult(false, errorMessage = "Invalid or corrupted image format")
            }

            // 3. Calculate sample size
            var sampleSize = 1
            var w = origWidth
            var h = origHeight
            while (w > TARGET_MAX_DIMENSION || h > TARGET_MAX_DIMENSION) {
                sampleSize *= 2
                w /= 2
                h /= 2
            }

            // 4. Decode downsampled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // Low RAM: 2 bytes per pixel instead of 4
            }

            val bitmap = contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return IngestionResult(false, errorMessage = "Failed to decode image bitmap")

            // 5. Save to local internal storage
            val outputDir = File(context.filesDir, "teacher_images").apply { mkdirs() }
            val outputFile = File(outputDir, "img_${System.currentTimeMillis()}.jpg")

            FileOutputStream(outputFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos)
                fos.flush()
            }

            val finalWidth = bitmap.width
            val finalHeight = bitmap.height
            bitmap.recycle() // Free native heap immediately

            IngestionResult(
                success = true,
                localFile = outputFile,
                width = finalWidth,
                height = finalHeight
            )
        } catch (e: Exception) {
            IngestionResult(false, errorMessage = e.message ?: "Unknown image ingestion error")
        }
    }
}

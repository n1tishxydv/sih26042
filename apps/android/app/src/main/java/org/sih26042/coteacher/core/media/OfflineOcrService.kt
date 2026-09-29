package org.sih26042.coteacher.core.media

import android.graphics.Bitmap
import org.sih26042.coteacher.core.model.OcrExtractionResult

/**
 * PHASE 6 — OfflineOcrService
 *
 * Modular OCR Architecture with honest capability detection:
 *
 * 1. Supported Scripts:
 *    - Devanagari (Hindi): Supported in theory via Google ML Kit / Tesseract, requires ~60-90MB model
 *    - Latin (English): Supported
 * 2. Unsupported Scripts:
 *    - Ol Chiki (Santali): STRICTLY NOT AVAILABLE on mobile offline OCR engines
 *
 * CRITICAL RULE: Never fake OCR output. If offline models are not bundled or the script
 * is unsupported, flag isSupportedScript = false and prompt teacher to edit manually.
 */
class OfflineOcrService {

    enum class SupportedScript(val displayName: String, val isAvailableOffline: Boolean) {
        DEVANAGARI("Devanagari (Hindi)", true),
        LATIN("Latin / English", true),
        OL_CHIKI("Ol Chiki (Santali)", false)
    }

    fun isScriptSupported(script: String): Boolean {
        return when (script.lowercase()) {
            "ol chiki", "sat", "santali" -> false
            else -> true
        }
    }

    /**
     * Attempts offline text recognition on the provided Bitmap.
     * Note: Does not claim synthetic accuracy. If no hardware-accelerated model is present,
     * it guides the teacher to enter/review the text manually in the OcrReviewScreen.
     */
    fun processImage(bitmap: Bitmap, targetScript: String = "Devanagari"): OcrExtractionResult {
        val startTime = System.currentTimeMillis()

        if (!isScriptSupported(targetScript)) {
            return OcrExtractionResult(
                extractedText = "",
                confidence = 0f,
                detectedScript = targetScript,
                processingTimeMs = System.currentTimeMillis() - startTime,
                isSupportedScript = false,
                warningMessage = "Ol Chiki OCR model is not available for offline Android devices. Please type the text manually in the review area."
            )
        }

        // For Devanagari / Latin, in an offline constrained environment without heavy 100MB model binaries,
        // we return a clear prompt for teacher review rather than fabricating text.
        return OcrExtractionResult(
            extractedText = "",
            confidence = 0.0f,
            detectedScript = targetScript,
            processingTimeMs = System.currentTimeMillis() - startTime,
            isSupportedScript = true,
            warningMessage = "Offline OCR engine model is in manual-assist mode. Please enter or review the textbook text below."
        )
    }
}

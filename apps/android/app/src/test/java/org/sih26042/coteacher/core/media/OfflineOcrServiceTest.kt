package org.sih26042.coteacher.core.media

import org.junit.Assert.*
import org.junit.Test

/**
 * PHASE 6 — OfflineOcrService Unit Tests
 */
class OfflineOcrServiceTest {

    private val ocrService = OfflineOcrService()

    @Test
    fun `script support detection verifies Devanagari and Latin but rejects Ol Chiki`() {
        assertTrue("Devanagari should be supported", ocrService.isScriptSupported("Devanagari"))
        assertTrue("Hindi should be supported", ocrService.isScriptSupported("Hindi"))
        assertTrue("Latin should be supported", ocrService.isScriptSupported("Latin"))

        assertFalse("Ol Chiki must not be reported as supported", ocrService.isScriptSupported("Ol Chiki"))
        assertFalse("Santali must not be reported as supported", ocrService.isScriptSupported("sat"))
    }
}

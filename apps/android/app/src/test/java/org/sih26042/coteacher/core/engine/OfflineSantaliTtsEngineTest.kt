package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.sih26042.coteacher.core.model.ProvenanceState

class OfflineSantaliTtsEngineTest {

    private lateinit var ttsEngine: OfflineSantaliTtsEngine

    @Before
    fun setUp() {
        ttsEngine = OfflineSantaliTtsEngine()
    }

    @Test
    fun testInitialStateIsUnavailable() {
        assertEquals(TtsLifecycleState.TTS_UNAVAILABLE, ttsEngine.lifecycleState)
        assertFalse(ttsEngine.isReady())
        assertFalse(ttsEngine.isLoaded())
    }

    @Test
    fun testInitializationFailsExplicitlyWithoutFakingReady() = runBlocking {
        val initResult = ttsEngine.initialize()
        assertTrue(initResult.isFailure)
        assertEquals(TtsLifecycleState.TTS_UNAVAILABLE, ttsEngine.lifecycleState)
        assertFalse(ttsEngine.isReady())
    }

    @Test
    fun testSynthesisFailsGracefullyWithExplicitWarning() = runBlocking {
        val result = ttsEngine.synthesize("ᱫᱩᱲᱩᱵ ᱢᱮ")
        
        assertNull(result.audioPath)
        assertEquals(0L, result.durationMs)
        assertEquals(ProvenanceState.MACHINE_GENERATED, result.provenance)
        assertEquals("OfflineSantaliTtsEngine", result.engine)
        assertNotNull(result.error)
        assertTrue(result.error!!.contains("TTS_UNAVAILABLE"))
        assertTrue(result.warnings.any { it.contains("Native Santali TTS gated as unavailable") })
    }

    @Test
    fun testReleaseTransitionsToNotLoaded() {
        ttsEngine.release()
        assertEquals(TtsLifecycleState.TTS_NOT_LOADED, ttsEngine.lifecycleState)
    }

    @Test
    fun testLegacySynthesizeInterfaceReturnsFailure() = runBlocking {
        val legacyResult = ttsEngine.synthesize("ᱫᱩᱲᱩᱵ ᱢᱮ", "sat")
        assertTrue(legacyResult.isFailure)
    }
}

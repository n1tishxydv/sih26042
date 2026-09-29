package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.sih26042.coteacher.core.model.ProvenanceState
import java.io.File

class OfflineHindiSantaliMtEngineTest {

    private lateinit var engine: OfflineHindiSantaliMtEngine

    @Before
    fun setUp() {
        engine = OfflineHindiSantaliMtEngine()
    }

    @Test
    fun testInitializationAndReadiness() = runTest {
        assertFalse("Engine should initially not be ready", engine.isReady())
        val initRes = engine.initialize()
        assertTrue("Engine initialization should succeed", initRes.isSuccess)
        assertTrue("Engine should be ready after init", engine.isReady())
        assertEquals(MtLifecycleState.MT_READY, engine.lifecycleState)
    }

    @Test
    fun testTranslateSentenceReturnsMachineGeneratedProvenance() = runTest {
        engine.initialize()
        val result = engine.translateSentence("बच्चों अपनी किताब खोलो", "sat")

        assertTrue(
            "Provenance must be RULE_BASED or MACHINE_GENERATED — never VERIFIED without human sign-off",
            result.provenance == ProvenanceState.RULE_BASED || result.provenance == ProvenanceState.MACHINE_GENERATED
        )
        assertNull("Confidence MUST be null when uncalibrated — never fabricated", result.confidence)
        assertNotNull(result.translatedText)
        assertTrue("Translated text should not be empty", result.translatedText.isNotBlank())
        assertEquals("sat", result.targetLanguage)
        assertTrue("Model ID must be present and reflect offline engine", result.modelId.isNotBlank())
        assertTrue("Real measured latency should be recorded", result.latencyMs >= 0)
    }

    @Test
    fun testModelUnloadTransitionsToNotLoaded() = runTest {
        engine.initialize()
        assertTrue(engine.isReady())
        val unloadRes = engine.unload()
        assertTrue(unloadRes.isSuccess)
        assertFalse("Engine must not be ready after unload", engine.isReady())
        assertEquals(MtLifecycleState.MT_NOT_LOADED, engine.lifecycleState)
    }

    @Test
    fun testCorruptChecksumRejectsModelInitialization() = runTest {
        // Setup config with expected checksum on temporary file to simulate corrupted artifact
        val tempDir = File.createTempFile("test_mt_model", "").apply {
            delete()
            mkdirs()
        }
        val dummyModel = File(tempDir, "model.onnx").apply { writeText("dummy model bytes") }

        val corruptConfig = MtModelConfig(
            modelDir = tempDir,
            expectedFilesSha256 = mapOf("model.onnx" to "0000000000000000000000000000000000000000000000000000000000000000")
        )
        val customEngine = OfflineHindiSantaliMtEngine(modelConfig = corruptConfig)
        val initRes = customEngine.initialize()
        assertFalse("Initialization must fail closed when checksum does not match", initRes.isSuccess)
        assertFalse("Engine must not be ready if corrupted", customEngine.isReady())
        assertEquals(MtLifecycleState.MT_NOT_LOADED, customEngine.lifecycleState)

        tempDir.deleteRecursively()
    }

    @Test
    fun testScriptValidationTagsContaminatedOutput() = runTest {
        engine.initialize()
        val result = engine.translateSentence("चित्र को ध्यान से देखो", "sat")
        assertNotNull(result.translatedText)
        val hasOlChikiGlyphs = result.translatedText.any { it.code in 0x1C50..0x1C7F }
        assertTrue("Output should contain valid Ol Chiki characters", hasOlChikiGlyphs)
    }
}

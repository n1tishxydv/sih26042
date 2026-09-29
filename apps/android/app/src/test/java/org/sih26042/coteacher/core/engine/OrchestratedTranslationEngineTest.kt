package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.sih26042.coteacher.core.matching.PhraseMatcher
import org.sih26042.coteacher.core.model.ClassroomPhrase
import org.sih26042.coteacher.core.model.ProvenanceState

class OrchestratedTranslationEngineTest {

    private lateinit var matcher: PhraseMatcher
    private lateinit var verifiedEngine: VerifiedPhraseTranslationEngine
    private lateinit var neuralEngine: NeuralTranslationEngine
    private lateinit var orchestrator: OrchestratedTranslationEngine

    @Before
    fun setUp() {
        val phrases = listOf(
            ClassroomPhrase(
                phraseId = "ph_sit_01",
                hindiCanonical = "बैठ जाओ",
                hindiNormalized = "बैठ जाओ",
                hindiAliases = listOf("बैठो"),
                targetNativeScript = "ᱫᱩᱲᱩᱵ ᱢᱮ",
                targetTransliterationLatin = "Duṛub me",
                audioPath = "audio/ph_sit_01.ogg",
                category = "classroom_management",
                provenance = ProvenanceState.VERIFIED
            )
        )
        matcher = PhraseMatcher(phrases)
        verifiedEngine = VerifiedPhraseTranslationEngine(matcher)
        neuralEngine = NeuralTranslationEngine()
        orchestrator = OrchestratedTranslationEngine(verifiedEngine, neuralEngine)
    }

    @Test
    fun testFastPathReturnsVerifiedProvenanceWithNativeAudio() = runTest {
        val result = orchestrator.process("बैठ जाओ", "sat", 350L)

        // Rule 9: Machine generated must NEVER be presented as verified. Verified must be genuine.
        assertEquals(ProvenanceState.VERIFIED, result.provenance)
        assertEquals("ᱫᱩᱲᱩᱵ ᱢᱮ", result.outputNativeScript)
        assertEquals("Duṛub me", result.outputTransliteration)
        assertNotNull("Verified phrases must have pre-recorded native audio ready", result.audioPath)
        assertEquals("VERIFIED_FAST_PATH", result.latency.pipelineMode)
        assertTrue("Verified target total latency should be ~1s", result.latency.totalLatencyMs < 1200L)
    }

    @Test
    fun testFallbackReturnsMachineGeneratedProvenanceWithoutFakeVerifiedAudio() = runTest {
        val result = orchestrator.process("आज हम सब मिलकर गाना गाएंगे", "sat", 400L)

        // Must be MACHINE_GENERATED, RULE_BASED, or LOW_CONFIDENCE, never VERIFIED
        assertNotEquals(ProvenanceState.VERIFIED, result.provenance)
        assertTrue(
            result.provenance == ProvenanceState.MACHINE_GENERATED ||
            result.provenance == ProvenanceState.RULE_BASED ||
            result.provenance == ProvenanceState.LOW_CONFIDENCE
        )
        assertNull("Machine generated phrases must not have native verified audio asset", result.audioPath)
        assertEquals("NEURAL_FALLBACK", result.latency.pipelineMode)
    }
}

package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.sih26042.coteacher.core.matching.PhraseMatcher
import org.sih26042.coteacher.core.model.ClassroomPhrase
import org.sih26042.coteacher.core.model.ProvenanceState
import org.sih26042.coteacher.core.model.TeacherCorrection

class NeuralFallbackOrchestrationTest {

    private lateinit var matcher: PhraseMatcher
    private lateinit var verifiedEngine: VerifiedPhraseTranslationEngine
    private lateinit var neuralEngine: OfflineHindiSantaliMtEngine
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
        neuralEngine = OfflineHindiSantaliMtEngine()
        orchestrator = OrchestratedTranslationEngine(verifiedEngine, neuralEngine, enableNeuralFallback = true)
    }

    @Test
    fun testUnmatchedSpokenSentenceReachesNeuralFallback() = runTest {
        // Long-tail utterance not in phrase bank
        val inputHindi = "बच्चों, अपनी किताब खोलो।"
        val asrLatency = 210L
        val result = orchestrator.process(inputHindi, "sat", asrLatency)

        // Must receive MACHINE_GENERATED or RULE_BASED provenance — never VERIFIED
        assertTrue(
            "Provenance must be RULE_BASED or MACHINE_GENERATED — never fake VERIFIED",
            result.provenance == ProvenanceState.MACHINE_GENERATED || result.provenance == ProvenanceState.RULE_BASED
        )
        assertNull("Machine translation confidence must be null (uncalibrated)", result.confidence)
        assertNull("Audio must NOT be fabricated for neural fallback (TTS feasibility gate: TTS_UNAVAILABLE)", result.audioPath)
        assertEquals("NEURAL_FALLBACK", result.latency.pipelineMode)
        assertTrue("Output should be in Ol Chiki script", result.outputNativeScript.isNotBlank())
        assertTrue("Total latency should include ASR and MT duration", result.latency.totalLatencyMs >= asrLatency)
    }

    @Test
    fun testMatchedSentencePreservesVerifiedAudioPath() = runTest {
        val inputHindi = "बैठ जाओ"
        val asrLatency = 180L
        val result = orchestrator.process(inputHindi, "sat", asrLatency)

        assertEquals(ProvenanceState.VERIFIED, result.provenance)
        assertEquals("audio/ph_sit_01.ogg", result.audioPath)
        assertEquals("VERIFIED_FAST_PATH", result.latency.pipelineMode)
    }

    @Test
    fun testDisabledFallbackReturnsNoMatchWithoutInvokingMt() = runTest {
        val orchestratorNoFallback = OrchestratedTranslationEngine(verifiedEngine, neuralEngine, enableNeuralFallback = false)
        val result = orchestratorNoFallback.process("बच्चों, अपनी किताब खोलो।", "sat", 150L)

        assertEquals(ProvenanceState.NO_MATCH, result.provenance)
        assertEquals("", result.outputNativeScript)
        assertEquals("PHRASE_ONLY_PHASE2", result.latency.pipelineMode)
    }

    @Test
    fun testTeacherCorrectionDataModelCreation() {
        val correction = TeacherCorrection(
            sourceHindi = "बच्चों, अपनी किताब खोलो।",
            candidateSantali = "ᱜᱤᱫᱽᱨᱟᱹ ᱠᱚ, ᱟᱯᱱᱟᱨᱟᱜ ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡ ᱯᱮ",
            suggestedSantali = "ᱥᱟᱱᱟᱢ ᱜᱤᱫᱽᱨᱟᱹ, ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡ ᱢᱮ",
            issueType = "DIALECT_VARIATION",
            dialectNote = "Mayurbhanj dialect",
            teacherNotes = "Used in Grade 1 classroom"
        )

        assertNotNull(correction.correctionId)
        assertEquals("QUEUED_OFFLINE", correction.syncStatus)
        assertEquals("DIALECT_VARIATION", correction.issueType)
        assertTrue(correction.timestampMs > 0)
    }
}

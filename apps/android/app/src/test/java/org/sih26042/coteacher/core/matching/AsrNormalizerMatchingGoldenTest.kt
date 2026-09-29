package org.sih26042.coteacher.core.matching

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.sih26042.coteacher.core.engine.*
import org.sih26042.coteacher.core.model.ClassroomPhrase
import org.sih26042.coteacher.core.model.ProvenanceState

class AsrNormalizerMatchingGoldenTest {

    private lateinit var phraseMatcher: PhraseMatcher
    private lateinit var verifiedEngine: VerifiedPhraseTranslationEngine
    private lateinit var neuralEngine: NeuralTranslationEngine
    private lateinit var orchestrator: OrchestratedTranslationEngine

    @Before
    fun setUp() {
        // Representative classroom phrases from Phase 1 Santali pack
        val phrases = listOf(
            ClassroomPhrase(
                phraseId = "teacher_sit_001",
                hindiCanonical = "बैठ जाओ",
                hindiNormalized = "बैठ जाओ",
                hindiAliases = listOf("बैठो", "सब बैठ जाओ", "सभी बैठो"),
                targetNativeScript = "ᱫᱩᱲᱩᱵ ᱢᱮ",
                targetTransliterationLatin = "Duṛub me",
                audioPath = "audio/ph_sit_down_01.wav",
                category = "CLASSROOM_MANAGEMENT",
                verificationStatus = "PENDING_VALIDATION" // Safe status from Phase 1
            ),
            ClassroomPhrase(
                phraseId = "teacher_stand_001",
                hindiCanonical = "खड़े हो जाओ",
                hindiNormalized = "खड़े हो जाओ",
                hindiAliases = listOf("खड़े हो", "खड़े होइए"),
                targetNativeScript = "ᱛᱤᱸᱜᱩᱱ ᱢᱮ",
                targetTransliterationLatin = "Tingun me",
                audioPath = "audio/ph_stand_up_01.wav",
                category = "CLASSROOM_MANAGEMENT",
                verificationStatus = "PENDING_VALIDATION"
            ),
            ClassroomPhrase(
                phraseId = "teacher_listen_001",
                hindiCanonical = "ध्यान से सुनो",
                hindiNormalized = "ध्यान से सुनो",
                hindiAliases = listOf("सुनो", "सुनों"),
                targetNativeScript = "ᱟᱧᱡᱚᱢ ᱢᱮ",
                targetTransliterationLatin = "Anjom me",
                audioPath = "audio/ph_listen_01.wav",
                category = "CLASSROOM_MANAGEMENT",
                verificationStatus = "PENDING_VALIDATION"
            ),
            ClassroomPhrase(
                phraseId = "teacher_praise_verified",
                hindiCanonical = "शाबाश",
                hindiNormalized = "शाबाश",
                hindiAliases = listOf("बहुत अच्छा"),
                targetNativeScript = "ᱵᱮᱥ ᱜᱮᱭᱟ",
                targetTransliterationLatin = "Bes geya",
                audioPath = "audio/ph_praise_01.wav",
                category = "ENCOURAGEMENT_AND_FEEDBACK",
                verificationStatus = "VERIFIED" // Established native verified control
            )
        )

        phraseMatcher = PhraseMatcher(phrases)
        verifiedEngine = VerifiedPhraseTranslationEngine(phraseMatcher)
        neuralEngine = NeuralTranslationEngine()
        orchestrator = OrchestratedTranslationEngine(
            verifiedEngine = verifiedEngine,
            neuralEngine = neuralEngine,
            enableNeuralFallback = false // Strict Phase 2 rule
        )
    }

    @Test
    fun testAsrRawOutputToNormalizedToPhraseMatchGolden() = runTest {
        val testCases = listOf(
            // Raw ASR output with whitespace, punctuation, danda -> Expected Canonical Phrase ID
            "  बैठ जाओ । " to "teacher_sit_001",
            "बैठो" to "teacher_sit_001",
            "खड़े हो जाओ!" to "teacher_stand_001",
            "  ध्यान   से सुनो... । " to "teacher_listen_001",
            "शाबाश ।" to "teacher_praise_verified"
        )

        for ((rawAsr, expectedPhraseId) in testCases) {
            val normalized = TextNormalizer.normalize(rawAsr)
            val matchResult = phraseMatcher.match(normalized)

            assertNotNull("Normalized '$normalized' must match a phrase", matchResult.matchedPhrase)
            assertEquals("Matched phrase ID must match expected", expectedPhraseId, matchResult.matchedPhrase?.phraseId)
            assertTrue("Match confidence must be >= 0.75", matchResult.confidence >= 0.75f)
        }
    }

    @Test
    fun testTrustModelNeverUpgradesPendingValidationToVerified() = runTest {
        // ASR says "बैठ जाओ", matcher matches teacher_sit_001 which has verificationStatus = PENDING_VALIDATION
        val normalized = TextNormalizer.normalize("बैठ जाओ")
        val result = orchestrator.process(
            normalizedHindi = normalized,
            targetLanguageCode = "sat",
            asrLatencyMs = 210L
        )

        assertEquals("बैठ जाओ", result.recognizedHindi)
        assertEquals("ᱫᱩᱲᱩᱵ ᱢᱮ", result.outputNativeScript)
        assertEquals(
            "Trust model must preserve PENDING_VALIDATION and not falsely claim VERIFIED",
            ProvenanceState.PENDING_VALIDATION,
            result.provenance
        )
        assertNotNull("Audio path must be present", result.audioPath)
    }

    @Test
    fun testTrustModelPreservesVerifiedWhenLinguisticallyVerified() = runTest {
        val normalized = TextNormalizer.normalize("शाबाश")
        val result = orchestrator.process(
            normalizedHindi = normalized,
            targetLanguageCode = "sat",
            asrLatencyMs = 150L
        )

        assertEquals("शाबाश", result.recognizedHindi)
        assertEquals(
            "Linguistically verified phrase returns VERIFIED",
            ProvenanceState.VERIFIED,
            result.provenance
        )
    }

    @Test
    fun testNoMatchStopsAtMatchDecisionWithoutCallingNeuralMtInPhase2() = runTest {
        // Unmatched out-of-domain phrase
        val outOfDomain = "आज मौसम बहुत गर्म है"
        val normalized = TextNormalizer.normalize(outOfDomain)

        val result = orchestrator.process(
            normalizedHindi = normalized,
            targetLanguageCode = "sat",
            asrLatencyMs = 300L
        )

        assertEquals(
            "Out of domain phrase must yield NO_MATCH in Phase 2",
            ProvenanceState.NO_MATCH,
            result.provenance
        )
        assertEquals("Output script must be empty on NO_MATCH", "", result.outputNativeScript)
        assertNull("Audio path must be null on NO_MATCH", result.audioPath)
        assertFalse("Neural MT must not be loaded in Phase 2", neuralEngine.isModelLoaded())
    }
}

package org.sih26042.coteacher.core.matching

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.sih26042.coteacher.core.model.ClassroomPhrase
import org.sih26042.coteacher.core.model.ProvenanceState

class PhraseMatcherTest {

    private lateinit var matcher: PhraseMatcher

    private val samplePhrases = listOf(
        ClassroomPhrase(
            phraseId = "ph_sit_01",
            hindiCanonical = "बैठ जाओ",
            hindiNormalized = "बैठ जाओ",
            hindiAliases = listOf("बैठो", "बैठ जाइए"),
            targetNativeScript = "ᱫᱩᱲᱩᱵ ᱢᱮ",
            targetTransliterationLatin = "Duṛub me",
            audioPath = "audio/ph_sit_01.ogg",
            category = "classroom_management",
            provenance = ProvenanceState.VERIFIED
        ),
        ClassroomPhrase(
            phraseId = "ph_stand_01",
            hindiCanonical = "खड़े हो जाओ",
            hindiNormalized = "खड़े हो जाओ",
            hindiAliases = listOf("खड़े हो"),
            targetNativeScript = "ᱛᱤᱸᱜᱩᱱ ᱢᱮ",
            targetTransliterationLatin = "Tingun me",
            audioPath = "audio/ph_stand_01.ogg",
            category = "classroom_management",
            provenance = ProvenanceState.VERIFIED
        ),
        ClassroomPhrase(
            phraseId = "ph_count_01",
            hindiCanonical = "मेरे साथ गिनो",
            hindiNormalized = "मेरे साथ गिनो",
            hindiAliases = listOf("गिनो"),
            targetNativeScript = "ᱤᱧ ᱥᱟᱶ ᱞᱮᱠᱷᱟᱭ ᱢᱮ",
            targetTransliterationLatin = "Iñ saw lekhay me",
            audioPath = "audio/ph_count_01.ogg",
            category = "nipun_math",
            provenance = ProvenanceState.VERIFIED
        )
    )

    @Before
    fun setUp() {
        matcher = PhraseMatcher(samplePhrases)
    }

    @Test
    fun testExactCanonicalMatch() {
        val result = matcher.match("बैठ जाओ")
        assertNotNull("Should match canonical phrase", result.matchedPhrase)
        assertEquals("ph_sit_01", result.matchedPhrase?.phraseId)
        assertEquals("EXACT_CANONICAL", result.matchType)
        assertEquals(1.0f, result.confidence, 0.01f)
    }

    @Test
    fun testExactCanonicalMatchWithPunctuationAndWhitespace() {
        val result = matcher.match("  बैठ जाओ! । ")
        assertNotNull(result.matchedPhrase)
        assertEquals("ph_sit_01", result.matchedPhrase?.phraseId)
        assertEquals("EXACT_CANONICAL", result.matchType)
    }

    @Test
    fun testAliasMatch() {
        val result = matcher.match("बैठो")
        assertNotNull("Should match alias", result.matchedPhrase)
        assertEquals("ph_sit_01", result.matchedPhrase?.phraseId)
        assertEquals("EXACT_ALIAS", result.matchType)
        assertTrue(result.confidence >= 0.90f)
    }

    @Test
    fun testFuzzyMatchWithMinorTypo() {
        // Minor typo in Hindi text
        val result = matcher.match("खडे हो जाओ")
        assertNotNull("Should match fuzzy", result.matchedPhrase)
        assertEquals("ph_stand_01", result.matchedPhrase?.phraseId)
        assertTrue(result.confidence >= 0.80f)
    }

    @Test
    fun testNegativePathUnmatchedPhrase() {
        val result = matcher.match("आज मौसम बहुत सुहावना है")
        assertNull("Out of scope phrase should not match", result.matchedPhrase)
        assertEquals("NONE", result.matchType)
        assertTrue(result.confidence < 0.70f)
    }

    @Test
    fun testMalformedEmptyInput() {
        val result = matcher.match("    ")
        assertNull(result.matchedPhrase)
        assertEquals("NONE", result.matchType)
        assertEquals(0.0f, result.confidence, 0.01f)
    }

    @Test
    fun testSub15MsExecutionBudget() {
        val result = matcher.match("बैठ जाओ")
        assertTrue("Matching latency should be under 50ms, was ${result.matchDurationMs}ms", result.matchDurationMs < 50L)
    }
}

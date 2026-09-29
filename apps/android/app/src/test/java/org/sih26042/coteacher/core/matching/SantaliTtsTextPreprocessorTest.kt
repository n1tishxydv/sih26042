package org.sih26042.coteacher.core.matching

import org.junit.Assert.*
import org.junit.Test

class SantaliTtsTextPreprocessorTest {

    @Test
    fun testNfcUnicodeNormalization() {
        val decomposed = "ᱫᱩᱲᱩᱵ"
        val result = SantaliTtsTextPreprocessor.preprocess(decomposed)
        assertEquals("ᱫᱩᱲᱩᱵ", result.normalizedText)
        assertTrue(result.isValidForSynthesis)
    }

    @Test
    fun testOlChikiDigitExpansion() {
        val input = "ᱦᱟᱹᱛᱤ ᱓"
        val result = SantaliTtsTextPreprocessor.preprocess(input)
        assertTrue(result.normalizedText.contains("ᱯᱮ"))
        assertEquals(1, result.numbersExpanded)
    }

    @Test
    fun testOlChikiDigitSequenceExpansion() {
        val input = "᱑ ᱒ ᱓ ᱔ ᱕"
        val result = SantaliTtsTextPreprocessor.preprocess(input)
        assertEquals(5, result.numbersExpanded)
        assertTrue(result.normalizedText.contains("ᱢᱤᱫ"))
        assertTrue(result.normalizedText.contains("ᱵᱟᱨ"))
        assertTrue(result.normalizedText.contains("ᱯᱮ"))
        assertTrue(result.normalizedText.contains("ᱯᱩᱱ"))
        assertTrue(result.normalizedText.contains("ᱢᱚᱬᱮ"))
    }

    @Test
    fun testSentenceSplitting() {
        val input = "ᱫᱩᱲᱩᱵ ᱢᱮ᱾ ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ!"
        val result = SantaliTtsTextPreprocessor.preprocess(input)
        assertEquals(2, result.sentences.size)
        assertEquals("ᱫᱩᱲᱩᱵ ᱢᱮ", result.sentences[0])
        assertEquals("ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ", result.sentences[1])
    }

    @Test
    fun testPhonemicTransliterationPreparation() {
        val olChiki = "ᱫᱩᱲᱩᱵ ᱢᱮ"
        val result = SantaliTtsTextPreprocessor.preprocess(olChiki)
        assertEquals("durub me", result.phonemicPrompt)
    }

    @Test
    fun testEmptyInputHandling() {
        val result = SantaliTtsTextPreprocessor.preprocess("   ")
        assertFalse(result.isValidForSynthesis)
        assertTrue(result.warnings.contains("Input text is empty"))
    }
}

package org.sih26042.coteacher.core.matching

import org.junit.Assert.*
import org.junit.Test

class OlChikiScriptValidatorTest {

    @Test
    fun testPureOlChikiPassesValidation() {
        val input = "ᱫᱩᱲᱩᱵ ᱢᱮ" // "Duṛub me"
        val result = OlChikiScriptValidator.validate(input)
        assertTrue("Pure Ol Chiki must be marked valid", result.isValid)
        assertTrue("No warnings should be present for pure Ol Chiki", result.warnings.isEmpty())
        assertEquals(input, result.normalizedText)
        val latin = OlChikiScriptValidator.transliterateToLatin(result.normalizedText)
        assertEquals("durub me", latin)
    }

    @Test
    fun testOlChikiWithPunctuationAndWhitespace() {
        val input = "ᱥᱟᱱᱟᱢ ᱠᱚ, ᱫᱩᱲᱩᱵ ᱯᱮ!"
        val result = OlChikiScriptValidator.validate(input)
        assertTrue("Ol Chiki with standard punctuation must pass", result.isValid)
        assertTrue(result.warnings.isEmpty())
    }

    @Test
    fun testContaminationWithDevanagariDetected() {
        val input = "ᱫᱩᱲᱩᱵ जाओ" // Mixed Ol Chiki and Devanagari
        val result = OlChikiScriptValidator.validate(input)
        assertFalse("Devanagari script contamination must fail or flag validation", result.isValid)
        assertTrue("Must contain contamination warning", result.warnings.any { it.contains("Contamination") || it.contains("Devanagari") })
    }

    @Test
    fun testContaminationWithBengaliDetected() {
        val input = "ᱫᱩᱲᱩᱵ বসো" // Mixed Ol Chiki and Bengali
        val result = OlChikiScriptValidator.validate(input)
        assertFalse("Bengali script contamination must fail or flag validation", result.isValid)
        assertTrue("Must contain Bengali contamination warning", result.warnings.any { it.contains("Bengali") })
    }

    @Test
    fun testContaminationWithLatinAlphabetDetected() {
        val input = "ᱫᱩᱲᱩᱵ sit" // Mixed Ol Chiki and English
        val result = OlChikiScriptValidator.validate(input)
        assertFalse("Latin alphabet contamination in native text must fail validation", result.isValid)
        assertTrue("Must contain Latin contamination warning", result.warnings.any { it.contains("Latin") })
    }

    @Test
    fun testEmptyOrWhitespaceRejected() {
        val result = OlChikiScriptValidator.validate("   ")
        assertFalse("Whitespace-only input must be rejected", result.isValid)
        assertTrue(result.warnings.any { it.contains("empty") })
    }

    @Test
    fun testControlCharactersStripped() {
        val inputWithControl = "ᱫᱩᱲᱩᱵ\u0000\u0007 ᱢᱮ"
        val result = OlChikiScriptValidator.validate(inputWithControl)
        assertTrue("Result after stripping control chars should be valid", result.isValid)
        assertFalse("Control characters must be removed", result.normalizedText.contains("\u0000"))
    }

    @Test
    fun testTransliterationMappingCompleteness() {
        // Test fundamental Ol Chiki letters
        val olChikiAlphabet = "ᱚᱛᱜᱝᱞᱟᱠᱡᱢᱣᱤᱥᱦᱧᱨᱩᱪᱫᱬᱭᱮᱯᱰᱱᱲᱳᱴᱵᱶᱷ"
        val latin = OlChikiScriptValidator.transliterateToLatin(olChikiAlphabet)
        assertNotNull(latin)
        assertTrue("Transliteration must not be empty", latin.isNotBlank())
        // Ensure no untranslated Ol Chiki remains
        assertFalse("All Ol Chiki characters must be mapped", latin.any { it.code in 0x1C50..0x1C7F })
    }
}

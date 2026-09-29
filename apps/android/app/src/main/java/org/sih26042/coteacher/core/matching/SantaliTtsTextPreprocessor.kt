package org.sih26042.coteacher.core.matching

import java.text.Normalizer

data class PreprocessedTtsText(
    val normalizedText: String,
    val phonemicPrompt: String,
    val sentences: List<String>,
    val numbersExpanded: Int,
    val warnings: List<String>,
    val isValidForSynthesis: Boolean
)

/**
 * Normalizes and prepares Santali text in Ol Chiki script for offline TTS synthesis.
 * 
 * Enforces:
 * 1. Unicode NFC normalization.
 * 2. Ol Chiki digit expansion to spoken phonetic Santali (e.g. ᱑ -> ᱢᱤᱫ, ᱒ -> ᱵᱟᱨ).
 * 3. Punctuation normalization and sentence segmentation.
 * 4. Detection of unsupported characters or foreign script contamination.
 */
object SantaliTtsTextPreprocessor {

    private val OL_CHIKI_DIGIT_MAP = mapOf(
        '᱐' to "ᱥᱩᱱ",   // 0: sun
        '᱑' to "ᱢᱤᱫ",   // 1: mid
        '᱒' to "ᱵᱟᱨ",   // 2: bar
        '᱓' to "ᱯᱮ",    // 3: pe
        '᱔' to "ᱯᱩᱱ",   // 4: pun
        '᱕' to "ᱢᱚᱬᱮ",  // 5: mone
        '᱖' to "ᱛᱩᱨᱩᱭ", // 6: turui
        '᱗' to "ᱮᱭᱟᱭ",  // 7: eyay
        '᱘' to "ᱤᱨᱟᱹᱞ", // 8: iral
        '᱙' to "ᱟᱨᱮ"    // 9: are
    )

    fun preprocess(rawText: String): PreprocessedTtsText {
        val warnings = mutableListOf<String>()
        if (rawText.isBlank()) {
            return PreprocessedTtsText(
                normalizedText = "",
                phonemicPrompt = "",
                sentences = emptyList(),
                numbersExpanded = 0,
                warnings = listOf("Input text is empty"),
                isValidForSynthesis = false
            )
        }

        // 1. Unicode NFC Normalization & Control Character Stripping
        val cleaned = rawText.filter { (it.code !in 0x00..0x1F && it.code !in 0x7F..0x9F) || it == '\n' || it == '\t' || it == '\r' }.trim()
        val nfc = Normalizer.normalize(cleaned, Normalizer.Form.NFC)

        // 2. Ol Chiki Number Expansion to Spoken Words
        var numbersCount = 0
        val expandedSb = StringBuilder()
        for (ch in nfc) {
            val word = OL_CHIKI_DIGIT_MAP[ch]
            if (word != null) {
                expandedSb.append(" ").append(word).append(" ")
                numbersCount++
            } else {
                expandedSb.append(ch)
            }
        }
        val textWithExpandedNumbers = expandedSb.toString().replace(Regex("\\s+"), " ").trim()

        // 3. Ol Chiki Script Validation & Contamination Check
        val validation = OlChikiScriptValidator.validate(textWithExpandedNumbers)
        warnings.addAll(validation.warnings)

        // 4. Sentence Segmentation (Splitting by Ol Chiki danda ᱾, double danda ᱿, !, ?)
        val sentences = textWithExpandedNumbers
            .split(Regex("[᱾᱿.!?\\n]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        // 5. Phonetic Transliteration (for TTS engines requiring Latin IPA prompts)
        val phonemicLatin = OlChikiScriptValidator.transliterateToLatin(textWithExpandedNumbers)

        return PreprocessedTtsText(
            normalizedText = textWithExpandedNumbers,
            phonemicPrompt = phonemicLatin,
            sentences = sentences,
            numbersExpanded = numbersCount,
            warnings = warnings,
            isValidForSynthesis = validation.isValid
        )
    }
}

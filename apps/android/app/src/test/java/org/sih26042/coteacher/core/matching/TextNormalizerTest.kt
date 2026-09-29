package org.sih26042.coteacher.core.matching

import org.junit.Assert.*
import org.junit.Test

/**
 * Cross-language verification test verifying that Kotlin's TextNormalizer
 * exactly produces canonical outputs specified in docs/NORMALIZATION_SPEC.md.
 */
class TextNormalizerTest {

    @Test
    fun testGoldenVectorsWithoutFillerRemoval() {
        // NORM-001: Clean command
        assertEquals("बैठ जाओ", TextNormalizer.normalize("बैठ जाओ", removeFillers = false))

        // NORM-002: Trailing punctuation
        assertEquals("बैठ जाओ", TextNormalizer.normalize("बैठ जाओ!", removeFillers = false))

        // NORM-003: Multi-space and danda
        assertEquals("अपनी किताब खोलो", TextNormalizer.normalize("अपनी  किताब   खोलो ।", removeFillers = false))

        // NORM-004: Politeness filler preserved
        assertEquals("कृपया बैठ जाइए", TextNormalizer.normalize("कृपया बैठ जाइए", removeFillers = false))

        // NORM-005: Punctuation strip with fillers kept
        assertEquals("अरे बच्चों शांत रहो", TextNormalizer.normalize("अरे बच्चों, शांत रहो!", removeFillers = false))

        // NORM-006: Quotes stripping
        assertEquals("खड़े हो जाओ", TextNormalizer.normalize("\"खड़े हो जाओ\"", removeFillers = false))

        // NORM-007: Ellipsis stripping
        assertEquals("ताली बजाओ और बोलो", TextNormalizer.normalize("ताली बजाओ... और बोलो", removeFillers = false))

        // NORM-009: Parentheses
        assertEquals("हाथ ऊपर करो सब", TextNormalizer.normalize("हाथ ऊपर करो (सब)", removeFillers = false))

        // NORM-010: Empty string invariant
        assertEquals("", TextNormalizer.normalize("   ", removeFillers = false))
        assertEquals("", TextNormalizer.normalize(null, removeFillers = false))
    }

    @Test
    fun testGoldenVectorsWithFillerRemoval() {
        // NORM-004: Politeness filler removed
        assertEquals("बैठ जाइए", TextNormalizer.normalize("कृपया बैठ जाइए", removeFillers = true))

        // NORM-005: Spoken attention particles stripped
        assertEquals("शांत रहो", TextNormalizer.normalize("अरे बच्चों, शांत रहो!", removeFillers = true))

        // NORM-008: Particles जरा and जी stripped
        assertEquals("सुनो", TextNormalizer.normalize("ज़रा सुनो जी", removeFillers = true))
    }

    @Test
    fun testLevenshteinSimilarity() {
        // Exact identical strings
        assertEquals(1.0f, TextNormalizer.computeSimilarity("बैठ जाओ", "बैठ जाओ"), 0.001f)

        // Minor acoustic punctuation or spacing variation yields >= 0.80
        val sim = TextNormalizer.computeSimilarity("बैठ जाओ!", "बैठ जाओ ।")
        assertTrue("Similarity should be 1.0 after normalization: $sim", sim >= 0.99f)

        // Completely different strings
        val lowSim = TextNormalizer.computeSimilarity("किताब खोलो", "पानी पियो")
        assertTrue("Completely different strings should have low similarity: $lowSim", lowSim < 0.40f)
    }
}

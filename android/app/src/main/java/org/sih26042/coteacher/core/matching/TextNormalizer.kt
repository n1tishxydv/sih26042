package org.sih26042.coteacher.core.matching

import java.text.Normalizer
import kotlin.math.max
import kotlin.math.min

object TextNormalizer {
    private val PUNCT_REGEX = Regex("[\\s.,।?!:;\\-_\"'()\\[\\]{}—/\\\\।]+")
    private val FILLER_WORDS = setOf("कृपया", "जरा", "अरे", "बेटा", "बच्चों", "बच्चो", "जी")

    fun normalize(text: String?, removeFillers: Boolean = false): String {
        if (text.isNullOrBlank()) return ""
        
        // Canonical Decomposition followed by Canonical Composition (NFC)
        var normalized = Normalizer.normalize(text.trim(), Normalizer.Form.NFC)

        // Replace danda and double danda
        normalized = normalized.replace("।", " ").replace("॥", " ")

        // Remove zero-width non-joiners / joiners
        normalized = normalized.replace("\u200C", "").replace("\u200D", "")

        val tokens = normalized.split("\\s+".toRegex())
            .map { PUNCT_REGEX.replace(it, "") }
            .filter { it.isNotBlank() }

        val filtered = if (removeFillers) {
            tokens.filter { it !in FILLER_WORDS }
        } else {
            tokens
        }

        return filtered.joinToString(" ")
    }

    fun computeSimilarity(s1: String, s2: String): Float {
        val norm1 = normalize(s1)
        val norm2 = normalize(s2)
        if (norm1 == norm2) return 1.0f
        if (norm1.isEmpty() || norm2.isEmpty()) return 0.0f

        val len1 = norm1.length
        val len2 = norm2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (norm1[i - 1] == norm2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        val distance = dp[len1][len2]
        val maxLen = max(len1, len2)
        return 1.0f - (distance.toFloat() / maxLen.toFloat())
    }
}

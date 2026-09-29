package org.sih26042.coteacher.core.matching

import org.sih26042.coteacher.core.model.ClassroomPhrase

data class MatchResult(
    val matchedPhrase: ClassroomPhrase?,
    val matchType: String, // EXACT_CANONICAL, EXACT_ALIAS, TOKEN_CONTAINED, FUZZY, NONE
    val confidence: Float,
    val matchDurationMs: Long
)

class PhraseMatcher(
    private var phraseBank: List<ClassroomPhrase> = emptyList(),
    private val similarityThreshold: Float = 0.80f
) {
    // Exact lookup map: normalized string -> Phrase
    private var canonicalMap = HashMap<String, ClassroomPhrase>()
    private var aliasMap = HashMap<String, ClassroomPhrase>()

    init {
        updatePhrases(phraseBank)
    }

    fun updatePhrases(phrases: List<ClassroomPhrase>) {
        this.phraseBank = phrases
        val newCanonical = HashMap<String, ClassroomPhrase>(phrases.size)
        val newAlias = HashMap<String, ClassroomPhrase>(phrases.size * 3)

        for (phrase in phrases) {
            val normCanonical = TextNormalizer.normalize(phrase.hindiCanonical)
            newCanonical[normCanonical] = phrase
            newAlias[normCanonical] = phrase

            for (alias in phrase.hindiAliases) {
                val normAlias = TextNormalizer.normalize(alias)
                newAlias[normAlias] = phrase
            }
        }
        this.canonicalMap = newCanonical
        this.aliasMap = newAlias
    }

    fun match(rawHindi: String): MatchResult {
        val startTime = System.currentTimeMillis()
        val normalized = TextNormalizer.normalize(rawHindi)
        val normalizedNoFillers = TextNormalizer.normalize(rawHindi, removeFillers = true)

        if (normalized.isBlank()) {
            return MatchResult(
                matchedPhrase = null,
                matchType = "NONE",
                confidence = 0.0f,
                matchDurationMs = System.currentTimeMillis() - startTime
            )
        }

        // 1. Exact canonical match
        canonicalMap[normalized]?.let { phrase ->
            return MatchResult(phrase, "EXACT_CANONICAL", 1.0f, System.currentTimeMillis() - startTime)
        }
        canonicalMap[normalizedNoFillers]?.let { phrase ->
            return MatchResult(phrase, "EXACT_CANONICAL", 0.98f, System.currentTimeMillis() - startTime)
        }

        // 2. Exact alias match
        aliasMap[normalized]?.let { phrase ->
            return MatchResult(phrase, "EXACT_ALIAS", 0.95f, System.currentTimeMillis() - startTime)
        }
        aliasMap[normalizedNoFillers]?.let { phrase ->
            return MatchResult(phrase, "EXACT_ALIAS", 0.93f, System.currentTimeMillis() - startTime)
        }

        // 3. Substring / Token containment
        for (phrase in phraseBank) {
            val pNorm = TextNormalizer.normalize(phrase.hindiCanonical)
            if (pNorm.isNotEmpty() && (normalized.contains(pNorm) || pNorm.contains(normalized))) {
                val conf = (pNorm.length.toFloat() / maxOf(pNorm.length, normalized.length)).coerceIn(0.70f, 0.90f)
                return MatchResult(phrase, "TOKEN_CONTAINED", conf, System.currentTimeMillis() - startTime)
            }
        }

        // 4. Fuzzy Levenshtein match against canonical and aliases
        var bestPhrase: ClassroomPhrase? = null
        var bestSim = 0.0f

        for (phrase in phraseBank) {
            val simCanonical = TextNormalizer.computeSimilarity(normalized, phrase.hindiCanonical)
            if (simCanonical > bestSim) {
                bestSim = simCanonical
                bestPhrase = phrase
            }

            for (alias in phrase.hindiAliases) {
                val simAlias = TextNormalizer.computeSimilarity(normalized, alias)
                if (simAlias > bestSim) {
                    bestSim = simAlias
                    bestPhrase = phrase
                }
            }
        }

        val duration = System.currentTimeMillis() - startTime
        return if (bestPhrase != null && bestSim >= similarityThreshold) {
            MatchResult(bestPhrase, "FUZZY", bestSim, duration)
        } else {
            MatchResult(null, "NONE", bestSim, duration)
        }
    }
}

package org.sih26042.coteacher.core.search

import org.sih26042.coteacher.core.content.SantaliNipunContent
import org.sih26042.coteacher.core.matching.TextNormalizer
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.data.ClassroomRepository
import org.sih26042.coteacher.data.TeacherMaterialRepository

/**
 * PHASE 6 — LocalSearchService
 *
 * Provides offline, normalized multi-domain search across:
 * - Classroom phrases
 * - FLN vocabulary
 * - NIPUN lessons
 * - Worksheets
 * - Flashcards
 * - Saved teacher materials
 *
 * Operates 100% offline with zero external network requests.
 */
class LocalSearchService(
    private val classroomRepository: ClassroomRepository,
    private val teacherMaterialRepository: TeacherMaterialRepository
) {
    fun search(
        rawQuery: String,
        filterCategory: String = "ALL",
        filterType: SearchResultType? = null
    ): List<SearchResultItem> {
        val query = rawQuery.trim()
        if (query.isEmpty()) return emptyList()

        val normalizedQuery = TextNormalizer.normalize(query).lowercase()
        val results = mutableListOf<SearchResultItem>()

        // 1. Classroom Phrases
        val phrases = classroomRepository.phrases.value
        for (p in phrases) {
            val normHindi = TextNormalizer.normalize(p.hindiCanonical).lowercase()
            val matchesHindi = normHindi.contains(normalizedQuery) || p.hindiNormalized.lowercase().contains(normalizedQuery)
            val matchesTarget = p.targetNativeScript.contains(query) || p.targetTransliterationLatin.lowercase().contains(normalizedQuery)
            val matchesCat = p.category.lowercase().contains(normalizedQuery)

            if (matchesHindi || matchesTarget || matchesCat) {
                if (filterCategory == "ALL" || p.category.equals(filterCategory, ignoreCase = true)) {
                    results.add(
                        SearchResultItem(
                            id = p.phraseId,
                            title = p.hindiCanonical,
                            subtitle = "Category: ${p.category}",
                            nativeScript = p.targetNativeScript,
                            latinTransliteration = p.targetTransliterationLatin,
                            type = SearchResultType.PHRASE,
                            category = p.category,
                            provenance = ContentProvenance.VERIFIED,
                            audioPath = p.audioPath
                        )
                    )
                }
            }
        }

        // 2. FLN Vocabulary
        val flnVocab = classroomRepository.flnVocabulary.value
        for (v in flnVocab) {
            val normHindi = TextNormalizer.normalize(v.hindiWord).lowercase()
            val matchesHindi = normHindi.contains(normalizedQuery)
            val matchesTarget = v.targetNativeScript.contains(query) || v.targetTransliteration.lowercase().contains(normalizedQuery)
            val matchesCat = v.category.lowercase().contains(normalizedQuery)

            if (matchesHindi || matchesTarget || matchesCat) {
                if (filterCategory == "ALL" || v.category.equals(filterCategory, ignoreCase = true)) {
                    results.add(
                        SearchResultItem(
                            id = v.wordId,
                            title = v.hindiWord,
                            subtitle = "FLN: ${v.category.replace('_', ' ').uppercase()}",
                            nativeScript = v.targetNativeScript,
                            latinTransliteration = v.targetTransliteration,
                            type = SearchResultType.VOCABULARY,
                            category = v.category,
                            provenance = ContentProvenance.VERIFIED,
                            audioPath = v.audioPath
                        )
                    )
                }
            }
        }

        // 3. NIPUN Lessons
        val lessons = SantaliNipunContent.LESSON_REGISTRY.values
        for (l in lessons) {
            val matchesTitle = l.title.lowercase().contains(normalizedQuery)
            val matchesObjective = l.learningOutcome.objectiveHindi.lowercase().contains(normalizedQuery) ||
                    l.learningOutcome.objectiveSantali.contains(query) ||
                    l.learningOutcome.objectiveLatin.lowercase().contains(normalizedQuery)
            val matchesDomain = l.domain.displayLabel.lowercase().contains(normalizedQuery)

            if (matchesTitle || matchesObjective || matchesDomain) {
                if (filterCategory == "ALL" || l.domain.name.equals(filterCategory, ignoreCase = true)) {
                    results.add(
                        SearchResultItem(
                            id = l.lessonId,
                            title = l.title,
                            subtitle = "${l.gradeLevel.displayLabel} • ${l.domain.displayLabel}",
                            nativeScript = l.learningOutcome.objectiveSantali,
                            latinTransliteration = l.learningOutcome.objectiveLatin,
                            type = SearchResultType.LESSON,
                            category = l.domain.name,
                            provenance = l.provenance
                        )
                    )
                }
            }
        }

        // 4. Saved Teacher Materials
        val materials = teacherMaterialRepository.materials.value
        for (m in materials) {
            val matchesTitle = m.title.lowercase().contains(normalizedQuery)
            val matchesNotes = m.notes.lowercase().contains(normalizedQuery)
            val matchesTags = m.tags.any { it.lowercase().contains(normalizedQuery) }

            if (matchesTitle || matchesNotes || matchesTags) {
                results.add(
                    SearchResultItem(
                        id = m.id,
                        title = m.title,
                        subtitle = "${m.type.displayName} • ${m.gradeLevel.displayLabel}",
                        nativeScript = "Version ${m.version}",
                        latinTransliteration = m.notes,
                        type = SearchResultType.SAVED_MATERIAL,
                        category = m.domain.name,
                        provenance = m.provenance
                    )
                )
            }
        }

        // 5. Flashcards
        val allDecks = teacherMaterialRepository.getAllDecks(flnVocab)
        for (deck in allDecks) {
            for (c in deck.cards) {
                val matchesFront = c.front.lowercase().contains(normalizedQuery)
                val matchesBack = c.back.contains(query)
                val matchesTrans = c.transliteration.lowercase().contains(normalizedQuery)

                if (matchesFront || matchesBack || matchesTrans) {
                    results.add(
                        SearchResultItem(
                            id = c.id,
                            title = c.front,
                            subtitle = "Deck: ${deck.title}",
                            nativeScript = c.back,
                            latinTransliteration = c.transliteration,
                            type = SearchResultType.FLASHCARD,
                            category = deck.category,
                            provenance = c.provenance,
                            audioPath = c.audioRef
                        )
                    )
                }
            }
        }

        return if (filterType != null) {
            results.filter { it.type == filterType }
        } else {
            results
        }
    }
}

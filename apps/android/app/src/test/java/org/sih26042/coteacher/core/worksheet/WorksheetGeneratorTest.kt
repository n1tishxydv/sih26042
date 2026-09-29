package org.sih26042.coteacher.core.worksheet

import org.junit.Assert.*
import org.junit.Test
import org.sih26042.coteacher.core.model.*

/**
 * PHASE 6 — WorksheetGenerator Unit Tests
 *
 * Verifies that all 5 templates generate correct, well-structured questions
 * from explicit FLN vocabulary without hallucinations.
 */
class WorksheetGeneratorTest {

    private val sampleVocab = listOf(
        FlnVocabularyItem(wordId = "num_1", category = "numbers", hindiWord = "एक", targetNativeScript = "ᱢᱤᱫ", targetTransliteration = "mid", numericalValue = 1),
        FlnVocabularyItem(wordId = "num_2", category = "numbers", hindiWord = "दो", targetNativeScript = "ᱵᱟᱨ", targetTransliteration = "bar", numericalValue = 2),
        FlnVocabularyItem(wordId = "num_3", category = "numbers", hindiWord = "तीन", targetNativeScript = "ᱯᱮ", targetTransliteration = "pe", numericalValue = 3),
        FlnVocabularyItem(wordId = "num_4", category = "numbers", hindiWord = "चार", targetNativeScript = "ᱯᱳᱱ", targetTransliteration = "pon", numericalValue = 4),
        FlnVocabularyItem(wordId = "num_5", category = "numbers", hindiWord = "पाँच", targetNativeScript = "ᱢᱚᱬᱮ", targetTransliteration = "mone", numericalValue = 5)
    )

    @Test
    fun `generate MATCHING template creates valid matching questions`() {
        val ws = WorksheetGenerator.generate(
            title = "गिनती मिलान",
            templateType = WorksheetTemplateType.MATCHING,
            selectedVocab = sampleVocab
        )

        assertEquals("गिनती मिलान", ws.title)
        assertEquals(ContentProvenance.TEACHER_CREATED, ws.provenance)
        assertEquals(5, ws.questions.size)

        ws.questions.forEach { q ->
            assertEquals("MATCHING", q.questionType)
            assertTrue(q.options.contains(q.correctAnswer))
            assertTrue(q.promptHindi.startsWith("मिलान करें:"))
        }
    }

    @Test
    fun `generate MULTIPLE_CHOICE template creates options with correct answer`() {
        val ws = WorksheetGenerator.generate(
            title = "सही विकल्प चुनें",
            templateType = WorksheetTemplateType.MULTIPLE_CHOICE,
            selectedVocab = sampleVocab
        )

        assertEquals(5, ws.questions.size)
        ws.questions.forEach { q ->
            assertEquals("MULTIPLE_CHOICE", q.questionType)
            assertTrue(q.options.size in 2..4)
            assertTrue("Correct answer must be in options", q.options.contains(q.correctAnswer))
        }
    }

    @Test
    fun `generate FILL_SELECT template creates fill-in prompts`() {
        val ws = WorksheetGenerator.generate(
            title = "रिक्त स्थान",
            templateType = WorksheetTemplateType.FILL_SELECT,
            selectedVocab = sampleVocab
        )

        assertEquals(5, ws.questions.size)
        ws.questions.forEach { q ->
            assertEquals("FILL_SELECT", q.questionType)
            assertTrue(q.promptHindi.contains("खाली स्थान भरें"))
            assertTrue(q.options.contains(q.correctAnswer))
        }
    }

    @Test
    fun `generate ORDERING template preserves numerical sequence order`() {
        val ws = WorksheetGenerator.generate(
            title = "क्रमबद्ध करें",
            templateType = WorksheetTemplateType.ORDERING,
            selectedVocab = sampleVocab
        )

        assertEquals(5, ws.questions.size)
        ws.questions.forEachIndexed { index, q ->
            assertEquals("ORDERING", q.questionType)
            assertTrue(q.promptHindi.contains("क्रम संख्या ${index + 1}"))
            assertTrue(q.options.contains(q.correctAnswer))
        }
    }

    @Test
    fun `generate PICTURE_RECOGNITION template sets visualAsset reference`() {
        val ws = WorksheetGenerator.generate(
            title = "चित्र पहचान",
            templateType = WorksheetTemplateType.PICTURE_RECOGNITION,
            selectedVocab = sampleVocab
        )

        assertEquals(4, ws.questions.size)
        ws.questions.forEach { q ->
            assertEquals("PICTURE_RECOGNITION", q.questionType)
            assertNotNull(q.visualAsset)
            assertTrue(q.options.contains(q.correctAnswer))
        }
    }
}

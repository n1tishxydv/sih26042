package org.sih26042.coteacher.core.validation

import org.junit.Assert.*
import org.junit.Test
import org.sih26042.coteacher.core.model.Flashcard
import org.sih26042.coteacher.core.model.WorksheetQuestion

/**
 * PHASE 6 — TeacherContentValidator Unit Tests
 */
class TeacherContentValidatorTest {

    @Test
    fun `validatePhrase succeeds on valid Santali phrase`() {
        val result = TeacherContentValidator.validatePhrase(
            hindiText = "किताब खोलो",
            targetText = "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱯᱮ",
            languageCode = "sat"
        )
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validatePhrase rejects blank text`() {
        val result = TeacherContentValidator.validatePhrase(
            hindiText = "",
            targetText = "",
            languageCode = "sat"
        )
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Hindi phrase cannot be empty") })
        assertTrue(result.errors.any { it.contains("Target translation cannot be empty") })
    }

    @Test
    fun `validatePhrase rejects unsupported language code`() {
        val result = TeacherContentValidator.validatePhrase(
            hindiText = "नमस्ते",
            targetText = "Hello",
            languageCode = "fra" // French unsupported
        )
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("Unsupported language code") })
    }

    @Test
    fun `path traversal attempts are strictly detected and rejected`() {
        assertTrue(TeacherContentValidator.isPathTraversalAttempt("../../../etc/passwd"))
        assertTrue(TeacherContentValidator.isPathTraversalAttempt("..\\..\\windows\\system32"))
        assertTrue(TeacherContentValidator.isPathTraversalAttempt("audio/../../secret.wav"))
        assertFalse(TeacherContentValidator.isPathTraversalAttempt("audio/ph_sit_down_01.wav"))
    }

    @Test
    fun `validateFlashcard enforces front, back, and difficulty bounds`() {
        val validCard = Flashcard(front = "पानी", back = "ᱫᱟᱜ", difficulty = 2)
        assertTrue(TeacherContentValidator.validateFlashcard(validCard).isValid)

        val invalidDifficulty = Flashcard(front = "पानी", back = "ᱫᱟᱜ", difficulty = 10)
        assertFalse(TeacherContentValidator.validateFlashcard(invalidDifficulty).isValid)

        val invalidPathCard = Flashcard(front = "पानी", back = "ᱫᱟᱜ", imageRef = "../evil.png")
        assertFalse(TeacherContentValidator.validateFlashcard(invalidPathCard).isValid)
    }

    @Test
    fun `validateWorksheet rejects questions where correct answer is missing from options`() {
        val invalidQuestion = WorksheetQuestion(
            itemId = "q1",
            worksheetId = "ws1",
            questionNumber = 1,
            promptHindi = "एक",
            promptTargetNative = "ᱢᱤᱫ",
            promptTargetTransliteration = "mid",
            questionType = "MULTIPLE_CHOICE",
            options = listOf("ᱵᱟᱨ", "ᱯᱮ", "ᱯᱳᱱ"), // "ᱢᱤᱫ" is missing!
            correctAnswer = "ᱢᱤᱫ"
        )

        val result = TeacherContentValidator.validateWorksheet("Test", listOf(invalidQuestion))
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.contains("correct answer 'ᱢᱤᱫ' is not among options") })
    }
}

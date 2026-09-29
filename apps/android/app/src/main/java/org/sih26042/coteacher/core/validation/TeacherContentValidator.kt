package org.sih26042.coteacher.core.validation

import org.sih26042.coteacher.core.matching.OlChikiScriptValidator
import org.sih26042.coteacher.core.model.*

/**
 * PHASE 6 — Teacher Content & Input Validator
 *
 * Enforces strict pedagogical and security guards:
 * - Empty text checks
 * - Language code and Ol Chiki script safety
 * - Path traversal prevention for imported filenames
 * - Worksheet question option validity
 */
object TeacherContentValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList()
    )

    fun validatePhrase(
        hindiText: String,
        targetText: String,
        languageCode: String,
        audioPath: String? = null
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (hindiText.isBlank()) {
            errors.add("Hindi phrase cannot be empty")
        }
        if (targetText.isBlank()) {
            errors.add("Target translation cannot be empty")
        }
        if (languageCode !in listOf("sat", "hoc", "unr")) {
            errors.add("Unsupported language code: $languageCode (supported: sat, hoc, unr)")
        }

        // Script validation for Santali
        if (languageCode == "sat" && targetText.isNotBlank()) {
            val scriptCheck = OlChikiScriptValidator.validate(targetText)
            if (!scriptCheck.isValid) {
                errors.add("Target text contains invalid characters: ${scriptCheck.warnings.joinToString(", ")}")
            }
        }

        if (audioPath != null && isPathTraversalAttempt(audioPath)) {
            errors.add("Illegal audio file path: path traversal detected")
        }

        return ValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    fun validateFlashcard(
        card: Flashcard,
        languageCode: String = "sat"
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (card.front.isBlank()) {
            errors.add("Flashcard front text cannot be empty")
        }
        if (card.back.isBlank()) {
            errors.add("Flashcard back text cannot be empty")
        }
        if (card.difficulty !in 1..5) {
            errors.add("Difficulty must be between 1 and 5")
        }
        if (card.imageRef != null && isPathTraversalAttempt(card.imageRef)) {
            errors.add("Illegal image path: path traversal detected")
        }
        if (card.audioRef != null && isPathTraversalAttempt(card.audioRef)) {
            errors.add("Illegal audio path: path traversal detected")
        }

        return ValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    fun validateWorksheet(
        title: String,
        questions: List<WorksheetQuestion>
    ): ValidationResult {
        val errors = mutableListOf<String>()

        if (title.isBlank()) {
            errors.add("Worksheet title cannot be empty")
        }
        if (questions.isEmpty()) {
            errors.add("Worksheet must have at least one question")
        }

        questions.forEachIndexed { idx, q ->
            if (q.promptHindi.isBlank() && q.promptTargetNative.isBlank()) {
                errors.add("Question #${idx + 1} has empty prompt")
            }
            if (q.options.isNotEmpty() && q.correctAnswer !in q.options) {
                errors.add("Question #${idx + 1} correct answer '${q.correctAnswer}' is not among options")
            }
            if (q.visualAsset != null && isPathTraversalAttempt(q.visualAsset)) {
                errors.add("Question #${idx + 1} visual asset has illegal path")
            }
        }

        return ValidationResult(isValid = errors.isEmpty(), errors = errors)
    }

    fun sanitizeFilename(filename: String): String {
        return filename.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            .removePrefix(".")
    }

    fun isPathTraversalAttempt(path: String): Boolean {
        val decoded = path.replace("\\", "/")
        return decoded.contains("../") || decoded.contains("/..") || decoded.startsWith("../") || decoded.contains("..\\")
    }
}

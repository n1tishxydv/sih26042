package org.sih26042.coteacher.core.model

import java.util.UUID

/**
 * PHASE 6 — Teacher Toolkit & Content Authoring Domain Models
 *
 * All models maintain strict provenance separation:
 * - Content authored by teacher is explicitly tagged TEACHER_CREATED
 * - Machine translation is tagged MACHINE_GENERATED
 * - No teacher-authored or unreviewed item is ever tagged VERIFIED
 */

// ---------------------------------------------------------------------------
// Teacher Saved Materials
// ---------------------------------------------------------------------------

enum class MaterialType(val displayName: String, val icon: String) {
    TRANSLATION("Quick Translation", "💬"),
    PHRASE("Classroom Phrase", "🗣️"),
    WORKSHEET("NIPUN Worksheet", "📝"),
    FLASHCARD_SET("Flashcard Deck", "🎴"),
    ACTIVITY("Student Activity", "🎮"),
    LESSON("Custom Lesson", "📚"),
    IMPORTED_PAGE("Imported Page/Image", "📄")
}

data class TeacherMaterial(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val type: MaterialType,
    val languageCode: String = "sat",
    val gradeLevel: GradeLevel = GradeLevel.GRADE_1,
    val domain: SubjectDomain = SubjectDomain.FOUNDATIONAL_NUMERACY,
    /** Serialized JSON payload containing the specific material body */
    val contentJson: String = "{}",
    val provenance: ContentProvenance = ContentProvenance.TEACHER_CREATED,
    val validationStatus: String = "PENDING_VALIDATION",
    val version: Int = 1,
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val tags: List<String> = emptyList(),
    val notes: String = ""
)

// ---------------------------------------------------------------------------
// Quick Translation History
// ---------------------------------------------------------------------------

data class TranslationHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val sourceLanguage: String = "hi",
    val targetLanguage: String = "sat",
    val sourceHindi: String,
    val targetSantali: String,
    val targetLatin: String = "",
    val script: String = "Ol Chiki",
    val provenance: ContentProvenance = ContentProvenance.MACHINE_GENERATED,
    val confidence: Float = 0.85f,
    val audioPath: String? = null,
    val packVersion: String = "1.0",
    val timestampMs: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val flagForCorrection: Boolean = false
)

// ---------------------------------------------------------------------------
// Flashcard Models
// ---------------------------------------------------------------------------

data class Flashcard(
    val id: String = UUID.randomUUID().toString(),
    val front: String,
    val back: String,
    val script: String = "Ol Chiki",
    val transliteration: String = "",
    val imageRef: String? = null,
    val audioRef: String? = null,
    val category: String = "general",
    val difficulty: Int = 1,
    val provenance: ContentProvenance = ContentProvenance.TEACHER_CREATED,
    val isFavorite: Boolean = false,
    val isDifficult: Boolean = false
)

data class FlashcardDeck(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val languageCode: String = "sat",
    val classLevel: GradeLevel = GradeLevel.GRADE_1,
    val category: String = "general",
    val provenance: ContentProvenance = ContentProvenance.TEACHER_CREATED,
    val packVersion: String = "1.0",
    val cards: List<Flashcard> = emptyList(),
    val isTeacherCreated: Boolean = true,
    val createdAtMs: Long = System.currentTimeMillis()
)

// ---------------------------------------------------------------------------
// OCR Extraction & Review Models
// ---------------------------------------------------------------------------

data class OcrExtractionResult(
    val extractedText: String,
    val confidence: Float,
    val detectedScript: String = "Devanagari",
    val processingTimeMs: Long = 0L,
    val isSupportedScript: Boolean = true,
    val warningMessage: String? = null
)

// ---------------------------------------------------------------------------
// Worksheet Generator Models
// ---------------------------------------------------------------------------

enum class WorksheetTemplateType(val displayName: String, val description: String) {
    MATCHING("Template A: Matching", "Match Hindi words/numerals with Santali translations"),
    MULTIPLE_CHOICE("Template B: Multiple Choice", "Choose correct translation from 3-4 options"),
    FILL_SELECT("Template C: Fill / Select", "Complete the sentence or count with correct word"),
    ORDERING("Template D: Ordering", "Sequence numerals or steps in natural order"),
    PICTURE_RECOGNITION("Template E: Picture Recognition", "Identify the correct vocabulary term for a visual")
}

data class GeneratedWorksheet(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val templateType: WorksheetTemplateType,
    val gradeLevel: GradeLevel = GradeLevel.GRADE_1,
    val domain: SubjectDomain = SubjectDomain.FOUNDATIONAL_NUMERACY,
    val instructionsHindi: String,
    val instructionsSantali: String,
    val questions: List<WorksheetQuestion>,
    val provenance: ContentProvenance = ContentProvenance.TEACHER_CREATED,
    val createdAtMs: Long = System.currentTimeMillis()
)

// ---------------------------------------------------------------------------
// Classroom Quick Tools
// ---------------------------------------------------------------------------

data class QuickToolItem(
    val id: String,
    val category: String,
    val labelHindi: String,
    val labelNative: String,
    val labelLatin: String,
    val symbolOrEmoji: String,
    val audioPath: String? = null,
    val numericalValue: Int? = null
)

// ---------------------------------------------------------------------------
// Local Search Result
// ---------------------------------------------------------------------------

enum class SearchResultType(val label: String, val badgeColorHex: Long) {
    PHRASE("Classroom Phrase", 0xFF2E7D32),
    VOCABULARY("FLN Vocabulary", 0xFF1565C0),
    LESSON("NIPUN Lesson", 0xFF6A1B9A),
    WORKSHEET("Worksheet", 0xFFEF6C00),
    FLASHCARD("Flashcard", 0xFFC2185B),
    SAVED_MATERIAL("Teacher Material", 0xFF00838F)
}

data class SearchResultItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val nativeScript: String,
    val latinTransliteration: String = "",
    val type: SearchResultType,
    val category: String,
    val provenance: ContentProvenance,
    val audioPath: String? = null
)

package org.sih26042.coteacher

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeDest : NavKey
@Serializable data object LiveClassDest : NavKey
@Serializable data class TranslationDetailDest(
    val hindiText: String,
    val nativeScriptText: String,
    val latinTransliteration: String,
    val provenanceState: String,
    val confidence: Float? = null,
    val audioPath: String?,
    val pedagogicalContext: String? = null,
    val engineName: String = "IndicTrans2-ONNX-INT8",
    val modelVersion: String = "v1.0-distilled-200m",
    val latencyMs: Long = 0L,
    val warnings: List<String> = emptyList(),
    val normalizedHindi: String = ""
) : NavKey
@Serializable data object WorksheetsDest : NavKey
@Serializable data object FlashcardsDest : NavKey
@Serializable data object ActivitiesDest : NavKey
@Serializable data object LanguagePacksDest : NavKey
@Serializable data object PerformanceDest : NavKey
@Serializable data object SettingsDest : NavKey
@Serializable data object ValidatorDest : NavKey

// Phase 5 — NIPUN/FLN Lesson Flow
@Serializable data object LessonListDest : NavKey
@Serializable data class LessonDetailDest(val lessonId: String) : NavKey
@Serializable data object ActivityRunnerDest : NavKey
@Serializable data object SessionSummaryDest : NavKey

// Phase 6 — Teacher Toolkit & Classroom Utilities
@Serializable data object TeacherToolkitDest : NavKey
@Serializable data object QuickTranslateDest : NavKey
@Serializable data object TranslationHistoryDest : NavKey
@Serializable data object SavedMaterialsDest : NavKey
@Serializable data object FlashcardDecksDest : NavKey
@Serializable data class FlashcardPlayerDest(val deckId: String) : NavKey
@Serializable data class CreateFlashcardDest(val deckId: String? = null) : NavKey
@Serializable data object ImportImageDest : NavKey
@Serializable data class OcrReviewDest(val imagePath: String? = null, val initialText: String = "") : NavKey
@Serializable data object ImportPdfDest : NavKey
@Serializable data class PdfViewerDest(val pdfPath: String) : NavKey
@Serializable data object WorksheetBuilderDest : NavKey
@Serializable data class WorksheetPreviewDest(val worksheetId: String) : NavKey
@Serializable data object ClassroomQuickToolsDest : NavKey
@Serializable data class LocalSearchDest(val initialQuery: String = "") : NavKey
@Serializable data object CreateMaterialDest : NavKey

// Phase 8 — SIH Judge Demonstration
@Serializable data object JudgeModeDest : NavKey

package org.sih26042.coteacher

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import org.sih26042.coteacher.core.content.SantaliNipunContent
import org.sih26042.coteacher.ui.screens.*

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as SIHApplication
    val container = app.container

    val backStack = rememberNavBackStack(HomeDest)

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            } else {
                (context as? Activity)?.finish()
            }
        },
        entryProvider = entryProvider {
            entry<HomeDest> {
                HomeScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) }
                )
            }
            entry<LiveClassDest> {
                LiveClassScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) }
                )
            }
            entry<TranslationDetailDest> { dest ->
                TranslationDetailScreen(
                    args = dest,
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<WorksheetsDest> {
                WorksheetsScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<FlashcardsDest> {
                FlashcardsScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ActivitiesDest> {
                ActivitiesScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<LanguagePacksDest> {
                LanguagePacksScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<PerformanceDest> {
                PerformanceScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<SettingsDest> {
                SettingsScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() },
                    onNavigateToValidator = { backStack.add(ValidatorDest) }
                )
            }
            entry<ValidatorDest> {
                ValidatorScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            // Phase 5 — NIPUN/FLN Lesson Flow
            entry<LessonListDest> {
                LessonListScreen(
                    lessonEngine = container.lessonEngine,
                    onStartLesson = { lesson ->
                        backStack.add(LessonDetailDest(lessonId = lesson.lessonId))
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<LessonDetailDest> { dest ->
                val lesson = SantaliNipunContent.LESSON_REGISTRY[dest.lessonId]
                if (lesson != null) {
                    LessonDetailScreen(
                        lesson = lesson,
                        lessonEngine = container.lessonEngine,
                        onStartLesson = {
                            backStack.add(ActivityRunnerDest)
                        },
                        onBack = { backStack.removeLastOrNull() }
                    )
                }
            }
            entry<ActivityRunnerDest> {
                ActivityScreen(
                    lessonEngine = container.lessonEngine,
                    onLessonComplete = {
                        // Replace ActivityRunner with Summary (don't leave runner on back stack)
                        backStack.removeLastOrNull()
                        backStack.add(SessionSummaryDest)
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<SessionSummaryDest> {
                SessionSummaryScreen(
                    lessonEngine = container.lessonEngine,
                    onStartNextLesson = { lessonId ->
                        // Replace summary + pop to clean state for next lesson
                        backStack.removeLastOrNull()
                        backStack.add(LessonDetailDest(lessonId = lessonId))
                    },
                    onGoHome = {
                        // Pop back to Home
                        while (backStack.size > 1) backStack.removeLastOrNull()
                    }
                )
            }

            // Phase 6 — Teacher Toolkit & Classroom Utilities
            entry<TeacherToolkitDest> {
                TeacherToolkitScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<QuickTranslateDest> {
                QuickTranslateScreen(
                    container = container,
                    onNavigateToHistory = { backStack.add(TranslationHistoryDest) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<TranslationHistoryDest> {
                TranslationHistoryScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<SavedMaterialsDest> {
                SavedMaterialsScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<FlashcardDecksDest> {
                FlashcardDecksScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<FlashcardPlayerDest> { dest ->
                FlashcardPlayerScreen(
                    deckId = dest.deckId,
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<CreateFlashcardDest> { dest ->
                CreateFlashcardScreen(
                    deckId = dest.deckId,
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ImportImageDest> {
                ImportImageScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<OcrReviewDest> { dest ->
                OcrReviewScreen(
                    imagePath = dest.imagePath,
                    initialText = dest.initialText,
                    container = container,
                    onNavigate = { destKey -> backStack.add(destKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ImportPdfDest> {
                ImportPdfScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<PdfViewerDest> { dest ->
                PdfViewerScreen(
                    pdfPath = dest.pdfPath,
                    container = container,
                    onNavigate = { destKey -> backStack.add(destKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<WorksheetBuilderDest> {
                WorksheetBuilderScreen(
                    container = container,
                    onNavigate = { dest -> backStack.add(dest) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<WorksheetPreviewDest> { dest ->
                WorksheetPreviewScreen(
                    worksheetId = dest.worksheetId,
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ClassroomQuickToolsDest> {
                ClassroomQuickToolsScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<LocalSearchDest> { dest ->
                LocalSearchScreen(
                    initialQuery = dest.initialQuery,
                    container = container,
                    onNavigate = { destKey -> backStack.add(destKey) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<CreateMaterialDest> {
                CreateMaterialScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<JudgeModeDest> {
                JudgeModeScreen(
                    container = container,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}

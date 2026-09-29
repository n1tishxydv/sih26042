package org.sih26042.coteacher.di

import android.content.Context
import org.sih26042.coteacher.core.audio.AudioPlayerService
import org.sih26042.coteacher.core.engine.*
import org.sih26042.coteacher.data.ClassroomRepository
import org.sih26042.coteacher.data.LanguagePackRepository
import org.sih26042.coteacher.domain.GetFlnContentUseCase
import org.sih26042.coteacher.domain.ManageLanguagePacksUseCase
import org.sih26042.coteacher.domain.ProcessTeacherSpeechUseCase
import org.sih26042.coteacher.domain.lesson.LessonEngine
import org.sih26042.coteacher.domain.lesson.SessionRepository

class AppContainer(val context: Context) {

    val sessionRepository: SessionRepository by lazy {
        SessionRepository(context)
    }

    val lessonEngine: LessonEngine by lazy {
        LessonEngine(sessionRepository)
    }
    val classroomRepository: ClassroomRepository by lazy {
        ClassroomRepository(context)
    }

    val languagePackRepository: LanguagePackRepository by lazy {
        LanguagePackRepository(context)
    }

    val asrEngine: OfflineHindiAsrEngine by lazy {
        OfflineHindiAsrEngine()
    }

    val neuralMtEngine: NeuralTranslationEngine by lazy {
        NeuralTranslationEngine()
    }

    val ttsEngine: OfflineTtsEngine by lazy {
        OfflineTtsEngine()
    }

    val modelLifecycleManager: ModelLifecycleManager by lazy {
        ModelLifecycleManager(asrEngine, neuralMtEngine, ttsEngine)
    }

    val verifiedPhraseEngine: VerifiedPhraseTranslationEngine by lazy {
        VerifiedPhraseTranslationEngine(classroomRepository.phraseMatcher)
    }

    val orchestratedTranslationEngine: OrchestratedTranslationEngine by lazy {
        OrchestratedTranslationEngine(verifiedPhraseEngine, neuralMtEngine)
    }

    val audioPlayerService: AudioPlayerService by lazy {
        AudioPlayerService(context)
    }

    val microphoneRecorder: org.sih26042.coteacher.core.audio.MicrophoneRecorder by lazy {
        org.sih26042.coteacher.core.audio.MicrophoneRecorder(context)
    }

    val processTeacherSpeechUseCase: ProcessTeacherSpeechUseCase by lazy {
        ProcessTeacherSpeechUseCase(
            asrEngine = asrEngine,
            orchestrator = orchestratedTranslationEngine,
            classroomRepository = classroomRepository,
            languagePackRepository = languagePackRepository
        )
    }

    val flnContentUseCase: GetFlnContentUseCase by lazy {
        GetFlnContentUseCase(classroomRepository)
    }

    val managePacksUseCase: ManageLanguagePacksUseCase by lazy {
        ManageLanguagePacksUseCase(languagePackRepository)
    }

    // Phase 6 — Teacher Toolkit & Authoring Infrastructure
    val teacherMaterialRepository: org.sih26042.coteacher.data.TeacherMaterialRepository by lazy {
        org.sih26042.coteacher.data.TeacherMaterialRepository(context, classroomRepository)
    }

    val localSearchService: org.sih26042.coteacher.core.search.LocalSearchService by lazy {
        org.sih26042.coteacher.core.search.LocalSearchService(classroomRepository, teacherMaterialRepository)
    }

    val imageIngestionService: org.sih26042.coteacher.core.media.ImageIngestionService by lazy {
        org.sih26042.coteacher.core.media.ImageIngestionService(context)
    }

    val pdfIngestionService: org.sih26042.coteacher.core.media.PdfIngestionService by lazy {
        org.sih26042.coteacher.core.media.PdfIngestionService(context)
    }

    val offlineOcrService: org.sih26042.coteacher.core.media.OfflineOcrService by lazy {
        org.sih26042.coteacher.core.media.OfflineOcrService()
    }

    val worksheetPdfExporter: org.sih26042.coteacher.core.worksheet.WorksheetPdfExporter by lazy {
        org.sih26042.coteacher.core.worksheet.WorksheetPdfExporter(context)
    }

    // Phase 7 — Language-Pack Lifecycle & Sync Queue Infrastructure
    val packLifecycleManager: org.sih26042.coteacher.core.pack.LanguagePackLifecycleManager by lazy {
        org.sih26042.coteacher.core.pack.LanguagePackLifecycleManager(context, languagePackRepository)
    }

    val pendingSyncQueue: org.sih26042.coteacher.core.sync.PendingSyncQueue by lazy {
        org.sih26042.coteacher.core.sync.PendingSyncQueue(context)
    }

    val syncCoordinator: org.sih26042.coteacher.core.sync.SyncCoordinator by lazy {
        org.sih26042.coteacher.core.sync.SyncCoordinator(pendingSyncQueue)
    }

    val storageDiagnosticsManager: org.sih26042.coteacher.core.storage.StorageDiagnosticsManager by lazy {
        org.sih26042.coteacher.core.storage.StorageDiagnosticsManager(context)
    }
}

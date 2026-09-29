package org.sih26042.coteacher.di

import android.content.Context
import org.sih26042.coteacher.core.audio.AudioPlayerService
import org.sih26042.coteacher.core.engine.*
import org.sih26042.coteacher.data.ClassroomRepository
import org.sih26042.coteacher.data.LanguagePackRepository
import org.sih26042.coteacher.domain.GetFlnContentUseCase
import org.sih26042.coteacher.domain.ManageLanguagePacksUseCase
import org.sih26042.coteacher.domain.ProcessTeacherSpeechUseCase

class AppContainer(val context: Context) {
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
}

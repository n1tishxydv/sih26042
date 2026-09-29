package org.sih26042.coteacher.domain

import org.sih26042.coteacher.core.engine.AsrResult
import org.sih26042.coteacher.core.engine.OfflineHindiAsrEngine
import org.sih26042.coteacher.core.engine.OrchestratedTranslationEngine
import org.sih26042.coteacher.core.matching.TextNormalizer
import org.sih26042.coteacher.core.model.ClassroomInteractionResult
import org.sih26042.coteacher.data.ClassroomRepository
import org.sih26042.coteacher.data.LanguagePackRepository

class ProcessTeacherSpeechUseCase(
    private val asrEngine: OfflineHindiAsrEngine,
    private val orchestrator: OrchestratedTranslationEngine,
    private val classroomRepository: ClassroomRepository,
    private val languagePackRepository: LanguagePackRepository
) {
    suspend fun execute(mockTeacherSpokenText: String? = null): ClassroomInteractionResult {
        // 1. Recognize Teacher Speech
        val asrResult = asrEngine.recognizeSpeech(mockTeacherSpokenText)
        return executeWithAsrResult(asrResult)
    }

    suspend fun executeWithAsrResult(asrResult: AsrResult): ClassroomInteractionResult {
        // 2. Normalize text deterministically (NFC, whitespace, danda, classroom aliases)
        val normalizedHindi = TextNormalizer.normalize(asrResult.transcript)

        // 3. Get active target language
        val activePack = languagePackRepository.activePack.value

        // 4. Orchestrate phrase matching (Phase 2: fast-path only, stops at match decision)
        val result = orchestrator.process(
            normalizedHindi = normalizedHindi,
            targetLanguageCode = activePack.languageCode,
            asrLatencyMs = asrResult.durationMs
        )

        // 5. Record real latency telemetry
        classroomRepository.recordLatency(result.latency)

        return result
    }
}

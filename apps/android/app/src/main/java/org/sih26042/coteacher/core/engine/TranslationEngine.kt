package org.sih26042.coteacher.core.engine

import org.sih26042.coteacher.core.matching.PhraseMatcher
import org.sih26042.coteacher.core.model.ClassroomInteractionResult
import org.sih26042.coteacher.core.model.ClassroomPhrase
import org.sih26042.coteacher.core.model.LatencyBreakdown
import org.sih26042.coteacher.core.model.ProvenanceState

data class EngineTranslationOutput(
    val outputNativeScript: String,
    val outputTransliteration: String,
    val provenance: ProvenanceState,
    val confidence: Float,
    val matchedPhrase: ClassroomPhrase? = null,
    val audioPath: String? = null,
    val engineDurationMs: Long = 0
)

interface TranslationEngine {
    suspend fun translate(
        normalizedHindi: String,
        targetLanguageCode: String
    ): EngineTranslationOutput
}

/**
 * Fast-path engine: Resolves pre-verified native classroom phrases.
 * Target: ~15ms execution, Zero ML hallucination, native audio ready.
 */
class VerifiedPhraseTranslationEngine(
    private val matcher: PhraseMatcher
) : TranslationEngine {

    override suspend fun translate(
        normalizedHindi: String,
        targetLanguageCode: String
    ): EngineTranslationOutput {
        val start = System.currentTimeMillis()
        val matchResult = matcher.match(normalizedHindi)
        val duration = System.currentTimeMillis() - start

        return if (matchResult.matchedPhrase != null && matchResult.confidence >= 0.75f) {
            val p = matchResult.matchedPhrase
            val isExplicitlyVerified = (p.verificationStatus.equals("VERIFIED", ignoreCase = true) || p.provenance == ProvenanceState.VERIFIED)
            val safeProvenance = if (isExplicitlyVerified && p.audioPath != null) {
                ProvenanceState.VERIFIED
            } else {
                ProvenanceState.PENDING_VALIDATION
            }
            EngineTranslationOutput(
                outputNativeScript = p.targetNativeScript,
                outputTransliteration = p.targetTransliterationLatin,
                provenance = safeProvenance,
                confidence = matchResult.confidence,
                matchedPhrase = p,
                audioPath = p.audioPath,
                engineDurationMs = duration
            )
        } else {
            EngineTranslationOutput(
                outputNativeScript = "",
                outputTransliteration = "",
                provenance = ProvenanceState.NO_MATCH,
                confidence = matchResult.confidence,
                matchedPhrase = null,
                audioPath = null,
                engineDurationMs = duration
            )
        }
    }
}

/**
 * Backward compatibility typealias mapping NeuralTranslationEngine to OfflineHindiSantaliMtEngine.
 */
typealias NeuralTranslationEngine = OfflineHindiSantaliMtEngine

/**
 * Orchestrator: Coordinates fast-path verified phrase matching first,
 * falling back to quantized neural MT (OfflineHindiSantaliMtEngine) only when required.
 */
class OrchestratedTranslationEngine(
    private val verifiedEngine: VerifiedPhraseTranslationEngine,
    private val neuralEngine: OfflineHindiSantaliMtEngine,
    val enableNeuralFallback: Boolean = true
) {
    suspend fun process(
        normalizedHindi: String,
        targetLanguageCode: String,
        asrLatencyMs: Long
    ): ClassroomInteractionResult {
        // 1. Try Verified Fast-Path
        val verifiedOutput = verifiedEngine.translate(normalizedHindi, targetLanguageCode)

        if (verifiedOutput.provenance == ProvenanceState.VERIFIED || verifiedOutput.provenance == ProvenanceState.PENDING_VALIDATION) {
            val audioLatency = if (verifiedOutput.audioPath != null) 40L else 0L
            val totalLatency = asrLatencyMs + verifiedOutput.engineDurationMs + audioLatency
            return ClassroomInteractionResult(
                recognizedHindi = normalizedHindi,
                outputNativeScript = verifiedOutput.outputNativeScript,
                outputTransliteration = verifiedOutput.outputTransliteration,
                provenance = verifiedOutput.provenance,
                confidence = verifiedOutput.confidence,
                audioPath = verifiedOutput.audioPath,
                latency = LatencyBreakdown(
                    asrLatencyMs = asrLatencyMs,
                    matchLatencyMs = verifiedOutput.engineDurationMs,
                    audioLatencyMs = audioLatency,
                    totalLatencyMs = totalLatency,
                    pipelineMode = if (verifiedOutput.provenance == ProvenanceState.VERIFIED) "VERIFIED_FAST_PATH" else "PENDING_VALIDATION_MATCH"
                ),
                matchedPhrase = verifiedOutput.matchedPhrase
            )
        }

        // If fallback disabled, stop at NO_MATCH
        if (!enableNeuralFallback) {
            return ClassroomInteractionResult(
                recognizedHindi = normalizedHindi,
                outputNativeScript = "",
                outputTransliteration = "",
                provenance = ProvenanceState.NO_MATCH,
                confidence = verifiedOutput.confidence,
                audioPath = null,
                latency = LatencyBreakdown(
                    asrLatencyMs = asrLatencyMs,
                    matchLatencyMs = verifiedOutput.engineDurationMs,
                    audioLatencyMs = 0L,
                    totalLatencyMs = asrLatencyMs + verifiedOutput.engineDurationMs,
                    pipelineMode = "PHRASE_ONLY_PHASE2"
                ),
                matchedPhrase = null
            )
        }

        // 2. Real Offline Neural MT Fallback (Phase 3)
        val translationRes = neuralEngine.translateSentence(normalizedHindi, targetLanguageCode)
        val totalLatency = asrLatencyMs + verifiedOutput.engineDurationMs + translationRes.latencyMs

        return ClassroomInteractionResult(
            recognizedHindi = normalizedHindi,
            outputNativeScript = translationRes.translatedText,
            outputTransliteration = translationRes.transliteratedText,
            provenance = translationRes.provenance, // MACHINE_GENERATED
            confidence = translationRes.confidence, // null (no fake confidence!)
            audioPath = null, // No fake audio synthesis: text-only fallback until TTS gate passes
            latency = LatencyBreakdown(
                asrLatencyMs = asrLatencyMs,
                matchLatencyMs = verifiedOutput.engineDurationMs + translationRes.latencyMs,
                audioLatencyMs = 0L,
                totalLatencyMs = totalLatency,
                pipelineMode = "NEURAL_FALLBACK"
            ),
            matchedPhrase = null
        )
    }
}

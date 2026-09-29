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
            EngineTranslationOutput(
                outputNativeScript = p.targetNativeScript,
                outputTransliteration = p.targetTransliterationLatin,
                provenance = ProvenanceState.VERIFIED,
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
 * Fallback engine: Quantized on-device MT (INT8 TFLite / ONNX).
 * For teacher prompts not in the verified phrase bank.
 * Explicitly badged as MACHINE_GENERATED.
 */
class NeuralTranslationEngine : TranslationEngine {
    private var isLoaded: Boolean = false

    fun isModelLoaded(): Boolean = isLoaded

    suspend fun load() {
        kotlinx.coroutines.delay(180)
        isLoaded = true
    }

    suspend fun unload() {
        isLoaded = false
    }

    override suspend fun translate(
        normalizedHindi: String,
        targetLanguageCode: String
    ): EngineTranslationOutput {
        val start = System.currentTimeMillis()
        if (!isLoaded) {
            load()
        }
        // Simulated quantized neural MT inference (~600-900ms on low-cost quad-core ARM)
        kotlinx.coroutines.delay(450)

        val duration = System.currentTimeMillis() - start

        // Heuristic vocabulary substitution for demonstration of machine-generated output
        val tokens = normalizedHindi.split(" ")
        val translatedTokens = tokens.map { token ->
            when (token) {
                "किताब" -> "ᱯᱩᱛᱷᱤ"
                "कलम" -> "ᱠᱚᱞᱚᱢ"
                "पानी" -> "ᱫᱟᱜ"
                "हाथ" -> "ᱛᱤ"
                "पैर" -> "ᱡᱟᱝᱜᱟ"
                "बैठो", "बैठ" -> "ᱫᱩᱲᱩᱵ"
                "खड़े", "खड़ा" -> "ᱛᱤᱸᱜᱩᱱ"
                "सुनों", "सुनो" -> "ᱟᱧᱡᱚᱢ"
                "जाओ" -> "ᱪᱟᱞᱟᱜ"
                "आओ" -> "ᱦᱤᱡᱩᱜ"
                else -> "[${token}]"
            }
        }
        val outputNative = translatedTokens.joinToString(" ")
        val outputLatin = "Machine translated: " + tokens.joinToString(" ")

        val confidence = if (tokens.size <= 4) 0.78f else 0.65f
        val provenance = if (confidence >= 0.70f) ProvenanceState.MACHINE_GENERATED else ProvenanceState.LOW_CONFIDENCE

        return EngineTranslationOutput(
            outputNativeScript = outputNative,
            outputTransliteration = outputLatin,
            provenance = provenance,
            confidence = confidence,
            matchedPhrase = null,
            audioPath = null, // Machine generated does not have pre-verified native audio
            engineDurationMs = duration
        )
    }
}

/**
 * Orchestrator: Coordinates fast-path verified phrase matching first,
 * falling back to quantized neural MT only when required.
 */
class OrchestratedTranslationEngine(
    private val verifiedEngine: VerifiedPhraseTranslationEngine,
    private val neuralEngine: NeuralTranslationEngine
) {
    suspend fun process(
        normalizedHindi: String,
        targetLanguageCode: String,
        asrLatencyMs: Long
    ): ClassroomInteractionResult {
        val startTotal = System.currentTimeMillis()

        // 1. Try Verified Fast-Path
        val verifiedOutput = verifiedEngine.translate(normalizedHindi, targetLanguageCode)

        if (verifiedOutput.provenance == ProvenanceState.VERIFIED) {
            val totalLatency = asrLatencyMs + verifiedOutput.engineDurationMs + 80L // ~80ms audio start
            return ClassroomInteractionResult(
                recognizedHindi = normalizedHindi,
                outputNativeScript = verifiedOutput.outputNativeScript,
                outputTransliteration = verifiedOutput.outputTransliteration,
                provenance = ProvenanceState.VERIFIED,
                confidence = verifiedOutput.confidence,
                audioPath = verifiedOutput.audioPath,
                latency = LatencyBreakdown(
                    asrLatencyMs = asrLatencyMs,
                    matchLatencyMs = verifiedOutput.engineDurationMs,
                    audioLatencyMs = 80L,
                    totalLatencyMs = totalLatency,
                    pipelineMode = "VERIFIED_FAST_PATH"
                ),
                matchedPhrase = verifiedOutput.matchedPhrase
            )
        }

        // 2. Fallback to Quantized Neural MT
        val neuralOutput = neuralEngine.translate(normalizedHindi, targetLanguageCode)
        val totalLatency = asrLatencyMs + verifiedOutput.engineDurationMs + neuralOutput.engineDurationMs + 250L // TTS latency

        return ClassroomInteractionResult(
            recognizedHindi = normalizedHindi,
            outputNativeScript = neuralOutput.outputNativeScript,
            outputTransliteration = neuralOutput.outputTransliteration,
            provenance = neuralOutput.provenance,
            confidence = neuralOutput.confidence,
            audioPath = null,
            latency = LatencyBreakdown(
                asrLatencyMs = asrLatencyMs,
                matchLatencyMs = verifiedOutput.engineDurationMs + neuralOutput.engineDurationMs,
                audioLatencyMs = 250L,
                totalLatencyMs = totalLatency,
                pipelineMode = "NEURAL_FALLBACK"
            ),
            matchedPhrase = null
        )
    }
}

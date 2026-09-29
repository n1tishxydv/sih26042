package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sih26042.coteacher.core.matching.SantaliTtsTextPreprocessor
import org.sih26042.coteacher.core.model.ProvenanceState
import java.util.UUID

enum class TtsLifecycleState {
    TTS_NOT_LOADED,
    TTS_LOADING,
    TTS_READY,
    TTS_BUSY,
    TTS_UNLOADING,
    TTS_FAILED,
    TTS_UNAVAILABLE
}

data class TtsResult(
    val audioPath: String? = null,
    val durationMs: Long = 0L,
    val modelId: String = "indic-parler-tts-sat",
    val modelVersion: String = "v1.0-research",
    val provenance: ProvenanceState = ProvenanceState.MACHINE_GENERATED,
    val engine: String = "OfflineSantaliTtsEngine",
    val latencyMs: Long = 0L,
    val traceId: String = UUID.randomUUID().toString(),
    val warnings: List<String> = emptyList(),
    val error: String? = null
)

interface OfflineTtsContract {
    val lifecycleState: TtsLifecycleState
    suspend fun initialize(): Result<Unit>
    fun isReady(): Boolean
    suspend fun synthesize(text: String, languageCode: String = "sat", traceId: String = UUID.randomUUID().toString()): TtsResult
    fun play(audioPath: String)
    fun stop()
    fun release()
}

/**
 * On-Device Santali Text-to-Speech Engine implementing strict Phase 4 Feasibility Gate.
 * 
 * Safety Policy:
 * - Indic Parler-TTS requires > 3.2 GB RAM (infeasible for 2 GB devices).
 * - Piper has zero Santali checkpoints.
 * - Meta MMS requires research-only Roman IPA with robotic prosody.
 * - This engine strictly enforces TTS_UNAVAILABLE on target to prevent fake audio generation
 *   or low-memory device crashes.
 */
class OfflineSantaliTtsEngine : OfflineTtsContract, TtsEngine {

    private val mutex = Mutex()
    private var _lifecycleState = TtsLifecycleState.TTS_UNAVAILABLE

    override val lifecycleState: TtsLifecycleState
        get() = _lifecycleState

    override suspend fun initialize(): Result<Unit> = mutex.withLock {
        // Enforce Phase 4 Feasibility Gate: Gated as TTS_UNAVAILABLE on 2 GB Android devices
        _lifecycleState = TtsLifecycleState.TTS_UNAVAILABLE
        Result.failure(
            IllegalStateException(
                "TTS_UNAVAILABLE_ON_TARGET: No verified offline Santali TTS model exists within the 256 MB heap budget on 2 GB physical RAM."
            )
        )
    }

    override fun isReady(): Boolean = false

    override suspend fun synthesize(
        text: String,
        languageCode: String,
        traceId: String
    ): TtsResult = mutex.withLock {
        val startNs = System.nanoTime()

        // 1. Text Normalization & Script Inspection
        val preprocessed = SantaliTtsTextPreprocessor.preprocess(text)
        val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L

        // Return explicit TTS_UNAVAILABLE result without fake audio
        TtsResult(
            audioPath = null,
            durationMs = 0L,
            modelId = "indic-parler-tts-sat",
            modelVersion = "v1.0-research",
            provenance = ProvenanceState.MACHINE_GENERATED,
            engine = "OfflineSantaliTtsEngine",
            latencyMs = elapsedMs,
            traceId = traceId,
            warnings = preprocessed.warnings + listOf("Native Santali TTS gated as unavailable; text-only fallback preserved"),
            error = "TTS_UNAVAILABLE: Real mobile model does not meet the 256 MB memory budget on 2 GB devices"
        )
    }

    override fun play(audioPath: String) {
        // No-op for unavailable TTS
    }

    override fun stop() {
        // No-op
    }

    override fun release() {
        _lifecycleState = TtsLifecycleState.TTS_NOT_LOADED
    }

    // --- Backward Compatibility with existing TtsEngine interface ---
    override fun isLoaded(): Boolean = false

    override suspend fun load() {
        initialize()
    }

    override suspend fun unload() {
        release()
    }

    override suspend fun synthesize(text: String, languageCode: String): Result<String> {
        return Result.failure(
            IllegalStateException("TTS_UNAVAILABLE: Real mobile model does not meet the 256 MB memory budget on 2 GB devices")
        )
    }
}

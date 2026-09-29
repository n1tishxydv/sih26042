package org.sih26042.coteacher.core.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Preprocessing mode toggle for comparative benchmarking:
 * RAW_AUDIO: PCM chunks passed untouched directly to ASR.
 * PROCESSED_AUDIO: High-pass filtered and energy VAD gated.
 */
enum class PreprocessingMode {
    RAW_AUDIO,
    PROCESSED_AUDIO
}

/**
 * Interface for audio preprocessing stages prior to ASR acoustic feature extraction.
 */
interface AudioPreprocessor {
    val mode: PreprocessingMode
    fun processChunk(pcmChunk: ShortArray): ShortArray
    fun detectVoiceActivity(pcmChunk: ShortArray): Boolean
    fun computeRms(pcmChunk: ShortArray): Float
    fun reset()
}

/**
 * Production AudioPreprocessor for classroom environments.
 * 
 * Pipeline:
 * 1. 80 Hz Single-pole/Biquad High-Pass Filter:
 *    Removes mechanical desk rumble, HVAC hum, and tablet handling noise (< 80 Hz)
 *    without touching Hindi vowel fundamentals (F0 >= 100 Hz).
 * 2. Energy-based Voice Activity Detection (VAD):
 *    Computes Frame Energy RMS. Tracks background acoustic noise floor with adaptive hangover
 *    frames to signal speech onset and automatic end-of-speech silence cutoff.
 */
class DefaultAudioPreprocessor(
    override val mode: PreprocessingMode = PreprocessingMode.PROCESSED_AUDIO,
    private val sampleRate: Int = 16000,
    private val highPassCutoffHz: Float = 80.0f,
    private val vadRmsThreshold: Float = 120.0f,
    private val vadHangoverFrames: Int = 5 // ~500ms hangover at 100ms chunks
) : AudioPreprocessor {

    // High-pass biquad filter state
    private var x1: Float = 0f
    private var x2: Float = 0f
    private var y1: Float = 0f
    private var y2: Float = 0f

    private var b0: Float = 1f
    private var b1: Float = 0f
    private var b2: Float = 0f
    private var a1: Float = 0f
    private var a2: Float = 0f

    // VAD tracking
    private var hangoverCounter: Int = 0
    private var backgroundRmsEstimate: Float = 60.0f

    init {
        setupHighPassFilter()
    }

    private fun setupHighPassFilter() {
        val omega = 2.0f * PI.toFloat() * (highPassCutoffHz / sampleRate)
        val alpha = sin(omega) / (2.0f * 0.7071f) // Q = 0.7071 (Butterworth)
        val cosW = cos(omega)

        val a0 = 1.0f + alpha
        b0 = ((1.0f + cosW) / 2.0f) / a0
        b1 = (-(1.0f + cosW)) / a0
        b2 = ((1.0f + cosW) / 2.0f) / a0
        a1 = (-2.0f * cosW) / a0
        a2 = (1.0f - alpha) / a0
    }

    override fun processChunk(pcmChunk: ShortArray): ShortArray {
        if (mode == PreprocessingMode.RAW_AUDIO) {
            return pcmChunk.clone()
        }

        val output = ShortArray(pcmChunk.size)
        for (i in pcmChunk.indices) {
            val x0 = pcmChunk[i].toFloat()
            val y0 = b0 * x0 + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2

            x2 = x1
            x1 = x0
            y2 = y1
            y1 = y0

            // Clamp to 16-bit signed PCM range [-32768, 32767]
            val clamped = y0.coerceIn(-32768.0f, 32767.0f)
            output[i] = clamped.toInt().toShort()
        }
        return output
    }

    override fun detectVoiceActivity(pcmChunk: ShortArray): Boolean {
        val rms = computeRms(pcmChunk)

        // Slowly adapt background noise floor when signal is quiet
        if (rms < vadRmsThreshold) {
            backgroundRmsEstimate = 0.95f * backgroundRmsEstimate + 0.05f * rms
        }

        val dynamicThreshold = maxOf(vadRmsThreshold, backgroundRmsEstimate * 2.2f)
        val isVoice = rms >= dynamicThreshold

        if (isVoice) {
            hangoverCounter = vadHangoverFrames
            return true
        } else if (hangoverCounter > 0) {
            hangoverCounter--
            return true
        }

        return false
    }

    override fun computeRms(pcmChunk: ShortArray): Float {
        if (pcmChunk.isEmpty()) return 0f
        var sumSquares = 0.0
        for (sample in pcmChunk) {
            val s = sample.toDouble()
            sumSquares += s * s
        }
        return sqrt(sumSquares / pcmChunk.size).toFloat()
    }

    override fun reset() {
        x1 = 0f
        x2 = 0f
        y1 = 0f
        y2 = 0f
        hangoverCounter = 0
        backgroundRmsEstimate = 60.0f
    }
}

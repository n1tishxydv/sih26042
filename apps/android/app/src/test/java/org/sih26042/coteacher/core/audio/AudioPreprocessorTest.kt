package org.sih26042.coteacher.core.audio

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class AudioPreprocessorTest {

    private lateinit var preprocessor: DefaultAudioPreprocessor

    @Before
    fun setUp() {
        preprocessor = DefaultAudioPreprocessor(
            mode = PreprocessingMode.PROCESSED_AUDIO,
            sampleRate = 16000,
            highPassCutoffHz = 80.0f,
            vadRmsThreshold = 100.0f,
            vadHangoverFrames = 3
        )
    }

    @Test
    fun testRawAudioBypassModeReturnsIdenticalSamples() {
        val rawPreprocessor = DefaultAudioPreprocessor(mode = PreprocessingMode.RAW_AUDIO)
        val input = shortArrayOf(100, -200, 300, -400, 500)
        val output = rawPreprocessor.processChunk(input)

        assertArrayEquals(input, output)
    }

    @Test
    fun testHighPassFilterAttenuatesLowFrequencyRumble() {
        // Generate 30 Hz low-frequency rumble (sample rate 16000)
        val numSamples = 1600
        val lowFreqChunk = ShortArray(numSamples) { i ->
            (sin(2.0 * PI * 30.0 * i / 16000.0) * 10000.0).toInt().toShort()
        }

        val inputRms = preprocessor.computeRms(lowFreqChunk)
        val processed = preprocessor.processChunk(lowFreqChunk)
        // Skip filter startup transient, take second half
        val steadyState = processed.copyOfRange(800, 1600)
        val outputRms = preprocessor.computeRms(steadyState)

        assertTrue(
            "High-pass filter must attenuate 30 Hz rumble by at least 40% (input: $inputRms, output: $outputRms)",
            outputRms < inputRms * 0.60f
        )
    }

    @Test
    fun testVoiceActivityDetectionWithSilenceAndSpeech() {
        // Pure silence frame
        val silence = ShortArray(1600) { 0 }
        assertFalse("Silence must not trigger VAD", preprocessor.detectVoiceActivity(silence))

        // Simulated voice frame (RMS ~ 5000)
        val speech = ShortArray(1600) { i ->
            (sin(2.0 * PI * 200.0 * i / 16000.0) * 5000.0).toInt().toShort()
        }
        assertTrue("Speech must trigger VAD", preprocessor.detectVoiceActivity(speech))

        // Subsequent silence frame should remain true due to hangover frames
        assertTrue("Hangover frame 1 should remain active", preprocessor.detectVoiceActivity(silence))
        assertTrue("Hangover frame 2 should remain active", preprocessor.detectVoiceActivity(silence))
        assertTrue("Hangover frame 3 should remain active", preprocessor.detectVoiceActivity(silence))

        // Once hangover expires, silence returns false
        assertFalse("VAD must turn off after hangover expires", preprocessor.detectVoiceActivity(silence))
    }

    @Test
    fun testRmsComputationAccuracy() {
        val constantSignal = ShortArray(1000) { 100 }
        val rms = preprocessor.computeRms(constantSignal)
        assertEquals(100.0f, rms, 0.5f)

        val emptySignal = ShortArray(0)
        assertEquals(0.0f, preprocessor.computeRms(emptySignal), 0.01f)
    }
}

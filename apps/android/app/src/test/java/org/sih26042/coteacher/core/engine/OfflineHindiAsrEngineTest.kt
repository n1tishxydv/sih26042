package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.util.UUID

class OfflineHindiAsrEngineTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var asrEngine: OfflineHindiAsrEngine

    @Before
    fun setUp() {
        asrEngine = OfflineHindiAsrEngine()
    }

    @Test
    fun testInitializationSetsReadyState() = runTest {
        assertFalse(asrEngine.isReady())
        val res = asrEngine.initialize()
        assertTrue(res.isSuccess)
        assertTrue(asrEngine.isReady())
        assertEquals(AsrState.IDLE, asrEngine.currentState.value)
    }

    @Test
    fun testModelIntegrityVerificationFailsClosedOnCorruptAsset() = runTest {
        val modelDir = tempFolder.newFolder("asr_model")
        val encoderFile = File(modelDir, "encoder.onnx")
        encoderFile.writeText("corrupted dummy weight content")

        val expectedSha = "4b6c3f683416e7592cf1a9cf0c9a41ee9cf2e26e5d0d82998a46cf7e7275d27b"
        val config = AsrModelConfig(
            modelDir = modelDir,
            expectedFilesSha256 = mapOf("encoder.onnx" to expectedSha)
        )

        val result = asrEngine.initialize(config)
        assertTrue("Initialization must fail closed on hash mismatch", result.isFailure)
        assertEquals(AsrState.ERROR, asrEngine.currentState.value)
        assertFalse(asrEngine.isReady())
    }

    @Test
    fun testModelIntegrityVerificationFailsWhenModelFileMissing() = runTest {
        val modelDir = tempFolder.newFolder("missing_model")
        val config = AsrModelConfig(
            modelDir = modelDir,
            expectedFilesSha256 = mapOf("tokens.txt" to "deadbeef")
        )

        val result = asrEngine.initialize(config)
        assertTrue("Initialization must fail when expected file is missing", result.isFailure)
        assertEquals(AsrState.ERROR, asrEngine.currentState.value)
    }

    @Test
    fun testConfidenceMustBeNullAndNeverFabricated() = runTest {
        asrEngine.initialize()
        val result = asrEngine.recognizeSpeech("बैठ जाओ")

        assertNull("Confidence must be null per SIH26042 safety rule (no fake percentages)", result.confidence)
        assertEquals("बैठ जाओ", result.transcript)
        assertEquals("बैठ जाओ", result.normalizedTranscript)
        assertTrue("Duration must be non-zero", result.durationMs >= 0)
        assertNotNull(result.traceId)
    }

    @Test
    fun testStreamingEventEmissionsAndTraceIdConsistency() = runTest {
        asrEngine.initialize()
        val traceId = UUID.randomUUID().toString()
        val events = mutableListOf<AsrEvent>()

        val startRes = asrEngine.startListening(traceId) { event ->
            events.add(event)
        }
        assertTrue(startRes.isSuccess)
        assertEquals(AsrState.LISTENING, asrEngine.currentState.value)
        assertEquals(traceId, asrEngine.activeTraceId)

        // Feed PCM audio chunks (1600 samples)
        asrEngine.feedAudio(ShortArray(1600) { 100 })
        asrEngine.feedAudio(ShortArray(1600) { 200 })

        val stopRes = asrEngine.stopListening()
        assertTrue(stopRes.isSuccess)
        val asrResult = stopRes.getOrThrow()

        assertEquals(traceId, asrResult.traceId)
        assertTrue("Events must contain ListeningStarted", events.any { it is AsrEvent.ListeningStarted && it.traceId == traceId })
        assertTrue("Events must contain FinalTranscript", events.any { it is AsrEvent.FinalTranscript && it.result.traceId == traceId })
        assertEquals(AsrState.IDLE, asrEngine.currentState.value)
        assertNull(asrEngine.activeTraceId)
    }

    @Test
    fun testConcurrentListeningSessionsAreForbidden() = runTest {
        asrEngine.initialize()
        val traceId1 = "session_001"
        val traceId2 = "session_002"

        asrEngine.startListening(traceId1) {}
        val secondStart = asrEngine.startListening(traceId2) {}

        assertTrue("Starting second session concurrently must fail", secondStart.isFailure)
        assertEquals(traceId1, asrEngine.activeTraceId)

        asrEngine.cancel()
    }

    @Test
    fun testCancellationDiscardsAudioAndEmitsCancelledEvent() = runTest {
        asrEngine.initialize()
        val traceId = "session_cancel_test"
        val events = mutableListOf<AsrEvent>()

        asrEngine.startListening(traceId) { events.add(it) }
        asrEngine.feedAudio(ShortArray(3200) { 500 })

        val cancelRes = asrEngine.cancel()
        assertTrue(cancelRes.isSuccess)
        assertEquals(AsrState.IDLE, asrEngine.currentState.value)
        assertNull(asrEngine.activeTraceId)

        assertTrue(
            "Events must contain Cancelled event with matching trace ID",
            events.any { it is AsrEvent.Cancelled && it.traceId == traceId }
        )

        // Stopping after cancellation must fail cleanly
        val stopAfterCancel = asrEngine.stopListening()
        assertTrue(stopAfterCancel.isFailure)
    }
}

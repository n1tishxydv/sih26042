package org.sih26042.coteacher.core.audio

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * State representing microphone capture lifecycle.
 */
sealed class MicCaptureState {
    object Idle : MicCaptureState()
    data class Recording(val sessionId: String) : MicCaptureState()
    data class Interrupted(val reason: String) : MicCaptureState()
    data class Error(val code: String, val message: String) : MicCaptureState()
}

/**
 * Pluggable audio input source abstraction.
 * Allows physical Android AudioRecord capture on devices and
 * clean deterministic PCM feeding in headless JVM unit tests.
 */
interface AudioRecordSource {
    val sampleRate: Int
    val channelConfig: Int
    val audioFormat: Int
    val bufferSizeBytes: Int

    fun isAvailable(): Boolean
    fun startRecording(): Boolean
    fun read(audioBuffer: ShortArray, offsetInShorts: Int, sizeInShorts: Int): Int
    fun stop()
    fun release()
}

/**
 * Production Android AudioRecord hardware implementation.
 */
class HardwareAudioRecordSource(
    override val sampleRate: Int = 16000,
    override val channelConfig: Int = AudioFormat.CHANNEL_IN_MONO,
    override val audioFormat: Int = AudioFormat.ENCODING_PCM_16BIT
) : AudioRecordSource {

    private var audioRecord: AudioRecord? = null
    override val bufferSizeBytes: Int

    init {
        val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        bufferSizeBytes = if (minBuf > 0) minBuf * 2 else 3200
    }

    override fun isAvailable(): Boolean {
        return try {
            val minBuf = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            minBuf > 0
        } catch (_: Throwable) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    override fun startRecording(): Boolean {
        return try {
            release()
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                sampleRate,
                channelConfig,
                audioFormat,
                bufferSizeBytes
            )
            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                release()
                return false
            }
            audioRecord?.startRecording()
            audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING
        } catch (_: Throwable) {
            release()
            false
        }
    }

    override fun read(audioBuffer: ShortArray, offsetInShorts: Int, sizeInShorts: Int): Int {
        val recorder = audioRecord ?: return -1
        return recorder.read(audioBuffer, offsetInShorts, sizeInShorts)
    }

    override fun stop() {
        try {
            if (audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                audioRecord?.stop()
            }
        } catch (_: Throwable) {}
    }

    override fun release() {
        try {
            stop()
            audioRecord?.release()
        } catch (_: Throwable) {}
        audioRecord = null
    }
}

/**
 * Production audio recorder that captures 16 kHz 16-bit mono PCM from Android microphone,
 * manages audio focus, executes safe cancellation, and streams chunks to AudioPreprocessor.
 */
class MicrophoneRecorder(
    private val context: Context,
    private val audioSource: AudioRecordSource = HardwareAudioRecordSource(),
    val preprocessor: AudioPreprocessor = DefaultAudioPreprocessor()
) {
    private val _captureState = MutableStateFlow<MicCaptureState>(MicCaptureState.Idle)
    val captureState: StateFlow<MicCaptureState> = _captureState.asStateFlow()

    private var recordingJob: Job? = null
    private val isCapturing = AtomicBoolean(false)
    private var activeSessionId: String? = null

    private val audioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    }

    private var audioFocusRequest: AudioFocusRequest? = null

    fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestAudioFocus(): Boolean {
        val am = audioManager ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val playbackAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(playbackAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                    ) {
                        handleInterruption("AUDIO_FOCUS_LOST")
                    }
                }
                .build()
            audioFocusRequest = focusRequest
            am.requestAudioFocus(focusRequest) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                { focusChange ->
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS ||
                        focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                    ) {
                        handleInterruption("AUDIO_FOCUS_LOST")
                    }
                },
                AudioManager.STREAM_VOICE_CALL,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val am = audioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(null)
        }
        audioFocusRequest = null
    }

    private fun handleInterruption(reason: String) {
        if (isCapturing.get()) {
            stopRecording()
            _captureState.value = MicCaptureState.Interrupted(reason)
        }
    }

    suspend fun startRecording(
        sessionId: String,
        scope: CoroutineScope,
        onPcmChunk: suspend (ShortArray) -> Unit,
        onVadStateChange: (Boolean) -> Unit = {}
    ): Result<Unit> = withContext(Dispatchers.Default) {
        if (isCapturing.get()) {
            return@withContext Result.failure(IllegalStateException("RECORDING_ALREADY_ACTIVE"))
        }

        if (!hasRecordPermission()) {
            _captureState.value = MicCaptureState.Error("MIC_PERMISSION_DENIED", "RECORD_AUDIO permission is not granted")
            return@withContext Result.failure(SecurityException("MIC_PERMISSION_DENIED"))
        }

        if (!audioSource.isAvailable()) {
            _captureState.value = MicCaptureState.Error("MICROPHONE_UNAVAILABLE", "Audio capture hardware unavailable")
            return@withContext Result.failure(IllegalStateException("MICROPHONE_UNAVAILABLE"))
        }

        if (!requestAudioFocus()) {
            _captureState.value = MicCaptureState.Error("AUDIO_FOCUS_DENIED", "Could not obtain audio focus for classroom speech")
            return@withContext Result.failure(IllegalStateException("AUDIO_FOCUS_DENIED"))
        }

        if (!audioSource.startRecording()) {
            abandonAudioFocus()
            _captureState.value = MicCaptureState.Error("AUDIO_CAPTURE_FAILED", "Failed to start AudioRecord hardware stream")
            return@withContext Result.failure(IllegalStateException("AUDIO_CAPTURE_FAILED"))
        }

        isCapturing.set(true)
        activeSessionId = sessionId
        preprocessor.reset()
        _captureState.value = MicCaptureState.Recording(sessionId)

        // Read in 100ms frames: 1600 samples at 16kHz
        val chunkSize = 1600
        val rawBuffer = ShortArray(chunkSize)

        recordingJob = scope.launch(Dispatchers.IO) {
            try {
                while (isActive && isCapturing.get()) {
                    val readSamples = audioSource.read(rawBuffer, 0, chunkSize)
                    if (readSamples > 0) {
                        val frame = if (readSamples == chunkSize) {
                            rawBuffer.clone()
                        } else {
                            rawBuffer.copyOf(readSamples)
                        }

                        // Preprocessing: High-pass + VAD
                        val processed = preprocessor.processChunk(frame)
                        val isVoice = preprocessor.detectVoiceActivity(processed)
                        onVadStateChange(isVoice)

                        onPcmChunk(processed)
                    } else if (readSamples < 0) {
                        _captureState.value = MicCaptureState.Error("AUDIO_CAPTURE_FAILED", "AudioRecord read returned error code $readSamples")
                        break
                    }
                }
            } catch (e: CancellationException) {
                // Cooperative coroutine cancellation
            } catch (e: Throwable) {
                _captureState.value = MicCaptureState.Error("AUDIO_CAPTURE_FAILED", e.message ?: "Unknown audio error")
            } finally {
                stopInternal()
            }
        }

        Result.success(Unit)
    }

    fun stopRecording() {
        stopInternal()
        _captureState.value = MicCaptureState.Idle
    }

    fun cancel() {
        stopInternal()
        _captureState.value = MicCaptureState.Idle
    }

    private fun stopInternal() {
        if (isCapturing.getAndSet(false)) {
            recordingJob?.cancel()
            recordingJob = null
            audioSource.stop()
            audioSource.release()
            abandonAudioFocus()
            activeSessionId = null
        }
    }

    fun isRecording(): Boolean = isCapturing.get()
}

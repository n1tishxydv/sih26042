package org.sih26042.coteacher.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build

sealed class AudioPlaybackState {
    object Idle : AudioPlaybackState()
    data class Playing(val audioPath: String, val speed: Float) : AudioPlaybackState()
    object Completed : AudioPlaybackState()
    data class Error(val error: String) : AudioPlaybackState()
}

class AudioPlayerService(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    var currentState: AudioPlaybackState = AudioPlaybackState.Idle
        private set

    fun playAudio(
        audioPath: String,
        playbackSpeed: Float = 1.0f,
        onCompletion: () -> Unit = {}
    ) {
        stopAudio()
        try {
            val player = MediaPlayer()
            
            // Check if audio file exists in embedded assets
            if (audioPath.startsWith("audio/")) {
                val assetPath = "embedded_pack/$audioPath"
                val afd = context.assets.openFd(assetPath)
                player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
            } else {
                player.setDataSource(audioPath)
            }

            player.prepare()

            // Set playback speed (e.g. 0.75x for foundational articulation)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && playbackSpeed != 1.0f) {
                val params = PlaybackParams()
                params.speed = playbackSpeed
                player.playbackParams = params
            }

            player.setOnCompletionListener {
                currentState = AudioPlaybackState.Completed
                it.release()
                mediaPlayer = null
                onCompletion()
            }

            player.setOnErrorListener { _, what, extra ->
                currentState = AudioPlaybackState.Error("MediaPlayer error: $what, $extra")
                true
            }

            player.start()
            mediaPlayer = player
            currentState = AudioPlaybackState.Playing(audioPath, playbackSpeed)
        } catch (e: Exception) {
            currentState = AudioPlaybackState.Error("Failed to play: ${e.message}")
        }
    }

    fun stopAudio() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        currentState = AudioPlaybackState.Idle
    }
}

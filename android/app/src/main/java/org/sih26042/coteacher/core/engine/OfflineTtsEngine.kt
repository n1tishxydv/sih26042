package org.sih26042.coteacher.core.engine

interface TtsEngine {
    fun isLoaded(): Boolean
    suspend fun load()
    suspend fun unload()
    suspend fun synthesize(text: String, languageCode: String): Result<String>
}

class OfflineTtsEngine : TtsEngine {
    private var isLoaded: Boolean = false

    override fun isLoaded(): Boolean = isLoaded

    override suspend fun load() {
        kotlinx.coroutines.delay(150)
        isLoaded = true
    }

    override suspend fun unload() {
        isLoaded = false
    }

    override suspend fun synthesize(text: String, languageCode: String): Result<String> {
        if (!isLoaded) {
            load()
        }
        // Simulated FastPitch / VITS synthesis time (~250-400ms)
        kotlinx.coroutines.delay(300)
        // Return dummy cached audio path
        return Result.success("cache/synth_${System.currentTimeMillis()}.wav")
    }
}

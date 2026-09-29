package org.sih26042.coteacher.core.engine

interface TtsEngine {
    fun isLoaded(): Boolean
    suspend fun load()
    suspend fun unload()
    suspend fun synthesize(text: String, languageCode: String): Result<String>
}

/**
 * Backward compatibility typealias mapping OfflineTtsEngine to OfflineSantaliTtsEngine.
 */
typealias OfflineTtsEngine = OfflineSantaliTtsEngine

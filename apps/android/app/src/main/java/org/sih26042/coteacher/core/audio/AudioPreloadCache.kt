package org.sih26042.coteacher.core.audio

import java.io.File

/**
 * In-memory / file LRU cache for decoded audio assets.
 * Adheres strictly to the 256 MB low-RAM memory envelope for 2 GB devices.
 * Preloads and keeps frequently used foundational phrases (e.g. "Sit down", "Look here")
 * in active memory to achieve <150 ms playback response on the verified fast-path.
 */
class AudioPreloadCache(
    private val maxCacheSizeBytes: Long = 16 * 1024 * 1024 // 16 MB max audio memory budget
) {
    private val memoryCache = LinkedHashMap<String, ByteArray>(16, 0.75f, true)
    private var currentSize: Long = 0L

    val size: Int
        @Synchronized get() = memoryCache.size

    val totalSizeBytes: Long
        @Synchronized get() = currentSize

    @Synchronized
    fun get(key: String): ByteArray? {
        return memoryCache[key]
    }

    @Synchronized
    fun put(key: String, data: ByteArray) {
        if (data.size > maxCacheSizeBytes) {
            // Asset larger than entire cache budget; do not store in LRU to avoid thrashing
            return
        }

        while (currentSize + data.size > maxCacheSizeBytes && memoryCache.isNotEmpty()) {
            val eldestKey = memoryCache.keys.first()
            val removed = memoryCache.remove(eldestKey)
            if (removed != null) {
                currentSize -= removed.size
            }
        }

        memoryCache[key] = data
        currentSize += data.size
    }

    @Synchronized
    fun remove(key: String): ByteArray? {
        val removed = memoryCache.remove(key)
        if (removed != null) {
            currentSize -= removed.size
        }
        return removed
    }

    @Synchronized
    fun clear() {
        memoryCache.clear()
        currentSize = 0L
    }

    @Synchronized
    fun contains(key: String): Boolean {
        return memoryCache.containsKey(key)
    }

    /**
     * Preloads top classroom phrases into LRU cache if files exist.
     */
    fun preloadPhrases(audioFiles: Map<String, File>) {
        for ((phraseId, file) in audioFiles) {
            if (file.exists() && file.isFile) {
                try {
                    val bytes = file.readBytes()
                    put(phraseId, bytes)
                } catch (_: Exception) {
                    // Non-fatal if preload fails for an asset
                }
            }
        }
    }
}

package org.sih26042.coteacher.core.audio

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class AudioPreloadCacheTest {

    private lateinit var cache: AudioPreloadCache

    @Before
    fun setUp() {
        // 1 KB budget for testing eviction
        cache = AudioPreloadCache(maxCacheSizeBytes = 1024L)
    }

    @Test
    fun testPutAndGet() {
        val data = ByteArray(256) { 1 }
        cache.put("ph_01", data)

        assertTrue(cache.contains("ph_01"))
        assertArrayEquals(data, cache.get("ph_01"))
        assertEquals(1, cache.size)
        assertEquals(256L, cache.totalSizeBytes)
    }

    @Test
    fun testLruEvictionWhenBudgetExceeded() {
        val data1 = ByteArray(512) { 1 }
        val data2 = ByteArray(512) { 2 }
        val data3 = ByteArray(512) { 3 }

        cache.put("ph_01", data1)
        cache.put("ph_02", data2)
        assertEquals(1024L, cache.totalSizeBytes)

        // Adding ph_03 should evict ph_01
        cache.put("ph_03", data3)
        assertFalse(cache.contains("ph_01"))
        assertTrue(cache.contains("ph_02"))
        assertTrue(cache.contains("ph_03"))
        assertEquals(1024L, cache.totalSizeBytes)
    }

    @Test
    fun testAssetLargerThanCapacityNotStored() {
        val largeData = ByteArray(2048) { 1 }
        cache.put("ph_large", largeData)
        assertFalse(cache.contains("ph_large"))
        assertEquals(0, cache.size)
    }

    @Test
    fun testRemoveAndClear() {
        cache.put("ph_01", ByteArray(100))
        cache.put("ph_02", ByteArray(100))
        assertEquals(2, cache.size)

        cache.remove("ph_01")
        assertFalse(cache.contains("ph_01"))
        assertEquals(1, cache.size)

        cache.clear()
        assertEquals(0, cache.size)
        assertEquals(0L, cache.totalSizeBytes)
    }
}

package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.sih26042.coteacher.core.model.DeviceDiagnostics

/**
 * Explicit model lifecycle states for ASR per SIH26042 specification.
 */
enum class LifecycleModelState {
    NOT_LOADED,
    LOADING,
    READY,
    BUSY,
    UNLOADING,
    FAILED
}

/**
 * Explicit model lifecycle states for MT per Phase 3 specification.
 */
enum class MtLifecycleState {
    MT_NOT_LOADED,
    MT_LOADING,
    MT_READY,
    MT_BUSY,
    MT_UNLOADING,
    MT_FAILED
}

/**
 * Manages memory footprint of on-device ML models for 2 GB RAM Android devices.
 * 
 * Rules:
 * 1. ASR model loads lazily; app startup does not automatically load heavy models.
 * 2. Never load all heavy models simultaneously (ASR + MT + TTS).
 * 3. Concurrent loads are prevented via Mutex synchronization.
 * 4. Sequential execution: ASR can be unloaded or idled prior to loading MT.
 * 5. onTrimMemory aggressively evicts fallback models and unloads ASR under severe pressure.
 */
class ModelLifecycleManager(
    private val asrEngine: OfflineHindiAsrEngine,
    private val neuralMtEngine: OfflineHindiSantaliMtEngine,
    private val ttsEngine: OfflineTtsEngine
) {
    private val lifecycleMutex = Mutex()

    private val _asrLifecycleState = MutableStateFlow(LifecycleModelState.NOT_LOADED)
    val asrLifecycleState: StateFlow<LifecycleModelState> = _asrLifecycleState.asStateFlow()

    private val _mtLifecycleState = MutableStateFlow(MtLifecycleState.MT_NOT_LOADED)
    val mtLifecycleState: StateFlow<MtLifecycleState> = _mtLifecycleState.asStateFlow()

    private var coldStartDurationMs: Long = 0L
    private var lastWarmStartDurationMs: Long = 0L

    private var mtColdStartDurationMs: Long = 0L
    private var mtWarmStartDurationMs: Long = 0L

    private val latencyHistory = mutableListOf<Long>()

    fun isAsrLoaded(): Boolean = asrEngine.isLoaded()
    fun isNeuralMtLoaded(): Boolean = neuralMtEngine.isModelLoaded()
    fun isTtsLoaded(): Boolean = ttsEngine.isLoaded()

    suspend fun preloadFastPath(): Result<Unit> = lifecycleMutex.withLock {
        if (_asrLifecycleState.value == LifecycleModelState.READY && asrEngine.isLoaded()) {
            return Result.success(Unit)
        }

        if (_asrLifecycleState.value == LifecycleModelState.LOADING) {
            return Result.failure(IllegalStateException("ASR model is already loading"))
        }

        _asrLifecycleState.value = LifecycleModelState.LOADING
        val startNs = System.nanoTime()

        return try {
            asrEngine.load()
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
            if (coldStartDurationMs == 0L) {
                coldStartDurationMs = elapsedMs
            } else {
                lastWarmStartDurationMs = elapsedMs
            }
            _asrLifecycleState.value = LifecycleModelState.READY
            Result.success(Unit)
        } catch (e: Throwable) {
            _asrLifecycleState.value = LifecycleModelState.FAILED
            Result.failure(e)
        }
    }

    suspend fun unloadAsr(): Result<Unit> = lifecycleMutex.withLock {
        if (_asrLifecycleState.value == LifecycleModelState.NOT_LOADED && !asrEngine.isLoaded()) {
            return Result.success(Unit)
        }
        _asrLifecycleState.value = LifecycleModelState.UNLOADING
        return try {
            asrEngine.unload()
            _asrLifecycleState.value = LifecycleModelState.NOT_LOADED
            Result.success(Unit)
        } catch (e: Throwable) {
            _asrLifecycleState.value = LifecycleModelState.FAILED
            Result.failure(e)
        }
    }

    suspend fun loadMt(sequentialUnloadAsr: Boolean = false): Result<Unit> = lifecycleMutex.withLock {
        if (_mtLifecycleState.value == MtLifecycleState.MT_READY && neuralMtEngine.isModelLoaded()) {
            return Result.success(Unit)
        }

        if (_mtLifecycleState.value == MtLifecycleState.MT_LOADING) {
            return Result.failure(IllegalStateException("MT model is already loading"))
        }

        // Sequential memory management: unload ASR to conserve RAM on 2 GB tablets
        if (sequentialUnloadAsr && asrEngine.isLoaded()) {
            asrEngine.unload()
            _asrLifecycleState.value = LifecycleModelState.NOT_LOADED
        }

        _mtLifecycleState.value = MtLifecycleState.MT_LOADING
        val startNs = System.nanoTime()

        return try {
            neuralMtEngine.initialize()
            val elapsedMs = (System.nanoTime() - startNs) / 1_000_000L
            if (mtColdStartDurationMs == 0L) {
                mtColdStartDurationMs = elapsedMs
            } else {
                mtWarmStartDurationMs = elapsedMs
            }
            _mtLifecycleState.value = MtLifecycleState.MT_READY
            Result.success(Unit)
        } catch (e: Throwable) {
            _mtLifecycleState.value = MtLifecycleState.MT_FAILED
            Result.failure(e)
        }
    }

    suspend fun unloadMt(): Result<Unit> = lifecycleMutex.withLock {
        if (_mtLifecycleState.value == MtLifecycleState.MT_NOT_LOADED && !neuralMtEngine.isModelLoaded()) {
            return Result.success(Unit)
        }
        _mtLifecycleState.value = MtLifecycleState.MT_UNLOADING
        return try {
            neuralMtEngine.unload()
            _mtLifecycleState.value = MtLifecycleState.MT_NOT_LOADED
            Result.success(Unit)
        } catch (e: Throwable) {
            _mtLifecycleState.value = MtLifecycleState.MT_FAILED
            Result.failure(e)
        }
    }

    suspend fun unloadHeavyFallbackEngines() {
        unloadMt()
        if (ttsEngine.isLoaded()) {
            ttsEngine.unload()
        }
    }

    fun onTrimMemory(level: Int) {
        // When Android OS signals memory pressure, aggressively unload fallback models
        kotlinx.coroutines.runBlocking {
            unloadHeavyFallbackEngines()
            // Under critical memory pressure (> 60), also unload ASR
            if (level >= 60) {
                unloadAsr()
            }
        }
    }

    fun recordLatencySample(latencyMs: Long) {
        synchronized(latencyHistory) {
            latencyHistory.add(latencyMs)
            if (latencyHistory.size > 100) {
                latencyHistory.removeAt(0)
            }
        }
    }

    fun getMemoryDiagnostics(): DeviceDiagnostics {
        val runtime = Runtime.getRuntime()
        val totalMemory = runtime.totalMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = totalMemory - freeMemory
        val maxMemory = runtime.maxMemory()

        val usedMb = usedMemory / (1024 * 1024)
        val maxMb = maxMemory / (1024 * 1024)
        val isLow = (usedMb.toFloat() / maxMb.toFloat().coerceAtLeast(1.0f)) > 0.80f

        val (p50, p90, p95, count) = synchronized(latencyHistory) {
            if (latencyHistory.isEmpty()) {
                listOf(0L, 0L, 0L, 0L)
            } else {
                val sorted = latencyHistory.sorted()
                val n = sorted.size
                val p50Val = sorted[(n * 0.50).toInt().coerceIn(0, n - 1)]
                val p90Val = sorted[(n * 0.90).toInt().coerceIn(0, n - 1)]
                val p95Val = sorted[(n * 0.95).toInt().coerceIn(0, n - 1)]
                listOf(p50Val, p90Val, p95Val, n.toLong())
            }
        }

        return DeviceDiagnostics(
            totalRamMb = 2048, // Standard 2 GB target device budget
            availableRamMb = (maxMb - usedMb).coerceAtLeast(0),
            usedHeapMb = usedMb,
            maxHeapMb = maxMb,
            lowMemoryWarn = isLow,
            p50LatencyMs = p50,
            p90LatencyMs = p90,
            p95LatencyMs = p95,
            samplesCount = count.toInt(),
            coldStartTimeMs = coldStartDurationMs,
            warmStartTimeMs = lastWarmStartDurationMs
        )
    }
}

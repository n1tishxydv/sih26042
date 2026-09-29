package org.sih26042.coteacher.core.engine

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ModelLifecycleManagerTest {

    private lateinit var asrEngine: OfflineHindiAsrEngine
    private lateinit var neuralMtEngine: NeuralTranslationEngine
    private lateinit var ttsEngine: OfflineTtsEngine
    private lateinit var lifecycleManager: ModelLifecycleManager

    @Before
    fun setUp() {
        asrEngine = OfflineHindiAsrEngine()
        neuralMtEngine = NeuralTranslationEngine()
        ttsEngine = OfflineTtsEngine()
        lifecycleManager = ModelLifecycleManager(asrEngine, neuralMtEngine, ttsEngine)
    }

    @Test
    fun testDoNotLoadAllLargeModelsSimultaneously() = runTest {
        // Initially no models are loaded
        assertFalse(lifecycleManager.isAsrLoaded())
        assertFalse(lifecycleManager.isNeuralMtLoaded())
        assertFalse(lifecycleManager.isTtsLoaded())

        // Fast path preloading only loads ASR
        lifecycleManager.preloadFastPath()
        assertTrue(lifecycleManager.isAsrLoaded())
        assertFalse("Heavy Neural MT should remain unloaded during fast path", lifecycleManager.isNeuralMtLoaded())
        assertFalse("Heavy TTS should remain unloaded during fast path", lifecycleManager.isTtsLoaded())
    }

    @Test
    fun testOnTrimMemoryUnloadsHeavyEngines() = runTest {
        // Simulate heavy models loaded
        neuralMtEngine.load()
        assertTrue(neuralMtEngine.isModelLoaded())
        assertFalse("TTS engine should remain unavailable on target", ttsEngine.isLoaded())

        // Trigger memory pressure trim
        lifecycleManager.onTrimMemory(level = 80)

        // Heavy models must be evicted from RAM
        assertFalse("Neural MT must be unloaded onTrimMemory", neuralMtEngine.isModelLoaded())
        assertFalse("TTS must remain unloaded onTrimMemory", ttsEngine.isLoaded())
    }

    @Test
    fun testRealDiagnosticsDoNotInventMetrics() {
        val diagnostics = lifecycleManager.getMemoryDiagnostics()
        assertTrue("Max heap should be positive", diagnostics.maxHeapMb > 0)
        assertTrue("Used heap should be non-negative", diagnostics.usedHeapMb >= 0)
        assertEquals(2048, diagnostics.totalRamMb)
    }
}

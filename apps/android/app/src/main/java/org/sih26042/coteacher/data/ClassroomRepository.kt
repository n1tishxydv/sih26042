package org.sih26042.coteacher.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.sih26042.coteacher.core.matching.PhraseMatcher
import org.sih26042.coteacher.core.model.*

class ClassroomRepository(
    private val context: Context,
    private val packParser: PackParser = PackParser(context)
) {
    private val _phrases = MutableStateFlow<List<ClassroomPhrase>>(emptyList())
    val phrases: StateFlow<List<ClassroomPhrase>> = _phrases.asStateFlow()

    private val _flnVocabulary = MutableStateFlow<List<FlnVocabularyItem>>(emptyList())
    val flnVocabulary: StateFlow<List<FlnVocabularyItem>> = _flnVocabulary.asStateFlow()

    private val _worksheets = MutableStateFlow<List<NipunWorksheet>>(emptyList())
    val worksheets: StateFlow<List<NipunWorksheet>> = _worksheets.asStateFlow()

    private val _activities = MutableStateFlow<List<StudentActivityItem>>(emptyList())
    val activities: StateFlow<List<StudentActivityItem>> = _activities.asStateFlow()

    val phraseMatcher = PhraseMatcher()

    private val _recordedLatencies = MutableStateFlow<List<LatencyBreakdown>>(emptyList())
    val recordedLatencies: StateFlow<List<LatencyBreakdown>> = _recordedLatencies.asStateFlow()

    private val _teacherCorrections = MutableStateFlow<List<TeacherCorrection>>(emptyList())
    val teacherCorrections: StateFlow<List<TeacherCorrection>> = _teacherCorrections.asStateFlow()

    fun recordCorrection(correction: TeacherCorrection) {
        val current = _teacherCorrections.value.toMutableList()
        current.add(correction)
        _teacherCorrections.value = current
    }

    fun getPendingCorrections(): List<TeacherCorrection> {
        return _teacherCorrections.value.filter { it.syncStatus == "QUEUED_OFFLINE" }
    }

    fun markCorrectionsSynced(syncedIds: Set<String>) {
        _teacherCorrections.value = _teacherCorrections.value.map {
            if (it.correctionId in syncedIds) it.copy(syncStatus = "SYNCED") else it
        }
    }

    init {
        loadEmbeddedContent()
    }

    fun loadEmbeddedContent() {
        val loadedPhrases = packParser.loadEmbeddedPhrases()
        _phrases.value = loadedPhrases
        phraseMatcher.updatePhrases(loadedPhrases)

        _flnVocabulary.value = packParser.loadEmbeddedFlnVocabulary()
        _worksheets.value = packParser.loadEmbeddedWorksheets()
        _activities.value = packParser.loadEmbeddedActivities()
    }

    fun recordLatency(metric: LatencyBreakdown) {
        val current = _recordedLatencies.value.toMutableList()
        current.add(metric)
        _recordedLatencies.value = current
    }

    fun getLatencyStats(): Triple<Long, Long, Long> {
        val samples = _recordedLatencies.value.map { it.totalLatencyMs }.sorted()
        if (samples.isEmpty()) return Triple(0L, 0L, 0L)
        val p50 = samples[samples.size * 50 / 100]
        val p90 = samples[samples.size * 90 / 100]
        val p95 = samples[samples.size * 95 / 100]
        return Triple(p50, p90, p95)
    }
}

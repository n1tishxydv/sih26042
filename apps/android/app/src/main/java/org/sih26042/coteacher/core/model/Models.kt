package org.sih26042.coteacher.core.model

/**
 * Explicit provenance states for pedagogical safety.
 * CRITICAL RULE: These states must NEVER be collapsed into a single generic 'translated' state.
 */
enum class ProvenanceState {
    /** Exact verified classroom phrase matched from active Language Pack. Zero ML hallucination, native audio ready. */
    VERIFIED,

    /** Phrase matched from pack but native linguistic verification is pending. Safe status. */
    PENDING_VALIDATION,

    /** Rule-based lexical mapping with phonetic fallback for off-script utterances. Explicitly non-neural. */
    RULE_BASED,

    /** Machine-generated translation via on-device quantized MT with TTS fallback. Explicit badge shown. */
    MACHINE_GENERATED,

    /** Low acoustic or translation confidence (< 0.70). Teacher must confirm before audio playback. */
    LOW_CONFIDENCE,

    /** Unintelligible or unmatched voice input. */
    NO_MATCH,

    /** Model or audio engine unavailable or language pack missing. */
    UNAVAILABLE
}

enum class PhraseCategory(val displayName: String) {
    CLASSROOM_MANAGEMENT("Classroom Management"),
    NIPUN_MATH("Foundational Numeracy"),
    NIPUN_LITERACY("Foundational Literacy"),
    HYGIENE_AND_ROUTINE("Hygiene & Routine"),
    GREETINGS_AND_COURTESY("Greetings & Courtesy"),
    ENCOURAGEMENT_AND_FEEDBACK("Encouragement & Praise")
}

data class ClassroomPhrase(
    val phraseId: String,
    val hindiCanonical: String,
    val hindiNormalized: String,
    val hindiAliases: List<String> = emptyList(),
    val targetNativeScript: String,
    val targetTransliterationLatin: String,
    val targetTransliterationDevanagari: String? = null,
    val audioPath: String? = null,
    val category: String,
    val flnDomain: String? = null,
    val pedagogicalContext: String? = null,
    val provenance: ProvenanceState = ProvenanceState.PENDING_VALIDATION,
    val intent: String = "CLASSROOM_ACTION",
    val verificationStatus: String = "PENDING_VALIDATION",
    val audioDurationMs: Int = 500,
    val difficultyLevel: Int = 1
)


data class FlnVocabularyItem(
    val wordId: String,
    val category: String, // numbers, body_parts, classroom_objects, colors, animals
    val hindiWord: String,
    val targetNativeScript: String,
    val targetTransliteration: String,
    val audioPath: String? = null,
    val numericalValue: Int? = null
)

data class WorksheetQuestion(
    val itemId: String,
    val worksheetId: String,
    val questionNumber: Int,
    val promptHindi: String,
    val promptTargetNative: String,
    val promptTargetTransliteration: String,
    val questionType: String,
    val options: List<String>,
    val correctAnswer: String,
    val visualAsset: String? = null
)

data class NipunWorksheet(
    val worksheetId: String,
    val title: String,
    val gradeLevel: String,
    val nipunCompetency: String,
    val items: List<WorksheetQuestion>
)

data class StudentActivityItem(
    val activityId: String,
    val title: String,
    val activityType: String,
    val teacherPromptHindi: String,
    val teacherPromptNative: String,
    val studentResponseNative: String,
    val studentResponseTransliteration: String,
    val audioPromptPath: String? = null,
    val pedagogicalObjective: String
)

data class LatencyBreakdown(
    val asrLatencyMs: Long = 0,
    val matchLatencyMs: Long = 0,
    val audioLatencyMs: Long = 0,
    val totalLatencyMs: Long = 0,
    val pipelineMode: String = "VERIFIED_FAST_PATH"
)

data class TeacherCorrection(
    val correctionId: String = java.util.UUID.randomUUID().toString(),
    val sourceHindi: String,
    val candidateSantali: String,
    val suggestedSantali: String,
    val issueType: String = "WRONG_TRANSLATION", // WRONG_TRANSLATION, DIALECT_VARIATION, SCRIPT_ERROR, OTHER
    val dialectNote: String? = null,
    val teacherNotes: String? = null,
    val timestampMs: Long = System.currentTimeMillis(),
    val syncStatus: String = "QUEUED_OFFLINE"
)

data class ClassroomInteractionResult(
    val recognizedHindi: String,
    val outputNativeScript: String,
    val outputTransliteration: String,
    val provenance: ProvenanceState,
    val confidence: Float? = null,
    val audioPath: String? = null,
    val latency: LatencyBreakdown = LatencyBreakdown(),
    val matchedPhrase: ClassroomPhrase? = null,
    val engineName: String = "IndicTrans2-ONNX-INT8",
    val modelVersion: String = "2024-03-int8",
    val warnings: List<String> = emptyList()
)

enum class PackLifecycleState {
    DOWNLOADED,
    VALIDATING,
    VALID,
    ACTIVE,
    FAILED,
    ROLLED_BACK
}

data class LanguagePackInfo(
    val packId: String,
    val languageCode: String,
    val languageName: String,
    val nativeName: String,
    val version: String,
    val primaryScript: String,
    val isInstalled: Boolean,
    val isActive: Boolean,
    val phrasesCount: Int,
    val flnVocabCount: Int,
    val worksheetsCount: Int,
    val sha256Checksum: String,
    val statusLabel: String = "FULL MVP",
    val contentStatus: String = "PRODUCTION",
    val packSizeBytes: Long = 0L,
    val lifecycleState: PackLifecycleState = if (isActive) PackLifecycleState.ACTIVE else if (isInstalled) PackLifecycleState.VALID else PackLifecycleState.DOWNLOADED
)


data class DeviceDiagnostics(
    val totalRamMb: Long,
    val availableRamMb: Long,
    val usedHeapMb: Long,
    val maxHeapMb: Long,
    val lowMemoryWarn: Boolean,
    val p50LatencyMs: Long,
    val p90LatencyMs: Long,
    val p95LatencyMs: Long,
    val samplesCount: Int,
    val coldStartTimeMs: Long,
    val warmStartTimeMs: Long
)

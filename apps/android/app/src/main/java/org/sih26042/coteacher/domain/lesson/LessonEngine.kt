package org.sih26042.coteacher.domain.lesson

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.sih26042.coteacher.core.content.SantaliNipunContent
import org.sih26042.coteacher.core.model.*

/**
 * PHASE 5 — LessonEngine
 *
 * Drives the teacher-facing lesson workflow:
 *   LessonList → LessonDetail → Activity sequence → Session summary
 *
 * Key design rules:
 *  - All state is ephemeral in-memory during a session; persisted to SessionRepository at end.
 *  - No network calls — entirely offline.
 *  - Does NOT expose raw ActivityItem to UI; UI drives through [ActivityState] state machine.
 *  - Session provenance tracking: counts verified vs. machine-generated translations used.
 */
class LessonEngine(
    private val sessionRepository: SessionRepository
) {
    // ------------------------------------------------------------------
    // State
    // ------------------------------------------------------------------

    private val _currentLesson = MutableStateFlow<Lesson?>(null)
    val currentLesson: StateFlow<Lesson?> = _currentLesson.asStateFlow()

    private val _currentActivity = MutableStateFlow<ClassroomActivity?>(null)
    val currentActivity: StateFlow<ClassroomActivity?> = _currentActivity.asStateFlow()

    private val _activityState = MutableStateFlow<ActivityState>(ActivityState.Idle)
    val activityState: StateFlow<ActivityState> = _activityState.asStateFlow()

    private val _session = MutableStateFlow<ClassSession?>(null)
    val session: StateFlow<ClassSession?> = _session.asStateFlow()

    private val _sessionSummary = MutableStateFlow<SessionSummary?>(null)
    val sessionSummary: StateFlow<SessionSummary?> = _sessionSummary.asStateFlow()

    // ------------------------------------------------------------------
    // Internal tracking
    // ------------------------------------------------------------------

    private val activityQueue = ArrayDeque<ClassroomActivity>()
    private val activityAttempts = mutableListOf<ActivityAttempt>()
    private val completedActivities = mutableListOf<ActivitySummary>()
    private var currentItemIndex = 0
    private var currentItemAttempts = mutableListOf<ActivityAttempt>()

    // ------------------------------------------------------------------
    // Catalog
    // ------------------------------------------------------------------

    fun getLessonsForGrade(gradeLevel: GradeLevel): List<Lesson> =
        SantaliNipunContent.getLessonsForGrade(gradeLevel)

    fun getAllLessons(): List<Lesson> =
        SantaliNipunContent.LESSON_REGISTRY.values.toList()

    fun getLessonById(id: String): Lesson? =
        SantaliNipunContent.LESSON_REGISTRY[id]

    fun getActivitiesForLesson(lesson: Lesson): List<ClassroomActivity> =
        SantaliNipunContent.getActivitiesForLesson(lesson)

    // ------------------------------------------------------------------
    // Session lifecycle
    // ------------------------------------------------------------------

    /**
     * Start a session for the given lesson. Resets all transient state.
     * Call this when the teacher taps "Start Lesson".
     */
    fun startLesson(lesson: Lesson) {
        _currentLesson.value = lesson
        _activityState.value = ActivityState.Idle
        _sessionSummary.value = null
        activityQueue.clear()
        activityAttempts.clear()
        completedActivities.clear()
        currentItemIndex = 0
        currentItemAttempts = mutableListOf()

        val activities = SantaliNipunContent.getActivitiesForLesson(lesson)
        activityQueue.addAll(activities)

        val session = ClassSession(
            lessonId = lesson.lessonId,
            gradeLevel = lesson.gradeLevel.displayLabel,
            domain = lesson.domain.displayLabel
        )
        _session.value = session
    }

    /**
     * Advance from Idle → Intro for the next activity in the queue.
     * Returns false if no more activities remain (lesson complete).
     */
    fun advanceToNextActivity(): Boolean {
        val activity = activityQueue.removeFirstOrNull() ?: run {
            finishLesson()
            return false
        }
        _currentActivity.value = activity
        currentItemIndex = 0
        currentItemAttempts = mutableListOf()
        _activityState.value = ActivityState.Intro
        return true
    }

    /**
     * Teacher taps "Start Activity" after reading the intro.
     * Transitions Intro → Instruction(firstItem).
     */
    fun beginActivity() {
        val activity = _currentActivity.value ?: return
        if (activity.items.isEmpty()) {
            markActivityComplete(activity)
            return
        }
        val firstItem = activity.items[0]
        currentItemIndex = 0
        _activityState.value = ActivityState.Instruction(firstItem)
    }

    /**
     * Teacher reads the instruction aloud and signals children to respond.
     * Transitions Instruction → AwaitingResponse.
     */
    fun presentItem() {
        val item = (_activityState.value as? ActivityState.Instruction)?.item ?: return
        _activityState.value = ActivityState.AwaitingResponse(item)
    }

    /**
     * Record the child's (or class's) response.
     * Transitions AwaitingResponse → Feedback, then auto-advances.
     */
    fun submitResponse(response: String) {
        val state = _activityState.value
        if (state !is ActivityState.AwaitingResponse) return
        val item = state.item
        val activity = _currentActivity.value ?: return

        val isCorrect = response.trim().equals(item.correctAnswer.trim(), ignoreCase = true)
        val attempt = ActivityAttempt(
            sessionId = _session.value?.sessionId ?: "",
            activityId = activity.activityId,
            itemId = item.itemId,
            response = response,
            correct = isCorrect
        )
        activityAttempts.add(attempt)
        currentItemAttempts.add(attempt)

        _activityState.value = ActivityState.Feedback(
            item = item,
            wasCorrect = isCorrect,
            response = response
        )
    }

    /**
     * After showing feedback, move to the next item or complete the activity.
     * Call when teacher taps "Next" in the feedback card.
     */
    fun advanceItem() {
        val activity = _currentActivity.value ?: return
        currentItemIndex++
        if (currentItemIndex >= activity.items.size) {
            markActivityComplete(activity)
        } else {
            _activityState.value = ActivityState.Instruction(activity.items[currentItemIndex])
        }
    }

    /**
     * Teacher explicitly skips the current item.
     */
    fun skipItem() {
        val state = _activityState.value
        val item = when (state) {
            is ActivityState.Instruction -> state.item
            is ActivityState.AwaitingResponse -> state.item
            else -> return
        }
        val activity = _currentActivity.value ?: return
        // Record as skipped (empty response, not correct)
        val attempt = ActivityAttempt(
            sessionId = _session.value?.sessionId ?: "",
            activityId = activity.activityId,
            itemId = item.itemId,
            response = "__SKIPPED__",
            correct = false
        )
        activityAttempts.add(attempt)
        currentItemAttempts.add(attempt)
        advanceItem()
    }

    /**
     * Record that a verified-phrase translation was used in this session.
     * Called by the speech pipeline.
     */
    fun recordVerifiedTranslation() {
        _session.update { s ->
            s?.copy(
                translationsUsed = (s.translationsUsed) + 1,
                verifiedTranslations = (s.verifiedTranslations) + 1
            )
        }
    }

    /**
     * Record that a machine-generated translation was used.
     */
    fun recordMachineGeneratedTranslation() {
        _session.update { s ->
            s?.copy(
                translationsUsed = (s.translationsUsed) + 1,
                machineGeneratedTranslations = (s.machineGeneratedTranslations) + 1
            )
        }
    }

    // ------------------------------------------------------------------
    // Internal helpers
    // ------------------------------------------------------------------

    private fun markActivityComplete(activity: ClassroomActivity) {
        val correct = currentItemAttempts.count { it.correct }
        val total = activity.items.size
        completedActivities.add(
            ActivitySummary(
                activityId = activity.activityId,
                title = activity.title,
                totalItems = total,
                correctItems = correct,
                completed = true
            )
        )
        _activityState.value = ActivityState.Complete
    }

    private fun finishLesson() {
        val lesson = _currentLesson.value ?: return
        val session = _session.value ?: return

        val finishedSession = session.copy(
            endTimeMs = System.currentTimeMillis(),
            completed = true
        )
        _session.value = finishedSession

        // Determine suggested next lesson
        val allLessons = SantaliNipunContent.getLessonsForGrade(lesson.gradeLevel)
        val currentIdx = allLessons.indexOfFirst { it.lessonId == lesson.lessonId }
        val nextLesson = if (currentIdx >= 0 && currentIdx + 1 < allLessons.size)
            allLessons[currentIdx + 1] else null

        val summary = SessionSummary(
            session = finishedSession,
            lessonTitle = lesson.title,
            activitiesSummary = completedActivities.toList(),
            nextSuggestedLessonId = nextLesson?.lessonId,
            nextSuggestedLessonTitle = nextLesson?.title
        )
        _sessionSummary.value = summary

        // Persist asynchronously; fire-and-forget from engine perspective
        sessionRepository.saveSession(finishedSession, activityAttempts.toList())
    }

    // ------------------------------------------------------------------
    // For choral / open-ended activities that have no wrong answer
    // ------------------------------------------------------------------

    fun acknowledgeChoralItem() {
        val state = _activityState.value
        val item = when (state) {
            is ActivityState.Instruction -> state.item
            is ActivityState.AwaitingResponse -> state.item
            else -> return
        }
        // For choral, mark as correct always
        submitResponse(item.correctAnswer)
    }
}

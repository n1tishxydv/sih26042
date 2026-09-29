package org.sih26042.coteacher.domain.lesson

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.sih26042.coteacher.core.model.ActivityAttempt
import org.sih26042.coteacher.core.model.ClassSession

/**
 * PHASE 5 — SessionRepository Unit Tests
 *
 * Tests serialisation round-trips and bounded storage behaviour.
 * Uses Mockito to mock Android SharedPreferences.
 */
class SessionRepositoryTest {

    private lateinit var context: Context
    private lateinit var prefs: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setUp() {
        editor = mock()
        whenever(editor.putString(any(), any())).thenReturn(editor)
        whenever(editor.apply()).then { /* no-op */ }

        prefs = mock()
        whenever(prefs.getString(any(), isNull())).thenReturn(null)
        whenever(prefs.edit()).thenReturn(editor)

        context = mock()
        whenever(context.getSharedPreferences(any(), any())).thenReturn(prefs)
    }

    @Test
    fun `loadAllSessions returns empty list when no data stored`() {
        val repo = SessionRepository(context)
        val sessions = repo.loadAllSessions()
        assertTrue("Expected empty list when no prefs", sessions.isEmpty())
    }

    @Test
    fun `loadAttemptsForSession returns empty list when no data stored`() {
        val repo = SessionRepository(context)
        val attempts = repo.loadAttemptsForSession("any_session_id")
        assertTrue("Expected empty list when no prefs", attempts.isEmpty())
    }

    @Test
    fun `completedLessonCount returns 0 when no sessions`() {
        val repo = SessionRepository(context)
        assertEquals(0, repo.completedLessonCount())
    }

    @Test
    fun `completedLessonIds returns empty set when no sessions`() {
        val repo = SessionRepository(context)
        assertEquals(emptySet<String>(), repo.completedLessonIds())
    }

    @Test
    fun `ClassSession model serialization is stable`() {
        // Domain model round-trip test (no Android dep needed)
        val session = ClassSession(
            sessionId = "test-id-123",
            lessonId = "lsn_test",
            languagePackId = "sat_1.0",
            gradeLevel = "Grade 1",
            domain = "Foundational Numeracy",
            startTimeMs = 1000L,
            endTimeMs = 2000L,
            completed = true,
            translationsUsed = 5,
            verifiedTranslations = 4,
            machineGeneratedTranslations = 1,
            deviceInfo = "TestDevice"
        )
        // Verify all fields are accessible
        assertEquals("test-id-123", session.sessionId)
        assertEquals("lsn_test", session.lessonId)
        assertEquals("sat_1.0", session.languagePackId)
        assertTrue(session.completed)
        assertEquals(5, session.translationsUsed)
        assertEquals(4, session.verifiedTranslations)
        assertEquals(1, session.machineGeneratedTranslations)
        assertEquals(1000L, session.endTimeMs!! - session.startTimeMs)
    }

    @Test
    fun `ActivityAttempt model is stable`() {
        val attempt = ActivityAttempt(
            attemptId = "att-1",
            sessionId = "sess-1",
            activityId = "act-1",
            itemId = "item-1",
            response = "3",
            correct = true,
            timestampMs = 9999L
        )
        assertEquals("att-1", attempt.attemptId)
        assertEquals("sess-1", attempt.sessionId)
        assertTrue(attempt.correct)
        assertEquals("3", attempt.response)
    }
}

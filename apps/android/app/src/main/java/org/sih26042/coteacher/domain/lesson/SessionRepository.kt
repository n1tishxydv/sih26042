package org.sih26042.coteacher.domain.lesson

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.sih26042.coteacher.core.model.ActivityAttempt
import org.sih26042.coteacher.core.model.ClassSession

/**
 * PHASE 5 — SessionRepository
 *
 * Persists completed sessions and activity attempts to device-local storage.
 * Uses SharedPreferences for simplicity on 2 GB target devices.
 * No Room dependency — keeps it lightweight and offline-safe.
 *
 * Data is never transmitted off-device.
 */
class SessionRepository(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "sih26042_sessions"
        private const val KEY_SESSIONS = "sessions"
        private const val KEY_ATTEMPTS = "attempts"
        private const val MAX_STORED_SESSIONS = 100
        private const val MAX_STORED_ATTEMPTS = 500
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    // ------------------------------------------------------------------
    // Write
    // ------------------------------------------------------------------

    fun saveSession(session: ClassSession, attempts: List<ActivityAttempt>) {
        scope.launch {
            try {
                persistSession(session)
                persistAttempts(attempts)
            } catch (e: Exception) {
                // Non-fatal: telemetry is best-effort
            }
        }
    }

    // ------------------------------------------------------------------
    // Read
    // ------------------------------------------------------------------

    fun loadAllSessions(): List<ClassSession> {
        val raw = prefs.getString(KEY_SESSIONS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                parseSession(arr.getJSONObject(i))
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun loadAttemptsForSession(sessionId: String): List<ActivityAttempt> {
        val raw = prefs.getString(KEY_ATTEMPTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                parseAttempt(arr.getJSONObject(i))
            }.filter { it.sessionId == sessionId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /** Returns number of completed lessons across all sessions */
    fun completedLessonCount(): Int =
        loadAllSessions().count { it.completed }

    /** Returns IDs of all lessons that have at least one completed session */
    fun completedLessonIds(): Set<String> =
        loadAllSessions().filter { it.completed }.map { it.lessonId }.toSet()

    // ------------------------------------------------------------------
    // Internal persistence helpers
    // ------------------------------------------------------------------

    private fun persistSession(session: ClassSession) {
        val existing = loadAllSessionsRaw()
        // Remove old sessions if at capacity
        val trimmed = if (existing.length() >= MAX_STORED_SESSIONS) {
            val arr = JSONArray()
            for (i in 1 until existing.length()) arr.put(existing.get(i))
            arr
        } else {
            existing
        }
        trimmed.put(sessionToJson(session))
        prefs.edit().putString(KEY_SESSIONS, trimmed.toString()).apply()
    }

    private fun persistAttempts(attempts: List<ActivityAttempt>) {
        val existing = loadAllAttemptsRaw()
        val trimmed = if (existing.length() + attempts.size >= MAX_STORED_ATTEMPTS) {
            // Keep newest portion
            val keep = maxOf(0, MAX_STORED_ATTEMPTS - attempts.size)
            val arr = JSONArray()
            val startIdx = existing.length() - keep
            for (i in startIdx until existing.length()) arr.put(existing.get(i))
            arr
        } else {
            existing
        }
        for (attempt in attempts) trimmed.put(attemptToJson(attempt))
        prefs.edit().putString(KEY_ATTEMPTS, trimmed.toString()).apply()
    }

    private fun loadAllSessionsRaw(): JSONArray {
        val raw = prefs.getString(KEY_SESSIONS, null) ?: return JSONArray()
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    private fun loadAllAttemptsRaw(): JSONArray {
        val raw = prefs.getString(KEY_ATTEMPTS, null) ?: return JSONArray()
        return try { JSONArray(raw) } catch (e: Exception) { JSONArray() }
    }

    // ------------------------------------------------------------------
    // Serialization
    // ------------------------------------------------------------------

    private fun sessionToJson(s: ClassSession) = JSONObject().apply {
        put("sessionId", s.sessionId)
        put("lessonId", s.lessonId)
        put("languagePackId", s.languagePackId)
        put("gradeLevel", s.gradeLevel)
        put("domain", s.domain)
        put("startTimeMs", s.startTimeMs)
        put("endTimeMs", s.endTimeMs ?: JSONObject.NULL)
        put("completed", s.completed)
        put("translationsUsed", s.translationsUsed)
        put("verifiedTranslations", s.verifiedTranslations)
        put("machineGeneratedTranslations", s.machineGeneratedTranslations)
        put("deviceInfo", s.deviceInfo)
    }

    private fun parseSession(obj: JSONObject): ClassSession? = try {
        ClassSession(
            sessionId = obj.getString("sessionId"),
            lessonId = obj.getString("lessonId"),
            languagePackId = obj.optString("languagePackId", "sat_1.0"),
            gradeLevel = obj.optString("gradeLevel", "Grade 1"),
            domain = obj.optString("domain", ""),
            startTimeMs = obj.getLong("startTimeMs"),
            endTimeMs = if (obj.isNull("endTimeMs")) null else obj.getLong("endTimeMs"),
            completed = obj.getBoolean("completed"),
            translationsUsed = obj.optInt("translationsUsed", 0),
            verifiedTranslations = obj.optInt("verifiedTranslations", 0),
            machineGeneratedTranslations = obj.optInt("machineGeneratedTranslations", 0),
            deviceInfo = obj.optString("deviceInfo", "")
        )
    } catch (e: Exception) { null }

    private fun attemptToJson(a: ActivityAttempt) = JSONObject().apply {
        put("attemptId", a.attemptId)
        put("sessionId", a.sessionId)
        put("activityId", a.activityId)
        put("itemId", a.itemId)
        put("response", a.response)
        put("correct", a.correct)
        put("timestampMs", a.timestampMs)
    }

    private fun parseAttempt(obj: JSONObject): ActivityAttempt? = try {
        ActivityAttempt(
            attemptId = obj.getString("attemptId"),
            sessionId = obj.getString("sessionId"),
            activityId = obj.getString("activityId"),
            itemId = obj.getString("itemId"),
            response = obj.getString("response"),
            correct = obj.getBoolean("correct"),
            timestampMs = obj.getLong("timestampMs")
        )
    } catch (e: Exception) { null }
}

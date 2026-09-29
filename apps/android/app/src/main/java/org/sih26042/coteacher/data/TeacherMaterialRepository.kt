package org.sih26042.coteacher.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.sih26042.coteacher.core.model.*
import java.util.UUID

/**
 * PHASE 6 — TeacherMaterialRepository
 *
 * Provides offline-first persistence for:
 * - Teacher-saved materials (translations, phrases, worksheets, decks)
 * - Translation history
 * - Custom flashcard decks
 * - Recent and favorite items
 *
 * Uses bounded SharedPreferences storage (low memory footprint for 2 GB devices).
 * Data is strictly on-device.
 */
class TeacherMaterialRepository(
    private val context: Context,
    private val classroomRepository: ClassroomRepository? = null
) {
    companion object {
        private const val PREFS_NAME = "sih26042_teacher_materials"
        private const val KEY_MATERIALS = "materials"
        private const val KEY_HISTORY = "history"
        private const val KEY_DECKS = "decks"
        private const val KEY_RECENTS = "recents"

        const val MAX_STORED_MATERIALS = 200
        const val MAX_STORED_HISTORY = 100
        const val MAX_STORED_DECKS = 50
        const val MAX_RECENT_ITEMS = 50
    }

    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    private val _materials = MutableStateFlow<List<TeacherMaterial>>(emptyList())
    val materials: StateFlow<List<TeacherMaterial>> = _materials.asStateFlow()

    private val _history = MutableStateFlow<List<TranslationHistoryItem>>(emptyList())
    val history: StateFlow<List<TranslationHistoryItem>> = _history.asStateFlow()

    private val _decks = MutableStateFlow<List<FlashcardDeck>>(emptyList())
    val decks: StateFlow<List<FlashcardDeck>> = _decks.asStateFlow()

    private val _recentItemIds = MutableStateFlow<List<String>>(emptyList())
    val recentItemIds: StateFlow<List<String>> = _recentItemIds.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        _materials.value = loadMaterialsFromStorage()
        _history.value = loadHistoryFromStorage()
        _decks.value = loadDecksFromStorage()
        _recentItemIds.value = loadRecentsFromStorage()
    }

    // ------------------------------------------------------------------
    // Teacher Materials (Save, Update, Delete, Duplicate, Favorite)
    // ------------------------------------------------------------------

    fun saveMaterial(material: TeacherMaterial) {
        val current = _materials.value.toMutableList()
        val index = current.indexOfFirst { it.id == material.id }
        if (index >= 0) {
            current[index] = material.copy(updatedAtMs = System.currentTimeMillis())
        } else {
            if (current.size >= MAX_STORED_MATERIALS) {
                current.removeAt(0)
            }
            current.add(material)
        }
        _materials.value = current
        recordRecent(material.id)
        persistMaterialsAsync(current)
    }

    fun getMaterial(id: String): TeacherMaterial? {
        return _materials.value.find { it.id == id }
    }

    fun deleteMaterial(id: String) {
        val updated = _materials.value.filter { it.id != id }
        _materials.value = updated
        persistMaterialsAsync(updated)
    }

    fun duplicateMaterial(id: String): TeacherMaterial? {
        val original = getMaterial(id) ?: return null
        val copy = original.copy(
            id = UUID.randomUUID().toString(),
            title = "${original.title} (Copy)",
            createdAtMs = System.currentTimeMillis(),
            updatedAtMs = System.currentTimeMillis()
        )
        saveMaterial(copy)
        return copy
    }

    fun toggleFavoriteMaterial(id: String) {
        val current = _materials.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(isFavorite = !item.isFavorite)
            _materials.value = current
            persistMaterialsAsync(current)
        }
    }

    // ------------------------------------------------------------------
    // Translation History
    // ------------------------------------------------------------------

    fun addHistoryItem(item: TranslationHistoryItem) {
        val current = _history.value.toMutableList()
        if (current.size >= MAX_STORED_HISTORY) {
            current.removeAt(0)
        }
        current.add(0, item) // most recent first
        _history.value = current
        persistHistoryAsync(current)
    }

    fun deleteHistoryItem(id: String) {
        val updated = _history.value.filter { it.id != id }
        _history.value = updated
        persistHistoryAsync(updated)
    }

    fun toggleFavoriteHistory(id: String) {
        val current = _history.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            val item = current[index]
            current[index] = item.copy(isFavorite = !item.isFavorite)
            _history.value = current
            persistHistoryAsync(current)
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
        persistHistoryAsync(emptyList())
    }

    // ------------------------------------------------------------------
    // Flashcard Decks
    // ------------------------------------------------------------------

    fun saveDeck(deck: FlashcardDeck) {
        val current = _decks.value.toMutableList()
        val index = current.indexOfFirst { it.id == deck.id }
        if (index >= 0) {
            current[index] = deck
        } else {
            if (current.size >= MAX_STORED_DECKS) {
                current.removeAt(0)
            }
            current.add(deck)
        }
        _decks.value = current
        persistDecksAsync(current)
    }

    fun deleteDeck(id: String) {
        val updated = _decks.value.filter { it.id != id }
        _decks.value = updated
        persistDecksAsync(updated)
    }

    /**
     * Returns default decks constructed from pack FLN vocabulary combined with custom decks
     */
    fun getAllDecks(flnVocab: List<FlnVocabularyItem>): List<FlashcardDeck> {
        val defaultDecks = createDefaultDecks(flnVocab)
        return defaultDecks + _decks.value
    }

    private fun createDefaultDecks(flnVocab: List<FlnVocabularyItem>): List<FlashcardDeck> {
        val categories = listOf("numbers", "animals", "body_parts", "colors", "classroom_objects")
        return categories.map { category ->
            val cards = flnVocab.filter { it.category == category }.map { v ->
                Flashcard(
                    id = "pack_card_${v.wordId}",
                    front = v.hindiWord,
                    back = v.targetNativeScript,
                    script = "Ol Chiki",
                    transliteration = v.targetTransliteration,
                    audioRef = v.audioPath,
                    category = v.category,
                    difficulty = 1,
                    provenance = ContentProvenance.VERIFIED,
                    isFavorite = false,
                    isDifficult = false
                )
            }
            FlashcardDeck(
                id = "default_deck_$category",
                title = "FLN: ${category.replace('_', ' ').uppercase()}",
                languageCode = "sat",
                classLevel = GradeLevel.GRADE_1,
                category = category,
                provenance = ContentProvenance.VERIFIED,
                packVersion = "1.0",
                cards = cards,
                isTeacherCreated = false
            )
        }
    }

    // ------------------------------------------------------------------
    // Recents Tracker
    // ------------------------------------------------------------------

    fun recordRecent(id: String) {
        val current = _recentItemIds.value.toMutableList()
        current.remove(id)
        current.add(0, id)
        if (current.size > MAX_RECENT_ITEMS) {
            current.removeAt(current.size - 1)
        }
        _recentItemIds.value = current
        scope.launch {
            try {
                val arr = JSONArray()
                current.forEach { arr.put(it) }
                prefs.edit().putString(KEY_RECENTS, arr.toString()).apply()
            } catch (t: Throwable) {
                // Ignore background storage errors
            }
        }
    }

    // ------------------------------------------------------------------
    // Serialization & Persistence Helpers
    // ------------------------------------------------------------------

    private fun persistMaterialsAsync(list: List<TeacherMaterial>) {
        scope.launch {
            try {
                val arr = JSONArray()
                list.forEach { m ->
                    val tagsArr = JSONArray()
                    m.tags.forEach { tagsArr.put(it) }
                    val obj = JSONObject().apply {
                        put("id", m.id)
                        put("title", m.title)
                        put("type", m.type.name)
                        put("languageCode", m.languageCode)
                        put("gradeLevel", m.gradeLevel.name)
                        put("domain", m.domain.name)
                        put("contentJson", m.contentJson)
                        put("provenance", m.provenance.name)
                        put("validationStatus", m.validationStatus)
                        put("version", m.version)
                        put("createdAtMs", m.createdAtMs)
                        put("updatedAtMs", m.updatedAtMs)
                        put("isFavorite", m.isFavorite)
                        put("tags", tagsArr)
                        put("notes", m.notes)
                    }
                    arr.put(obj)
                }
                prefs.edit().putString(KEY_MATERIALS, arr.toString()).apply()
            } catch (t: Throwable) {
                // Ignore background storage errors
            }
        }
    }

    private fun loadMaterialsFromStorage(): List<TeacherMaterial> {
        val raw = prefs.getString(KEY_MATERIALS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                val tagsArr = obj.optJSONArray("tags")
                val tags = if (tagsArr != null) (0 until tagsArr.length()).map { tagsArr.getString(it) } else emptyList()
                TeacherMaterial(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    type = MaterialType.valueOf(obj.getString("type")),
                    languageCode = obj.optString("languageCode", "sat"),
                    gradeLevel = GradeLevel.valueOf(obj.optString("gradeLevel", GradeLevel.GRADE_1.name)),
                    domain = SubjectDomain.valueOf(obj.optString("domain", SubjectDomain.FOUNDATIONAL_NUMERACY.name)),
                    contentJson = obj.optString("contentJson", "{}"),
                    provenance = ContentProvenance.valueOf(obj.optString("provenance", ContentProvenance.TEACHER_CREATED.name)),
                    validationStatus = obj.optString("validationStatus", "PENDING_VALIDATION"),
                    version = obj.optInt("version", 1),
                    createdAtMs = obj.optLong("createdAtMs", System.currentTimeMillis()),
                    updatedAtMs = obj.optLong("updatedAtMs", System.currentTimeMillis()),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    tags = tags,
                    notes = obj.optString("notes", "")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistHistoryAsync(list: List<TranslationHistoryItem>) {
        scope.launch {
            try {
                val arr = JSONArray()
                list.forEach { h ->
                    val obj = JSONObject().apply {
                        put("id", h.id)
                        put("sourceLanguage", h.sourceLanguage)
                        put("targetLanguage", h.targetLanguage)
                        put("sourceHindi", h.sourceHindi)
                        put("targetSantali", h.targetSantali)
                        put("targetLatin", h.targetLatin)
                        put("script", h.script)
                        put("provenance", h.provenance.name)
                        put("confidence", h.confidence.toDouble())
                        put("audioPath", h.audioPath ?: JSONObject.NULL)
                        put("packVersion", h.packVersion)
                        put("timestampMs", h.timestampMs)
                        put("isFavorite", h.isFavorite)
                        put("flagForCorrection", h.flagForCorrection)
                    }
                    arr.put(obj)
                }
                prefs.edit().putString(KEY_HISTORY, arr.toString()).apply()
            } catch (t: Throwable) {
                // Ignore storage error
            }
        }
    }

    private fun loadHistoryFromStorage(): List<TranslationHistoryItem> {
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                TranslationHistoryItem(
                    id = obj.getString("id"),
                    sourceLanguage = obj.optString("sourceLanguage", "hi"),
                    targetLanguage = obj.optString("targetLanguage", "sat"),
                    sourceHindi = obj.getString("sourceHindi"),
                    targetSantali = obj.getString("targetSantali"),
                    targetLatin = obj.optString("targetLatin", ""),
                    script = obj.optString("script", "Ol Chiki"),
                    provenance = ContentProvenance.valueOf(obj.optString("provenance", ContentProvenance.MACHINE_GENERATED.name)),
                    confidence = obj.optDouble("confidence", 0.85).toFloat(),
                    audioPath = if (obj.isNull("audioPath")) null else obj.getString("audioPath"),
                    packVersion = obj.optString("packVersion", "1.0"),
                    timestampMs = obj.optLong("timestampMs", System.currentTimeMillis()),
                    isFavorite = obj.optBoolean("isFavorite", false),
                    flagForCorrection = obj.optBoolean("flagForCorrection", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun persistDecksAsync(list: List<FlashcardDeck>) {
        scope.launch {
            try {
                val arr = JSONArray()
                list.forEach { d ->
                    val cardsArr = JSONArray()
                    d.cards.forEach { c ->
                        val cObj = JSONObject().apply {
                            put("id", c.id)
                            put("front", c.front)
                            put("back", c.back)
                            put("script", c.script)
                            put("transliteration", c.transliteration)
                            put("imageRef", c.imageRef ?: JSONObject.NULL)
                            put("audioRef", c.audioRef ?: JSONObject.NULL)
                            put("category", c.category)
                            put("difficulty", c.difficulty)
                            put("provenance", c.provenance.name)
                            put("isFavorite", c.isFavorite)
                            put("isDifficult", c.isDifficult)
                        }
                        cardsArr.put(cObj)
                    }
                    val obj = JSONObject().apply {
                        put("id", d.id)
                        put("title", d.title)
                        put("languageCode", d.languageCode)
                        put("classLevel", d.classLevel.name)
                        put("category", d.category)
                        put("provenance", d.provenance.name)
                        put("packVersion", d.packVersion)
                        put("isTeacherCreated", d.isTeacherCreated)
                        put("createdAtMs", d.createdAtMs)
                        put("cards", cardsArr)
                    }
                    arr.put(obj)
                }
                prefs.edit().putString(KEY_DECKS, arr.toString()).apply()
            } catch (t: Throwable) {
                // Ignore storage error
            }
        }
    }

    private fun loadDecksFromStorage(): List<FlashcardDeck> {
        val raw = prefs.getString(KEY_DECKS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val obj = arr.getJSONObject(i)
                val cardsArr = obj.getJSONArray("cards")
                val cards = (0 until cardsArr.length()).map { cIdx ->
                    val cObj = cardsArr.getJSONObject(cIdx)
                    Flashcard(
                        id = cObj.getString("id"),
                        front = cObj.getString("front"),
                        back = cObj.getString("back"),
                        script = cObj.optString("script", "Ol Chiki"),
                        transliteration = cObj.optString("transliteration", ""),
                        imageRef = if (cObj.isNull("imageRef")) null else cObj.getString("imageRef"),
                        audioRef = if (cObj.isNull("audioRef")) null else cObj.getString("audioRef"),
                        category = cObj.optString("category", "general"),
                        difficulty = cObj.optInt("difficulty", 1),
                        provenance = ContentProvenance.valueOf(cObj.optString("provenance", ContentProvenance.TEACHER_CREATED.name)),
                        isFavorite = cObj.optBoolean("isFavorite", false),
                        isDifficult = cObj.optBoolean("isDifficult", false)
                    )
                }
                FlashcardDeck(
                    id = obj.getString("id"),
                    title = obj.getString("title"),
                    languageCode = obj.optString("languageCode", "sat"),
                    classLevel = GradeLevel.valueOf(obj.optString("classLevel", GradeLevel.GRADE_1.name)),
                    category = obj.optString("category", "general"),
                    provenance = ContentProvenance.valueOf(obj.optString("provenance", ContentProvenance.TEACHER_CREATED.name)),
                    packVersion = obj.optString("packVersion", "1.0"),
                    cards = cards,
                    isTeacherCreated = obj.optBoolean("isTeacherCreated", true),
                    createdAtMs = obj.optLong("createdAtMs", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadRecentsFromStorage(): List<String> {
        val raw = prefs.getString(KEY_RECENTS, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { arr.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

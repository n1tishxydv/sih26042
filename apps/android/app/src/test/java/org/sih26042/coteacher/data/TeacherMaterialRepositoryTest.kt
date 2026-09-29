package org.sih26042.coteacher.data

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.sih26042.coteacher.core.model.*

/**
 * PHASE 6 — TeacherMaterialRepository Unit Tests
 *
 * Validates save, read, duplicate, delete, and bounded collection behavior.
 */
class TeacherMaterialRepositoryTest {

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
    fun `initial state loads empty collections when no prefs exist`() {
        val repo = TeacherMaterialRepository(context)
        assertTrue(repo.materials.value.isEmpty())
        assertTrue(repo.history.value.isEmpty())
        assertTrue(repo.decks.value.isEmpty())
        assertTrue(repo.recentItemIds.value.isEmpty())
    }

    @Test
    fun `saveMaterial stores item and marks provenance as TEACHER_CREATED`() {
        val repo = TeacherMaterialRepository(context)
        val mat = TeacherMaterial(
            id = "mat-001",
            title = "फल और सब्जियाँ",
            type = MaterialType.PHRASE,
            languageCode = "sat",
            provenance = ContentProvenance.TEACHER_CREATED
        )

        repo.saveMaterial(mat)

        val retrieved = repo.getMaterial("mat-001")
        assertNotNull(retrieved)
        assertEquals("फल और सब्जियाँ", retrieved?.title)
        assertEquals(ContentProvenance.TEACHER_CREATED, retrieved?.provenance)
        assertEquals(1, repo.materials.value.size)
    }

    @Test
    fun `deleteMaterial removes item cleanly`() {
        val repo = TeacherMaterialRepository(context)
        val mat = TeacherMaterial(id = "mat-002", title = "गिनती अभ्यास", type = MaterialType.WORKSHEET)
        repo.saveMaterial(mat)
        assertEquals(1, repo.materials.value.size)

        repo.deleteMaterial("mat-002")
        assertNull(repo.getMaterial("mat-002"))
        assertEquals(0, repo.materials.value.size)
    }

    @Test
    fun `duplicateMaterial creates unique copy with incremented title`() {
        val repo = TeacherMaterialRepository(context)
        val original = TeacherMaterial(id = "mat-orig", title = "शरीर के अंग", type = MaterialType.FLASHCARD_SET)
        repo.saveMaterial(original)

        val copy = repo.duplicateMaterial("mat-orig")
        assertNotNull(copy)
        assertNotEquals("mat-orig", copy?.id)
        assertEquals("शरीर के अंग (Copy)", copy?.title)
        assertEquals(2, repo.materials.value.size)
    }

    @Test
    fun `toggleFavorite toggles favorite state`() {
        val repo = TeacherMaterialRepository(context)
        val mat = TeacherMaterial(id = "mat-fav", title = "अभिवादन", type = MaterialType.PHRASE, isFavorite = false)
        repo.saveMaterial(mat)

        repo.toggleFavoriteMaterial("mat-fav")
        assertTrue(repo.getMaterial("mat-fav")!!.isFavorite)

        repo.toggleFavoriteMaterial("mat-fav")
        assertFalse(repo.getMaterial("mat-fav")!!.isFavorite)
    }

    @Test
    fun `translation history stores items in descending order`() {
        val repo = TeacherMaterialRepository(context)
        val item1 = TranslationHistoryItem(id = "hist-1", sourceHindi = "किताब खोलो", targetSantali = "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱯᱮ")
        val item2 = TranslationHistoryItem(id = "hist-2", sourceHindi = "बैठ जाओ", targetSantali = "ᱫᱩᱲᱩᱵ ᱯᱮ")

        repo.addHistoryItem(item1)
        repo.addHistoryItem(item2)

        val historyList = repo.history.value
        assertEquals(2, historyList.size)
        assertEquals("hist-2", historyList[0].id) // Most recent first
    }

    @Test
    fun `getAllDecks merges built-in pack vocab decks with custom decks`() {
        val repo = TeacherMaterialRepository(context)
        val mockFlnVocab = listOf(
            FlnVocabularyItem(wordId = "num_1", category = "numbers", hindiWord = "एक", targetNativeScript = "ᱢᱤᱫ", targetTransliteration = "mid"),
            FlnVocabularyItem(wordId = "ani_1", category = "animals", hindiWord = "गाय", targetNativeScript = "ᱜᱟᱹᱭ", targetTransliteration = "ga:y")
        )

        val customDeck = FlashcardDeck(
            id = "custom-deck-1",
            title = "My Custom Deck",
            cards = listOf(Flashcard(front = "पानी", back = "ᱫᱟᱜ")),
            isTeacherCreated = true
        )
        repo.saveDeck(customDeck)

        val allDecks = repo.getAllDecks(mockFlnVocab)
        // 5 default category decks + 1 custom deck = 6 decks
        assertEquals(6, allDecks.size)
        assertTrue(allDecks.any { it.id == "custom-deck-1" })
        assertTrue(allDecks.any { it.id == "default_deck_numbers" })
    }
}

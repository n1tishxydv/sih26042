package org.sih26042.coteacher.core.search

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.data.ClassroomRepository
import org.sih26042.coteacher.data.TeacherMaterialRepository

/**
 * PHASE 6 — LocalSearchService Unit Tests
 */
class LocalSearchServiceTest {

    private lateinit var classroomRepo: ClassroomRepository
    private lateinit var teacherMaterialRepo: TeacherMaterialRepository
    private lateinit var searchService: LocalSearchService

    @Before
    fun setUp() {
        val mockContext: Context = mock()
        val mockPrefs: SharedPreferences = mock()
        val mockEditor: SharedPreferences.Editor = mock()

        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
        whenever(mockPrefs.getString(any(), isNull())).thenReturn(null)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockContext.getSharedPreferences(any(), any())).thenReturn(mockPrefs)

        classroomRepo = mock()
        teacherMaterialRepo = mock()

        val samplePhrases = listOf(
            ClassroomPhrase(
                phraseId = "ph_open_book",
                hindiCanonical = "किताब खोलो",
                hindiNormalized = "किताब खोलो",
                targetNativeScript = "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱯᱮ",
                targetTransliterationLatin = "puthi jhij pe",
                category = "Classroom Management",
                provenance = ProvenanceState.VERIFIED
            ),
            ClassroomPhrase(
                phraseId = "ph_sit_down",
                hindiCanonical = "बैठ जाओ",
                hindiNormalized = "बैठ जाओ",
                targetNativeScript = "ᱫᱩᱲᱩᱵ ᱯᱮ",
                targetTransliterationLatin = "durub pe",
                category = "Classroom Management",
                provenance = ProvenanceState.VERIFIED
            )
        )

        val sampleVocab = listOf(
            FlnVocabularyItem(
                wordId = "voc_water",
                category = "classroom_objects",
                hindiWord = "पानी",
                targetNativeScript = "ᱫᱟᱜ",
                targetTransliteration = "da:g"
            )
        )

        whenever(classroomRepo.phrases).thenReturn(MutableStateFlow(samplePhrases))
        whenever(classroomRepo.flnVocabulary).thenReturn(MutableStateFlow(sampleVocab))
        whenever(teacherMaterialRepo.materials).thenReturn(MutableStateFlow(emptyList()))
        whenever(teacherMaterialRepo.getAllDecks(any())).thenReturn(emptyList())

        searchService = LocalSearchService(classroomRepo, teacherMaterialRepo)
    }

    @Test
    fun `search with empty query returns empty list`() {
        val results = searchService.search("")
        assertTrue(results.isEmpty())
    }

    @Test
    fun `search with Hindi query finds matching phrase`() {
        val results = searchService.search("किताब")
        assertTrue(results.isNotEmpty())
        assertEquals("ph_open_book", results[0].id)
        assertEquals(SearchResultType.PHRASE, results[0].type)
    }

    @Test
    fun `search with Ol Chiki query finds matching vocabulary`() {
        val results = searchService.search("ᱫᱟᱜ")
        assertTrue(results.isNotEmpty())
        assertEquals("voc_water", results[0].id)
        assertEquals(SearchResultType.VOCABULARY, results[0].type)
    }

    @Test
    fun `search filtering by SearchResultType restricts results`() {
        val resultsOnlyVocab = searchService.search("खोलो", filterType = SearchResultType.VOCABULARY)
        assertTrue(resultsOnlyVocab.isEmpty()) // "खोलो" is a phrase, not vocab

        val resultsPhrases = searchService.search("खोलो", filterType = SearchResultType.PHRASE)
        assertEquals(1, resultsPhrases.size)
    }
}

package org.sih26042.coteacher.domain

import kotlinx.coroutines.flow.StateFlow
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.data.ClassroomRepository
import org.sih26042.coteacher.data.LanguagePackRepository

class GetFlnContentUseCase(
    private val classroomRepository: ClassroomRepository
) {
    val flnVocabulary: StateFlow<List<FlnVocabularyItem>> = classroomRepository.flnVocabulary
    val worksheets: StateFlow<List<NipunWorksheet>> = classroomRepository.worksheets
    val activities: StateFlow<List<StudentActivityItem>> = classroomRepository.activities
    val phrases: StateFlow<List<ClassroomPhrase>> = classroomRepository.phrases
}

class ManageLanguagePacksUseCase(
    private val languagePackRepository: LanguagePackRepository
) {
    val availablePacks: StateFlow<List<LanguagePackInfo>> = languagePackRepository.availablePacks
    val activePack: StateFlow<LanguagePackInfo> = languagePackRepository.activePack

    fun switchPack(packId: String) {
        languagePackRepository.switchActivePack(packId)
    }

    fun verifyIntegrity(packId: String): Boolean {
        return languagePackRepository.verifyPackIntegrity(packId)
    }
}

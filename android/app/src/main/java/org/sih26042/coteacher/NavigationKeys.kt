package org.sih26042.coteacher

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object HomeDest : NavKey
@Serializable data object LiveClassDest : NavKey
@Serializable data class TranslationDetailDest(
    val hindiText: String,
    val nativeScriptText: String,
    val latinTransliteration: String,
    val provenanceState: String,
    val confidence: Float,
    val audioPath: String?,
    val pedagogicalContext: String? = null
) : NavKey
@Serializable data object WorksheetsDest : NavKey
@Serializable data object FlashcardsDest : NavKey
@Serializable data object ActivitiesDest : NavKey
@Serializable data object LanguagePacksDest : NavKey
@Serializable data object PerformanceDest : NavKey
@Serializable data object SettingsDest : NavKey

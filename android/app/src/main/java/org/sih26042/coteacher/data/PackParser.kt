package org.sih26042.coteacher.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import org.sih26042.coteacher.core.model.*
import java.io.InputStream
import java.security.MessageDigest

class PackParser(private val context: Context) {

    private fun JSONObject.optNullableString(name: String): String? {
        return if (has(name) && !isNull(name)) getString(name) else null
    }

    fun loadEmbeddedPhrases(): List<ClassroomPhrase> {
        val list = mutableListOf<ClassroomPhrase>()
        try {
            val jsonStr = context.assets.open("embedded_pack/phrases.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val aliases = mutableListOf<String>()
                val aliasesArr = obj.optJSONArray("hindi_aliases")
                if (aliasesArr != null) {
                    for (j in 0 until aliasesArr.length()) {
                        aliases.add(aliasesArr.getString(j))
                    }
                }

                list.add(
                    ClassroomPhrase(
                        phraseId = obj.getString("phrase_id"),
                        hindiCanonical = obj.getString("hindi_canonical"),
                        hindiNormalized = obj.getString("hindi_normalized"),
                        hindiAliases = aliases,
                        targetNativeScript = obj.getString("target_native_script"),
                        targetTransliterationLatin = obj.getString("target_transliteration_latin"),
                        targetTransliterationDevanagari = obj.optNullableString("target_transliteration_devanagari"),
                        audioPath = obj.optNullableString("audio_path"),
                        category = obj.getString("category"),
                        flnDomain = obj.optNullableString("fln_domain"),
                        pedagogicalContext = obj.optNullableString("pedagogical_context"),
                        provenance = ProvenanceState.valueOf(obj.optString("provenance", "VERIFIED")),
                        difficultyLevel = obj.optInt("difficulty_level", 1)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun loadEmbeddedFlnVocabulary(): List<FlnVocabularyItem> {
        val list = mutableListOf<FlnVocabularyItem>()
        try {
            val jsonStr = context.assets.open("embedded_pack/fln_vocabulary.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    FlnVocabularyItem(
                        wordId = obj.getString("word_id"),
                        category = obj.getString("category"),
                        hindiWord = obj.getString("hindi_word"),
                        targetNativeScript = obj.getString("target_native_script"),
                        targetTransliteration = obj.getString("target_transliteration"),
                        audioPath = obj.optNullableString("audio_path"),
                        numericalValue = if (obj.has("numerical_value")) obj.getInt("numerical_value") else null
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun loadEmbeddedWorksheets(): List<NipunWorksheet> {
        val list = mutableListOf<NipunWorksheet>()
        try {
            val jsonStr = context.assets.open("embedded_pack/worksheets.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val wsObj = array.getJSONObject(i)
                val itemsList = mutableListOf<WorksheetQuestion>()
                val itemsArr = wsObj.getJSONArray("items")
                for (j in 0 until itemsArr.length()) {
                    val qObj = itemsArr.getJSONObject(j)
                    val opts = mutableListOf<String>()
                    val optsArr = qObj.getJSONArray("options")
                    for (k in 0 until optsArr.length()) {
                        opts.add(optsArr.getString(k))
                    }
                    itemsList.add(
                        WorksheetQuestion(
                            itemId = qObj.getString("item_id"),
                            worksheetId = qObj.getString("worksheet_id"),
                            questionNumber = qObj.getInt("question_number"),
                            promptHindi = qObj.getString("prompt_hindi"),
                            promptTargetNative = qObj.getString("prompt_target_native"),
                            promptTargetTransliteration = qObj.getString("prompt_target_transliteration"),
                            questionType = qObj.getString("question_type"),
                            options = opts,
                            correctAnswer = qObj.getString("correct_answer"),
                            visualAsset = qObj.optNullableString("visual_asset")
                        )
                    )
                }
                list.add(
                    NipunWorksheet(
                        worksheetId = wsObj.getString("worksheet_id"),
                        title = wsObj.getString("title"),
                        gradeLevel = wsObj.getString("grade_level"),
                        nipunCompetency = wsObj.getString("nipun_competency"),
                        items = itemsList
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun loadEmbeddedActivities(): List<StudentActivityItem> {
        val list = mutableListOf<StudentActivityItem>()
        try {
            val jsonStr = context.assets.open("embedded_pack/activities.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    StudentActivityItem(
                        activityId = obj.getString("activity_id"),
                        title = obj.getString("title"),
                        activityType = obj.getString("activity_type"),
                        teacherPromptHindi = obj.getString("teacher_prompt_hindi"),
                        teacherPromptNative = obj.getString("teacher_prompt_native"),
                        studentResponseNative = obj.getString("student_response_native"),
                        studentResponseTransliteration = obj.getString("student_response_transliteration"),
                        audioPromptPath = obj.optNullableString("audio_prompt_path"),
                        pedagogicalObjective = obj.getString("pedagogical_objective")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun computeStreamSha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(8192)
        var read: Int
        while (inputStream.read(buffer).also { read = it } != -1) {
            digest.update(buffer, 0, read)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}

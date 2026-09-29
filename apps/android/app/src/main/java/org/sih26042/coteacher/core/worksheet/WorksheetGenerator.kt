package org.sih26042.coteacher.core.worksheet

import org.sih26042.coteacher.core.model.*
import java.util.UUID

/**
 * PHASE 6 — WorksheetGenerator
 *
 * Deterministic, offline NIPUN worksheet generation from explicit curriculum inputs.
 * Strictly avoids generative AI hallucination.
 *
 * Supported templates:
 * - Template A: Matching (Hindi <-> Santali)
 * - Template B: Multiple Choice (Select target word from 3-4 options)
 * - Template C: Fill / Select
 * - Template D: Ordering (Sequence numbers or terms)
 * - Template E: Picture Recognition
 */
object WorksheetGenerator {

    fun generate(
        title: String,
        templateType: WorksheetTemplateType,
        selectedVocab: List<FlnVocabularyItem>,
        gradeLevel: GradeLevel = GradeLevel.GRADE_1,
        domain: SubjectDomain = SubjectDomain.FOUNDATIONAL_NUMERACY
    ): GeneratedWorksheet {
        val worksheetId = UUID.randomUUID().toString()
        val questions = mutableListOf<WorksheetQuestion>()

        when (templateType) {
            WorksheetTemplateType.MATCHING -> {
                selectedVocab.take(5).forEachIndexed { index, item ->
                    val otherOptions = selectedVocab.filter { it.wordId != item.wordId }
                        .map { it.targetNativeScript }
                        .shuffled()
                        .take(2)
                    val allOptions = (otherOptions + item.targetNativeScript).shuffled()

                    questions.add(
                        WorksheetQuestion(
                            itemId = "match_${item.wordId}",
                            worksheetId = worksheetId,
                            questionNumber = index + 1,
                            promptHindi = "मिलान करें: '${item.hindiWord}'",
                            promptTargetNative = item.targetNativeScript,
                            promptTargetTransliteration = item.targetTransliteration,
                            questionType = "MATCHING",
                            options = allOptions,
                            correctAnswer = item.targetNativeScript
                        )
                    )
                }
            }

            WorksheetTemplateType.MULTIPLE_CHOICE -> {
                selectedVocab.take(5).forEachIndexed { index, item ->
                    val distractorOptions = selectedVocab.filter { it.wordId != item.wordId }
                        .map { it.targetNativeScript }
                        .shuffled()
                        .take(3)
                    val options = (distractorOptions + item.targetNativeScript).shuffled()

                    questions.add(
                        WorksheetQuestion(
                            itemId = "mcq_${item.wordId}",
                            worksheetId = worksheetId,
                            questionNumber = index + 1,
                            promptHindi = "सही संताली शब्द चुनें: '${item.hindiWord}'",
                            promptTargetNative = "?",
                            promptTargetTransliteration = item.targetTransliteration,
                            questionType = "MULTIPLE_CHOICE",
                            options = options,
                            correctAnswer = item.targetNativeScript
                        )
                    )
                }
            }

            WorksheetTemplateType.FILL_SELECT -> {
                selectedVocab.take(5).forEachIndexed { index, item ->
                    val distractorOptions = selectedVocab.filter { it.wordId != item.wordId }
                        .map { it.targetNativeScript }
                        .shuffled()
                        .take(2)
                    val options = (distractorOptions + item.targetNativeScript).shuffled()

                    questions.add(
                        WorksheetQuestion(
                            itemId = "fill_${item.wordId}",
                            worksheetId = worksheetId,
                            questionNumber = index + 1,
                            promptHindi = "खाली स्थान भरें: ${item.hindiWord} = ______",
                            promptTargetNative = "______",
                            promptTargetTransliteration = item.targetTransliteration,
                            questionType = "FILL_SELECT",
                            options = options,
                            correctAnswer = item.targetNativeScript
                        )
                    )
                }
            }

            WorksheetTemplateType.ORDERING -> {
                val sortedVocab = selectedVocab.filter { it.numericalValue != null }
                    .sortedBy { it.numericalValue }
                    .take(5)
                val itemsToUse = if (sortedVocab.size >= 3) sortedVocab else selectedVocab.take(4)

                itemsToUse.forEachIndexed { index, item ->
                    val options = itemsToUse.map { it.targetNativeScript }.shuffled()
                    questions.add(
                        WorksheetQuestion(
                            itemId = "order_${item.wordId}",
                            worksheetId = worksheetId,
                            questionNumber = index + 1,
                            promptHindi = "क्रम संख्या ${index + 1} पर क्या आएगा?",
                            promptTargetNative = item.targetNativeScript,
                            promptTargetTransliteration = item.targetTransliteration,
                            questionType = "ORDERING",
                            options = options,
                            correctAnswer = item.targetNativeScript
                        )
                    )
                }
            }

            WorksheetTemplateType.PICTURE_RECOGNITION -> {
                selectedVocab.take(4).forEachIndexed { index, item ->
                    val distractorOptions = selectedVocab.filter { it.wordId != item.wordId }
                        .map { it.targetNativeScript }
                        .shuffled()
                        .take(2)
                    val options = (distractorOptions + item.targetNativeScript).shuffled()

                    questions.add(
                        WorksheetQuestion(
                            itemId = "pic_${item.wordId}",
                            worksheetId = worksheetId,
                            questionNumber = index + 1,
                            promptHindi = "चित्र पहचानें: ${item.hindiWord}",
                            promptTargetNative = item.targetNativeScript,
                            promptTargetTransliteration = item.targetTransliteration,
                            questionType = "PICTURE_RECOGNITION",
                            options = options,
                            correctAnswer = item.targetNativeScript,
                            visualAsset = "drawable/ic_${item.category}"
                        )
                    )
                }
            }
        }

        return GeneratedWorksheet(
            id = worksheetId,
            title = title,
            templateType = templateType,
            gradeLevel = gradeLevel,
            domain = domain,
            instructionsHindi = "सभी प्रश्नों को ध्यान से पढ़ें और सही उत्तर चुनें।",
            instructionsSantali = "ᱡᱚᱛᱚ ᱠᱩᱠᱞᱤ ᱵᱮᱥ ᱛᱮ ᱯᱟᱲᱦᱟᱣ ᱢᱮ ᱟᱨ ᱥᱟᱹᱦᱤ ᱛᱮᱞᱟ ᱵᱟᱪᱷᱟᱣ ᱢᱮ ᱾",
            questions = questions,
            provenance = ContentProvenance.TEACHER_CREATED
        )
    }
}

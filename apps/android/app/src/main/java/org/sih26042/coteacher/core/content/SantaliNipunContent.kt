package org.sih26042.coteacher.core.content

import org.sih26042.coteacher.core.model.*

/**
 * PHASE 5 — Santali NIPUN/FLN Classroom Content
 *
 * LINGUISTIC DISCLAIMER (required on all UI surfaces):
 *   All Santali content is PENDING_VALIDATION — not yet reviewed by a certified
 *   native speaker. Status badges MUST be shown wherever this content appears.
 *
 * CONTENT PROVENANCE:
 *   - Phrases: derived from NIPUN Bharat / NCERT primary pedagogy
 *   - Santali Ol Chiki script: linguistically curated prototype
 *   - Native speaker acoustic sign-off: PENDING
 *
 * NIPUN COMPETENCY REFERENCES:
 *   FLN stands for Foundational Literacy and Numeracy.
 *   Competency codes align with NIPUN Bharat 2021 Lakshya targets.
 */
object SantaliNipunContent {

    // ------------------------------------------------------------------
    // LEARNING OUTCOMES
    // ------------------------------------------------------------------

    val OUTCOME_NUM_1_COUNTING = LearningOutcome(
        outcomeId = "out_num_g1_counting",
        domain = SubjectDomain.FOUNDATIONAL_NUMERACY,
        gradeLevel = GradeLevel.GRADE_1,
        objectiveHindi = "1 से 10 तक गिनना",
        objectiveSantali = "ᱢᱤᱫ ᱴᱷᱮ ᱜᱮᱞ ᱴᱷᱟᱱᱩᱜ ᱜᱮ ᱢᱤᱴᱷᱟᱣ",
        objectiveLatin = "Mid ṭhe gel ṭhānug ge miṭhāo",
        nipunCompetencyRef = "FLN-NUM-G1-C1",
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val OUTCOME_NUM_1_SHAPES = LearningOutcome(
        outcomeId = "out_num_g1_shapes",
        domain = SubjectDomain.FOUNDATIONAL_NUMERACY,
        gradeLevel = GradeLevel.GRADE_1,
        objectiveHindi = "आकार पहचानना — वृत्त, त्रिकोण, वर्ग",
        objectiveSantali = "ᱟᱠᱟᱨ ᱪᱤᱱᱦᱟᱹ ᱢᱮ",
        objectiveLatin = "Ākār cinhā me",
        nipunCompetencyRef = "FLN-NUM-G1-C4",
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val OUTCOME_LIT_1_ALPHABET = LearningOutcome(
        outcomeId = "out_lit_g1_alphabet",
        domain = SubjectDomain.FOUNDATIONAL_LITERACY,
        gradeLevel = GradeLevel.GRADE_1,
        objectiveHindi = "ओल चिकी अक्षर पहचानना",
        objectiveSantali = "ᱚᱞ ᱪᱤᱠᱤ ᱟᱠ੍ᱥᱚᱨ ᱪᱤᱱᱦᱟᱹ ᱢᱮ",
        objectiveLatin = "Ol Ciki aksar cinhā me",
        nipunCompetencyRef = "FLN-LIT-G1-C1",
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val OUTCOME_LIT_1_ORAL = LearningOutcome(
        outcomeId = "out_lit_g1_oral",
        domain = SubjectDomain.ORAL_LANGUAGE,
        gradeLevel = GradeLevel.GRADE_1,
        objectiveHindi = "बोलकर पूछना और जवाब देना",
        objectiveSantali = "ᱜᱮᱞ ᱩᱰᱟᱹᱲ ᱮᱛᱟᱜ ᱢᱮ",
        objectiveLatin = "Gel uḍāṛ etāg me",
        nipunCompetencyRef = "FLN-LIT-G1-C5",
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val OUTCOME_ENV_1_BODY = LearningOutcome(
        outcomeId = "out_env_g1_body",
        domain = SubjectDomain.ENVIRONMENTAL_AWARENESS,
        gradeLevel = GradeLevel.GRADE_1,
        objectiveHindi = "शरीर के अंग पहचानना",
        objectiveSantali = "ᱜᱟᱢᱛᱟ ᱪᱤᱨᱟ ᱪᱤᱱᱦᱟᱹ ᱢᱮ",
        objectiveLatin = "Gamtā cirā cinhā me",
        nipunCompetencyRef = "FLN-ENV-G1-C2",
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    // ------------------------------------------------------------------
    // ACTIVITIES
    // ------------------------------------------------------------------

    val ACTIVITY_COUNTING_1_10 = ClassroomActivity(
        activityId = "act_counting_1_10",
        title = "1 से 10 गिनो",
        type = ActivityType.NUMBER_SELECTION,
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.FOUNDATIONAL_NUMERACY,
        objectiveHindi = "1 से 10 तक संख्या पहचानें और बोलें",
        objectiveSantali = "ᱢᱤᱫ ᱴᱷᱮ ᱜᱮᱞ ᱴᱷᱟᱱᱩᱜ ᱜᱮ ᱪᱤᱱᱦᱟᱹ ᱫᱚ ᱜᱮᱞ ᱩᱰᱟᱹᱲ ᱢᱮ",
        objectiveLatin = "Mid ṭhe gel ṭhānug ge cinhā do gel uḍāṛ me",
        items = listOf(
            ActivityItem(
                itemId = "act_cnt_01",
                activityId = "act_counting_1_10",
                promptHindi = "यह कितना है? (1 उंगली दिखाएं)",
                promptSantali = "ᱱᱚᱣᱟ ᱜᱮᱞ ᱴᱷᱮ?",
                promptLatin = "Noā gel ṭhe?",
                options = listOf("1", "2", "3"),
                correctAnswer = "1"
            ),
            ActivityItem(
                itemId = "act_cnt_02",
                activityId = "act_counting_1_10",
                promptHindi = "यह कितना है? (3 उंगलियाँ दिखाएं)",
                promptSantali = "ᱱᱚᱣᱟ ᱜᱮᱞ ᱴᱷᱮ?",
                promptLatin = "Noā gel ṭhe?",
                options = listOf("2", "3", "4"),
                correctAnswer = "3"
            ),
            ActivityItem(
                itemId = "act_cnt_03",
                activityId = "act_counting_1_10",
                promptHindi = "यह कितना है? (5 उंगलियाँ दिखाएं)",
                promptSantali = "ᱱᱚᱣᱟ ᱜᱮᱞ ᱴᱷᱮ?",
                promptLatin = "Noā gel ṭhe?",
                options = listOf("4", "5", "6"),
                correctAnswer = "5"
            ),
            ActivityItem(
                itemId = "act_cnt_04",
                activityId = "act_counting_1_10",
                promptHindi = "यह कितना है? (7 उंगलियाँ दिखाएं)",
                promptSantali = "ᱱᱚᱣᱟ ᱜᱮᱞ ᱴᱷᱮ?",
                promptLatin = "Noā gel ṭhe?",
                options = listOf("6", "7", "8"),
                correctAnswer = "7"
            ),
            ActivityItem(
                itemId = "act_cnt_05",
                activityId = "act_counting_1_10",
                promptHindi = "यह कितना है? (10 उंगलियाँ दिखाएं)",
                promptSantali = "ᱱᱚᱣᱟ ᱜᱮᱞ ᱴᱷᱮ?",
                promptLatin = "Noā gel ṭhe?",
                options = listOf("9", "10", "11"),
                correctAnswer = "10"
            )
        ),
        estimatedMinutes = 5,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val ACTIVITY_SHAPES = ClassroomActivity(
        activityId = "act_shapes_basic",
        title = "आकार पहचानो",
        type = ActivityType.PICTURE_SELECTION,
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.FOUNDATIONAL_NUMERACY,
        objectiveHindi = "वृत्त, त्रिकोण और वर्ग पहचानें",
        objectiveSantali = "ᱜᱚᱞ ᱫᱟᱲᱮ, ᱴᱤᱭᱟᱬ ᱫᱟᱲᱮ ᱟᱨ ᱪᱩᱠᱩ ᱫᱟᱲᱮ ᱪᱤᱱᱦᱟᱹ ᱢᱮ",
        objectiveLatin = "Gol ḍāṛe, Tiāṇ ḍāṛe āṛ cuku ḍāṛe cinhā me",
        items = listOf(
            ActivityItem(
                itemId = "act_shp_01",
                activityId = "act_shapes_basic",
                promptHindi = "वृत्त कौन सा है? (गोल आकार)",
                promptSantali = "ᱜᱚᱞ ᱫᱟᱲᱮ ᱠᱚᱱᱟ?",
                promptLatin = "Gol ḍāṛe konā?",
                options = listOf("⭕ वृत्त", "🔺 त्रिकोण", "🟦 वर्ग"),
                correctAnswer = "⭕ वृत्त",
                visualAsset = null
            ),
            ActivityItem(
                itemId = "act_shp_02",
                activityId = "act_shapes_basic",
                promptHindi = "त्रिकोण कौन सा है?",
                promptSantali = "ᱴᱤᱭᱟᱬ ᱫᱟᱲᱮ ᱠᱚᱱᱟ?",
                promptLatin = "Tiāṇ ḍāṛe konā?",
                options = listOf("⭕ वृत्त", "🔺 त्रिकोण", "🟦 वर्ग"),
                correctAnswer = "🔺 त्रिकोण"
            ),
            ActivityItem(
                itemId = "act_shp_03",
                activityId = "act_shapes_basic",
                promptHindi = "वर्ग कौन सा है?",
                promptSantali = "ᱪᱩᱠᱩ ᱫᱟᱲᱮ ᱠᱚᱱᱟ?",
                promptLatin = "Cuku ḍāṛe konā?",
                options = listOf("⭕ वृत्त", "🔺 त्रिकोण", "🟦 वर्ग"),
                correctAnswer = "🟦 वर्ग"
            )
        ),
        estimatedMinutes = 4,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val ACTIVITY_CHORAL_GREETINGS = ClassroomActivity(
        activityId = "act_choral_greetings",
        title = "सुप्रभात — स्वागत",
        type = ActivityType.CHORAL_RESPONSE,
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.ORAL_LANGUAGE,
        objectiveHindi = "अभिवादन दोहराना और समझना",
        objectiveSantali = "ᱥᱟᱞᱟᱢ ᱞᱟᱹᱜᱤᱫ ᱢᱮᱞᱟᱜ ᱢᱮ",
        objectiveLatin = "Sālām lāgid melāg me",
        items = listOf(
            ActivityItem(
                itemId = "act_chr_01",
                activityId = "act_choral_greetings",
                promptHindi = "सभी बच्चे एक साथ बोलें: 'जोहार!'",
                promptSantali = "ᱡᱚᱦᱟᱨ!",
                promptLatin = "Johār!",
                audioPath = "audio/ph_good_morning_01.wav",
                options = emptyList(),
                correctAnswer = "ᱡᱚᱦᱟᱨ"
            ),
            ActivityItem(
                itemId = "act_chr_02",
                activityId = "act_choral_greetings",
                promptHindi = "शिक्षक: 'आप कैसे हैं?' — बच्चे जवाब दें",
                promptSantali = "ᱟᱯᱮ ᱪᱮᱫ ᱠᱟᱱᱟ?",
                promptLatin = "Āpe ced kānā?",
                audioPath = null,
                options = emptyList(),
                correctAnswer = "ᱦᱚᱲ ᱠᱟᱱᱟ"
            ),
            ActivityItem(
                itemId = "act_chr_03",
                activityId = "act_choral_greetings",
                promptHindi = "सभी बच्चे धन्यवाद बोलें",
                promptSantali = "ᱥᱮᱸᱜᱮᱞ ᱢᱮᱱᱛᱮ",
                promptLatin = "Sengel mente",
                audioPath = null,
                options = emptyList(),
                correctAnswer = "ᱥᱮᱸᱜᱮᱞ"
            )
        ),
        estimatedMinutes = 3,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val ACTIVITY_BODY_PARTS = ClassroomActivity(
        activityId = "act_body_parts",
        title = "शरीर के अंग",
        type = ActivityType.RECOGNITION_TAP,
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.ENVIRONMENTAL_AWARENESS,
        objectiveHindi = "शरीर के अंग पहचानें और नाम बोलें",
        objectiveSantali = "ᱜᱟᱢᱛᱟ ᱪᱤᱨᱟ ᱪᱤᱱᱦᱟᱹ ᱫᱚ ᱱᱤᱡ ᱠᱟᱴᱷᱟ ᱩᱰᱟᱹᱲ ᱢᱮ",
        objectiveLatin = "Gamtā cirā cinhā do nij kāṭhā uḍāṛ me",
        items = listOf(
            ActivityItem(
                itemId = "act_body_01",
                activityId = "act_body_parts",
                promptHindi = "आँख कहाँ है? अपनी आँख दिखाओ",
                promptSantali = "ᱢᱮᱛ ᱦᱩᱡᱩ ᱠᱟᱱᱟ? ᱱᱤᱡ ᱢᱮᱛ ᱤᱫᱤ ᱢᱮ",
                promptLatin = "Met huju kānā? Nij met idi me",
                options = listOf("आँख / ᱢᱮᱛ", "कान / ᱠᱟᱱ", "नाक / ᱢᱩᱱᱫᱟᱹ"),
                correctAnswer = "आँख / ᱢᱮᱛ"
            ),
            ActivityItem(
                itemId = "act_body_02",
                activityId = "act_body_parts",
                promptHindi = "कान कहाँ है? अपना कान दिखाओ",
                promptSantali = "ᱠᱟᱱ ᱦᱩᱡᱩ ᱠᱟᱱᱟ? ᱱᱤᱡ ᱠᱟᱱ ᱤᱫᱤ ᱢᱮ",
                promptLatin = "Kān huju kānā? Nij kān idi me",
                options = listOf("आँख / ᱢᱮᱛ", "कान / ᱠᱟᱱ", "नाक / ᱢᱩᱱᱫᱟᱹ"),
                correctAnswer = "कान / ᱠᱟᱱ"
            ),
            ActivityItem(
                itemId = "act_body_03",
                activityId = "act_body_parts",
                promptHindi = "नाक कहाँ है? अपनी नाक दिखाओ",
                promptSantali = "ᱢᱩᱱᱫᱟᱹ ᱦᱩᱡᱩ ᱠᱟᱱᱟ? ᱱᱤᱡ ᱢᱩᱱᱫᱟᱹ ᱤᱫᱤ ᱢᱮ",
                promptLatin = "Mundā huju kānā? Nij mundā idi me",
                options = listOf("आँख / ᱢᱮᱛ", "कान / ᱠᱟᱱ", "नाक / ᱢᱩᱱᱫᱟᱹ"),
                correctAnswer = "नाक / ᱢᱩᱱᱫᱟᱹ"
            ),
            ActivityItem(
                itemId = "act_body_04",
                activityId = "act_body_parts",
                promptHindi = "हाथ कहाँ है? दोनों हाथ ऊपर उठाओ",
                promptSantali = "ᱦᱟᱴ ᱦᱩᱡᱩ ᱠᱟᱱᱟ? ᱫᱟᱬᱮᱭ ᱦᱟᱴ ᱩᱯᱟᱹᱨ ᱪᱟᱸᱜᱲᱟᱜ ᱢᱮ",
                promptLatin = "Hāṭ huju kānā? Ḍāṇe hāṭ upāṛ cāṅṛāg me",
                options = listOf("हाथ / ᱦᱟᱴ", "पैर / ᱫᱟᱬᱟ", "मुँह / ᱢᱩᱲ"),
                correctAnswer = "हाथ / ᱦᱟᱴ"
            )
        ),
        estimatedMinutes = 6,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val ACTIVITY_YES_NO_ANIMALS = ClassroomActivity(
        activityId = "act_yes_no_animals",
        title = "हाँ या नहीं?",
        type = ActivityType.YES_NO,
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.ENVIRONMENTAL_AWARENESS,
        objectiveHindi = "हाँ/नहीं में जवाब देना",
        objectiveSantali = "ᱦᱚ / ᱱᱟᱸᱦᱚ ᱫᱚ ᱡᱟᱵᱟᱵ ᱢᱮ",
        objectiveLatin = "Ho / Nāho do jābāb me",
        items = listOf(
            ActivityItem(
                itemId = "act_yn_01",
                activityId = "act_yes_no_animals",
                promptHindi = "क्या बाघ जंगल में रहता है? हाँ या नहीं?",
                promptSantali = "ᱠᱤ ᱵᱟᱜᱷ ᱵᱤᱨ ᱫᱚ ᱛᱷᱤᱠᱟᱣ ᱠᱟᱱᱟ?",
                promptLatin = "Ki bāgh bir do ṭhikāo kānā?",
                options = listOf("हाँ / ᱦᱚ", "नहीं / ᱱᱟᱸᱦᱚ"),
                correctAnswer = "हाँ / ᱦᱚ"
            ),
            ActivityItem(
                itemId = "act_yn_02",
                activityId = "act_yes_no_animals",
                promptHindi = "क्या मछली पेड़ पर रहती है? हाँ या नहीं?",
                promptSantali = "ᱠᱤ ᱦᱟᱠᱩ ᱫᱟᱨᱮ ᱫᱚ ᱛᱷᱤᱠᱟᱣ ᱠᱟᱱᱟ?",
                promptLatin = "Ki hāku ḍāre do ṭhikāo kānā?",
                options = listOf("हाँ / ᱦᱚ", "नहीं / ᱱᱟᱸᱦᱚ"),
                correctAnswer = "नहीं / ᱱᱟᱸᱦᱚ"
            ),
            ActivityItem(
                itemId = "act_yn_03",
                activityId = "act_yes_no_animals",
                promptHindi = "क्या पक्षी आसमान में उड़ता है? हाँ या नहीं?",
                promptSantali = "ᱠᱤ ᱪᱩᱲᱩᱭ ᱪᱮᱫᱼᱚᱭᱰᱤᱡ ᱫᱚ ᱩᱰᱟᱹᱲ ᱠᱟᱱᱟ?",
                promptLatin = "Ki cuṛuy ced-oyḍij do uḍāṛ kānā?",
                options = listOf("हाँ / ᱦᱚ", "नहीं / ᱱᱟᱸᱦᱚ"),
                correctAnswer = "हाँ / ᱦᱚ"
            )
        ),
        estimatedMinutes = 4,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    // ------------------------------------------------------------------
    // LESSONS
    // ------------------------------------------------------------------

    val LESSON_COUNTING_INTRO = Lesson(
        lessonId = "lsn_num_counting_g1_01",
        title = "गिनती सीखो — 1 से 10",
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.FOUNDATIONAL_NUMERACY,
        learningOutcome = OUTCOME_NUM_1_COUNTING,
        phraseIds = listOf(
            "ph_sit_down_01", "ph_listen_01", "ph_repeat_01",
            "ph_good_morning_01", "ph_how_many_01"
        ),
        vocabularyIds = listOf(
            "vocab_num_1", "vocab_num_2", "vocab_num_3",
            "vocab_num_4", "vocab_num_5", "vocab_num_6",
            "vocab_num_7", "vocab_num_8", "vocab_num_9", "vocab_num_10"
        ),
        activityIds = listOf("act_choral_greetings", "act_counting_1_10"),
        worksheetIds = listOf("ws_counting_1_10"),
        estimatedMinutes = 20,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val LESSON_SHAPES = Lesson(
        lessonId = "lsn_num_shapes_g1_01",
        title = "आकार पहचानो",
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.FOUNDATIONAL_NUMERACY,
        learningOutcome = OUTCOME_NUM_1_SHAPES,
        phraseIds = listOf(
            "ph_sit_down_01", "ph_look_01", "ph_repeat_01", "ph_very_good_01"
        ),
        vocabularyIds = listOf(
            "vocab_circle", "vocab_triangle", "vocab_square"
        ),
        activityIds = listOf("act_choral_greetings", "act_shapes_basic"),
        worksheetIds = listOf("ws_shapes_g1"),
        estimatedMinutes = 18,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val LESSON_BODY_PARTS = Lesson(
        lessonId = "lsn_env_body_g1_01",
        title = "शरीर के अंग",
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.ENVIRONMENTAL_AWARENESS,
        learningOutcome = OUTCOME_ENV_1_BODY,
        phraseIds = listOf(
            "ph_sit_down_01", "ph_stand_up_01", "ph_point_01", "ph_very_good_01"
        ),
        vocabularyIds = listOf(
            "vocab_eye", "vocab_ear", "vocab_nose", "vocab_hand", "vocab_foot"
        ),
        activityIds = listOf("act_body_parts", "act_yes_no_animals"),
        worksheetIds = listOf("ws_body_parts_g1"),
        estimatedMinutes = 22,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    val LESSON_ORAL_GREETINGS = Lesson(
        lessonId = "lsn_oral_greet_g1_01",
        title = "अभिवादन — जोहार!",
        gradeLevel = GradeLevel.GRADE_1,
        domain = SubjectDomain.ORAL_LANGUAGE,
        learningOutcome = OUTCOME_LIT_1_ORAL,
        phraseIds = listOf(
            "ph_good_morning_01", "ph_how_are_you_01",
            "ph_thank_you_01", "ph_welcome_01", "ph_goodbye_01"
        ),
        vocabularyIds = emptyList(),
        activityIds = listOf("act_choral_greetings"),
        worksheetIds = listOf("ws_greetings_g1"),
        estimatedMinutes = 15,
        provenance = ContentProvenance.PENDING_VALIDATION
    )

    // ------------------------------------------------------------------
    // REGISTRIES (single source of truth for runtime lookup)
    // ------------------------------------------------------------------

    /** All activities, keyed by activityId */
    val ACTIVITY_REGISTRY: Map<String, ClassroomActivity> = mapOf(
        ACTIVITY_COUNTING_1_10.activityId to ACTIVITY_COUNTING_1_10,
        ACTIVITY_SHAPES.activityId to ACTIVITY_SHAPES,
        ACTIVITY_CHORAL_GREETINGS.activityId to ACTIVITY_CHORAL_GREETINGS,
        ACTIVITY_BODY_PARTS.activityId to ACTIVITY_BODY_PARTS,
        ACTIVITY_YES_NO_ANIMALS.activityId to ACTIVITY_YES_NO_ANIMALS
    )

    /** All lessons, keyed by lessonId */
    val LESSON_REGISTRY: Map<String, Lesson> = mapOf(
        LESSON_COUNTING_INTRO.lessonId to LESSON_COUNTING_INTRO,
        LESSON_SHAPES.lessonId to LESSON_SHAPES,
        LESSON_BODY_PARTS.lessonId to LESSON_BODY_PARTS,
        LESSON_ORAL_GREETINGS.lessonId to LESSON_ORAL_GREETINGS
    )

    /** Grade 1 lesson catalog — ordered for curriculum sequence */
    val GRADE_1_LESSONS = listOf(
        LESSON_ORAL_GREETINGS,
        LESSON_COUNTING_INTRO,
        LESSON_SHAPES,
        LESSON_BODY_PARTS
    )

    fun getLessonsForGrade(gradeLevel: GradeLevel): List<Lesson> =
        LESSON_REGISTRY.values.filter { it.gradeLevel == gradeLevel }

    fun getLessonsForDomain(domain: SubjectDomain): List<Lesson> =
        LESSON_REGISTRY.values.filter { it.domain == domain }

    fun getActivitiesForLesson(lesson: Lesson): List<ClassroomActivity> =
        lesson.activityIds.mapNotNull { ACTIVITY_REGISTRY[it] }
}

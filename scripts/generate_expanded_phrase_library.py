#!/usr/bin/env python3
"""
Generates the 200+ target classroom phrase library for SIH26042 Phase 4.
All phrases adhere to NIPUN Bharat Foundational Literacy and Numeracy competencies.
Rule Enforced: All phrases start in PENDING_VALIDATION status until real linguistic field verification occurs.
"""

import json
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
OUTPUT_FILE = REPO_ROOT / "data" / "packs" / "santali" / "expanded_phrases.json"

BASE_CATEGORIES = {
    "CLASSROOM_MANAGEMENT": [
        ("बैठ जाओ", "ᱫᱩᱲᱩᱵ ᱢᱮ", "Duṛub me", "CLASSROOM_ACTION_SIT"),
        ("खड़े हो जाओ", "ᱛᱤᱸᱜᱩᱱ ᱢᱮ", "Tingun me", "CLASSROOM_ACTION_STAND"),
        ("इधर आओ", "ᱱᱚᱰᱮ ᱦᱤᱡᱩᱜ ᱢᱮ", "Node hijug me", "CLASSROOM_ACTION_COME"),
        ("वहां जाओ", "ᱦᱟᱸᱰᱮ ᱪᱟᱞᱟᱜ ᱢᱮ", "Hande chalag me", "CLASSROOM_ACTION_GO"),
        ("लाइन बनाओ", "ᱞᱟᱭᱤᱱ ᱵᱮᱱᱟᱣ ᱯᱮ", "Line benaw pe", "CLASSROOM_ACTION_QUEUE"),
        ("शांत रहो", "ᱛᱷᱤᱨ ᱛᱟᱦᱮᱸᱱ ᱢᱮ", "Thir tahen me", "CLASSROOM_DISCIPLINE_QUIET"),
        ("ध्यान से सुनो", "ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱟᱧᱡᱚᱢ ᱢᱮ", "Dheyan te anjom me", "CLASSROOM_ATTENTION_LISTEN"),
        ("इधर देखो", "ᱱᱚᱛᱮ ᱧᱮᱞ ᱢᱮ", "Note nyel me", "CLASSROOM_ATTENTION_LOOK"),
        ("दौड़ो मत", "ᱟᱞᱚᱢ ᱫᱟᱹᱲᱟ", "Alom dara", "CLASSROOM_SAFETY_DONT_RUN"),
        ("अपनी जगह पर जाओ", "ᱟᱢᱟᱜ ᱴᱷᱟᱶ ᱛᱮ ᱪᱟᱞᱟᱜ ᱢᱮ", "Amag thaw te chalag me", "CLASSROOM_ACTION_PLACE"),
        ("हाथ मत लगाओ", "ᱛᱤ ᱟᱞᱚᱢ ᱞᱟᱜᱟᱣᱟ", "Ti alom lagawa", "CLASSROOM_SAFETY_DONT_TOUCH"),
        ("धीरे चलो", "ᱵᱟᱹᱭ-ᱵᱟᱹᱭ ᱛᱮ ᱛᱟᱲᱟᱢ ᱢᱮ", "Bay-bay te taram me", "CLASSROOM_ACTION_WALK_SLOW"),
        ("अपना बस्ता रखो", "ᱟᱢᱟᱜ ᱡᱷᱳᱞᱟ ᱫᱚᱦᱚᱭ ᱢᱮ", "Amag jhola dohoy me", "CLASSROOM_ACTION_BAG"),
        ("कचरा कूड़ेदान में डालो", "ᱡᱟᱵᱽᱨᱟ ᱠᱩᱲᱟᱫᱟᱱ ᱨᱮ ᱜᱤᱰᱤ ᱢᱮ", "Jabra kuradan re gidi me", "CLASSROOM_HYGIENE_TRASH"),
        ("कक्षा साफ रखो", "ᱠᱞᱟᱥ ᱥᱟᱯᱷᱟ ᱫᱚᱦᱚᱭ ᱢᱮ", "Class sapha dohoy me", "CLASSROOM_HYGIENE_CLEAN"),
        ("धक्का मत दो", "ᱟᱞᱚᱢ ᱴᱷᱮᱞᱟᱣᱟ", "Alom thelawa", "CLASSROOM_SAFETY_DONT_PUSH"),
        ("सीधे बैठो", "ᱥᱚᱡᱷᱮ ᱫᱩᱲᱩᱵ ᱢᱮ", "Sojhe durub me", "CLASSROOM_POSTURE_SIT_STRAIGHT"),
        ("हाथ जोड़ो", "ᱛᱤ ᱡᱚᱲᱟᱣ ᱢᱮ", "Ti joraw me", "CLASSROOM_ROUTINE_FOLD_HANDS"),
        ("आंखें बंद करो", "ᱢᱮᱫ ᱵᱚᱸᱫᱽ ᱢᱮ", "Med bond me", "CLASSROOM_ROUTINE_CLOSE_EYES"),
        ("हाथ ऊपर करो", "ᱛᱤ ᱪᱮᱛᱟᱱ ᱢᱮ", "Ti chetan me", "CLASSROOM_ACTION_HANDS_UP"),
        ("हाथ नीचे करो", "ᱛᱤ ᱞᱟᱛᱟᱨ ᱢᱮ", "Ti latar me", "CLASSROOM_ACTION_HANDS_DOWN"),
        ("कूदना बंद करो", "ᱫᱚᱱ ᱵᱚᱸᱫᱽ ᱢᱮ", "Don bond me", "CLASSROOM_SAFETY_STOP_JUMP"),
        ("पानी पी लो", "ᱫᱟᱜ ᱧᱩᱭ ᱢᱮ", "Dag nyuy me", "CLASSROOM_HYGIENE_WATER"),
        ("हाथ धो लो", "ᱛᱤ ᱟᱹᱨᱩᱵ ᱢᱮ", "Ti arub me", "CLASSROOM_HYGIENE_WASH"),
        ("ताली बजाओ", "ᱛᱷᱟᱹᱭᱞᱤ ᱛᱟᱦᱨᱤ ᱢᱮ", "Thari me", "CLASSROOM_ACTION_CLAP")
    ],
    "TEACHING_INSTRUCTION": [
        ("अपनी किताब खोलो", "ᱟᱯᱱᱟᱨᱟᱜ ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡ ᱢᱮ", "Apnarag puthi jhij me", "INSTRUCTION_BOOK_OPEN"),
        ("किताब बंद करो", "ᱯᱩᱛᱷᱤ ᱵᱚᱸᱫᱽ ᱢᱮ", "Puthi bond me", "INSTRUCTION_BOOK_CLOSE"),
        ("यह पढ़ो", "ᱱᱚᱣᱟ ᱯᱟᱲᱦᱟᱣ ᱢᱮ", "Nowa parhaw me", "INSTRUCTION_READ_THIS"),
        ("मेरे बाद बोलो", "ᱤᱧ ᱛᱟᱭᱚᱢ ᱨᱚᱲ ᱢᱮ", "Inj tayom ror me", "INSTRUCTION_REPEAT"),
        ("अपनी कॉपी में लिखो", "ᱟᱯᱱᱟᱨᱟᱜ ᱠᱷᱟᱛᱟ ᱨᱮ ᱚᱞ ᱢᱮ", "Apnarag khata re ol me", "INSTRUCTION_WRITE"),
        ("मुझे दिखाओ", "ᱤᱧ ᱩᱫᱩᱜ-ᱟᱹᱧ ᱢᱮ", "Inj udug-anj me", "INSTRUCTION_SHOW_ME"),
        ("चित्र पर उंगली रखो", "ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱴᱤᱯᱟᱹᱣ ᱢᱮ", "Chitar re tipaw me", "INSTRUCTION_POINT_PICTURE"),
        ("पन्ना पलटो", "ᱥᱟᱠᱟᱢ ᱯᱟᱞᱴᱟᱣ ᱢᱮ", "Sakam paltaw me", "INSTRUCTION_TURN_PAGE"),
        ("बोर्ड की तरफ देखो", "ᱵᱳᱨᱰ ᱥᱮᱫ ᱧᱮᱞ ᱢᱮ", "Bord sed nyel me", "INSTRUCTION_LOOK_BOARD"),
        ("पेंसिल निकालो", "ᱯᱮᱱᱥᱤᱞ ᱚᱰᱚᱠ ᱢᱮ", "Pencil odok me", "INSTRUCTION_TAKE_PENCIL"),
        ("पेंसिल नीचे रखो", "ᱯᱮᱱᱥᱤᱞ ᱞᱟᱛᱟᱨ ᱨᱮ ᱫᱚᱦᱚᱭ ᱢᱮ", "Pencil latar re dohoy me", "INSTRUCTION_PUT_PENCIL"),
        ("जोर से बोलो", "ᱡᱚᱨ ᱛᱮ ᱨᱚᱲ ᱢᱮ", "Jor te ror me", "INSTRUCTION_SPEAK_LOUD"),
        ("धीरे से बोलो", "ᱵᱟᱹᱭ ᱛᱮ ᱨᱚᱲ ᱢᱮ", "Bay te ror me", "INSTRUCTION_SPEAK_SOFT"),
        ("साफ-साफ लिखो", "ᱥᱟᱯᱷᱟ-ᱥᱟᱯᱷᱟ ᱚᱞ ᱢᱮ", "Sapha-sapha ol me", "INSTRUCTION_WRITE_NEAT"),
        ("पहला अक्षर बोलो", "ᱯᱩᱭᱞᱩ ᱟᱠᱷᱚᱨ ᱨᱚᱲ ᱢᱮ", "Puylu akhor ror me", "INSTRUCTION_FIRST_LETTER"),
        ("आखिरी अक्षर बोलो", "ᱢᱩᱪᱟᱹᱫ ᱟᱠᱷᱚᱨ ᱨᱚᱲ ᱢᱮ", "Muchad akhor ror me", "INSTRUCTION_LAST_LETTER"),
        ("शब्द को जोड़ो", "ᱟᱹᱲᱟᱹ ᱡᱚᱲᱟᱣ ᱢᱮ", "Ara joraw me", "INSTRUCTION_JOIN_WORD"),
        ("वाक्य पढ़ो", "ᱟᱭᱟᱛ ᱯᱟᱲᱦᱟᱣ ᱢᱮ", "Ayat parhaw me", "INSTRUCTION_READ_SENTENCE"),
        ("मात्रा पहचानो", "ᱢᱟᱛᱨᱟ ᱩᱨᱩᱢ ᱢᱮ", "Matra urum me", "INSTRUCTION_IDENTIFY_VOWEL"),
        ("रंग भरो", "ᱨᱚᱝ ᱯᱮᱨᱮᱡ ᱢᱮ", "Rong perej me", "INSTRUCTION_COLOR"),
        ("गोला बनाओ", "ᱜᱳᱞ ᱵᱮᱱᱟᱣ ᱢᱮ", "Gol benaw me", "INSTRUCTION_DRAW_CIRCLE"),
        ("सीधी रेखा खींचो", "ᱥᱚᱡᱷᱮ ᱜᱟᱟᱨ ᱴᱟᱱᱟᱣ ᱢᱮ", "Sojhe gar tanaw me", "INSTRUCTION_DRAW_LINE"),
        ("सही मिलान करो", "ᱥᱟᱹᱨᱤ ᱡᱚᱲᱟᱣ ᱢᱮ", "Sari joraw me", "INSTRUCTION_MATCH"),
        ("अलग को चुनो", "ᱡᱩᱫᱟᱹ ᱵᱟᱪᱷᱟᱣ ᱢᱮ", "Juda bachhaw me", "INSTRUCTION_PICK_ODD"),
        ("कहानी सुनो", "ᱠᱟᱹᱦᱱᱤ ᱟᱧᱡᱚᱢ ᱢᱮ", "Kahni anjom me", "INSTRUCTION_LISTEN_STORY"),
        ("कविता बोलो", "ᱚᱱᱚᱬᱦᱮ ᱨᱚᱲ ᱢᱮ", "Ononhe ror me", "INSTRUCTION_REPLY_POEM"),
        ("अक्षर पहचानो", "ᱟᱠᱷᱚᱨ ᱩᱨᱩᱢ ᱢᱮ", "Akhor urum me", "INSTRUCTION_RECOGNIZE_LETTER"),
        ("शब्द दोहराओ", "ᱟᱹᱲᱟᱹ ᱫᱚᱦᱲᱟᱭ ᱢᱮ", "Ara dohray me", "INSTRUCTION_REPEAT_WORD"),
        ("हाथ उठाओ", "ᱛᱤ ᱛᱩᱞ ᱢᱮ", "Ti tul me", "INSTRUCTION_RAISE_HAND"),
        ("ध्यान दो", "ᱫᱷᱮᱭᱟᱱ ᱮᱢ ᱢᱮ", "Dheyan em me", "INSTRUCTION_PAY_ATTENTION"),
        ("एक साथ बोलो", "ᱢᱤᱫ ᱛᱮ ᱨᱚᱲ ᱯᱮ", "Mid te ror pe", "INSTRUCTION_CHORAL_SPEAK"),
        ("दोहराना बंद करो", "ᱫᱚᱦᱲᱟ ᱵᱚᱸᱫᱽ ᱢᱮ", "Dohra bond me", "INSTRUCTION_STOP_REPEAT"),
        ("काम पूरा करो", "ᱠᱟᱹᱢᱤ ᱯᱩᱨᱟᱹᱣ ᱢᱮ", "Kami puraw me", "INSTRUCTION_FINISH_WORK"),
        ("सब मिलकर पढ़ो", "ᱥᱟᱱᱟᱢ ᱠᱚ ᱯᱟᱲᱦᱟᱣ ᱯᱮ", "Sanam ko parhaw pe", "INSTRUCTION_READ_TOGETHER"),
        ("अपनी बारी का इंतजार करो", "ᱟᱢᱟᱜ ᱯᱟᱞᱟ ᱛᱟᱺᱜᱤ ᱢᱮ", "Amag pala tangi me", "INSTRUCTION_WAIT_TURN")
    ],
    "QUESTIONS": [
        ("कौन उत्तर देगा?", "ᱚᱠᱚᱭ ᱛᱮᱞᱟᱭ ᱮᱢᱟ?", "Okoy telay ema?", "QUESTION_WHO_ANSWER"),
        ("यह क्या है?", "ᱱᱚᱣᱟ ᱫᱚ ᱪᱮᱫ ᱠᱟᱱᱟ?", "Nowa do ched kana?", "QUESTION_WHAT_IS_THIS"),
        ("कितने हैं?", "ᱛᱤᱱᱟᱹᱜ ᱢᱮᱱᱟᱜ-ᱟ?", "Tinag menag-a?", "QUESTION_HOW_MANY"),
        ("कौन सा बड़ा है?", "ᱚᱠᱟᱴᱟᱜ ᱫᱚ ᱢᱟᱨᱟᱝ ᱜᱮᱭᱟ?", "Okatag do marang geya?", "QUESTION_WHICH_BIGGER"),
        ("कौन सा छोटा है?", "ᱚᱠᱟᱴᱟᱜ ᱫᱚ ᱦᱩᱰᱤᱧ ᱜᱮᱭᱟ?", "Okatag do hudinj geya?", "QUESTION_WHICH_SMALLER"),
        ("आपकी किताब कहां है?", "ᱟᱢᱟᱜ ᱯᱩᱛᱷᱤ ᱚᱠᱟᱨᱮ?", "Amag puthi okare?", "QUESTION_WHERE_BOOK"),
        ("क्या समझ गए?", "ᱪᱮᱫ ᱵᱩᱡᱷᱟᱹᱣ ᱠᱮᱫ-ᱟᱢ?", "Ched bujhaw ked-am?", "QUESTION_DID_UNDERSTAND"),
        ("किसका काम पूरा हुआ?", "ᱚᱠᱚᱭᱟᱜ ᱠᱟᱹᱢᱤ ᱯᱩᱨᱟᱹᱣ ᱮᱱᱟ?", "Okoyag kami puraw ena?", "QUESTION_WHO_FINISHED"),
        ("देरी क्यों हुई?", "ᱪᱮᱫᱟᱜ ᱵᱤᱞᱚᱢ ᱮᱱᱟ?", "Chedag bilom ena?", "QUESTION_WHY_LATE"),
        ("कौन बताएगा?", "ᱚᱠᱚᱭ ᱞᱟᱹᱭᱟ?", "Okoy laya?", "QUESTION_WHO_TELL"),
        ("यह कौन सा रंग है?", "ᱱᱚᱣᱟ ᱫᱚ ᱪᱮᱫ ᱨᱚᱝ ᱠᱟᱱᱟ?", "Nowa do ched rong kana?", "QUESTION_WHAT_COLOR"),
        ("यह कौन सा जानवर है?", "ᱱᱚᱣᱟ ᱫᱚ ᱪᱮᱫ ᱡᱤᱵᱽ ᱠᱟᱱᱟ?", "Nowa do ched jib kana?", "QUESTION_WHAT_ANIMAL"),
        ("यह कौन सा फल है?", "ᱱᱚᱣᱟ ᱫᱚ ᱪᱮᱫ ᱡᱚ ᱠᱟᱱᱟ?", "Nowa do ched jo kana?", "QUESTION_WHAT_FRUIT"),
        ("इसकी गिनती कितनी है?", "ᱱᱚᱣᱟ ᱨᱮᱭᱟᱜ ᱞᱮᱠᱷᱟ ᱛᱤᱱᱟᱹᱜ?", "Nowa reyag lekha tinag?", "QUESTION_COUNT_VALUE"),
        ("क्या आप तैयार हैं?", "ᱪᱮᱫ ᱟᱢ ᱥᱟᱯᱲᱟᱣ ᱢᱮᱱᱟᱢᱟ?", "Ched am sapraw menama?", "QUESTION_ARE_READY"),
        ("किसने किया?", "ᱚᱠᱚᱭ ᱠᱟᱹᱢᱤ ᱠᱮᱫ-ᱟ?", "Okoy kami ked-a?", "QUESTION_WHO_DID"),
        ("अगला कौन है?", "ᱤᱱᱟᱹ ᱛᱟᱭᱚᱢ ᱚᱠᱚᱭ?", "Ina tayom okoy?", "QUESTION_WHO_NEXT"),
        ("क्या कोई सवाल है?", "ᱪᱮᱫ ᱡᱟᱦᱟᱸᱱ ᱠᱩᱠᱞᱤ ᱢᱮᱱᱟᱜ-ᱟ?", "Ched jahan kukli menag-a?", "QUESTION_ANY_DOUBT"),
        ("इसका नाम क्या है?", "ᱱᱚᱣᱟ ᱨᱮᱭᱟᱜ ᱧᱩᱛᱩᱢ ᱪᱮᱫ?", "Nowa reyag nyutum ched?", "QUESTION_WHAT_NAME"),
        ("कहाँ जा रहे हो?", "ᱚᱠᱟ ᱛᱮᱢ ᱪᱟᱞᱟᱜ ᱠᱟᱱᱟ?", "Oka tem chalag kana?", "QUESTION_WHERE_GOING"),
        ("क्या हुआ?", "ᱪᱮᱫ ᱦᱩᱭ ᱮᱱᱟ?", "Ched huy ena?", "QUESTION_WHAT_HAPPENED"),
        ("पेंसिल किसकी है?", "ᱯᱮᱱᱥᱤᱞ ᱚᱠᱚᱭᱟᱜ ᱠᱟᱱᱟ?", "Pencil okoyag kana?", "QUESTION_WHOSE_PENCIL"),
        ("क्या आपने खाना खाया?", "ᱪᱮᱫ ᱟᱢ ᱫᱟᱠᱟᱢ ᱡᱚᱢ ᱠᱮᱫ-ᱟ?", "Ched am dakam jom ked-a?", "QUESTION_HAD_FOOD"),
        ("घर पर कौन है?", "ᱚᱲᱟᱜ ᱨᱮ ᱚᱠᱚᱭ ᱢᱮᱱᱟᱜ ᱠᱚᱣᱟ?", "Orag re okoy menag kowa?", "QUESTION_WHO_AT_HOME"),
        ("आज कौन अनुपस्थित है?", "ᱛᱮᱦᱮᱧ ᱚᱠᱚᱭ ᱵᱟᱹᱱᱩᱜ-ᱮᱭᱟ?", "Tehenj okoy banug-eya?", "QUESTION_WHO_ABSENT"),
        ("यह सही है या गलत?", "ᱱᱚᱣᱟ ᱫᱚ ᱥᱟᱹᱨᱤ ᱥᱮ ᱵᱟᱹᱲᱤᱡ?", "Nowa do sari se barij?", "QUESTION_RIGHT_OR_WRONG"),
        ("क्या आप दोहरा सकते हैं?", "ᱪᱮᱫ ᱟᱢ ᱫᱚᱦᱲᱟ ᱫᱟᱲᱮᱭᱟᱜ-ᱟᱢ?", "Ched am dohra dareyag-am?", "QUESTION_CAN_REPEAT"),
        ("आपकी उम्र क्या है?", "ᱟᱢᱟᱜ ᱩᱢᱮᱨ ᱛᱤᱱᱟᱹᱜ?", "Amag umer tinag?", "QUESTION_WHAT_AGE"),
        ("यह कहाँ से आया?", "ᱱᱚᱣᱟ ᱫᱚ ᱚᱠᱟ ᱠᱷᱚᱱ ᱦᱮᱡ ᱮᱱᱟ?", "Nowa do oka khon hej ena?", "QUESTION_WHERE_FROM"),
        ("आपको क्या पसंद है?", "ᱟᱢ ᱫᱚ ᱪᱮᱫ ᱠᱩᱥᱤᱭᱟᱜ-ᱟᱢ?", "Am do ched kusiyag-am?", "QUESTION_WHAT_LIKE")
    ],
    "PRAISE_AND_ENCOURAGEMENT": [
        ("बहुत अच्छा", "ᱟᱹᱰᱤ ᱵᱮᱥ", "Adi bes", "PRAISE_VERY_GOOD"),
        ("शाबाश", "ᱥᱟᱵᱟᱥ", "Sabas", "PRAISE_EXCELLENT"),
        ("शानदार", "ᱢᱚᱡᱽ ᱜᱮ", "Moj ge", "PRAISE_WONDERFUL"),
        ("फिर से कोशिश करो", "ᱫᱚᱦᱲᱟ ᱛᱮ ᱪᱮᱥᱴᱟᱭ ᱢᱮ", "Dohra te chestay me", "PRAISE_TRY_AGAIN"),
        ("आप कर सकते हैं", "ᱟᱢ ᱫᱟᱲᱮᱭᱟᱜ-ᱟᱢ", "Am dareyag-am", "PRAISE_YOU_CAN"),
        ("बहुत बढ़िया काम", "ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ ᱠᱟᱹᱢᱤ", "Adi napay kami", "PRAISE_GREAT_JOB"),
        ("सुंदर लिखावट", "ᱪᱚᱨᱚᱠ ᱚᱞ", "Chorok ol", "PRAISE_NEAT_HANDWRITING"),
        ("कमाल कर दिया", "ᱟᱹᱰᱤ ᱪᱚᱨᱚᱠ", "Adi chorok", "PRAISE_AWESOME"),
        ("जारी रखो", "ᱪᱟᱞᱟᱣ ᱫᱚᱦᱚᱭ ᱢᱮ", "Chalaw dohoy me", "PRAISE_KEEP_IT_UP"),
        ("आप तेजी से सीख रहे हैं", "ᱟᱢ ᱞᱚᱜᱚᱱ ᱮᱢ ᱪᱮᱫᱚᱜ ᱠᱟᱱᱟ", "Am logon em chedog kana", "PRAISE_LEARNING_FAST"),
        ("साहस रखो", "ᱥᱟᱦᱟᱥ ᱫᱚᱦᱚᱭ ᱢᱮ", "Sahas dohoy me", "PRAISE_HAVE_COURAGE"),
        ("चिंता मत करो", "ᱟᱞᱚᱢ ᱪᱤᱱᱛᱟᱹᱜ-ᱟ", "Alom chintag-a", "PRAISE_DONT_WORRY"),
        ("गलतियों से सीखते हैं", "ᱵᱷᱩᱞ ᱠᱷᱚᱱ ᱜᱮᱵᱚ ᱪᱮᱫᱚᱜ-ᱟ", "Bhul khon gebo chedog-a", "PRAISE_MISTAKES_TEACH"),
        ("बहुत होशियार बच्चे", "ᱟᱹᱰᱤ ᱞᱟᱹᱥᱠᱟᱹ ᱜᱤᱫᱽᱨᱟᱹ", "Adi laska gidra", "PRAISE_SMART_KIDS"),
        ("सही उत्तर", "ᱥᱟᱹᱨᱤ ᱛᱮᱞᱟ", "Sari tela", "PRAISE_CORRECT_ANSWER"),
        ("मुझे आप पर गर्व है", "ᱤᱧ ᱟᱢ ᱨᱮ ᱜᱚᱨᱚᱵᱽ ᱢᱮᱱᱟᱜ-ᱟ", "Inj am re gorob menag-a", "PRAISE_PROUD_OF_YOU"),
        ("अच्छा प्रयास", "ᱵᱮᱥ ᱪᱮᱥᱴᱟ", "Bes chesta", "PRAISE_GOOD_EFFORT"),
        ("कल और बेहतर होगा", "ᱜᱟᱯᱟ ᱟᱨᱦᱚᱸ ᱵᱮᱥ ᱦᱩᱭᱩᱜ-ᱟ", "Gapa arho bes huyug-a", "PRAISE_TOMORROW_BETTER"),
        ("बहुत सुंदर", "ᱟᱹᱰᱤ ᱢᱚᱡᱽ", "Adi moj", "PRAISE_BEAUTIFUL"),
        ("धन्यवाद", "ᱥᱟᱨᱦᱟᱣ", "Sarhaw", "PRAISE_THANK_YOU")
    ],
    "PICTURE_ACTIVITIES": [
        ("चित्र को ध्यान से देखो", "ᱪᱤᱛᱟᱹᱨ ᱫᱷᱮᱭᱟᱱ ᱛᱮ ᱧᱮᱞ ᱢᱮ", "Chitar dheyan te nyel me", "PICTURE_OBSERVE"),
        ("जानवर को ढूंढो", "ᱡᱤᱵᱽ ᱯᱟᱱᱛᱮᱭᱮ ᱢᱮ", "Jib panteye me", "PICTURE_FIND_ANIMAL"),
        ("लाल वाले पर उंगली रखो", "ᱟᱨᱟᱜ ᱪᱮᱛᱟᱱ ᱨᱮ ᱴᱤᱯᱟᱹᱣ ᱢᱮ", "Arag chetan re tipaw me", "PICTURE_POINT_RED"),
        ("चित्र में क्या दिखाई दे रहा है?", "ᱪᱤᱛᱟᱹᱨ ᱨᱮ ᱪᱮᱫ ᱧᱮᱞᱚᱜ ᱠᱟᱱᱟ?", "Chitar re ched nyelog kana?", "PICTURE_DESCRIBE"),
        ("चित्र की कहानी बताओ", "ᱪᱤᱛᱟᱹᱨ ᱨᱮᱭᱟᱜ ᱠᱟᱹᱦᱱᱤ ᱞᱟᱹᱭ ᱢᱮ", "Chitar reyag kahni lay me", "PICTURE_STORY"),
        ("चिड़िया कहाँ है?", "ᱪᱮᱬᱮ ᱚᱠᱟᱨᱮ?", "Chene okare?", "PICTURE_FIND_BIRD"),
        ("पेड़ दिखाओ", "ᱫᱟᱨᱮ ᱩᱫᱩᱜ ᱢᱮ", "Dare udug me", "PICTURE_SHOW_TREE"),
        ("पीला फूल कौन सा है?", "ᱥᱟᱥᱟᱝ ᱵᱟᱦᱟ ᱚᱠᱟᱴᱟᱜ?", "Sasang baha okatag?", "PICTURE_YELLOW_FLOWER"),
        ("सूरज कहाँ चमक रहा है?", "ᱵᱮᱲᱟ ᱚᱠᱟᱨᱮ ᱛᱟᱨᱟᱥᱚᱜ ᱠᱟᱱᱟ?", "Bera okare tarasog kana?", "PICTURE_FIND_SUN"),
        ("नदी दिखाओ", "ᱜᱟᱰᱟ ᱩᱫᱩᱜ ᱢᱮ", "Gada udug me", "PICTURE_FIND_RIVER"),
        ("घर पर उंगली रखो", "ᱚᱲᱟᱜ ᱨᱮ ᱴᱤᱯᱟᱹᱣ ᱢᱮ", "Orag re tipaw me", "PICTURE_POINT_HOUSE"),
        ("बड़ा हाथी कौन सा है?", "ᱢᱟᱨᱟᱝ ᱦᱟᱹᱛᱤ ᱚᱠᱟᱴᱟᱜ?", "Marang hati okatag?", "PICTURE_BIG_ELEPHANT"),
        ("छोटा बंदर कौन सा है?", "ᱦᱩᱰᱤᱧ ᱜᱟᱹᱲᱤ ᱚᱠᱟᱴᱟᱜ?", "Hudinj gari okatag?", "PICTURE_SMALL_MONKEY"),
        ("नाव कहाँ तैर रही है?", "ᱞᱟᱹᱣᱠᱟᱹ ᱚᱠᱟᱨᱮ ᱯᱟᱭᱨᱟᱜ ᱠᱟᱱᱟ?", "Lawka okare payrag kana?", "PICTURE_BOAT_WATER"),
        ("मछली को पहचानो", "ᱦᱟᱹᱠᱩ ᱩᱨᱩᱢ ᱢᱮ", "Haku urum me", "PICTURE_RECOGNIZE_FISH"),
        ("गाय का रंग क्या है?", "ᱜᱟᱹᱭᱟᱜ ᱨᱚᱝ ᱪᱮᱫ?", "Gayag rong ched?", "PICTURE_COW_COLOR"),
        ("आसमान में क्या है?", "ᱥᱮᱨᱢᱟ ᱨᱮ ᱪᱮᱫ ᱢᱮᱱᱟᱜ-ᱟ?", "Serma re ched menag-a?", "PICTURE_SKY_OBJECT"),
        ("पहाड़ दिखाओ", "ᱵᱩᱨᱩ ᱩᱫᱩᱜ ᱢᱮ", "Buru udug me", "PICTURE_SHOW_MOUNTAIN"),
        ("रास्ता कहाँ जा रहा है?", "ᱦᱚᱨ ᱚᱠᱟ ᱛᱮ ᱪᱟᱞᱟᱜ ᱠᱟᱱᱟ?", "Hor oka te chalag kana?", "PICTURE_ROAD_PATH"),
        ("बच्चा क्या कर रहा है?", "ᱜᱤᱫᱽᱨᱟᱹ ᱪᱮᱫ ᱮ ᱪᱤᱠᱟᱹᱭᱮᱫ-ᱟ?", "Gidra ched e chikayed-a?", "PICTURE_ACTION_BOY"),
        ("लड़की कहाँ खड़ी है?", "ᱠᱩᱲᱤ ᱜᱤᱫᱽᱨᱟᱹ ᱚᱠᱟᱨᱮ ᱛᱤᱸᱜᱩ ᱟᱠᱟᱱᱟ?", "Kuri gidra okare tingu akana?", "PICTURE_ACTION_GIRL"),
        ("गेंद कहाँ है?", "ᱵᱚᱞ ᱚᱠᱟᱨᱮ ᱢᱮᱱᱟᱜ-ᱟ?", "Bol okare menag-a?", "PICTURE_FIND_BALL"),
        ("कुत्ता क्या खा रहा है?", "ᱥᱮᱛᱟ ᱪᱮᱫ ᱮ ᱡᱚᱢᱮᱫ-ᱟ?", "Seta ched e jomed-a?", "PICTURE_DOG_EATING"),
        ("पत्ता किस रंग का है?", "ᱥᱟᱠᱟᱢ ᱪᱮᱫ ᱨᱚᱝ ᱠᱟᱱᱟ?", "Sakam ched rong kana?", "PICTURE_LEAF_COLOR"),
        ("तितली दिखाओ", "ᱯᱤᱯᱤᱲᱤᱭᱟᱹᱝ ᱩᱫᱩᱜ ᱢᱮ", "Pipiriyang udug me", "PICTURE_SHOW_BUTTERFLY")
    ],
    "NUMBER_ACTIVITIES": [
        ("मेरे साथ एक से पाँच तक गिनो", "ᱤᱧ ᱥᱟᱶᱛᱮ ᱢᱤᱫ ᱠᱷᱚᱱ ᱢᱚᱬᱮ ᱫᱷᱟᱹᱵᱤᱡ ᱞᱮᱠᱷᱟᱭ ᱢᱮ", "Inj sawte mid khon mone dhabij lekhay me", "NUM_COUNT_1_TO_5"),
        ("सब मिलकर गिनो", "ᱥᱟᱱᱟᱢ ᱠᱚ ᱢᱮᱥᱟ ᱠᱟᱛᱮ ᱞᱮᱠᱷᱟᱭ ᱯᱮ", "Sanam ko mesa kate lekhay pe", "NUM_COUNT_ALL"),
        ("तीन उंगलियां दिखाओ", "ᱯᱮᱭᱟ ᱛᱤ-ᱠᱟᱹᱴᱩᱵ ᱩᱫᱩᱜ ᱢᱮ", "Peya ti-katub udug me", "NUM_SHOW_THREE"),
        ("कितनी उंगलियां हैं?", "ᱛᱤᱱᱟᱹᱜ ᱠᱟᱹᱴᱩᱵ ᱢᱮᱱᱟᱜ-ᱟ?", "Tinag katub menag-a?", "NUM_HOW_MANY_FINGERS"),
        ("सेब गिनो", "ᱥᱮᱣ ᱞᱮᱠᱷᱟᱭ ᱢᱮ", "Sew lekhay me", "NUM_COUNT_APPLES"),
        ("एक और जोड़ो", "ᱟᱨ ᱢᱤᱫᱴᱟᱹᱝ ᱡᱚᱲᱟᱣ ᱢᱮ", "Ar midtang joraw me", "NUM_ADD_ONE"),
        ("दो के बाद क्या आता है?", "ᱵᱟᱨ ᱛᱟᱭᱚᱢ ᱪᱮᱫ ᱦᱤᱡᱩᱜ-ᱟ?", "Bar tayom ched hijug-a?", "NUM_AFTER_TWO"),
        ("चार से पहले क्या आता है?", "ᱯᱩᱱ ᱞᱟᱦᱟ ᱨᱮ ᱪᱮᱫ ᱦᱤᱡᱩᱜ-ᱟ?", "Pun laha re ched hijug-a?", "NUM_BEFORE_FOUR"),
        ("पांच बार ताली बजाओ", "ᱢᱚᱬᱮ ᱫᱷᱟᱣ ᱛᱟᱦᱨᱤ ᱢᱮ", "Mone dhaw thari me", "NUM_CLAP_FIVE"),
        ("तीन बार कूदो", "ᱯᱮ ᱫᱷᱟᱣ ᱫᱚᱱ ᱢᱮ", "Pe dhaw don me", "NUM_JUMP_THREE"),
        ("एक", "ᱢᱤᱫ", "Mid", "NUM_DIGIT_1"),
        ("दो", "ᱵᱟᱨ", "Bar", "NUM_DIGIT_2"),
        ("तीन", "ᱯᱮ", "Pe", "NUM_DIGIT_3"),
        ("चार", "ᱯᱩᱱ", "Pun", "NUM_DIGIT_4"),
        ("पांच", "ᱢᱚᱬᱮ", "Mone", "NUM_DIGIT_5"),
        ("छह", "ᱛᱩᱨᱩᱭ", "Turuy", "NUM_DIGIT_6"),
        ("सात", "ᱮᱭᱟᱭ", "Eyay", "NUM_DIGIT_7"),
        ("आठ", "ᱤᱨᱟᱹᱞ", "Iral", "NUM_DIGIT_8"),
        ("नौ", "ᱟᱨᱮ", "Are", "NUM_DIGIT_9"),
        ("दस", "ᱜᱮᱞ", "Gel", "NUM_DIGIT_10"),
        ("कौन सा समूह बड़ा है?", "ᱚᱠᱟ ᱫᱚᱞ ᱫᱚ ᱢᱟᱨᱟᱝ ᱜᱮᱭᱟ?", "Oka dol do marang geya?", "NUM_WHICH_GROUP_BIGGER"),
        ("दो गेंदें अलग करो", "ᱵᱟᱨᱭᱟ ᱵᱚᱞ ᱵᱷᱮᱜᱟᱨ ᱢᱮ", "Barya bol bhegar me", "NUM_SEPARATE_TWO"),
        ("एक कम करो", "ᱢᱤᱫᱴᱟᱹᱝ ᱠᱚᱢ ᱢᱮ", "Midtang kom me", "NUM_SUBTRACT_ONE"),
        ("शून्य का अर्थ कुछ नहीं", "ᱥᱩᱱ ᱨᱮᱭᱟᱜ ᱢᱮᱱᱮᱛ ᱫᱚ ᱪᱮᱫ ᱦᱚᱸ ᱵᱟᱝ", "Sun reyag menet do ched ho bang", "NUM_ZERO_CONCEPT"),
        ("पेंसिल गिन कर बताओ", "ᱯᱮᱱᱥᱤᱞ ᱞᱮᱠᱷᱟ ᱠᱟᱛᱮ ᱞᱟᱹᱭ ᱢᱮ", "Pencil lekha kate lay me", "NUM_COUNT_PENCILS"),
        ("दस तक उल्टी गिनती करो", "ᱜᱮᱞ ᱠᱷᱚᱱ ᱩᱞᱴᱟᱹ ᱞᱮᱠᱷᱟᱭ ᱢᱮ", "Gel khon ulta lekhay me", "NUM_COUNT_BACKWARDS"),
        ("कितने बच्चे हैं?", "ᱛᱤᱱᱟᱹᱜ ᱜᱤᱫᱽᱨᱟᱹ ᱢᱮᱱᱟᱜ ᱠᱚᱣᱟ?", "Tinag gidra menag kowa?", "NUM_COUNT_CHILDREN"),
        ("जोड़ा बनाओ", "ᱡᱚᱲ ᱵᱮᱱᱟᱣ ᱢᱮ", "Jor benaw me", "NUM_MAKE_PAIRS"),
        ("संख्या लिखो", "ᱮᱞᱠᱷᱟ ᱚᱞ ᱢᱮ", "Elkha ol me", "NUM_WRITE_NUMBER"),
        ("संख्या पहचानो", "ᱮᱞᱠᱷᱟ ᱩᱨᱩᱢ ᱢᱮ", "Elkha urum me", "NUM_RECOGNIZE_NUMBER")
    ],
    "ASSESSMENT": [
        ("सही उत्तर चुनो", "ᱥᱟᱹᱨᱤ ᱛᱮᱞᱟ ᱵᱟᱪᱷᱟᱣ ᱢᱮ", "Sari tela bachhaw me", "ASSESS_CHOOSE_CORRECT"),
        ("उत्तर पर गोला लगाओ", "ᱛᱮᱞᱟ ᱨᱮ ᱜᱳᱞ ᱢᱮ", "Tela re gol me", "ASSESS_CIRCLE_ANSWER"),
        ("अपना हाथ उठाओ", "ᱟᱢᱟᱜ ᱛᱤ ᱛᱩᱞ ᱢᱮ", "Amag ti tul me", "ASSESS_RAISE_HAND"),
        ("मुझे उत्तर बताओ", "ᱤᱧ ᱛᱮᱞᱟ ᱞᱟᱹᱭᱟᱹᱧ ᱢᱮ", "Inj tela layanj me", "ASSESS_TELL_ANSWER"),
        ("जोड़ी मिलाओ", "ᱡᱚᱲ ᱢᱤᱞᱟᱹᱣ ᱢᱮ", "Jor milaw me", "ASSESS_MATCH_PAIR"),
        ("सही पर टिक लगाओ", "ᱥᱟᱹᱨᱤ ᱨᱮ ᱴᱤᱠ ᱪᱤᱱᱦᱟᱹ ᱮᱢ ᱢᱮ", "Sari re tik chinha em me", "ASSESS_TICK_RIGHT"),
        ("गलत पर क्रॉस लगाओ", "ᱵᱟᱹᱲᱤᱡ ᱨᱮ ᱠᱨᱚᱥ ᱪᱤᱱᱦᱟᱹ ᱮᱢ ᱢᱮ", "Barij re kros chinha em me", "ASSESS_CROSS_WRONG"),
        ("अपना नाम लिखो", "ᱟᱢᱟᱜ ᱧᱩᱛᱩᱢ ᱚᱞ ᱢᱮ", "Amag nyutum ol me", "ASSESS_WRITE_NAME"),
        ("तारीख लिखो", "ᱢᱟᱹᱦᱤᱛ ᱚᱞ ᱢᱮ", "Mahit ol me", "ASSESS_WRITE_DATE"),
        ("खाली जगह भरो", "ᱠᱷᱟᱹᱞᱤ ᱴᱷᱟᱶ ᱯᱮᱨᱮᱡ ᱢᱮ", "Khali thaw perej me", "ASSESS_FILL_BLANK"),
        ("पहला सवाल देखो", "ᱯᱩᱭᱞᱩ ᱠᱩᱠᱞᱤ ᱧᱮᱞ ᱢᱮ", "Puylu kukli nyel me", "ASSESS_FIRST_QUESTION"),
        ("दूसरा सवाल पढ़ो", "ᱫᱚᱥᱟᱨ ᱠᱩᱠᱞᱤ ᱯᱟᱲᱦᱟᱣ ᱢᱮ", "Dosar kukli parhaw me", "ASSESS_SECOND_QUESTION"),
        ("अंतिम प्रश्न हल करो", "ᱢᱩᱪᱟᱹᱫ ᱠᱩᱠᱞᱤ ᱥᱚᱞᱦᱮ ᱢᱮ", "Muchad kukli solhe me", "ASSESS_LAST_QUESTION"),
        ("अपनी कॉपी जमा करो", "ᱟᱢᱟᱜ ᱠᱷᱟᱛᱟ ᱡᱚᱢᱟᱭ ᱢᱮ", "Amag khata jomay me", "ASSESS_SUBMIT_COPY"),
        ("जाँच कर लो", "ᱯᱚᱨᱚᱠ ᱢᱮ", "Porok me", "ASSESS_CHECK_WORK"),
        ("दोबारा पढ़ो", "ᱟᱨᱦᱚᱸ ᱯᱟᱲᱦᱟᱣ ᱢᱮ", "Arho parhaw me", "ASSESS_READ_AGAIN"),
        ("क्या सबने कर लिया?", "ᱪᱮᱫ ᱥᱟᱱᱟᱢ ᱠᱚ ᱠᱟᱹᱢᱤ ᱯᱮ ᱯᱩᱨᱟᱹᱣ ᱠᱮᱫ-ᱟ?", "Ched sanam ko kami pe puraw ked-a?", "ASSESS_DID_ALL_FINISH"),
        ("एक दूसरे की मदद मत करो", "ᱢᱤᱫ ᱟᱨ ᱢᱤᱫ ᱟᱞᱚᱯᱮ ᱜᱚᱲᱚᱣᱟᱭᱟ", "Mid ar mid alope gorowaya", "ASSESS_NO_HELPING"),
        ("समय समाप्त हो गया", "ᱚᱠᱛᱚ ᱪᱟᱵᱟ ᱮᱱᱟ", "Okto chaba ena", "ASSESS_TIME_UP"),
        ("बहुत अच्छे अंक आए", "ᱟᱹᱰᱤ ᱵᱮᱥ ᱱᱚᱢᱵᱚᱨ ᱦᱮᱡ ᱮᱱᱟ", "Adi bes nombor hej ena", "ASSESS_GOOD_MARKS")
    ],
    "GREETING_AND_TRANSITION": [
        ("नमस्ते / जोहार", "ᱡᱚᱦᱟᱨ", "Johar", "GREET_JOHAR"),
        ("सुप्रभात", "ᱥᱟᱹᱜᱩᱱ ᱥᱮᱛᱟᱜ", "Sagun setag", "GREET_GOOD_MORNING"),
        ("शुभ दोपहर", "ᱥᱟᱹᱜᱩᱱ ᱛᱤᱠᱤᱱ", "Sagun tikin", "GREET_GOOD_AFTERNOON"),
        ("शुभ संध्या", "ᱥᱟᱹᱜᱩᱱ ᱟᱹᱭᱩᱵ", "Sagun ayub", "GREET_GOOD_EVENING"),
        ("शुभ रात्रि", "ᱥᱟᱹᱜᱩᱱ ᱧᱤᱫᱟᱹ", "Sagun nyida", "GREET_GOOD_NIGHT"),
        ("आप सब कैसे हैं?", "ᱟᱯᱮ ᱠᱚ ᱪᱮᱫ ᱞᱮᱠᱟ ᱢᱮᱱᱟᱜ ᱯᱮᱭᱟ?", "Ape ko ched leka menag peya?", "GREET_HOW_ARE_YOU"),
        ("मैं ठीक हूँ", "ᱤᱧ ᱫᱚ ᱵᱮᱥ ᱜᱮ ᱢᱮᱱᱟᱹᱧᱟ", "Inj do bes ge menanya", "GREET_I_AM_FINE"),
        ("आइए, शुरू करते हैं", "ᱫᱮᱞᱟᱵᱚᱱ, ᱮᱛᱚᱦᱚᱵ-ᱟ", "Delabon, etohob-a", "TRANS_LETS_BEGIN"),
        ("अब हम पढ़ेंगे", "ᱱᱤᱛᱚᱜ ᱵᱚ ᱯᱟᱲᱦᱟᱣ-ᱟ", "Nitog bo parhaw-a", "TRANS_NOW_WE_READ"),
        ("अब हम गाएंगे", "ᱱᱤᱛᱚᱜ ᱵᱚ ᱥᱮᱨᱮᱧ-ᱟ", "Nitog bo serenj-a", "TRANS_NOW_WE_SING"),
        ("अब हम खेलेंगे", "ᱱᱤᱛᱚᱜ ᱵᱚ ᱮᱱᱮᱡ-ᱟ", "Nitog bo enej-a", "TRANS_NOW_WE_PLAY"),
        ("कक्षा समाप्त हुई", "ᱠᱞᱟᱥ ᱪᱟᱵᱟ ᱮᱱᱟ", "Class chaba ena", "TRANS_CLASS_OVER"),
        ("अपना बस्ता समेटो", "ᱟᱢᱟᱜ ᱡᱷᱳᱞᱟ ᱥᱟᱢᱵᱲᱟᱣ ᱢᱮ", "Amag jhola sambraw me", "TRANS_PACK_BAGS"),
        ("कल मिलेंगे", "ᱜᱟᱯᱟ ᱵᱚ ᱧᱟᱯᱟᱢ-ᱟ", "Gapa bo nyapam-a", "TRANS_SEE_TOMORROW"),
        ("घर सुरक्षित जाओ", "ᱚᱲᱟᱜ ᱥᱩᱠᱷ ᱛᱮ ᱪᱟᱞᱟᱜ ᱯᱮ", "Orag sukh te chalag pe", "TRANS_GO_SAFE")
    ]
}

def main():
    phrases = []
    phrase_counter = 1

    for category, items in BASE_CATEGORIES.items():
        for hindi, ol_chiki, latin, intent in items:
            p_id = f"exp_ph_{phrase_counter:03d}"
            # Preserve ph_sit_down_01 as verified test control
            if phrase_counter == 1:
                p_id = "ph_sit_down_01"
                verif_status = "APPROVED"
                prov = "VERIFIED"
                audio_stat = "APPROVED"
                audio_file = "audio/ph_sit_down_01.wav"
            else:
                verif_status = "PENDING_VALIDATION"
                prov = "PENDING_VALIDATION"
                audio_stat = "PENDING_RECORDING"
                audio_file = None

            phrases.append({
                "phrase_id": p_id,
                "category": category,
                "intent": intent,
                "hindi_canonical": hindi,
                "hindi_normalized": hindi.replace("?", "").replace("।", "").strip(),
                "hindi_aliases": [hindi.replace(" जाओ", "िए"), hindi.replace(" करो", "ें")],
                "target_native_script": ol_chiki,
                "target_transliteration_latin": latin,
                "dialect_profile": {
                    "primary_dialect": "mayurbhanj",
                    "supported_dialects": ["mayurbhanj", "santhal_pargana"],
                    "status": "CANONICAL"
                },
                "audio_path": audio_file,
                "audio_status": audio_stat,
                "verification_status": verif_status,
                "provenance": prov,
                "difficulty_level": 1
            })
            phrase_counter += 1

    with open(OUTPUT_FILE, "w", encoding="utf-8") as f:
        json.dump(phrases, f, ensure_ascii=False, indent=2)

    print(f"Successfully generated {len(phrases)} expanded phrases in {OUTPUT_FILE}")
    print(f"Verified count: 1 | Pending validation count: {len(phrases) - 1}")

if __name__ == "__main__":
    main()

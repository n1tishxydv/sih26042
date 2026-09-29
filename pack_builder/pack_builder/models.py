"""Pydantic models for SIH26042 Language Packs."""

from __future__ import annotations
from enum import Enum
from typing import Dict, List, Optional
from pydantic import BaseModel, Field, ConfigDict


class Provenance(str, Enum):
    VERIFIED = "VERIFIED"
    MACHINE_GENERATED = "MACHINE_GENERATED"
    LOW_CONFIDENCE = "LOW_CONFIDENCE"
    NO_MATCH = "NO_MATCH"
    UNAVAILABLE = "UNAVAILABLE"


class ScriptConfig(BaseModel):
    primary_script_name: str = Field(..., description="E.g. Ol Chiki")
    iso_15924: str = Field(..., description="E.g. Olck")
    unicode_range_start: str = Field(..., description="Hex e.g. 0x1C50")
    unicode_range_end: str = Field(..., description="Hex e.g. 0x1C7F")
    transliteration_scripts: List[str] = Field(default_factory=lambda: ["Latin", "Devanagari"])
    font_file: Optional[str] = Field(None, description="Path within pack, e.g. fonts/NotoSansOlChiki.ttf")
    font_family: str = Field(default="sans-serif")


class ModelConfig(BaseModel):
    asr_model_id: str = "hindi_asr_int8"
    asr_sample_rate: int = 16000
    mt_model_id: str = "hi_sat_mt_int8"
    mt_format: str = "tflite"
    mt_quantization: str = "int8"
    tts_model_id: str = "sat_tts_fastpitch_int8"
    tts_sample_rate: int = 22050


class PhraseEntry(BaseModel):
    phrase_id: str = Field(..., description="Unique ID, e.g. ph_sit_down_01")
    hindi_canonical: str = Field(..., description="Standard classroom Hindi prompt, e.g. बैठ जाओ")
    hindi_normalized: str = Field(..., description="Normalized form for fast lookup")
    hindi_aliases: List[str] = Field(default_factory=list, description="Alternative teacher utterances")
    target_native_script: str = Field(..., description="Translation in native script (Ol Chiki for Santali)")
    target_transliteration_latin: str = Field(..., description="Phonetic Latin transliteration")
    target_transliteration_devanagari: Optional[str] = Field(None, description="Phonetic Devanagari transliteration")
    audio_path: Optional[str] = Field(None, description="Relative path to native audio asset (e.g. audio/ph_sit_down_01.ogg)")
    category: str = Field(..., description="E.g. classroom_management, nipun_math, hygiene, greetings")
    fln_domain: Optional[str] = Field(None, description="E.g. Foundational Literacy, Foundational Numeracy, Socio-Emotional")
    pedagogical_context: Optional[str] = Field(None, description="Guidance notes for teacher")
    provenance: Provenance = Provenance.VERIFIED
    difficulty_level: int = Field(default=1, ge=1, le=5)


class FlnWord(BaseModel):
    word_id: str
    category: str = Field(..., description="numbers, body_parts, classroom_objects, colors, animals, greetings")
    hindi_word: str
    target_native_script: str
    target_transliteration: str
    audio_path: Optional[str] = None
    image_asset: Optional[str] = None
    numerical_value: Optional[int] = None


class WorksheetItem(BaseModel):
    item_id: str
    worksheet_id: str
    question_number: int
    prompt_hindi: str
    prompt_target_native: str
    prompt_target_transliteration: str
    question_type: str = Field(..., description="count_and_match, identify_object, trace_number, fill_in_blank")
    options: List[str] = Field(default_factory=list)
    correct_answer: str
    visual_asset: Optional[str] = None


class Worksheet(BaseModel):
    worksheet_id: str
    title: str
    grade_level: str = Field(..., description="Balvatika, Grade 1, Grade 2")
    nipun_competency: str = Field(..., description="e.g. Counting up to 20, Identifying shapes, Listening comprehension")
    items: List[WorksheetItem] = Field(default_factory=list)


class StudentActivity(BaseModel):
    activity_id: str
    title: str
    activity_type: str = Field(..., description="call_and_response, action_game, rhyme, choral_counting")
    teacher_prompt_hindi: str
    teacher_prompt_native: str
    student_response_native: str
    student_response_transliteration: str
    audio_prompt_path: Optional[str] = None
    pedagogical_objective: str


class PackStats(BaseModel):
    phrases_count: int = 0
    fln_vocab_count: int = 0
    worksheets_count: int = 0
    activities_count: int = 0
    audio_files_count: int = 0


class PackManifest(BaseModel):
    pack_id: str = Field(..., description="E.g. lang-pack-sat-olck-v1")
    pack_format_version: str = "1.0.0"
    language_code: str = Field(..., description="ISO 639-3 e.g. sat")
    language_name: str = "Santali"
    native_name: str = "ᱥᱟᱱᱛᱟᱲᱤ"
    version: str = "1.0.0"
    min_app_version: str = "1.0.0"
    script: ScriptConfig
    model_config = ConfigDict(populate_by_name=True)
    runtime_model_config: ModelConfig = Field(default_factory=ModelConfig, alias="model_config")
    stats: PackStats = Field(default_factory=PackStats)
    checksums: Dict[str, str] = Field(default_factory=dict, description="Relative path -> SHA-256")
    manifest_hash: Optional[str] = None

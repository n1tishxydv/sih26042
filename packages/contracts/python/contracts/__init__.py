"""Shared domain contracts for SIH26042.

These contracts define canonical data models shared across:
- apps/android (Kotlin representation & Room entities)
- services/api (FastAPI control plane)
- services/pack-builder (Deterministic compiler & validator)
- packages/language-pack-schema (Schema validation)
"""

from __future__ import annotations
from datetime import datetime, timezone
from enum import Enum

from typing import Dict, List, Optional, Any
from pydantic import BaseModel, Field, ConfigDict, model_validator


# ============================================================================
# 1. ENUMS & CONSTANTS
# ============================================================================

class ProvenanceStatus(str, Enum):
    """Explicit runtime provenance status for classroom safety.
    Rule: Never collapse these states into one generic 'translated' state.
    """
    VERIFIED = "VERIFIED"
    MACHINE_GENERATED = "MACHINE_GENERATED"
    LOW_CONFIDENCE = "LOW_CONFIDENCE"
    NO_MATCH = "NO_MATCH"
    UNAVAILABLE = "UNAVAILABLE"


class ContentVerificationStatus(str, Enum):
    """Content-level verification status.
    Security / Trust rule:
    If verification evidence is missing, use PENDING_VALIDATION or UNVERIFIED.
    Never mark data VERIFIED without genuine native-speaker review evidence.
    """
    VERIFIED = "VERIFIED"
    PENDING_VALIDATION = "PENDING_VALIDATION"
    UNVERIFIED = "UNVERIFIED"


class VerificationMethod(str, Enum):
    """Method used to establish linguistic and pedagogical correctness."""
    NATIVE_SPEAKER_REVIEW = "NATIVE_SPEAKER_REVIEW"
    EXPERT_CONSENSUS = "EXPERT_CONSENSUS"
    FIELD_OBSERVATION = "FIELD_OBSERVATION"
    AUTOMATED_HEURISTIC = "AUTOMATED_HEURISTIC"
    SYNTHETIC_PROTOTYPE = "SYNTHETIC_PROTOTYPE"


class PhraseCategory(str, Enum):
    """Production categories for primary classroom teacher utterances."""
    CLASSROOM_MANAGEMENT = "CLASSROOM_MANAGEMENT"
    TEACHING_INSTRUCTION = "TEACHING_INSTRUCTION"
    QUESTION = "QUESTION"
    PRAISE = "PRAISE"
    ENCOURAGEMENT = "ENCOURAGEMENT"
    PICTURE_ACTIVITY = "PICTURE_ACTIVITY"
    NUMBER_ACTIVITY = "NUMBER_ACTIVITY"
    ASSESSMENT = "ASSESSMENT"
    GREETING = "GREETING"
    TRANSITION = "TRANSITION"
    ATTENTION = "ATTENTION"


class FlnVocabCategory(str, Enum):
    """Foundational Literacy and Numeracy vocabulary categories."""
    NUMBERS = "NUMBERS"
    BODY_PARTS = "BODY_PARTS"
    COLORS = "COLORS"
    ANIMALS = "ANIMALS"
    CLASSROOM_OBJECTS = "CLASSROOM_OBJECTS"
    FAMILY = "FAMILY"
    FOOD = "FOOD"
    SHAPES = "SHAPES"
    BASIC_ACTIONS = "BASIC_ACTIONS"
    DIRECTIONS = "DIRECTIONS"


class WorksheetActivityType(str, Enum):
    """Worksheet pedagogical interaction patterns."""
    COUNTING = "COUNTING"
    MATCHING = "MATCHING"
    IDENTIFICATION = "IDENTIFICATION"
    SORTING = "SORTING"
    LETTER_RECOGNITION = "LETTER_RECOGNITION"
    PICTURE_SELECTION = "PICTURE_SELECTION"
    YES_NO = "YES_NO"
    TRACE = "TRACE"
    SIMPLE_CLASSIFICATION = "SIMPLE_CLASSIFICATION"


# ============================================================================
# 2. VERIFICATION & AUDIO METADATA
# ============================================================================

class VerificationRecord(BaseModel):
    """Structured linguistic and pedagogical verification audit record."""
    status: ContentVerificationStatus = ContentVerificationStatus.PENDING_VALIDATION
    method: VerificationMethod = VerificationMethod.SYNTHETIC_PROTOTYPE
    reviewer_id: Optional[str] = Field(None, description="Identifier of verifying linguist or educator")
    reviewed_at: Optional[str] = Field(None, description="ISO-8601 date of review")
    reviewed_version: Optional[str] = Field(None, description="Pack version when reviewed")
    notes: Optional[str] = Field(None, description="Pedagogical notes or dialect specifics")


class AudioMetadata(BaseModel):
    """Physical and structural audio asset parameters."""
    audio_path: str = Field(..., description="Relative path in pack (e.g. audio/ph_sit_down_01.wav)")
    format: str = Field(default="wav", description="Container/codec (wav, ogg)")
    sample_rate: int = Field(default=16000, description="Hz (e.g. 16000)")
    channels: int = Field(default=1, description="1=Mono, 2=Stereo")
    bit_depth: int = Field(default=16, description="Bits per sample (16-bit PCM)")
    duration_ms: int = Field(default=500, description="Audio duration in milliseconds")
    file_size_bytes: int = Field(default=0, description="File size in bytes")
    sha256: str = Field(default="", description="Cryptographic hash of audio file")


# ============================================================================
# 3. LANGUAGE PACK SPECIFICATION & MANIFEST
# ============================================================================

class ScriptConfig(BaseModel):
    primary_script_name: str = Field(..., description="E.g. Ol Chiki")
    iso_15924: str = Field(..., description="E.g. Olck")
    unicode_range_start: str = Field(..., description="Hex e.g. 0x1C50")
    unicode_range_end: str = Field(..., description="Hex e.g. 0x1C7F")
    transliteration_scripts: List[str] = Field(default_factory=lambda: ["Latin", "Devanagari"])
    font_file: Optional[str] = Field(None, description="Path within pack, e.g. fonts/NotoSansOlChiki-Regular.ttf")
    font_family: str = Field(default="sans-serif")


class LanguageMetadata(BaseModel):
    """Linguistic and script identifiers for classroom language support."""
    iso_639_3: str = Field(default="sat", description="Three-letter ISO code")
    bcp_47: str = Field(default="sat-Olck-IN", description="BCP-47 language tag")
    directionality: str = Field(default="ltr", description="ltr or rtl")
    unicode_ranges: List[str] = Field(default_factory=lambda: ["U+1C50-U+1C7F"])
    supported_input_scripts: List[str] = Field(default_factory=lambda: ["Devanagari", "Latin"])
    supported_output_scripts: List[str] = Field(default_factory=lambda: ["Ol Chiki", "Latin", "Devanagari"])
    transliteration_available: bool = True


class ModelEngineConfig(BaseModel):
    """Specification for future or pluggable on-device AI engines."""
    provider: str = Field(..., description="E.g. sherpa-onnx, onnxruntime-mobile, piper")
    model_id: str = Field(..., description="Identifier for quantized model")
    model_version: str = Field(default="1.0.0")
    model_format: str = Field(default="onnx", description="onnx, tflite, or bin")
    quantization: str = Field(default="int8", description="int8, fp16, or dynamic")
    expected_memory_mb: int = Field(default=60, description="Estimated peak RAM in MB")
    optional: bool = Field(default=True, description="Whether engine is required for pack activation")
    license: str = Field(default="Apache-2.0")


# Backward compatibility alias
ModelConfig = ModelEngineConfig


class RuntimeModelSuite(BaseModel):
    """Model suite configuration for offline pipeline execution."""
    asr: Optional[ModelEngineConfig] = None
    translation: Optional[ModelEngineConfig] = None
    tts: Optional[ModelEngineConfig] = None


class PackStats(BaseModel):
    phrases_count: int = 0
    fln_vocab_count: int = 0
    worksheets_count: int = 0
    activities_count: int = 0
    audio_files_count: int = 0


class LanguagePackManifest(BaseModel):
    """Authoritative language pack manifest schema."""
    pack_id: str = Field(..., description="Unique ID, e.g. lang-pack-sat-olck-v1")
    pack_format_version: str = Field(default="1.0.0", description="Specification version of the .slp container")
    schema_version: str = Field(default="1.1.0", description="JSON schema revision")
    language_code: str = Field(..., pattern=r"^[a-z]{3}$", description="ISO 639-3 e.g. sat")
    language_name: str = Field(..., description="Standard English name")
    native_name: str = Field(..., description="Native name in tribal script e.g. ᱥᱟᱱᱛᱟᱲᱤ")
    version: str = Field(..., pattern=r"^\d+\.\d+\.\d+$", description="Semver string e.g. 1.0.0")
    min_app_version: str = Field(default="1.0.0")
    max_app_version: Optional[str] = None
    description: str = Field(default="")
    locale: str = Field(default="sat_IN")
    created_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))
    updated_at: str = Field(default_factory=lambda: datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"))

    pack_size_bytes: int = 0
    checksum: str = Field(default="", description="Cryptographic SHA-256 hash of the compiled archive")
    validation_status: str = Field(default="VALIDATED", description="VALIDATED, PENDING, or EXPERIMENTAL")
    script: ScriptConfig
    language_metadata: Optional[LanguageMetadata] = None

    model_config = ConfigDict(populate_by_name=True)
    runtime_model_config: Optional[Any] = Field(default=None, alias="model_config")
    stats: PackStats = Field(default_factory=PackStats)
    checksums: Dict[str, str] = Field(default_factory=dict, description="Relative path -> SHA-256 hex string")
    manifest_hash: Optional[str] = None


# ============================================================================
# 4. PHRASE BANK DATA MODEL
# ============================================================================

class Phrase(BaseModel):
    """Production classroom phrase entity with structured verification and alias support."""
    phrase_id: str = Field(..., description="Unique ID e.g. ph_sit_down_01")
    category: PhraseCategory = Field(default=PhraseCategory.CLASSROOM_MANAGEMENT)
    grade: str = Field(default="Grade 1", description="Target grade: Balvatika, Grade 1, Grade 2")
    subject: str = Field(default="Classroom Routine", description="Subject or pedagogical domain")
    intent: str = Field(..., description="Machine-readable intent enum e.g. CLASSROOM_ACTION_SIT")

    # Text fields with backwards-compatible aliases
    hindi_canonical: str = Field(..., description="Standard classroom Hindi prompt, e.g. बैठ जाओ")
    hindi_normalized: str = Field(default="", description="Normalized form for fast lookup")
    hindi_aliases: List[str] = Field(default_factory=list, description="Alternative teacher utterances")

    target_native_script: str = Field(..., description="Translation in Ol Chiki script")
    target_transliteration_latin: str = Field(..., description="Phonetic Latin transliteration")
    target_transliteration_devanagari: Optional[str] = Field(None, description="Phonetic Devanagari transliteration")

    # Audio fields
    audio_path: Optional[str] = Field(None, description="Relative path to audio file (e.g. audio/ph_sit_down_01.wav)")
    audio_format: str = Field(default="wav", description="Audio format (wav, ogg)")
    audio_duration_ms: int = Field(default=500, description="Audio duration in milliseconds")

    # Verification & provenance
    verification: VerificationRecord = Field(default_factory=VerificationRecord)
    source_reference: Optional[str] = Field("NCERT / NIPUN Bharat Primary Pedagogy", description="Source reference")
    confidence_policy: str = Field(default="PREFER_EXACT_MATCH")
    pack_version: str = Field(default="1.0.0")
    difficulty_level: int = Field(default=1, ge=1, le=5)

    @property
    def provenance_state(self) -> ProvenanceStatus:
        """Map content verification status to runtime provenance state."""
        if self.verification.status == ContentVerificationStatus.VERIFIED:
            return ProvenanceStatus.VERIFIED
        return ProvenanceStatus.LOW_CONFIDENCE

    @model_validator(mode="before")
    @classmethod
    def populate_defaults_and_aliases(cls, data: Any) -> Any:
        if isinstance(data, dict):
            # Map legacy category strings if needed
            cat = data.get("category")
            if cat and isinstance(cat, str):
                cat_upper = cat.upper()
                if cat_upper in PhraseCategory.__members__:
                    data["category"] = PhraseCategory[cat_upper]
                elif cat == "classroom_management":
                    data["category"] = PhraseCategory.CLASSROOM_MANAGEMENT
                elif "math" in cat or "numeracy" in cat:
                    data["category"] = PhraseCategory.NUMBER_ACTIVITY
                elif "literacy" in cat:
                    data["category"] = PhraseCategory.TEACHING_INSTRUCTION
                elif "greeting" in cat:
                    data["category"] = PhraseCategory.GREETING
                elif "encouragement" in cat or "praise" in cat:
                    data["category"] = PhraseCategory.PRAISE
                else:
                    data["category"] = PhraseCategory.CLASSROOM_MANAGEMENT

            # Map legacy verification/provenance
            if "verification" not in data:
                prov = data.get("provenance", "PENDING_VALIDATION")
                if prov == "VERIFIED" and "verification_reviewer" in data:
                    status = ContentVerificationStatus.VERIFIED
                else:
                    # In Phase 1, unverified prototype data is safely marked PENDING_VALIDATION
                    status = ContentVerificationStatus.PENDING_VALIDATION
                data["verification"] = {
                    "status": status,
                    "method": VerificationMethod.SYNTHETIC_PROTOTYPE,
                    "notes": data.get("pedagogical_context")
                }

            # Ensure intent is populated
            if "intent" not in data or not data["intent"]:
                data["intent"] = data.get("phrase_id", "CLASSROOM_INTENT").upper()
        return data


# ============================================================================
# 5. FLN VOCABULARY DATA MODEL
# ============================================================================

class FlnVocabularyItem(BaseModel):
    """Foundational Literacy & Numeracy vocabulary entry."""
    vocabulary_id: str = Field(..., description="Unique word ID e.g. fln_num_01")
    hindi_text: str = Field(..., description="Hindi word e.g. एक (1)")
    santali_text: str = Field(..., description="Santali text in Ol Chiki script")
    ol_chiki: str = Field(..., description="Ol Chiki characters e.g. ᱑ (ᱢᱤᱫ)")
    transliteration: str = Field(..., description="Phonetic transliteration e.g. Mitʻ (1)")
    category: FlnVocabCategory = Field(..., description="Category enum e.g. NUMBERS, ANIMALS")
    grade: str = Field(default="Grade 1")
    subject: str = Field(default="Foundational Numeracy")
    audio_asset: Optional[str] = Field(None, description="Relative audio path")
    image_asset: Optional[str] = Field(None, description="Relative visual image path")
    numerical_value: Optional[int] = Field(None, description="Integer value for number items")
    verification: VerificationRecord = Field(default_factory=VerificationRecord)

    @model_validator(mode="before")
    @classmethod
    def map_legacy_fln(cls, data: Any) -> Any:
        if isinstance(data, dict):
            if "word_id" in data and "vocabulary_id" not in data:
                data["vocabulary_id"] = data["word_id"]
            if "hindi_word" in data and "hindi_text" not in data:
                data["hindi_text"] = data["hindi_word"]
            if "target_native_script" in data:
                if "santali_text" not in data:
                    data["santali_text"] = data["target_native_script"]
                if "ol_chiki" not in data:
                    data["ol_chiki"] = data["target_native_script"]
            if "target_transliteration" in data and "transliteration" not in data:
                data["transliteration"] = data["target_transliteration"]
            if "audio_path" in data and "audio_asset" not in data:
                data["audio_asset"] = data["audio_path"]

            # Map category
            cat = data.get("category", "NUMBERS")
            if isinstance(cat, str):
                cat_upper = cat.upper()
                if cat_upper in FlnVocabCategory.__members__:
                    data["category"] = FlnVocabCategory[cat_upper]
                elif cat_upper == "NUMBERS":
                    data["category"] = FlnVocabCategory.NUMBERS
                elif "BODY" in cat_upper:
                    data["category"] = FlnVocabCategory.BODY_PARTS
                elif "COLOR" in cat_upper:
                    data["category"] = FlnVocabCategory.COLORS
                elif "ANIMAL" in cat_upper:
                    data["category"] = FlnVocabCategory.ANIMALS
                elif "OBJECT" in cat_upper:
                    data["category"] = FlnVocabCategory.CLASSROOM_OBJECTS
                else:
                    data["category"] = FlnVocabCategory.CLASSROOM_OBJECTS
        return data


# ============================================================================
# 6. WORKSHEET & ACTIVITY DATA MODELS
# ============================================================================

class WorksheetItem(BaseModel):
    item_id: str
    worksheet_id: str
    question_number: int
    prompt_hindi: str
    prompt_target_native: str
    prompt_target_transliteration: str
    question_type: str = Field(..., description="count_and_match, identify_object, trace_number")
    options: List[str] = Field(default_factory=list)
    correct_answer: str
    visual_asset: Optional[str] = None


class WorksheetDefinition(BaseModel):
    """NIPUN Bharat worksheet definition."""
    worksheet_id: str
    grade: str = Field(default="Grade 1")
    subject: str = Field(default="Foundational Numeracy")
    learning_outcome_id: str = Field(default="NIPUN_FLN_M1")
    title: str
    instructions: str = Field(default="Listen to prompt and select correct answer")
    hindi_content: str = Field(default="")
    santali_content: str = Field(default="")
    activity_type: WorksheetActivityType = WorksheetActivityType.COUNTING
    difficulty: int = Field(default=1, ge=1, le=5)
    response_type: str = Field(default="MULTIPLE_CHOICE")
    image_assets: List[str] = Field(default_factory=list)
    number_assets: List[int] = Field(default_factory=list)
    verification: VerificationRecord = Field(default_factory=VerificationRecord)
    content_version: str = Field(default="1.0.0")
    items: List[WorksheetItem] = Field(default_factory=list)


class StudentActivity(BaseModel):
    """Interactive classroom game / activity."""
    activity_id: str
    activity_type: str = Field(..., description="ACTION_GAME, CALL_AND_RESPONSE, CHORAL_COUNTING")
    grade: str = Field(default="Grade 1")
    goal: str = Field(default="")
    instructions_hindi: str
    instructions_santali: str
    audio: Optional[str] = None
    items: List[str] = Field(default_factory=list)
    expected_response: str = Field(default="")
    difficulty: int = Field(default=1, ge=1, le=5)
    verification: VerificationRecord = Field(default_factory=VerificationRecord)


# ============================================================================
# 7. RUNTIME TELEMETRY & TRANSLATION CONTRACTS
# ============================================================================

class TranslationResult(BaseModel):
    operation_id: str = Field(..., description="UUID for pipeline tracking")
    recognized_hindi: str
    output_native_script: str
    output_transliteration: str
    provenance: ProvenanceStatus
    confidence: float = Field(..., ge=0.0, le=1.0)
    audio_path: Optional[str] = None
    matched_phrase_id: Optional[str] = None
    asr_latency_ms: float = 0.0
    match_latency_ms: float = 0.0
    audio_latency_ms: float = 0.0
    total_latency_ms: float = 0.0
    pipeline_mode: str = "VERIFIED_FAST_PATH"


class LearningOutcome(BaseModel):
    competency_id: str
    domain: str
    grade_level: str
    nipun_code: str
    description_en: str
    description_hi: str


class Flashcard(BaseModel):
    word_id: str
    category: str
    hindi_word: str
    target_native_script: str
    target_transliteration: str
    audio_path: Optional[str] = None
    image_asset: Optional[str] = None
    numerical_value: Optional[int] = None


class PerformanceMetric(BaseModel):
    operation_id: str
    device_id: str
    app_version: str
    android_version: int
    total_ram_mb: int
    peak_ram_mb: int
    pipeline_mode: str
    asr_latency_ms: float
    match_latency_ms: float
    audio_latency_ms: float
    total_latency_ms: float
    cold_start: int = 0
    created_at: datetime = Field(default_factory=datetime.utcnow)

package org.sih26042.coteacher.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import org.sih26042.coteacher.TranslationDetailDest
import org.sih26042.coteacher.core.engine.AsrEvent
import org.sih26042.coteacher.core.engine.AsrResult
import org.sih26042.coteacher.core.model.ClassroomInteractionResult
import org.sih26042.coteacher.core.model.LatencyBreakdown
import org.sih26042.coteacher.core.model.ProvenanceState
import org.sih26042.coteacher.di.AppContainer
import java.util.UUID

/**
 * Explicit Live Class UI States distinguishing every phase of speech recognition, matching, and fallback.
 */
enum class LiveClassUiState {
    IDLE,
    LISTENING,
    PROCESSING,
    MATCHED,
    PENDING_VALIDATION,
    NEURAL_FALLBACK,
    NO_MATCH,
    ERROR,
    UNAVAILABLE
}

@Composable
fun LiveClassScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val activePack by container.languagePackRepository.activePack.collectAsState()
    val phrases by container.classroomRepository.phrases.collectAsState()

    var uiState by remember { mutableStateOf(LiveClassUiState.IDLE) }
    var partialTranscript by remember { mutableStateOf("") }
    var activeTraceId by remember { mutableStateOf<String?>(null) }
    var audioLevelRms by remember { mutableFloatStateOf(0f) }
    var isVoiceDetected by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDiagnostics by remember { mutableStateOf(false) }
    var copiedNotice by remember { mutableStateOf(false) }

    // Dialog state for editing recognized Hindi text
    var showEditDialog by remember { mutableStateOf(false) }
    var editedHindiText by remember { mutableStateOf("") }

    var currentResult by remember {
        mutableStateOf(
            ClassroomInteractionResult(
                recognizedHindi = "बैठ जाओ",
                outputNativeScript = "ᱫᱩᱲᱩᱵ ᱢᱮ",
                outputTransliteration = "Duṛub me",
                provenance = ProvenanceState.PENDING_VALIDATION,
                confidence = null,
                audioPath = "audio/ph_sit_down_01.wav",
                latency = LatencyBreakdown(
                    asrLatencyMs = 180,
                    matchLatencyMs = 12,
                    audioLatencyMs = 40,
                    totalLatencyMs = 232,
                    pipelineMode = "PENDING_VALIDATION_MATCH"
                )
            )
        )
    }

    var isSlowMode by remember { mutableStateOf(false) }
    var permissionDeniedNotice by remember { mutableStateOf(false) }

    // Pulsing animation for microphone
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState == LiveClassUiState.LISTENING) 1.28f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Permission launcher for runtime RECORD_AUDIO
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            permissionDeniedNotice = false
            startMicrophoneSession(
                container = container,
                onStateChange = { uiState = it },
                onPartial = { partialTranscript = it },
                onRms = { audioLevelRms = it },
                onVoice = { isVoiceDetected = it },
                onComplete = { result, nextState ->
                    currentResult = result
                    uiState = nextState
                    if (result.audioPath != null) {
                        container.audioPlayerService.playAudio(
                            result.audioPath,
                            if (isSlowMode) 0.75f else 1.0f
                        )
                    }
                },
                onError = { err ->
                    errorMessage = err
                    uiState = LiveClassUiState.ERROR
                },
                scope = coroutineScope,
                isSlowMode = isSlowMode
            )
        } else {
            permissionDeniedNotice = true
            uiState = LiveClassUiState.ERROR
            errorMessage = "Microphone permission is required for classroom speech recognition"
        }
    }

    fun handleMicTap() {
        when (uiState) {
            LiveClassUiState.LISTENING -> {
                coroutineScope.launch {
                    uiState = LiveClassUiState.PROCESSING
                    container.microphoneRecorder.stopRecording()
                    val asrRes = container.asrEngine.stopListening().getOrNull()
                    if (asrRes != null) {
                        val interaction = container.processTeacherSpeechUseCase.executeWithAsrResult(asrRes)
                        currentResult = interaction
                        uiState = when (interaction.provenance) {
                            ProvenanceState.VERIFIED -> LiveClassUiState.MATCHED
                            ProvenanceState.PENDING_VALIDATION -> LiveClassUiState.PENDING_VALIDATION
                            ProvenanceState.MACHINE_GENERATED, ProvenanceState.RULE_BASED -> LiveClassUiState.NEURAL_FALLBACK
                            ProvenanceState.NO_MATCH -> LiveClassUiState.NO_MATCH
                            else -> LiveClassUiState.ERROR
                        }
                        if (interaction.audioPath != null) {
                            container.audioPlayerService.playAudio(
                                interaction.audioPath,
                                if (isSlowMode) 0.75f else 1.0f
                            )
                        }
                    } else {
                        uiState = LiveClassUiState.ERROR
                        errorMessage = "Speech capture stopped without valid result"
                    }
                }
            }
            LiveClassUiState.PROCESSING -> {
                // Ignore taps during processing
            }
            else -> {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                } else {
                    permissionDeniedNotice = false
                    startMicrophoneSession(
                        container = container,
                        onStateChange = { uiState = it },
                        onPartial = { partialTranscript = it },
                        onRms = { audioLevelRms = it },
                        onVoice = { isVoiceDetected = it },
                        onComplete = { result, nextState ->
                            currentResult = result
                            uiState = nextState
                            if (result.audioPath != null) {
                                container.audioPlayerService.playAudio(
                                    result.audioPath,
                                    if (isSlowMode) 0.75f else 1.0f
                                )
                            }
                        },
                        onError = { err ->
                            errorMessage = err
                            uiState = LiveClassUiState.ERROR
                        },
                        scope = coroutineScope,
                        isSlowMode = isSlowMode
                    )
                }
            }
        }
    }

    fun handleCancel() {
        coroutineScope.launch {
            container.microphoneRecorder.cancel()
            container.asrEngine.cancel()
            uiState = LiveClassUiState.IDLE
            partialTranscript = ""
            errorMessage = null
        }
    }

    fun triggerSimulatedPhrase(phraseText: String) {
        coroutineScope.launch {
            uiState = LiveClassUiState.PROCESSING
            val result = container.processTeacherSpeechUseCase.execute(phraseText)
            currentResult = result
            uiState = when (result.provenance) {
                ProvenanceState.VERIFIED -> LiveClassUiState.MATCHED
                ProvenanceState.PENDING_VALIDATION -> LiveClassUiState.PENDING_VALIDATION
                ProvenanceState.MACHINE_GENERATED, ProvenanceState.RULE_BASED -> LiveClassUiState.NEURAL_FALLBACK
                ProvenanceState.NO_MATCH -> LiveClassUiState.NO_MATCH
                else -> LiveClassUiState.ERROR
            }
            if (result.audioPath != null) {
                container.audioPlayerService.playAudio(
                    result.audioPath,
                    if (isSlowMode) 0.75f else 1.0f
                )
            }
        }
    }

    // Modal Dialog for Editing Recognized Text
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Teacher Spoken Text") },
            text = {
                Column {
                    Text(
                        text = "Modify the recognized Hindi text to re-translate via offline neural MT:",
                        fontSize = 13.sp,
                        color = Color(0xFF616161)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editedHindiText,
                        onValueChange = { editedHindiText = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Hindi Input") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val textToProcess = editedHindiText.trim()
                        showEditDialog = false
                        if (textToProcess.isNotBlank()) {
                            triggerSimulatedPhrase(textToProcess)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E))
                ) {
                    Text("TRANSLATE OFFLINE")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("CANCEL")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status Bar: Offline Indicator & Active Language Pack
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Color(0xFF2E7D32),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "● OFFLINE CLASSROOM (NO CLOUD)",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    color = Color(0xFF1A237E),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Pack: ${activePack.languageName} (${activePack.primaryScript})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Permission notice if denied
        if (permissionDeniedNotice) {
            item {
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️ Microphone permission denied. Please allow audio recording in device settings.",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Live Recognition & Fallback Card
        item {
            val isFallback = currentResult.provenance == ProvenanceState.MACHINE_GENERATED ||
                currentResult.provenance == ProvenanceState.RULE_BASED ||
                uiState == LiveClassUiState.NEURAL_FALLBACK
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isFallback) Color(0xFFFAF5FF) else Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isFallback) Modifier.border(1.5.dp, Color(0xFF7E57C2), RoundedCornerShape(16.dp))
                        else Modifier
                    )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEACHER HINDI SPEECH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                        Surface(
                            color = if (isFallback) Color(0xFFEDE7F6) else Color(0xFFE8EAF6),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⏱️ ${currentResult.latency.totalLatencyMs}ms (ASR: ${currentResult.latency.asrLatencyMs}ms | ${if (isFallback) "MT" else "Match"}: ${currentResult.latency.matchLatencyMs}ms)",
                                color = if (isFallback) Color(0xFF512DA8) else Color(0xFF283593),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dynamic text based on current UI state
                    when (uiState) {
                        LiveClassUiState.LISTENING -> {
                            Text(
                                text = if (partialTranscript.isNotEmpty()) "\"$partialTranscript\"" else "Listening for Hindi classroom speech...",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF8F00)
                            )
                        }
                        LiveClassUiState.PROCESSING -> {
                            Text(
                                text = "Recognizing Hindi speech & matching...",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5E35B1)
                            )
                        }
                        LiveClassUiState.ERROR -> {
                            Text(
                                text = "Error: ${errorMessage ?: "Recognition failure"}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD32F2F)
                            )
                        }
                        else -> {
                            Text(
                                text = "\"${currentResult.recognizedHindi}\"",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    // Native Script Output Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activePack.languageName.uppercase()} (${activePack.primaryScript.uppercase()})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                        ProvenanceBadge(provenance = currentResult.provenance)
                    }

                    // Fallback Visual Clarification Notice
                    if (isFallback) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            color = Color(0xFFF3E5F5),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚡ Long-Tail Fallback: No verified phrase matched. Translation generated via quantized offline IndicTrans2. Not yet native-verified.",
                                color = Color(0xFF4A148C),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState == LiveClassUiState.NO_MATCH || currentResult.provenance == ProvenanceState.NO_MATCH) {
                        Text(
                            text = "Didn't Understand / आवाज़ समझ नहीं आई",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC62828)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (currentResult.recognizedHindi.isNotBlank())
                                "Recognized: \"${currentResult.recognizedHindi}\". No verified phrase matched."
                            else
                                "No classroom phrase detected. Please choose a recovery option below:",
                            fontSize = 12.sp,
                            color = Color(0xFF616161)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { handleMicTap() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔄 TRY AGAIN", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = {
                                    editedHindiText = currentResult.recognizedHindi
                                    showEditDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("⌨️ TYPE MANUALLY", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text(
                            text = currentResult.outputNativeScript.ifEmpty { "—" },
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFallback) Color(0xFF311B92) else Color(0xFF1A237E),
                            lineHeight = 38.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Phonetic: ${currentResult.outputTransliteration.ifEmpty { "—" }}",
                            fontSize = 15.sp,
                            color = Color(0xFF424242),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Playback & Detail Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val hasAudio = currentResult.audioPath != null
                        Button(
                            onClick = {
                                currentResult.audioPath?.let {
                                    container.audioPlayerService.playAudio(it, if (isSlowMode) 0.75f else 1.0f)
                                }
                            },
                            enabled = hasAudio,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasAudio) Color(0xFF2E7D32) else Color(0xFFBDBDBD)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (hasAudio) "🔊 PLAY AUDIO" else "🔇 TEXT ONLY (NO TTS)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        if (hasAudio) {
                            OutlinedButton(
                                onClick = { isSlowMode = !isSlowMode },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (isSlowMode) Color(0xFFFF8F00) else Color(0xFF424242)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(if (isSlowMode) "🐢 0.75x" else "1.0x", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                onNavigate(
                                    TranslationDetailDest(
                                        hindiText = currentResult.recognizedHindi,
                                        nativeScriptText = currentResult.outputNativeScript,
                                        latinTransliteration = currentResult.outputTransliteration,
                                        provenanceState = currentResult.provenance.name,
                                        confidence = currentResult.confidence,
                                        audioPath = currentResult.audioPath,
                                        pedagogicalContext = currentResult.matchedPhrase?.pedagogicalContext,
                                        engineName = currentResult.engineName,
                                        modelVersion = currentResult.modelVersion,
                                        latencyMs = currentResult.latency.totalLatencyMs,
                                        warnings = currentResult.warnings,
                                        normalizedHindi = currentResult.matchedPhrase?.hindiNormalized ?: ""
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DETAILS 🔍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Fallback Action Utility Row (Retry, Edit Spoken Text, Copy Ol Chiki)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                editedHindiText = currentResult.recognizedHindi
                                showEditDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✏️ EDIT TEXT", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(currentResult.outputNativeScript))
                                copiedNotice = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (copiedNotice) "✓ COPIED" else "📋 COPY OL CHIKI", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Push-to-Talk Microphone & Control Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState == LiveClassUiState.LISTENING || uiState == LiveClassUiState.ERROR) {
                    OutlinedButton(
                        onClick = { handleCancel() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        shape = CircleShape,
                        modifier = Modifier.size(54.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("✕", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                }

                // Microphone Main Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(110.dp)
                ) {
                    if (uiState == LiveClassUiState.LISTENING) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0x33FF6F00))
                        )
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(
                                when (uiState) {
                                    LiveClassUiState.IDLE, LiveClassUiState.MATCHED, LiveClassUiState.PENDING_VALIDATION -> Color(0xFF1A237E)
                                    LiveClassUiState.NEURAL_FALLBACK -> Color(0xFF4A148C)
                                    LiveClassUiState.LISTENING -> Color(0xFFFF6F00)
                                    LiveClassUiState.PROCESSING -> Color(0xFF5E35B1)
                                    LiveClassUiState.NO_MATCH -> Color(0xFF455A64)
                                    LiveClassUiState.ERROR, LiveClassUiState.UNAVAILABLE -> Color(0xFFC62828)
                                }
                            )
                            .clickable { handleMicTap() }
                    ) {
                        Text(
                            text = when (uiState) {
                                LiveClassUiState.IDLE, LiveClassUiState.MATCHED, LiveClassUiState.PENDING_VALIDATION -> "🎙️\nTAP"
                                LiveClassUiState.NEURAL_FALLBACK -> "🎙️\nAGAIN"
                                LiveClassUiState.LISTENING -> "⏹️\nSTOP"
                                LiveClassUiState.PROCESSING -> "⚙️\n..."
                                LiveClassUiState.NO_MATCH -> "🎙️\nRETRY"
                                LiveClassUiState.ERROR, LiveClassUiState.UNAVAILABLE -> "⚠️\nRETRY"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (uiState == LiveClassUiState.LISTENING || uiState == LiveClassUiState.ERROR) {
                    Spacer(modifier = Modifier.width(74.dp))
                }
            }
        }

        // Quick Spoken Phrase Chips (Simulator / Evaluation shortcuts)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Tap a phrase to simulate teacher speaking:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(phrases) { p ->
                        AssistChip(
                            onClick = { triggerSimulatedPhrase(p.hindiCanonical) },
                            label = { Text(p.hindiCanonical, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF1A237E)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Long-Tail / Off-Script Simulation Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Simulate Off-Script / Long-Tail Classroom Utterances (Triggers Neural Fallback):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF7E57C2)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(
                        listOf(
                            "बच्चों, अपनी किताब खोलो।",
                            "चित्र को ध्यान से देखो।",
                            "मेरे साथ एक से पाँच तक गिनो।",
                            "कौन उत्तर देगा?",
                            "अपना हाथ उठाओ।"
                        )
                    ) { offScriptUtterance ->
                        AssistChip(
                            onClick = { triggerSimulatedPhrase(offScriptUtterance) },
                            label = { Text(offScriptUtterance, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFEDE7F6),
                                labelColor = Color(0xFF4A148C)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }

        // Developer Diagnostics Section Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { showDiagnostics = !showDiagnostics }) {
                    Text(
                        text = if (showDiagnostics) "Hide Pipeline Diagnostics ▲" else "Show Pipeline Diagnostics (Engineer Mode) ▼",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5C6BC0)
                    )
                }
            }
        }

        // Developer Diagnostics Card
        if (showDiagnostics) {
            item {
                val diagnostics = container.modelLifecycleManager.getMemoryDiagnostics()
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🔧 FULL PIPELINE TELEMETRY & DIAGNOSTICS",
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• ASR Engine: sherpa-onnx-streaming-zipformer-hindi (INT8)", color = Color.White, fontSize = 11.sp)
                        Text("• MT Fallback Engine: ${currentResult.engineName} (${currentResult.modelVersion})", color = Color.White, fontSize = 11.sp)
                        Text("• Script Validator: OlChikiScriptValidator (U+1C50..U+1C7F)", color = Color.White, fontSize = 11.sp)
                        Text("• Pipeline Provenance: ${currentResult.provenance.name}", color = Color(0xFFFFCC80), fontSize = 11.sp)
                        Text("• Active Audio Capture: 16 kHz Mono PCM + VAD Energy: ${audioLevelRms.toInt()}", color = Color.White, fontSize = 11.sp)
                        Text("• Heap Memory: ${diagnostics.usedHeapMb} MB / ${diagnostics.maxHeapMb} MB (Budget: 256 MB)", color = Color.White, fontSize = 11.sp)
                        Text("• TTS Feasibility Gate: TTS_UNAVAILABLE (Fake audio strictly prevented)", color = Color(0xFFFFAB91), fontSize = 11.sp)
                        Text("• Device Target: 2 GB Physical RAM Envelope (Low RAM: ${diagnostics.lowMemoryWarn})", color = Color(0xFFA5D6A7), fontSize = 11.sp)
                        Text("• Session Trace ID: ${activeTraceId ?: "None"}", color = Color(0xFFB0BEC5), fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

private fun startMicrophoneSession(
    container: AppContainer,
    onStateChange: (LiveClassUiState) -> Unit,
    onPartial: (String) -> Unit,
    onRms: (Float) -> Unit,
    onVoice: (Boolean) -> Unit,
    onComplete: (ClassroomInteractionResult, LiveClassUiState) -> Unit,
    onError: (String) -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
    isSlowMode: Boolean
) {
    scope.launch {
        val traceId = UUID.randomUUID().toString()
        onStateChange(LiveClassUiState.LISTENING)
        onPartial("")

        // 1. Initialize ASR engine if needed
        if (!container.asrEngine.isReady()) {
            val initRes = container.asrEngine.initialize()
            if (initRes.isFailure) {
                onError("ASR engine initialization failed: ${initRes.exceptionOrNull()?.message}")
                return@launch
            }
        }

        // 2. Start ASR listening session
        val asrStartRes = container.asrEngine.startListening(traceId) { event ->
            when (event) {
                is AsrEvent.PartialTranscript -> {
                    onPartial(event.transcript)
                }
                is AsrEvent.RecognitionError -> {
                    onError("[${event.errorCode}] ${event.message}")
                }
                else -> {}
            }
        }

        if (asrStartRes.isFailure) {
            onError("Failed to start ASR session: ${asrStartRes.exceptionOrNull()?.message}")
            return@launch
        }

        // 3. Start microphone recording
        val micStartRes = container.microphoneRecorder.startRecording(
            sessionId = traceId,
            scope = scope,
            onPcmChunk = { pcmChunk ->
                val rms = container.microphoneRecorder.preprocessor.computeRms(pcmChunk)
                onRms(rms)
                container.asrEngine.feedAudio(pcmChunk)
            },
            onVadStateChange = { isVoice ->
                onVoice(isVoice)
            }
        )

        if (micStartRes.isFailure) {
            container.asrEngine.cancel()
            onError("Microphone capture failed: ${micStartRes.exceptionOrNull()?.message}")
        }
    }
}

@Composable
fun ProvenanceBadge(provenance: ProvenanceState) {
    val (bgColor, textColor, label) = when (provenance) {
        ProvenanceState.VERIFIED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "✓ VERIFIED NATIVE PHRASE")
        ProvenanceState.PENDING_VALIDATION -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "PENDING VALIDATION")
        ProvenanceState.RULE_BASED -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "RULE-BASED / PHONETIC")
        ProvenanceState.MACHINE_GENERATED -> Triple(Color(0xFFEDE7F6), Color(0xFF512DA8), "AI-GENERATED TRANSLATION")
        ProvenanceState.LOW_CONFIDENCE -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "LOW CONFIDENCE ⚠️")
        ProvenanceState.NO_MATCH -> Triple(Color(0xFFECEFF1), Color(0xFF546E7A), "NO MATCH")
        ProvenanceState.UNAVAILABLE -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "AUDIO UNAVAILABLE")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

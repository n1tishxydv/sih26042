package org.sih26042.coteacher.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import org.sih26042.coteacher.TranslationDetailDest
import org.sih26042.coteacher.core.model.ClassroomInteractionResult
import org.sih26042.coteacher.core.model.ProvenanceState
import org.sih26042.coteacher.di.AppContainer

enum class MicState {
    IDLE,
    LISTENING,
    PROCESSING
}

@Composable
fun LiveClassScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val activePack by container.languagePackRepository.activePack.collectAsState()
    val phrases by container.classroomRepository.phrases.collectAsState()

    var micState by remember { mutableStateOf(MicState.IDLE) }
    var currentResult by remember {
        mutableStateOf(
            ClassroomInteractionResult(
                recognizedHindi = "बैठ जाओ",
                outputNativeScript = "ᱫᱩᱲᱩᱵ ᱢᱮ",
                outputTransliteration = "Duṛub me",
                provenance = ProvenanceState.VERIFIED,
                confidence = 1.0f,
                audioPath = "audio/ph_sit_down_01.ogg"
            )
        )
    }

    var playbackSpeed by remember { mutableStateOf(1.0f) }
    var isSlowMode by remember { mutableStateOf(false) }

    // Pulsing animation for microphone
    val infiniteTransition = rememberInfiniteTransition(label = "micPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (micState == MicState.LISTENING) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    fun triggerSpeechProcessing(spokenPrompt: String? = null) {
        coroutineScope.launch {
            micState = MicState.LISTENING
            kotlinx.coroutines.delay(400) // Simulated teacher speech duration
            micState = MicState.PROCESSING
            val result = container.processTeacherSpeechUseCase.execute(spokenPrompt)
            currentResult = result
            micState = MicState.IDLE

            // Automatically play verified audio if fast path
            if (result.provenance == ProvenanceState.VERIFIED && result.audioPath != null) {
                container.audioPlayerService.playAudio(result.audioPath, if (isSlowMode) 0.75f else 1.0f)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status Bar: Offline Indicator & Active Language
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
                        text = "● OFFLINE (NO CLOUD)",
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
                        text = "Lang: ${activePack.languageName} (${activePack.primaryScript})",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Live Recognition Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TEACHER SPEECH (HINDI)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                        // Latency telemetry pill
                        Surface(
                            color = Color(0xFFE8EAF6),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "⏱️ ${currentResult.latency.totalLatencyMs}ms (ASR: ${currentResult.latency.asrLatencyMs}ms)",
                                color = Color(0xFF283593),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (micState == MicState.LISTENING) "Listening to teacher..." else "\"${currentResult.recognizedHindi}\"",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (micState == MicState.LISTENING) Color(0xFFFF8F00) else Color(0xFF212121)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFFEEEEEE))

                    // Output in Native Mother Tongue Script
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${activePack.languageName.uppercase()} (OL CHIKI)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                        ProvenanceBadge(provenance = currentResult.provenance)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentResult.outputNativeScript,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        lineHeight = 38.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Phonetic: ${currentResult.outputTransliteration}",
                        fontSize = 15.sp,
                        color = Color(0xFF424242),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Audio Playback Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                currentResult.audioPath?.let {
                                    container.audioPlayerService.playAudio(it, if (isSlowMode) 0.75f else 1.0f)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔊 PLAY AUDIO", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                isSlowMode = !isSlowMode
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isSlowMode) Color(0xFFFF8F00) else Color(0xFF424242)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isSlowMode) "🐢 0.75x SLOW" else "1.0x SPEED", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                        pedagogicalContext = currentResult.matchedPhrase?.pedagogicalContext
                                    )
                                )
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("DETAILS 🔍", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Big Push-To-Talk Microphone Button
        item {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(110.dp)
            ) {
                // Pulse halo
                if (micState == MicState.LISTENING) {
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
                            when (micState) {
                                MicState.IDLE -> Color(0xFF1A237E)
                                MicState.LISTENING -> Color(0xFFFF6F00)
                                MicState.PROCESSING -> Color(0xFF5E35B1)
                            }
                        )
                        .clickable {
                            if (micState == MicState.IDLE) {
                                triggerSpeechProcessing(null)
                            }
                        }
                ) {
                    Text(
                        text = when (micState) {
                            MicState.IDLE -> "🎙️\nTAP"
                            MicState.LISTENING -> "👂\nLISTENING"
                            MicState.PROCESSING -> "⚙️\nMATCH"
                        },
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Quick Spoken Phrase Simulator Chips
        item {
            Text(
                text = "Tap a phrase to simulate teacher speaking:",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF616161)
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(phrases) { p ->
                    AssistChip(
                        onClick = {
                            triggerSpeechProcessing(p.hindiCanonical)
                        },
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

        // Neural Fallback Simulation
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Test Neural Fallback (Unseen Phrase)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A148C)
                        )
                        Text(
                            text = "Tests quantized on-device MT for unverified phrases",
                            fontSize = 10.sp,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                    Button(
                        onClick = {
                            triggerSpeechProcessing("किताब और कलम लाओ")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("TEST MT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProvenanceBadge(provenance: ProvenanceState) {
    val (bgColor, textColor, label) = when (provenance) {
        ProvenanceState.VERIFIED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "VERIFIED (NATIVE AUDIO)")
        ProvenanceState.MACHINE_GENERATED -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "MACHINE GENERATED (MT)")
        ProvenanceState.LOW_CONFIDENCE -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "LOW CONFIDENCE ⚠️")
        ProvenanceState.NO_MATCH -> Triple(Color(0xFFECEFF1), Color(0xFF546E7A), "NO MATCH")
        ProvenanceState.UNAVAILABLE -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "UNAVAILABLE")
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

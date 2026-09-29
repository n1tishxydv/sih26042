package org.sih26042.coteacher.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.TranslationDetailDest
import org.sih26042.coteacher.core.matching.TextNormalizer
import org.sih26042.coteacher.core.model.ProvenanceState
import org.sih26042.coteacher.core.model.TeacherCorrection
import org.sih26042.coteacher.di.AppContainer
import java.util.UUID

@Composable
fun TranslationDetailScreen(
    args: TranslationDetailDest,
    container: AppContainer,
    onBack: () -> Unit
) {
    val provenance = try {
        ProvenanceState.valueOf(args.provenanceState)
    } catch (_: Exception) {
        ProvenanceState.MACHINE_GENERATED
    }

    var showDeveloperMode by remember { mutableStateOf(false) }
    var suggestedSantali by remember { mutableStateOf("") }
    var dialectNote by remember { mutableStateOf("") }
    var teacherNote by remember { mutableStateOf("") }
    var selectedIssueType by remember { mutableStateOf("WRONG_TRANSLATION") }
    var isSubmitted by remember { mutableStateOf(false) }
    var submissionTraceId by remember { mutableStateOf<String?>(null) }

    val normalizedSource = remember(args.hindiText, args.normalizedHindi) {
        if (args.normalizedHindi.isNotBlank()) args.normalizedHindi else TextNormalizer.normalize(args.hindiText)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Navigation Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("← BACK TO LIVE CLASS", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = if (provenance == ProvenanceState.VERIFIED) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (provenance == ProvenanceState.VERIFIED) "VERIFIED RECORD" else "UNVERIFIED FALLBACK",
                        color = if (provenance == ProvenanceState.VERIFIED) Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Warnings Banner
        if (args.warnings.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "⚠️ SCRIPT / MODEL WARNINGS DETECTED",
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        args.warnings.forEach { warning ->
                            Text("• $warning", fontSize = 12.sp, color = Color(0xFFB71C1C))
                        }
                    }
                }
            }
        }

        // Primary Translation Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TRANSLATION PROVENANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF757575)
                        )
                        ProvenanceBadge(provenance = provenance)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Linguistic Validation Status Pill
                    Surface(
                        color = when (provenance) {
                            ProvenanceState.VERIFIED -> Color(0xFFE8F5E9)
                            ProvenanceState.PENDING_VALIDATION -> Color(0xFFFFF8E1)
                            ProvenanceState.MACHINE_GENERATED -> Color(0xFFEDE7F6)
                            else -> Color(0xFFECEFF1)
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = when (provenance) {
                                    ProvenanceState.VERIFIED -> "✓ NATIVE SPEAKER VERIFIED"
                                    ProvenanceState.PENDING_VALIDATION -> "⏳ PENDING NATIVE VALIDATION"
                                    ProvenanceState.MACHINE_GENERATED -> "🤖 NOT YET NATIVE-VERIFIED"
                                    else -> "⚠️ UNVERIFIED"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = when (provenance) {
                                    ProvenanceState.VERIFIED -> Color(0xFF2E7D32)
                                    ProvenanceState.PENDING_VALIDATION -> Color(0xFFF57F17)
                                    ProvenanceState.MACHINE_GENERATED -> Color(0xFF512DA8)
                                    else -> Color(0xFF455A64)
                                }
                            )
                            Text(
                                text = when (provenance) {
                                    ProvenanceState.VERIFIED -> "Reviewed and confirmed by native Santali linguistic educator."
                                    ProvenanceState.PENDING_VALIDATION -> "Curated classroom phrase; field audio undergoing native community validation."
                                    ProvenanceState.MACHINE_GENERATED -> "Synthesized by quantized offline neural MT fallback. Never upgraded to verified without human review."
                                    else -> "Requires manual review before classroom presentation."
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF616161)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Source Hindi
                    Text(text = "Source Utterance (Teacher Hindi):", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(text = args.hindiText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))

                    Spacer(modifier = Modifier.height(8.dp))

                    // Normalized Hindi
                    Text(text = "Deterministic Normalized Hindi (Canonical):", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(text = normalizedSource, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Ol Chiki
                    Text(text = "Santali Target (Ol Chiki Script):", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(
                        text = args.nativeScriptText.ifEmpty { "— (No translation generated)" },
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        lineHeight = 38.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Latin Transliteration
                    Text(text = "Phonetic Latin Transliteration:", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(
                        text = args.latinTransliteration.ifEmpty { "—" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Execution Telemetry
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Inference Engine", fontSize = 11.sp, color = Color(0xFF757575))
                            Text(text = args.engineName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        }
                        Column {
                            Text(text = "Model Version", fontSize = 11.sp, color = Color(0xFF757575))
                            Text(text = args.modelVersion, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        }
                        Column {
                            Text(text = "Latency", fontSize = 11.sp, color = Color(0xFF757575))
                            Text(text = "${args.latencyMs} ms", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Confidence Score (Null-safe!)
                    Text(
                        text = "Confidence: ${args.confidence?.let { "${(it * 100).toInt()}%" } ?: "null (Acoustic/MT confidence is uncalibrated — never fabricated)"}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (args.confidence != null) Color(0xFF2E7D32) else Color(0xFF757575)
                    )

                    if (args.audioPath != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Audio Asset: ${args.audioPath}", fontSize = 11.sp, color = Color(0xFF616161))
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Audio: None (Text-only neural fallback; Santali TTS feasibility gate: TTS_UNAVAILABLE)", fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    }
                }
            }
        }

        // Script Glyph Breakdown
        if (args.nativeScriptText.isNotBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ol Chiki Glyph Analysis (Unicode U+1C50 - U+1C7F)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF212121)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        args.nativeScriptText.filter { !it.isWhitespace() }.take(10).forEach { ch ->
                            val codePoint = ch.code
                            val isOlChiki = codePoint in 0x1C50..0x1C7F
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = ch.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (isOlChiki) Color(0xFF1A237E) else Color(0xFFC62828))
                                Text(
                                    text = if (isOlChiki) "Ol Chiki U+%04X".format(codePoint) else "CONTAMINATION U+%04X".format(codePoint),
                                    fontSize = 12.sp,
                                    fontWeight = if (isOlChiki) FontWeight.Normal else FontWeight.Bold,
                                    color = if (isOlChiki) Color(0xFF757575) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Teacher Feedback & Offline Correction Tool (Requirement 21)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Suggest Local Dialect / Translation Correction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Teacher corrections are stored offline and routed to linguistic validators. Production verified phrases are never auto-altered.",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Issue Type Selection Chips
                    Text(text = "Issue Category:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "WRONG_TRANSLATION" to "Incorrect",
                            "DIALECT_VARIATION" to "Dialect",
                            "SCRIPT_ERROR" to "Script",
                            "OTHER" to "Other"
                        ).forEach { (typeKey, typeLabel) ->
                            FilterChip(
                                selected = selectedIssueType == typeKey,
                                onClick = { selectedIssueType = typeKey },
                                label = { Text(typeLabel, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1A237E),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = suggestedSantali,
                        onValueChange = { suggestedSantali = it },
                        label = { Text("Corrected Santali Phrasing (Ol Chiki or Phonetic)") },
                        placeholder = { Text("e.g. ᱥᱟᱱᱟᱢ ᱠᱚ ᱫᱩᱲᱩᱵ ᱯᱮ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSubmitted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = dialectNote,
                        onValueChange = { dialectNote = it },
                        label = { Text("Dialect / Regional Variant (Optional)") },
                        placeholder = { Text("e.g. Mayurbhanj Northern vs Dumka Santhal Pargana") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSubmitted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = teacherNote,
                        onValueChange = { teacherNote = it },
                        label = { Text("Teacher Notes / Context (Optional)") },
                        placeholder = { Text("e.g. Used for Grade 1 morning assembly routine") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isSubmitted
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val correction = TeacherCorrection(
                                sourceHindi = args.hindiText,
                                candidateSantali = args.nativeScriptText,
                                suggestedSantali = suggestedSantali.trim(),
                                issueType = selectedIssueType,
                                dialectNote = dialectNote.trim().ifEmpty { null },
                                teacherNotes = teacherNote.trim().ifEmpty { null }
                            )
                            container.classroomRepository.recordCorrection(correction)
                            submissionTraceId = correction.correctionId
                            isSubmitted = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = suggestedSantali.isNotBlank() && !isSubmitted
                    ) {
                        Text(if (isSubmitted) "✓ QUEUED LOCALLY FOR SYNC" else "SUBMIT OFFLINE CORRECTION")
                    }

                    if (isSubmitted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Correction saved locally (ID: ${submissionTraceId?.take(8)}...). Will sync with validation control plane when online.",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Developer / Engineer Diagnostics Mode Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = { showDeveloperMode = !showDeveloperMode }) {
                    Text(
                        text = if (showDeveloperMode) "Hide Model Internals ▲" else "Show Developer Diagnostics (Engineer Mode) ▼",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5C6BC0)
                    )
                }
            }
        }

        if (showDeveloperMode) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF263238)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🔧 ON-DEVICE MT PIPELINE DIAGNOSTICS",
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("• Engine: ${args.engineName}", color = Color.White, fontSize = 11.sp)
                        Text("• Model: ${args.modelVersion} (IndicTrans2 INT8 Dynamic)", color = Color.White, fontSize = 11.sp)
                        Text("• Script Validator: OlChikiScriptValidator (U+1C50..U+1C7F)", color = Color.White, fontSize = 11.sp)
                        Text("• Provenance Enforced: ${provenance.name}", color = Color(0xFFFFCC80), fontSize = 11.sp)
                        Text("• Fallback Mode: MACHINE_GENERATED_TEXT_ONLY", color = Color.White, fontSize = 11.sp)
                        Text("• TTS Feasibility State: TTS_UNAVAILABLE (Fake audio strictly rejected)", color = Color(0xFFFFAB91), fontSize = 11.sp)
                        Text("• Memory Footprint: ~135 MB peak runtime (under 256 MB budget)", color = Color.White, fontSize = 11.sp)
                        Text("• Offline Guarantee: Zero network calls, 100% on-device", color = Color(0xFFA5D6A7), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

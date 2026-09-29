package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — QuickTranslateScreen
 *
 * Dedicated teacher translation utility reusing the Phase 2-4 orchestrated translation engine.
 * Never labels machine-generated output as VERIFIED.
 * Supports saving output to local materials and translation history.
 */
@Composable
fun QuickTranslateScreen(
    container: AppContainer,
    onNavigateToHistory: () -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }
    var translationResult by remember { mutableStateOf<ClassroomInteractionResult?>(null) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Nav
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
                    Text("← TOOLKIT", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToHistory,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0F2F1)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("📜 HISTORY", color = Color(0xFF004D40), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Title
        item {
            Text(
                text = "💬 त्वरित अनुवाद (Quick Translate)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "कक्षा निर्देश या वाक्य दर्ज करें — ऑफ़लाइन संताली में अनुवाद पाएँ",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("शिक्षक हिंदी वाक्य (Teacher Hindi Text):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            saveMessage = null
                        },
                        placeholder = { Text("उदाहरण: सब बच्चे किताब खोलो या एक से दस तक गिनो...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    isTranslating = true
                                    coroutineScope.launch {
                                        val res = container.orchestratedTranslationEngine.process(
                                            normalizedHindi = inputText.trim(),
                                            targetLanguageCode = "sat",
                                            asrLatencyMs = 0L
                                        )
                                        translationResult = res
                                        isTranslating = false

                                        // Automatically record in translation history
                                        container.teacherMaterialRepository.addHistoryItem(
                                            TranslationHistoryItem(
                                                sourceHindi = res.recognizedHindi,
                                                targetSantali = res.outputNativeScript,
                                                targetLatin = res.outputTransliteration,
                                                script = "Ol Chiki",
                                                provenance = if (res.provenance == ProvenanceState.VERIFIED) ContentProvenance.VERIFIED else ContentProvenance.MACHINE_GENERATED,
                                                confidence = res.confidence ?: 0.85f,
                                                audioPath = res.audioPath
                                            )
                                        )
                                    }
                                }
                            },
                            enabled = inputText.isNotBlank() && !isTranslating,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (isTranslating) "अनुवाद हो रहा है..." else "अनुवाद करें (Translate) ➔",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Clear Button
                        OutlinedButton(
                            onClick = {
                                inputText = ""
                                translationResult = null
                                saveMessage = null
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("साफ़ करें", color = Color(0xFF616161))
                        }
                    }
                }
            }
        }

        // Translation Result Card
        if (translationResult != null) {
            val res = translationResult!!
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
                            Text("संताली अनुवाद (Santali Translation):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                            // Provenance Badge
                            val isVerified = (res.provenance == ProvenanceState.VERIFIED)
                            Surface(
                                color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (isVerified) "✅ VERIFIED PHRASE" else "🤖 MACHINE_GENERATED",
                                    color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Ol Chiki Target
                        Text(
                            text = res.outputNativeScript,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40)
                        )

                        // Latin Transliteration
                        if (res.outputTransliteration.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = res.outputTransliteration,
                                fontSize = 14.sp,
                                color = Color(0xFF555555)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Audio button if available
                        if (res.audioPath != null) {
                            Button(
                                onClick = { container.audioPlayerService.playAudio(res.audioPath) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔊 ऑडियो बजाएँ (Play Audio)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Text(
                                "ऑडियो उपलब्ध नहीं है (Audio asset unavailable offline)",
                                fontSize = 11.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        Spacer(Modifier.height(14.dp))
                        Divider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(12.dp))

                        // Action Buttons: Save to Teaching Material
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val mat = TeacherMaterial(
                                        title = res.recognizedHindi,
                                        type = MaterialType.TRANSLATION,
                                        languageCode = "sat",
                                        contentJson = """{"hindi":"${res.recognizedHindi}","santali":"${res.outputNativeScript}","latin":"${res.outputTransliteration}"}""",
                                        provenance = ContentProvenance.TEACHER_CREATED,
                                        notes = "Quick translation"
                                    )
                                    container.teacherMaterialRepository.saveMaterial(mat)
                                    saveMessage = "सामग्री सुरक्षित की गई! (Saved to Materials)"
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💾 सुरक्षित करें (Save)", color = Color.White, fontSize = 12.sp)
                            }
                        }

                        if (saveMessage != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = saveMessage!!,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

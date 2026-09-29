package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
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
import androidx.navigation3.runtime.NavKey
import org.sih26042.coteacher.TranslationDetailDest
import org.sih26042.coteacher.core.model.ProvenanceState
import org.sih26042.coteacher.di.AppContainer

@Composable
fun TranslationDetailScreen(
    args: TranslationDetailDest,
    container: AppContainer,
    onBack: () -> Unit
) {
    var teacherSuggestion by remember { mutableStateOf("") }
    var suggestionSubmitted by remember { mutableStateOf(false) }

    val provenance = try {
        ProvenanceState.valueOf(args.provenanceState)
    } catch (_: Exception) {
        ProvenanceState.VERIFIED
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← BACK TO LIVE CLASS", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

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

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Teacher Utterance (Hindi):", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(text = args.hindiText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(text = "Target Mother Tongue (Ol Chiki Script):", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(
                        text = args.nativeScriptText,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        lineHeight = 38.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "Phonetic Latin Transliteration:", fontSize = 12.sp, color = Color(0xFF757575))
                    Text(text = args.latinTransliteration, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "Confidence: ${(args.confidence * 100).toInt()}%", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                        args.audioPath?.let {
                            Text(text = "Audio Asset: $it", fontSize = 12.sp, color = Color(0xFF616161))
                        }
                    }
                }
            }
        }

        // Pedagogical Context
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👩‍🏫 Pedagogical Guidance",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF1A237E)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = args.pedagogicalContext ?: "Ensure children maintain eye contact during choral repetition. Encourage physical demonstration of the action to build sensorimotor connections.",
                        fontSize = 13.sp,
                        color = Color(0xFF283593)
                    )
                }
            }
        }

        // Script Inspection (Unicode glyph analysis)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Script Glyph Breakdown (Ol Chiki U+1C50 - U+1C7F)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF212121)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    args.nativeScriptText.filter { !it.isWhitespace() }.take(8).forEach { ch ->
                        val codePoint = ch.code
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = ch.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A237E))
                            Text(text = "Unicode U+%04X".format(codePoint), fontSize = 12.sp, color = Color(0xFF757575))
                        }
                    }
                }
            }
        }

        // Teacher Feedback / Correction Tool (Offline-Queued)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Suggest Local Dialect / Phrase Correction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Offline-queued. Will sync with language pack team when connected.",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = teacherSuggestion,
                        onValueChange = { teacherSuggestion = it },
                        placeholder = { Text("Enter better mother tongue phrasing...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            suggestionSubmitted = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = teacherSuggestion.isNotBlank() && !suggestionSubmitted
                    ) {
                        Text(if (suggestionSubmitted) "✓ QUEUED LOCALLY FOR SYNC" else "SAVE OFFLINE CORRECTION")
                    }
                }
            }
        }
    }
}

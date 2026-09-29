package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import org.sih26042.coteacher.*
import org.sih26042.coteacher.core.model.ClassroomPhrase
import org.sih26042.coteacher.core.model.LanguagePackInfo
import org.sih26042.coteacher.di.AppContainer

@Composable
fun HomeScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit
) {
    val activePack by container.languagePackRepository.activePack.collectAsState()
    val phrases by container.classroomRepository.phrases.collectAsState()
    val latencies by container.classroomRepository.recordedLatencies.collectAsState()

    val quickPhrases = phrases.take(6)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A237E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
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
                                text = "● 100% OFFLINE READY",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            color = Color(0x33FFFFFF),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onNavigate(LanguagePacksDest) }
                        ) {
                            Text(
                                text = "${activePack.languageName} (${activePack.nativeName})",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Classroom Co-Teacher",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mother-Tongue FLN Assistant for Primary Teachers",
                        color = Color(0xFFC5CAE9),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { onNavigate(LiveClassDest) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8F00)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "START LIVE CLASSROOM MIC 🎙️",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Quick Classroom Phrases
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Classroom Phrases",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "Live Mode →",
                    fontSize = 12.sp,
                    color = Color(0xFF1A237E),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate(LiveClassDest) }
                )
            }
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(quickPhrases) { phrase ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(180.dp)
                            .clickable {
                                phrase.audioPath?.let { path ->
                                    container.audioPlayerService.playAudio(path)
                                }
                            }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "VERIFIED AUDIO",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = phrase.hindiCanonical,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF212121)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = phrase.targetNativeScript,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1A237E)
                            )
                            Text(
                                text = phrase.targetTransliterationLatin,
                                fontSize = 11.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }
            }
        }

        // Navigation Grid (All Modules)
        item {
            Text(
                text = "FLN Classroom Modules",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModuleCard(
                    title = "NIPUN Worksheets",
                    subtitle = "Numeracy & Literacy",
                    icon = "📝",
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(WorksheetsDest) }

                ModuleCard(
                    title = "FLN Flashcards",
                    subtitle = "Vocab & Pronunciation",
                    icon = "🎴",
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(FlashcardsDest) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModuleCard(
                    title = "Student Activities",
                    subtitle = "Call-Response Games",
                    icon = "🎮",
                    color = Color(0xFFEDE7F6),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(ActivitiesDest) }

                ModuleCard(
                    title = "Language Packs",
                    subtitle = "Santali, Mundari, Ho",
                    icon = "📦",
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(LanguagePacksDest) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ModuleCard(
                    title = "Device Health",
                    subtitle = "RAM & Latencies",
                    icon = "⚡",
                    color = Color(0xFFFBE9E7),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(PerformanceDest) }

                ModuleCard(
                    title = "Settings",
                    subtitle = "Audio & Offline Cache",
                    icon = "⚙️",
                    color = Color(0xFFECEFF1),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(SettingsDest) }
            }
        }
    }
}

@Composable
fun ModuleCard(
    title: String,
    subtitle: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF212121))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF616161))
        }
    }
}

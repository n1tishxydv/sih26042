package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.di.AppContainer

@Composable
fun FlashcardsScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val flnVocab by container.classroomRepository.flnVocabulary.collectAsState()
    val categories = listOf("ALL", "numbers", "body_parts", "colors", "animals", "classroom_objects")

    var selectedCategory by remember { mutableStateOf("ALL") }
    val filteredVocab = remember(selectedCategory, flnVocab) {
        if (selectedCategory == "ALL") flnVocab else flnVocab.filter { it.category == selectedCategory }
    }

    var currentIndex by remember { mutableStateOf(0) }
    val currentCard = filteredVocab.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("← BACK TO DASHBOARD", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "FLN Vocabulary Flashcards",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E)
        )

        // Category Filter
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                FilterChip(
                    selected = (selectedCategory == cat),
                    onClick = {
                        selectedCategory = cat
                        currentIndex = 0
                    },
                    label = { Text(cat.replace('_', ' ').uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (currentCard != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = currentCard.category.replace('_', ' ').uppercase(),
                            color = Color(0xFFE65100),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Ol Chiki native script
                    Text(
                        text = currentCard.targetNativeScript,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A237E),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Hindi: ${currentCard.hindiWord}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Phonetic: ${currentCard.targetTransliteration}",
                        fontSize = 16.sp,
                        color = Color(0xFF757575),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Audio Playback button
                    Button(
                        onClick = {
                            currentCard.audioPath?.let {
                                container.audioPlayerService.playAudio(it)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔊 HEAR NATIVE AUDIO", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            // Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (currentIndex > 0) currentIndex--
                    },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("← PREVIOUS")
                }

                Text(
                    text = "${currentIndex + 1} / ${filteredVocab.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF616161)
                )

                Button(
                    onClick = {
                        if (currentIndex < filteredVocab.size - 1) currentIndex++
                    },
                    enabled = currentIndex < filteredVocab.size - 1,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("NEXT →")
                }
            }
        }
    }
}

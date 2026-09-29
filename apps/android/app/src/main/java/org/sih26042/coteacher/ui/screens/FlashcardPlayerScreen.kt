package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import org.sih26042.coteacher.core.model.ContentProvenance
import org.sih26042.coteacher.core.model.Flashcard
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — FlashcardPlayerScreen
 *
 * Reusable, interactive offline flashcard runner:
 * - Content-driven
 * - Front/back reveal
 * - Prev/next, shuffle
 * - Audio replay
 * - Mark favorite / mark difficult
 * - Progress indicator
 */
@Composable
fun FlashcardPlayerScreen(
    deckId: String,
    container: AppContainer,
    onBack: () -> Unit
) {
    val flnVocab by container.classroomRepository.flnVocabulary.collectAsState()
    val allDecks = remember(flnVocab) { container.teacherMaterialRepository.getAllDecks(flnVocab) }
    val deck = remember(deckId, allDecks) { allDecks.find { it.id == deckId } }

    var cards by remember(deck) { mutableStateOf(deck?.cards ?: emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    val currentCard: Flashcard? = cards.getOrNull(currentIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Nav
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
                Text("← DECKS", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Shuffle Button
            OutlinedButton(
                onClick = {
                    cards = cards.shuffled()
                    currentIndex = 0
                    isRevealed = false
                },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("🔀 फेरबदल (Shuffle)", fontSize = 11.sp, color = Color(0xFF004D40))
            }
        }

        if (deck == null || cards.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("इस डेक में कोई कार्ड नहीं है।", color = Color.Gray)
            }
            return
        }

        // Title & Progress Bar
        Column {
            Text(
                text = deck.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "कार्ड ${currentIndex + 1} / ${cards.size}",
                    fontSize = 12.sp,
                    color = Color(0xFF616161),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isRevealed) "टैप करके छिपाएँ" else "टैप करके अर्थ देखें (Tap to Reveal)",
                    fontSize = 11.sp,
                    color = Color(0xFF004D40)
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / cards.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF004D40),
                trackColor = Color(0xFFE0E0E0)
            )
        }

        // Active Flashcard (Card Face)
        if (currentCard != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { isRevealed = !isRevealed }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Category & Provenance Tags
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE0F2F1),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = currentCard.category.uppercase(),
                                color = Color(0xFF004D40),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        val isVerified = (currentCard.provenance == ContentProvenance.VERIFIED)
                        Surface(
                            color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (isVerified) "VERIFIED" else "TEACHER_CREATED",
                                color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Front: Hindi Word
                    Text(
                        text = currentCard.front,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Back: Santali Ol Chiki + Latin (Revealed or Hidden)
                    if (isRevealed) {
                        Surface(
                            color = Color(0xFFF1F8E9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = currentCard.back,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20),
                                    textAlign = TextAlign.Center
                                )

                                if (currentCard.transliteration.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = currentCard.transliteration,
                                        fontSize = 15.sp,
                                        color = Color(0xFF558B2F),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else {
                        Surface(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "👆 उत्तर देखने के लिए यहाँ टैप करें",
                                color = Color(0xFF757575),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Audio Button if available
                    if (currentCard.audioRef != null) {
                        Button(
                            onClick = { container.audioPlayerService.playAudio(currentCard.audioRef) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🔊 सही उच्चारण सुनें (Play Audio)", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Bottom Controls: Prev, Next
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (currentIndex > 0) {
                        currentIndex--
                        isRevealed = false
                    }
                },
                enabled = currentIndex > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("⯇ पिछला (Prev)", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.width(12.dp))

            Button(
                onClick = {
                    if (currentIndex < cards.size - 1) {
                        currentIndex++
                        isRevealed = false
                    }
                },
                enabled = currentIndex < cards.size - 1,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("अगला (Next) ⯈", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

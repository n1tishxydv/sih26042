package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import org.sih26042.coteacher.CreateFlashcardDest
import org.sih26042.coteacher.FlashcardPlayerDest
import org.sih26042.coteacher.core.model.ContentProvenance
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — FlashcardDecksScreen
 *
 * Shows built-in FLN language pack flashcard decks and custom teacher-authored decks.
 */
@Composable
fun FlashcardDecksScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val flnVocab by container.classroomRepository.flnVocabulary.collectAsState()
    val allDecks = remember(flnVocab) { container.teacherMaterialRepository.getAllDecks(flnVocab) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    onClick = { onNavigate(CreateFlashcardDest()) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ नया कार्ड (New Card)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = "🎴 फ्लैशकार्ड डेक (Flashcard Decks)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "मातृभाषा शिक्षण और उच्चारण अभ्यास के लिए विषयवार कार्ड्स",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        items(allDecks, key = { it.id }) { deck ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(FlashcardPlayerDest(deck.id)) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎴", fontSize = 28.sp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = deck.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "${deck.cards.size} Cards • Class: ${deck.classLevel.displayLabel}",
                            fontSize = 11.sp,
                            color = Color(0xFF616161)
                        )

                        Spacer(Modifier.height(4.dp))

                        // Provenance
                        val isVerified = (deck.provenance == ContentProvenance.VERIFIED)
                        Surface(
                            color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isVerified) "VERIFIED PACK" else "TEACHER_CREATED",
                                color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { onNavigate(FlashcardPlayerDest(deck.id)) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("अभ्यास (Play) ➔", color = Color.White, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

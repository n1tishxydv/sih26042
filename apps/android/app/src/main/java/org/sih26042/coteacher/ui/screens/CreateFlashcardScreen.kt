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
import org.sih26042.coteacher.core.model.ContentProvenance
import org.sih26042.coteacher.core.model.Flashcard
import org.sih26042.coteacher.core.model.FlashcardDeck
import org.sih26042.coteacher.core.validation.TeacherContentValidator
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — CreateFlashcardScreen
 *
 * Content authoring UI for teachers to create custom flashcards with Ol Chiki validation.
 */
@Composable
fun CreateFlashcardScreen(
    deckId: String?,
    container: AppContainer,
    onBack: () -> Unit
) {
    var frontText by remember { mutableStateOf("") }
    var backText by remember { mutableStateOf("") }
    var transliteration by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("custom") }
    var difficulty by remember { mutableStateOf(1) }
    var errors by remember { mutableStateOf<List<String>>(emptyList()) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Nav
        item {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← BACK", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "✍️ नया फ्लैशकार्ड बनाएँ (Create Flashcard)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "मातृभाषा शिक्षण के लिए नया शब्द या वाक्य कार्ड जोड़ें",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Form Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Front Hindi
                    Text("आगे का भाग — हिंदी शब्द (Front - Hindi):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = frontText,
                        onValueChange = {
                            frontText = it
                            errors = emptyList()
                            successMessage = null
                        },
                        placeholder = { Text("उदा. पानी / किताब / गाय") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Back Santali
                    Text("पीछे का भाग — संताली (Back - Ol Chiki Santali):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = backText,
                        onValueChange = {
                            backText = it
                            errors = emptyList()
                            successMessage = null
                        },
                        placeholder = { Text("उदा. ᱫᱟᱜ / ᱯᱩᱛᱷᱤ / ᱜᱟᱹᱭ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Transliteration
                    Text("रोमन उच्चारण (Pronunciation in Latin):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = transliteration,
                        onValueChange = { transliteration = it },
                        placeholder = { Text("उदा. da:g / puthi / ga:y") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Category
                    Text("श्रेणी (Category):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        placeholder = { Text("उदा. numbers, animals, general") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(18.dp))

                    // Disclaimer notice
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "शिक्षक द्वारा बनाए गए कार्ड 'TEACHER_CREATED' के रूप में चिह्नित होंगे।",
                                fontSize = 11.sp,
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    if (errors.isNotEmpty()) {
                        errors.forEach { err ->
                            Text("❌ $err", color = Color(0xFFD32F2F), fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    if (successMessage != null) {
                        Text("✅ $successMessage", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(12.dp))
                    }

                    Button(
                        onClick = {
                            val newCard = Flashcard(
                                front = frontText.trim(),
                                back = backText.trim(),
                                script = "Ol Chiki",
                                transliteration = transliteration.trim(),
                                category = category.trim(),
                                difficulty = difficulty,
                                provenance = ContentProvenance.TEACHER_CREATED
                            )

                            val validation = TeacherContentValidator.validateFlashcard(newCard)
                            if (!validation.isValid) {
                                errors = validation.errors
                            } else {
                                // Add to or create custom deck
                                val targetDeckId = deckId ?: "teacher_custom_deck"
                                val currentDecks = container.teacherMaterialRepository.decks.value
                                val existingDeck = currentDecks.find { it.id == targetDeckId }

                                val updatedDeck = if (existingDeck != null) {
                                    existingDeck.copy(cards = existingDeck.cards + newCard)
                                } else {
                                    FlashcardDeck(
                                        id = targetDeckId,
                                        title = "शिक्षक कस्टम डेक (Teacher Custom Deck)",
                                        cards = listOf(newCard),
                                        isTeacherCreated = true
                                    )
                                }

                                container.teacherMaterialRepository.saveDeck(updatedDeck)
                                successMessage = "फ्लैशकार्ड सफलतापूर्वक सुरक्षित किया गया!"
                                frontText = ""
                                backText = ""
                                transliteration = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("फ्लैशकार्ड सुरक्षित करें (Save Card)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

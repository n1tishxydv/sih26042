package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.core.validation.TeacherContentValidator
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — CreateMaterialScreen
 *
 * Dedicated teacher authoring interface to safely author:
 * - Classroom Phrases
 * - Vocabulary items
 * - Simple interactive classroom activities
 * Enforces TEACHER_CREATED provenance and Ol Chiki validation.
 */
@Composable
fun CreateMaterialScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val materialTypes = listOf(MaterialType.PHRASE, MaterialType.ACTIVITY, MaterialType.TRANSLATION)
    var selectedType by remember { mutableStateOf(MaterialType.PHRASE) }

    var titleText by remember { mutableStateOf("") }
    var hindiText by remember { mutableStateOf("") }
    var santaliText by remember { mutableStateOf("") }
    var latinText by remember { mutableStateOf("") }
    var categoryText by remember { mutableStateOf("Classroom Routine") }
    var notesText by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf(GradeLevel.GRADE_1) }

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
                text = "✍️ नई पाठ्य सामग्री बनाएँ (Create Content)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "कक्षा के लिए नए मुहावरे, शब्दावली या गतिविधियाँ सुरक्षित करें",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Material Type Selector
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(materialTypes) { type ->
                    FilterChip(
                        selected = (selectedType == type),
                        onClick = {
                            selectedType = type
                            errors = emptyList()
                            successMessage = null
                        },
                        label = { Text("${type.icon} ${type.displayName}", fontSize = 12.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Form Fields
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Title
                    Text("शीर्षक (Title):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        placeholder = { Text("उदा. कक्षा अनुशासन वाक्य / फल और सब्जियाँ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Hindi Text
                    Text("हिंदी वाक्य / शब्द (Hindi Text):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = hindiText,
                        onValueChange = { hindiText = it },
                        placeholder = { Text("उदा. अपनी कॉपी निकालो") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Santali Text
                    Text("संताली अनुवाद (Santali in Ol Chiki):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = santaliText,
                        onValueChange = { santaliText = it },
                        placeholder = { Text("उदा. ᱟᱢᱟᱜ ᱠᱷᱟᱛᱟ ᱚᱰᱚᱠ ᱢᱮ") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Latin Transliteration
                    Text("रोमन उच्चारण (Latin Transliteration):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = latinText,
                        onValueChange = { latinText = it },
                        placeholder = { Text("उदा. amak' khata odok' me") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    // Category
                    Text("श्रेणी (Category):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = categoryText,
                        onValueChange = { categoryText = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(18.dp))

                    // Provenance Disclaimer
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("⚠️", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "यह सामग्री 'TEACHER_CREATED / PENDING_VALIDATION' के रूप में सुरक्षित होगी।",
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
                        Spacer(Modifier.height(10.dp))
                    }

                    if (successMessage != null) {
                        Text("✅ $successMessage", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(Modifier.height(10.dp))
                    }

                    Button(
                        onClick = {
                            val validation = TeacherContentValidator.validatePhrase(
                                hindiText = hindiText.trim(),
                                targetText = santaliText.trim(),
                                languageCode = "sat"
                            )

                            if (titleText.isBlank()) {
                                errors = listOf("कृपया सामग्री का शीर्षक दर्ज करें")
                            } else if (!validation.isValid) {
                                errors = validation.errors
                            } else {
                                val mat = TeacherMaterial(
                                    title = titleText.trim(),
                                    type = selectedType,
                                    languageCode = "sat",
                                    gradeLevel = gradeLevel,
                                    contentJson = """{"hindi":"${hindiText.trim()}","santali":"${santaliText.trim()}","latin":"${latinText.trim()}","category":"${categoryText.trim()}"}""",
                                    provenance = ContentProvenance.TEACHER_CREATED,
                                    validationStatus = "PENDING_VALIDATION",
                                    notes = notesText.trim()
                                )
                                container.teacherMaterialRepository.saveMaterial(mat)
                                successMessage = "सामग्री सफलतापूर्वक सुरक्षित की गई!"
                                titleText = ""
                                hindiText = ""
                                santaliText = ""
                                latinText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("सामग्री सुरक्षित करें (Save Material)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

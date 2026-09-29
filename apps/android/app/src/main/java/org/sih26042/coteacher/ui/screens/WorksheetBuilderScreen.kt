package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.navigation3.runtime.NavKey
import org.sih26042.coteacher.WorksheetPreviewDest
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.core.worksheet.WorksheetGenerator
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — WorksheetBuilderScreen
 *
 * Deterministic worksheet authoring from FLN curriculum items.
 * Allows teachers to select templates (A-E), choose vocabulary sets,
 * and generate structured bilingual classroom exercises.
 */
@Composable
fun WorksheetBuilderScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val flnVocab by container.classroomRepository.flnVocabulary.collectAsState()
    val categories = listOf("numbers", "animals", "body_parts", "colors", "classroom_objects")

    var worksheetTitle by remember { mutableStateOf("कक्षा 1 — संताली शब्द अभ्यास") }
    var selectedTemplate by remember { mutableStateOf(WorksheetTemplateType.MATCHING) }
    var selectedGrade by remember { mutableStateOf(GradeLevel.GRADE_1) }
    var selectedCategory by remember { mutableStateOf("numbers") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val relevantVocab = remember(selectedCategory, flnVocab) {
        flnVocab.filter { it.category == selectedCategory }
    }

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
                Text("← TOOLKIT", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "📝 NIPUN वर्कशीट निर्माता (Worksheet Builder)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "पाठ्य सामग्री से अभ्यास पत्र तैयार करें — 100% ऑफ़लाइन एवं मुद्रण-योग्य",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Title & Grade
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("वर्कशीट का शीर्षक (Worksheet Title):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = worksheetTitle,
                        onValueChange = { worksheetTitle = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    Text("कक्षा (Grade Level):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(GradeLevel.values()) { grade ->
                            FilterChip(
                                selected = (selectedGrade == grade),
                                onClick = { selectedGrade = grade },
                                label = { Text(grade.displayLabel, fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Template Selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("अभ्यास प्रारूप चुनें (Select Template):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))

                    WorksheetTemplateType.values().forEach { tmpl ->
                        val isSelected = (selectedTemplate == tmpl)
                        Surface(
                            color = if (isSelected) Color(0xFFE0F2F1) else Color(0xFFFAFAFA),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedTemplate = tmpl }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedTemplate = tmpl },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF004D40))
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = tmpl.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color(0xFF004D40) else Color(0xFF212121)
                                    )
                                }
                                Text(
                                    text = tmpl.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFF757575),
                                    modifier = Modifier.padding(start = 40.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Vocabulary Source Selection
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("शब्दावली स्रोत चुनें (Select Vocabulary Theme):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = (selectedCategory == cat),
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.replace('_', ' ').uppercase(), fontSize = 11.sp) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "उपलब्ध शब्द: ${relevantVocab.size} (संताली ओल चिकी सहित)",
                        fontSize = 11.sp,
                        color = Color(0xFF004D40)
                    )
                }
            }
        }

        // Generate Action Button
        item {
            if (errorMessage != null) {
                Text("❌ $errorMessage", color = Color(0xFFD32F2F), fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
            }

            Button(
                onClick = {
                    if (worksheetTitle.isBlank()) {
                        errorMessage = "कृपया वर्कशीट का शीर्षक दर्ज करें।"
                    } else if (relevantVocab.isEmpty()) {
                        errorMessage = "चयनित श्रेणी में कोई शब्द उपलब्ध नहीं हैं।"
                    } else {
                        val generated = WorksheetGenerator.generate(
                            title = worksheetTitle.trim(),
                            templateType = selectedTemplate,
                            selectedVocab = relevantVocab,
                            gradeLevel = selectedGrade
                        )

                        // Save as teacher material
                        val mat = TeacherMaterial(
                            id = generated.id,
                            title = generated.title,
                            type = MaterialType.WORKSHEET,
                            gradeLevel = generated.gradeLevel,
                            domain = generated.domain,
                            notes = "Generated with ${selectedTemplate.name}"
                        )
                        container.teacherMaterialRepository.saveMaterial(mat)

                        onNavigate(WorksheetPreviewDest(worksheetId = generated.id))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("वर्कशीट तैयार करें एवं देखें (Generate & Preview) ➔", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

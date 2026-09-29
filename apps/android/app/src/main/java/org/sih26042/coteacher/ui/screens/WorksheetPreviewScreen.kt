package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import org.sih26042.coteacher.core.model.WorksheetTemplateType
import org.sih26042.coteacher.core.worksheet.WorksheetGenerator
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — WorksheetPreviewScreen
 *
 * Print-ready preview of generated worksheet with local offline PDF export.
 */
@Composable
fun WorksheetPreviewScreen(
    worksheetId: String,
    container: AppContainer,
    onBack: () -> Unit
) {
    val flnVocab by container.classroomRepository.flnVocabulary.collectAsState()
    val savedMaterial = container.teacherMaterialRepository.getMaterial(worksheetId)

    // Reconstruct or load worksheet
    val worksheet = remember(worksheetId, flnVocab) {
        WorksheetGenerator.generate(
            title = savedMaterial?.title ?: "कक्षा 1 — NIPUN वर्कशीट",
            templateType = WorksheetTemplateType.MATCHING,
            selectedVocab = flnVocab.filter { it.category == "numbers" }
        )
    }

    var exportedPath by remember { mutableStateOf<String?>(null) }
    var isExporting by remember { mutableStateOf(false) }

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
                    Text("← BUILDER", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isExporting = true
                        val file = container.worksheetPdfExporter.exportToPdf(worksheet)
                        isExporting = false
                        exportedPath = file.absolutePath
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isExporting) "PDF बन रहा है..." else "📄 PDF निर्यात (Export)",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (exportedPath != null) {
            item {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("✅ PDF सफलतापूर्वक निर्यात की गई!", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 13.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("स्थान: $exportedPath", fontSize = 10.sp, color = Color(0xFF1B5E20))
                    }
                }
            }
        }

        // Worksheet Sheet Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "NIPUN FLN WORKSHEET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40)
                        )

                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "TEACHER_CREATED",
                                color = Color(0xFFE65100),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = worksheet.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "Grade: ${worksheet.gradeLevel.displayLabel}  |  Template: ${worksheet.templateType.displayName}",
                        fontSize = 11.sp,
                        color = Color(0xFF616161)
                    )

                    Spacer(Modifier.height(14.dp))
                    Divider(color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(12.dp))

                    Text("निर्देश (Instructions):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text(worksheet.instructionsHindi, fontSize = 12.sp, color = Color(0xFF424242))
                    Text(worksheet.instructionsSantali, fontSize = 13.sp, color = Color(0xFF004D40), fontWeight = FontWeight.Medium)

                    Spacer(Modifier.height(14.dp))
                    Divider(color = Color(0xFFEEEEEE))
                    Spacer(Modifier.height(12.dp))

                    Text("विद्यार्थी का नाम: _________________   दिनांक: _________", fontSize = 11.sp, color = Color(0xFF757575))
                }
            }
        }

        // Questions List
        itemsIndexed(worksheet.questions) { idx, q ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "प्रश्न ${idx + 1}. ${q.promptHindi}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "संताली: ${q.promptTargetNative} (${q.promptTargetTransliteration})",
                        fontSize = 13.sp,
                        color = Color(0xFF004D40),
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(Modifier.height(10.dp))

                    if (q.options.isNotEmpty()) {
                        q.options.forEach { opt ->
                            Surface(
                                color = Color(0xFFF5F5F5),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Text(
                                    text = "⚪  $opt",
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        Text("उत्तर: _________________________________", color = Color(0xFF9E9E9E), fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import org.sih26042.coteacher.core.model.NipunWorksheet
import org.sih26042.coteacher.core.model.WorksheetQuestion
import org.sih26042.coteacher.di.AppContainer

@Composable
fun WorksheetsScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val worksheets by container.classroomRepository.worksheets.collectAsState()
    var selectedWorksheetIndex by remember { mutableStateOf(0) }

    val activeWorksheet = worksheets.getOrNull(selectedWorksheetIndex)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← BACK TO DASHBOARD", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "NIPUN Bharat FLN Worksheets",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Bilingual Foundational Numeracy & Literacy exercises aligned with mother-tongue bridge pedagogy",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Worksheet Tab Selector
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                worksheets.forEachIndexed { index, ws ->
                    FilterChip(
                        selected = (selectedWorksheetIndex == index),
                        onClick = { selectedWorksheetIndex = index },
                        label = { Text(ws.title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (activeWorksheet != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Target Competency: ${activeWorksheet.nipunCompetency}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Grade Level: ${activeWorksheet.gradeLevel}",
                            fontSize = 11.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }

            items(activeWorksheet.items) { question ->
                WorksheetQuestionCard(question = question)
            }
        }
    }
}

@Composable
fun WorksheetQuestionCard(question: WorksheetQuestion) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    val isAnswered = selectedOption != null
    val isCorrect = selectedOption == question.correctAnswer

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Q${question.questionNumber}. ${question.questionType.replace('_', ' ').uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF757575)
                )
                if (isAnswered) {
                    Text(
                        text = if (isCorrect) "✓ CORRECT (ᱟᱹᱰᱤ ᱱᱟᱯᱟᱭ)" else "✗ TRY AGAIN",
                        color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.promptHindi,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = question.promptTargetNative,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Phonetic: ${question.promptTargetTransliteration}",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                question.options.forEach { opt ->
                    val isOptSelected = (selectedOption == opt)
                    val optColor = when {
                        !isAnswered -> Color(0xFFF5F5F5)
                        isOptSelected && isCorrect -> Color(0xFFC8E6C9)
                        isOptSelected && !isCorrect -> Color(0xFFFFCDD2)
                        else -> Color(0xFFF5F5F5)
                    }

                    Surface(
                        color = optColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedOption = opt }
                            .border(
                                width = if (isOptSelected) 2.dp else 1.dp,
                                color = if (isOptSelected) Color(0xFF1A237E) else Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Text(
                            text = opt,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121),
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

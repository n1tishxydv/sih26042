package org.sih26042.coteacher.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.core.content.SantaliNipunContent
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.domain.lesson.LessonEngine

/**
 * PHASE 5 — LessonListScreen
 *
 * Teacher-facing view: all available NIPUN/FLN lessons for a grade.
 * Shows lesson domain, duration, provenance badge.
 *
 * DESIGN PRINCIPLE:
 *   Lessons are arranged by domain color, not arbitrary order.
 *   Provenance status is always visible — NEVER hidden from the teacher.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonListScreen(
    lessonEngine: LessonEngine,
    onStartLesson: (Lesson) -> Unit,
    onBack: () -> Unit,
    selectedGrade: GradeLevel = GradeLevel.GRADE_1
) {
    val lessons = remember(selectedGrade) {
        SantaliNipunContent.getLessonsForGrade(selectedGrade)
    }

    var selectedGradeTab by remember { mutableStateOf(selectedGrade) }
    val grades = GradeLevel.values().toList()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "पाठ चुनें",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            "NIPUN/FLN कक्षा पाठ",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Grade filter tabs
            ScrollableTabRow(
                selectedTabIndex = grades.indexOf(selectedGradeTab),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                edgePadding = 0.dp
            ) {
                grades.forEach { grade ->
                    Tab(
                        selected = selectedGradeTab == grade,
                        onClick = { selectedGradeTab = grade },
                        text = {
                            Text(
                                grade.displayLabel,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                        }
                    )
                }
            }

            val filteredLessons = remember(selectedGradeTab) {
                SantaliNipunContent.getLessonsForGrade(selectedGradeTab)
            }

            if (filteredLessons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "📚",
                            modifier = Modifier.size(64.dp),
                            fontSize = 48.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "इस कक्षा के लिए पाठ जल्द आएंगे",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Domain grouping header
                    val byDomain = filteredLessons.groupBy { it.domain }
                    byDomain.forEach { (domain, domainLessons) ->
                        item {
                            DomainHeader(domain = domain)
                        }
                        items(domainLessons) { lesson ->
                            LessonCard(
                                lesson = lesson,
                                onStart = { onStartLesson(lesson) }
                            )
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun DomainHeader(domain: SubjectDomain) {
    val color = domainColor(domain)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
                .background(color, MaterialTheme.shapes.small)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "${domain.icon} ${domain.displayLabel}",
            style = MaterialTheme.typography.labelLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LessonCard(
    lesson: Lesson,
    onStart: () -> Unit
) {
    val color = domainColor(lesson.domain)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color accent
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 60.dp)
                    .background(color, MaterialTheme.shapes.small)
            )
            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    lesson.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    lesson.learningOutcome.objectiveHindi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Duration
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("⏱ ${lesson.estimatedMinutes} मिनट", fontSize = 11.sp)
                        }
                    )
                    // Activities count
                    AssistChip(
                        onClick = {},
                        label = {
                            Text("🎯 ${lesson.activityIds.size} गतिविधि", fontSize = 11.sp)
                        }
                    )
                }
                // Provenance badge
                ProvenanceBadge(provenance = lesson.provenance)
            }

            Spacer(Modifier.width(8.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = color)
            ) {
                Text("शुरू करें", fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ProvenanceBadge(
    provenance: ContentProvenance,
    modifier: Modifier = Modifier
) {
    val (label, color) = when (provenance) {
        ContentProvenance.VERIFIED ->
            "✅ सत्यापित" to Color(0xFF2E7D32)
        ContentProvenance.PENDING_VALIDATION ->
            "⏳ समीक्षा बाकी" to Color(0xFFF57F17)
        ContentProvenance.MACHINE_GENERATED ->
            "🤖 AI-जनित" to Color(0xFF1565C0)
        ContentProvenance.TEACHER_CREATED ->
            "📝 शिक्षक-निर्मित" to Color(0xFF6A1B9A)
        ContentProvenance.LOW_CONFIDENCE ->
            "⚠️ कम विश्वास" to Color(0xFFBF360C)
        ContentProvenance.UNAVAILABLE ->
            "❌ अनुपलब्ध" to Color(0xFF455A64)
    }

    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

fun domainColor(domain: SubjectDomain): Color = when (domain) {
    SubjectDomain.FOUNDATIONAL_NUMERACY -> Color(0xFF1565C0)
    SubjectDomain.FOUNDATIONAL_LITERACY -> Color(0xFF2E7D32)
    SubjectDomain.ORAL_LANGUAGE -> Color(0xFF6A1B9A)
    SubjectDomain.ENVIRONMENTAL_AWARENESS -> Color(0xFF00695C)
    SubjectDomain.CLASSROOM_ROUTINE -> Color(0xFF4E342E)
}

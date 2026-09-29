package org.sih26042.coteacher.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.domain.lesson.LessonEngine

/**
 * PHASE 5 — LessonDetailScreen
 *
 * Shows full lesson overview before teacher starts:
 *  - Learning objective (Hindi + Santali)
 *  - Key classroom phrases to use
 *  - Activity list with types and durations
 *  - NIPUN competency reference
 *  - Start Lesson button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonDetailScreen(
    lesson: Lesson,
    lessonEngine: LessonEngine,
    onStartLesson: () -> Unit,
    onBack: () -> Unit
) {
    val accentColor = domainColor(lesson.domain)
    val activities = remember(lesson) { lessonEngine.getActivitiesForLesson(lesson) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        lesson.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 20.sp, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accentColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        lessonEngine.startLesson(lesson)
                        onStartLesson()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("▶", fontSize = 18.sp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("पाठ शुरू करें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header card
            item {
                LessonHeaderCard(lesson = lesson, accentColor = accentColor)
            }

            // Learning objective
            item {
                LessonObjectiveCard(lesson = lesson)
            }

            // Activities list
            item {
                Text(
                    "गतिविधियाँ (${activities.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(activities) { activity ->
                ActivityPreviewCard(activity = activity)
            }

            // Classroom phrases
            if (lesson.phraseIds.isNotEmpty()) {
                item {
                    Text(
                        "मुख्य कक्षा वाक्यांश",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    PhrasePlanCard(phraseIds = lesson.phraseIds)
                }
            }

            // Disclaimer
            item {
                ContentDisclaimerCard(provenance = lesson.provenance)
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LessonHeaderCard(lesson: Lesson, accentColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    lesson.domain.icon,
                    fontSize = 32.sp
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        lesson.gradeLevel.displayLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )
                    Text(
                        lesson.domain.displayLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip(label = "⏱ ${lesson.estimatedMinutes} मिनट")
                InfoChip(label = "🎯 ${lesson.activityIds.size} गतिविधि")
                InfoChip(label = "📝 ${lesson.worksheetIds.size} कार्यपत्रक")
            }
            if (lesson.learningOutcome.nipunCompetencyRef != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "NIPUN: ${lesson.learningOutcome.nipunCompetencyRef}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
private fun LessonObjectiveCard(lesson: Lesson) {
    val outcome = lesson.learningOutcome
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "सीखने का उद्देश्य",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            // Hindi
            Row(verticalAlignment = Alignment.Top) {
                Text("🇮🇳 ", fontSize = 16.sp)
                Text(
                    outcome.objectiveHindi,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(Modifier.height(6.dp))
            // Santali Ol Chiki
            Row(verticalAlignment = Alignment.Top) {
                Text("📜 ", fontSize = 16.sp)
                Column {
                    Text(
                        outcome.objectiveSantali,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        outcome.objectiveLatin,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            ProvenanceBadge(provenance = outcome.provenance)
        }
    }
}

@Composable
private fun ActivityPreviewCard(activity: ClassroomActivity) {
    val typeIcon = when (activity.type) {
        ActivityType.NUMBER_SELECTION -> "🔢"
        ActivityType.PICTURE_SELECTION -> "🖼️"
        ActivityType.MATCHING -> "🔗"
        ActivityType.YES_NO -> "✅"
        ActivityType.CHORAL_RESPONSE -> "🎵"
        ActivityType.ORDERING -> "📊"
        ActivityType.RECOGNITION_TAP -> "👆"
    }

    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(typeIcon, fontSize = 28.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    activity.objectiveHindi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "${activity.items.size} प्रश्न",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        "⏱ ${activity.estimatedMinutes} मिनट",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
private fun PhrasePlanCard(phraseIds: List<String>) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "इस पाठ में उपयोग करें",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "माइक बटन दबाएं और हिंदी में बोलें — ऐप सांताली में अनुवाद करेगा",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${phraseIds.size} वाक्यांश सुझाव उपलब्ध",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ContentDisclaimerCard(provenance: ContentProvenance) {
    if (provenance == ContentProvenance.VERIFIED) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF8E1)
        )
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text("⚠️", fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                "यह सामग्री मूल वक्ता द्वारा अभी तक सत्यापित नहीं है। " +
                "इसे कक्षा में उपयोग से पहले स्थानीय भाषा विशेषज्ञ से जांच करवाएं।",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF5D4037)
            )
        }
    }
}

@Composable
private fun InfoChip(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}

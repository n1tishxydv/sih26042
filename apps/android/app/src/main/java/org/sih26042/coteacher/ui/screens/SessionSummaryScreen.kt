package org.sih26042.coteacher.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.domain.lesson.LessonEngine
import java.text.SimpleDateFormat
import java.util.*

/**
 * PHASE 5 — SessionSummaryScreen
 *
 * End-of-lesson teacher summary:
 *  - Which activities were completed and their scores
 *  - Translation usage breakdown: verified vs. machine-generated (provenance integrity)
 *  - Session duration
 *  - Next suggested lesson
 *
 * DESIGN PRINCIPLE:
 *  Teachers see exactly what provenance mix was used in their session.
 *  Machine-generated counts are never hidden — this preserves educator trust.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSummaryScreen(
    lessonEngine: LessonEngine,
    onStartNextLesson: (String) -> Unit,
    onGoHome: () -> Unit
) {
    val summary by lessonEngine.sessionSummary.collectAsState()
    val session by lessonEngine.session.collectAsState()
    val lesson by lessonEngine.currentLesson.collectAsState()

    val accentColor = lesson?.let { domainColor(it.domain) } ?: MaterialTheme.colorScheme.primary

    if (summary == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val sum = summary!!
    val ses = session

    val durationMs = if (ses?.endTimeMs != null && ses.startTimeMs > 0)
        ses.endTimeMs - ses.startTimeMs
    else 0L
    val durationMin = (durationMs / 60000).toInt()
    val durationSec = ((durationMs % 60000) / 1000).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "पाठ सारांश",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accentColor,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero
            item {
                SummaryHeroCard(
                    summary = sum,
                    durationMin = durationMin,
                    durationSec = durationSec,
                    accentColor = accentColor
                )
            }

            // Activity results
            item {
                Text(
                    "गतिविधि परिणाम",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(sum.activitiesSummary) { actSummary ->
                ActivityResultCard(actSummary = actSummary)
            }

            // Translation provenance
            if (ses != null && ses.translationsUsed > 0) {
                item {
                    TranslationProvenanceCard(session = ses)
                }
            }

            // Next lesson suggestion
            if (sum.nextSuggestedLessonId != null) {
                item {
                    NextLessonCard(
                        lessonId = sum.nextSuggestedLessonId,
                        lessonTitle = sum.nextSuggestedLessonTitle ?: "अगला पाठ",
                        accentColor = accentColor,
                        onStart = { onStartNextLesson(sum.nextSuggestedLessonId) }
                    )
                }
            }

            // Home button
            item {
                OutlinedButton(
                    onClick = onGoHome,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("🏠", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text("होम पर जाएं")
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SummaryHeroCard(
    summary: SessionSummary,
    durationMin: Int,
    durationSec: Int,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎓", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                summary.lessonTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = accentColor
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBlock(
                    value = "${summary.completedActivities}/${summary.totalActivities}",
                    label = "गतिविधियाँ पूरी"
                )
                StatBlock(
                    value = if (durationMin > 0) "${durationMin}m ${durationSec}s" else "${durationSec}s",
                    label = "समय लगा"
                )
                StatBlock(
                    value = "${summary.session.translationsUsed}",
                    label = "अनुवाद"
                )
            }
        }
    }
}

@Composable
private fun StatBlock(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ActivityResultCard(actSummary: ActivitySummary) {
    val score = actSummary.score
    val scoreColor = when {
        score >= 0.8f -> Color(0xFF2E7D32)
        score >= 0.5f -> Color(0xFFF57F17)
        else -> Color(0xFFB71C1C)
    }

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    actSummary.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${actSummary.correctItems} / ${actSummary.totalItems} सही",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { score },
                    modifier = Modifier.fillMaxWidth(),
                    color = scoreColor,
                    trackColor = scoreColor.copy(alpha = 0.2f)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                "${(score * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
        }
    }
}

@Composable
private fun TranslationProvenanceCard(session: ClassSession) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "अनुवाद विवरण",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            ProvenanceRow(
                icon = "✅",
                label = "सत्यापित वाक्यांश",
                count = session.verifiedTranslations,
                color = Color(0xFF2E7D32)
            )
            Spacer(Modifier.height(6.dp))
            ProvenanceRow(
                icon = "🤖",
                label = "AI-जनित (MT)",
                count = session.machineGeneratedTranslations,
                color = Color(0xFF1565C0)
            )
            if (session.machineGeneratedTranslations > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "⚠️ AI-जनित अनुवाद सत्यापित नहीं हैं। उपयोग से पहले भाषा विशेषज्ञ से जाँच करें।",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5D4037)
                )
            }
        }
    }
}

@Composable
private fun ProvenanceRow(icon: String, label: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Surface(
            color = color.copy(alpha = 0.12f),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                "$count",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun NextLessonCard(
    lessonId: String,
    lessonTitle: String,
    accentColor: Color,
    onStart: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = accentColor.copy(alpha = 0.06f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "अगला सुझाव",
                style = MaterialTheme.typography.labelMedium,
                color = accentColor,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                lessonTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("अगला पाठ शुरू करें →")
            }
        }
    }
}

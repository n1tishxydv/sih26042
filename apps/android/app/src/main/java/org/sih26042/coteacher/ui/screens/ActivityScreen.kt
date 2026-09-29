package org.sih26042.coteacher.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.domain.lesson.LessonEngine

/**
 * PHASE 5 — ActivityScreen
 *
 * Drives the teacher through each classroom activity using the ActivityState machine:
 *
 *   Idle → (advanceToNextActivity) → Intro → (beginActivity) → Instruction
 *   → (presentItem) → AwaitingResponse → (submitResponse) → Feedback
 *   → (advanceItem) → [next Instruction | Complete]
 *   → [next activity or session complete]
 *
 * DESIGN PRINCIPLE:
 *   Teacher reads the instruction aloud in Santali; children respond.
 *   The screen shows one item at a time — no cognitive overload.
 *   For CHORAL_RESPONSE activities, the teacher taps "सभी ने बोला" (All spoke).
 *   For YES_NO and NUMBER_SELECTION, children tap the big buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    lessonEngine: LessonEngine,
    onLessonComplete: () -> Unit,
    onBack: () -> Unit
) {
    val lesson by lessonEngine.currentLesson.collectAsState()
    val activity by lessonEngine.currentActivity.collectAsState()
    val state by lessonEngine.activityState.collectAsState()
    val session by lessonEngine.session.collectAsState()

    val accentColor = lesson?.let { domainColor(it.domain) } ?: MaterialTheme.colorScheme.primary

    // On first entry, start the first activity
    LaunchedEffect(Unit) {
        if (state is ActivityState.Idle) {
            val hasMore = lessonEngine.advanceToNextActivity()
            if (!hasMore) onLessonComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            activity?.title ?: lesson?.title ?: "गतिविधि",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            lesson?.gradeLevel?.displayLabel ?: "",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("✕", fontSize = 18.sp, color = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = accentColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(tween(300)) + slideInHorizontally(tween(300)) { it / 4 } togetherWith
                    fadeOut(tween(200))
                }
            ) { currentState ->
                when (currentState) {
                    is ActivityState.Idle -> LoadingState()

                    is ActivityState.Intro -> activity?.let { act ->
                        ActivityIntroPanel(
                            activity = act,
                            accentColor = accentColor,
                            onBegin = { lessonEngine.beginActivity() }
                        )
                    }

                    is ActivityState.Instruction -> {
                        InstructionPanel(
                            item = currentState.item,
                            activity = activity,
                            accentColor = accentColor,
                            onPresent = { lessonEngine.presentItem() },
                            onSkip = { lessonEngine.skipItem() }
                        )
                    }

                    is ActivityState.AwaitingResponse -> {
                        ResponsePanel(
                            item = currentState.item,
                            activity = activity,
                            accentColor = accentColor,
                            lessonEngine = lessonEngine
                        )
                    }

                    is ActivityState.Feedback -> {
                        FeedbackPanel(
                            item = currentState.item,
                            wasCorrect = currentState.wasCorrect,
                            response = currentState.response,
                            accentColor = accentColor,
                            onNext = { lessonEngine.advanceItem() }
                        )
                    }

                    is ActivityState.Complete -> {
                        ActivityCompletePanel(
                            onNextActivity = {
                                val hasMore = lessonEngine.advanceToNextActivity()
                                if (!hasMore) onLessonComplete()
                            },
                            onFinish = onLessonComplete,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Sub-panels
// ---------------------------------------------------------------------------

@Composable
private fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ActivityIntroPanel(
    activity: ClassroomActivity,
    accentColor: Color,
    onBegin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        val typeIcon = activityTypeIcon(activity.type)
        Text(typeIcon, fontSize = 64.sp)

        Text(
            activity.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "उद्देश्य",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    activity.objectiveHindi,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    activity.objectiveSantali,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    activity.objectiveLatin,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = {},
                label = { Text("${activity.items.size} प्रश्न", fontSize = 12.sp) }
            )
            AssistChip(
                onClick = {},
                label = { Text("⏱ ${activity.estimatedMinutes} मिनट", fontSize = 12.sp) }
            )
        }

        Button(
            onClick = onBegin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("▶", fontSize = 18.sp, color = Color.White)
            Spacer(Modifier.width(8.dp))
            Text("गतिविधि शुरू करें", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun InstructionPanel(
    item: ActivityItem,
    activity: ClassroomActivity?,
    accentColor: Color,
    onPresent: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Teacher instruction card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("👩‍🏫", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "शिक्षक पढ़ें:",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    item.promptHindi,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Santali translation card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = accentColor.copy(alpha = 0.06f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "सांताली में:",
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    item.promptSantali,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.promptLatin,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f)
            ) {
                Text("छोड़ें")
            }
            Button(
                onClick = onPresent,
                modifier = Modifier.weight(2f).height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("बच्चे जवाब दें", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ResponsePanel(
    item: ActivityItem,
    activity: ClassroomActivity?,
    accentColor: Color,
    lessonEngine: LessonEngine
) {
    val isChoral = activity?.type == ActivityType.CHORAL_RESPONSE

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Question display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    item.promptSantali,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    item.promptHindi,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }

        Spacer(Modifier.weight(0.2f))

        if (isChoral) {
            // Choral: teacher taps once all children have responded
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎵",
                    fontSize = 64.sp
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "सभी बच्चे एक साथ बोलें",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { lessonEngine.acknowledgeChoralItem() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Text("✅ सभी ने बोला", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (item.options.isNotEmpty()) {
            // Multiple choice: large tap targets
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item.options.forEach { option ->
                    Button(
                        onClick = { lessonEngine.submitResponse(option) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            option,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // No options: teacher observes and confirms
            Button(
                onClick = { lessonEngine.submitResponse(item.correctAnswer) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text("बच्चे ने सही जवाब दिया", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = { lessonEngine.submitResponse("__WRONG__") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("गलत जवाब — फिर प्रयास करें")
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun FeedbackPanel(
    item: ActivityItem,
    wasCorrect: Boolean,
    response: String,
    accentColor: Color,
    onNext: () -> Unit
) {
    val bgColor = if (wasCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val iconColor = if (wasCorrect) Color(0xFF2E7D32) else Color(0xFFB71C1C)
    val emoji = if (wasCorrect) "✅" else "❌"
    val message = if (wasCorrect) "शाबाश! 🎉" else "फिर कोशिश करें 💪"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Text(emoji, fontSize = 72.sp)

        Text(
            message,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = iconColor
        )

        if (!wasCorrect && item.correctAnswer.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "सही जवाब:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        item.correctAnswer,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("अगला →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
private fun ActivityCompletePanel(
    onNextActivity: () -> Unit,
    onFinish: () -> Unit,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
    ) {
        Text("🎊", fontSize = 72.sp)
        Text(
            "गतिविधि पूरी हुई!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            "सभी बच्चों को शाबाशी दें।",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onNextActivity,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = accentColor)
        ) {
            Text("अगली गतिविधि →", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onFinish,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("पाठ समाप्त करें")
        }
    }
}

// ------------------------------------------------------------------
// Utilities
// ------------------------------------------------------------------

fun activityTypeIcon(type: ActivityType): String = when (type) {
    ActivityType.NUMBER_SELECTION -> "🔢"
    ActivityType.PICTURE_SELECTION -> "🖼️"
    ActivityType.MATCHING -> "🔗"
    ActivityType.YES_NO -> "✅❌"
    ActivityType.CHORAL_RESPONSE -> "🎵"
    ActivityType.ORDERING -> "📊"
    ActivityType.RECOGNITION_TAP -> "👆"
}

package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.core.model.StudentActivityItem
import org.sih26042.coteacher.di.AppContainer

@Composable
fun ActivitiesScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val activities by container.classroomRepository.activities.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                text = "Classroom Student Activities",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Interactive mother-tongue games, call-and-response, and choral rhythm exercises",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        items(activities) { activity ->
            ActivityCard(activity = activity, container = container)
        }
    }
}

@Composable
fun ActivityCard(
    activity: StudentActivityItem,
    container: AppContainer
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                color = Color(0xFFEDE7F6),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = activity.activityType.replace('_', ' ').uppercase(),
                    color = Color(0xFF5E35B1),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = activity.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Teacher Cue (Hindi):", fontSize = 11.sp, color = Color(0xFF757575))
            Text(text = activity.teacherPromptHindi, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Teacher Cue (Mother Tongue):", fontSize = 11.sp, color = Color(0xFF757575))
            Text(
                text = activity.teacherPromptNative,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Expected Student Choral Response:", fontSize = 11.sp, color = Color(0xFF757575))
            Text(
                text = activity.studentResponseNative,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32)
            )
            Text(
                text = activity.studentResponseTransliteration,
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                color = Color(0xFFF5F5F5),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎯 Objective: ${activity.pedagogicalObjective}",
                    fontSize = 11.sp,
                    color = Color(0xFF424242),
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            activity.audioPromptPath?.let { path ->
                Button(
                    onClick = { container.audioPlayerService.playAudio(path) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("🔊 PLAY TEACHER CUE AUDIO", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

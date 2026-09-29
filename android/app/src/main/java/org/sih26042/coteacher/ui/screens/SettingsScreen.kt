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
import org.sih26042.coteacher.di.AppContainer

@Composable
fun SettingsScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    var slowPlaybackEnabled by remember { mutableStateOf(false) }
    var neuralFallbackEnabled by remember { mutableStateOf(true) }
    var optInTelemetry by remember { mutableStateOf(false) }
    var cacheCleared by remember { mutableStateOf(false) }

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
                text = "Teacher Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Classroom audio preferences, offline mode controls, and privacy settings",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Classroom Audio Preferences
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "CLASSROOM AUDIO & SPEECH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Early Learner Slow Speed (0.75x)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Speaks mother-tongue phrases slower for Grade 1 children", fontSize = 11.sp, color = Color(0xFF757575))
                        }
                        Switch(
                            checked = slowPlaybackEnabled,
                            onCheckedChange = { slowPlaybackEnabled = it }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Enable Quantized Neural MT Fallback", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Translates unverified phrases when no match exists in pack", fontSize = 11.sp, color = Color(0xFF757575))
                        }
                        Switch(
                            checked = neuralFallbackEnabled,
                            onCheckedChange = { neuralFallbackEnabled = it }
                        )
                    }
                }
            }
        }

        // Offline Storage & Cache
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "OFFLINE STORAGE & CACHE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF757575)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Cached Audio & Model Buffers: 14.8 MB", fontSize = 13.sp, color = Color(0xFF424242))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { cacheCleared = true },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (cacheCleared) "✓ CACHE PURGED" else "PURGE TEMPORARY CACHE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Privacy & Data Sovereignty
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔒 Classroom Privacy Guarantee",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "100% on-device operation. Audio recordings of teacher and students are NEVER uploaded to any cloud server or third-party service.",
                        fontSize = 12.sp,
                        color = Color(0xFF1B5E20)
                    )
                }
            }
        }
    }
}

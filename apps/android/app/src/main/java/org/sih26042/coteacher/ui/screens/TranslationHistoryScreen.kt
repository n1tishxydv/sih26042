package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
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
import org.sih26042.coteacher.core.model.ContentProvenance
import org.sih26042.coteacher.di.AppContainer
import java.text.SimpleDateFormat
import java.util.*

/**
 * PHASE 6 — TranslationHistoryScreen
 *
 * Displays local, bounded translation history with audio playback, favorites,
 * and deletion capabilities.
 */
@Composable
fun TranslationHistoryScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val history by container.teacherMaterialRepository.history.collectAsState()
    val dateFormat = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

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
                    Text("← QUICK TRANSLATE", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (history.isNotEmpty()) {
                    TextButton(onClick = { container.teacherMaterialRepository.clearHistory() }) {
                        Text("इतिहास मिटाएँ (Clear)", color = Color(0xFFD32F2F), fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text(
                text = "📜 अनुवाद इतिहास (Translation History)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "स्थानीय डिवाइस पर सुरक्षित किए गए हालिया अनुवाद (${history.size})",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        if (history.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📭", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("कोई अनुवाद इतिहास नहीं है", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("त्वरित अनुवाद का उपयोग करने पर यहाँ इतिहास दिखेगा।", fontSize = 11.sp, color = Color(0xFF757575))
                    }
                }
            }
        } else {
            items(history, key = { it.id }) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateFormat.format(Date(item.timestampMs)),
                                fontSize = 10.sp,
                                color = Color(0xFF757575)
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Provenance Badge
                                val isVerified = (item.provenance == ContentProvenance.VERIFIED)
                                Surface(
                                    color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = if (isVerified) "VERIFIED" else "MACHINE_GEN",
                                        color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                // Favorite Toggle
                                Text(
                                    text = if (item.isFavorite) "★" else "☆",
                                    fontSize = 18.sp,
                                    color = if (item.isFavorite) Color(0xFFFFB300) else Color(0xFF9E9E9E),
                                    modifier = Modifier.clickable {
                                        container.teacherMaterialRepository.toggleFavoriteHistory(item.id)
                                    }
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // Hindi Source
                        Text(
                            text = item.sourceHindi,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF212121)
                        )

                        Spacer(Modifier.height(2.dp))

                        // Santali Target
                        Text(
                            text = item.targetSantali,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFF004D40)
                        )

                        if (item.targetLatin.isNotBlank()) {
                            Text(
                                text = item.targetLatin,
                                fontSize = 11.sp,
                                color = Color(0xFF616161)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.audioPath != null) {
                                OutlinedButton(
                                    onClick = { container.audioPlayerService.playAudio(item.audioPath) },
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("🔊 ऑडियो", fontSize = 11.sp)
                                }
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }

                            TextButton(
                                onClick = { container.teacherMaterialRepository.deleteHistoryItem(item.id) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("हटाएँ", color = Color(0xFFD32F2F), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

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
import org.sih26042.coteacher.core.model.ContentProvenance
import org.sih26042.coteacher.core.model.SearchResultItem
import org.sih26042.coteacher.core.model.SearchResultType
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — LocalSearchScreen
 *
 * Offline normalized search engine searching phrases, vocabulary, lessons,
 * worksheets, flashcards, and teacher-saved materials.
 */
@Composable
fun LocalSearchScreen(
    initialQuery: String,
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var selectedFilterType by remember { mutableStateOf<SearchResultType?>(null) }
    var searchResults by remember { mutableStateOf<List<SearchResultItem>>(emptyList()) }

    // Execute search when query or filter changes
    LaunchedEffect(searchQuery, selectedFilterType) {
        if (searchQuery.isNotBlank()) {
            searchResults = container.localSearchService.search(
                rawQuery = searchQuery,
                filterType = selectedFilterType
            )
        } else {
            searchResults = emptyList()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Nav
        item {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← BACK", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "🔍 स्थानीय ऑफ़लाइन खोज (Local Search)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "मुहावरे, शब्दावली, पाठ, वर्कशीट और सुरक्षित सामग्री में खोजें",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Search Input
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("खोजें: गिनो, नमस्ते, आँख, पानी, आदि...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }
            }
        }

        // Filter chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = (selectedFilterType == null),
                        onClick = { selectedFilterType = null },
                        label = { Text("ALL (${searchResults.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                items(SearchResultType.values()) { type ->
                    val isSelected = (selectedFilterType == type)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilterType = if (isSelected) null else type },
                        label = { Text(type.label, fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (searchQuery.isBlank()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("खोजने के लिए ऊपर शब्द टाइप करें", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("हिंदी या संताली (ओल चिकी) दोनों लिपियों में खोज समर्थित है।", fontSize = 11.sp, color = Color(0xFF757575))
                    }
                }
            }
        } else if (searchResults.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📭", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("कोई परिणाम नहीं मिला ('$searchQuery')", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("कृपया अन्य शब्द या वर्तनी आज़माएँ।", fontSize = 11.sp, color = Color(0xFF757575))
                    }
                }
            }
        } else {
            items(searchResults, key = { "${it.type}_${it.id}" }) { item ->
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
                            Surface(
                                color = Color(item.type.badgeColorHex),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = item.type.label.uppercase(),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            val isVerified = (item.provenance == ContentProvenance.VERIFIED)
                            Surface(
                                color = if (isVerified) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isVerified) "VERIFIED" else "TEACHER_CREATED",
                                    color = if (isVerified) Color(0xFF2E7D32) else Color(0xFFE65100),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = item.nativeScript,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF004D40)
                        )

                        if (item.latinTransliteration.isNotBlank()) {
                            Text(
                                text = item.latinTransliteration,
                                fontSize = 11.sp,
                                color = Color(0xFF616161)
                            )
                        }

                        if (item.audioPath != null) {
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { container.audioPlayerService.playAudio(item.audioPath) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("🔊 ऑडियो बजाएँ", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

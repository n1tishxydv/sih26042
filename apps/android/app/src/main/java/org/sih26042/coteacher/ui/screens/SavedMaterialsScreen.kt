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
import org.sih26042.coteacher.CreateMaterialDest
import org.sih26042.coteacher.core.model.MaterialType
import org.sih26042.coteacher.core.model.TeacherMaterial
import org.sih26042.coteacher.di.AppContainer
import java.text.SimpleDateFormat
import java.util.*

/**
 * PHASE 6 — SavedMaterialsScreen
 *
 * Local persistence management for teacher-created materials:
 * - Filter by MaterialType
 * - Duplicate, Delete, Favorite
 * - Clear teacher provenance badges: TEACHER_CREATED
 */
@Composable
fun SavedMaterialsScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val materials by container.teacherMaterialRepository.materials.collectAsState()
    var selectedTypeFilter by remember { mutableStateOf<MaterialType?>(null) }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val filteredMaterials = remember(selectedTypeFilter, materials) {
        if (selectedTypeFilter == null) materials else materials.filter { it.type == selectedTypeFilter }
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
                    Text("← TOOLKIT", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigate(CreateMaterialDest) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+ नई सामग्री (New)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Text(
                text = "📁 सुरक्षित शिक्षण सामग्री (Saved Materials)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "आपके द्वारा बनाई गई या सुरक्षित की गई सामग्री (${materials.size})",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Material Type Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = (selectedTypeFilter == null),
                        onClick = { selectedTypeFilter = null },
                        label = { Text("ALL (${materials.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                items(MaterialType.values()) { type ->
                    val count = materials.count { it.type == type }
                    FilterChip(
                        selected = (selectedTypeFilter == type),
                        onClick = { selectedTypeFilter = type },
                        label = { Text("${type.displayName} ($count)", fontSize = 11.sp) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (filteredMaterials.isEmpty()) {
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
                        Text("📂", fontSize = 32.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("कोई सामग्री नहीं मिली", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("नई सामग्री बनाने के लिए ऊपर '+ नई सामग्री' बटन दबाएँ।", fontSize = 11.sp, color = Color(0xFF757575))
                    }
                }
            }
        } else {
            items(filteredMaterials, key = { it.id }) { mat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(mat.type.icon, fontSize = 20.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = mat.type.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF004D40)
                                )
                            }

                            // Favorite Toggle
                            Text(
                                text = if (mat.isFavorite) "★" else "☆",
                                fontSize = 20.sp,
                                color = if (mat.isFavorite) Color(0xFFFFB300) else Color(0xFF9E9E9E),
                                modifier = Modifier.clickable {
                                    container.teacherMaterialRepository.toggleFavoriteMaterial(mat.id)
                                }
                            )
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = mat.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )

                        if (mat.notes.isNotBlank()) {
                            Spacer(Modifier.height(2.dp))
                            Text(mat.notes, fontSize = 12.sp, color = Color(0xFF616161))
                        }

                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${mat.gradeLevel.displayLabel} • ${dateFormat.format(Date(mat.createdAtMs))} • v${mat.version}",
                                fontSize = 10.sp,
                                color = Color(0xFF757575)
                            )

                            // Clear Provenance Badge
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

                        Spacer(Modifier.height(10.dp))
                        Divider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(8.dp))

                        // Actions: Duplicate, Delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { container.teacherMaterialRepository.duplicateMaterial(mat.id) }
                            ) {
                                Text("प्रतिलिपि (Duplicate)", fontSize = 11.sp, color = Color(0xFF004D40))
                            }
                            Spacer(Modifier.width(8.dp))
                            TextButton(
                                onClick = { container.teacherMaterialRepository.deleteMaterial(mat.id) }
                            ) {
                                Text("हटाएँ (Delete)", fontSize = 11.sp, color = Color(0xFFD32F2F))
                            }
                        }
                    }
                }
            }
        }
    }
}

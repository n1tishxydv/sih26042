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
import org.sih26042.coteacher.*
import org.sih26042.coteacher.core.model.MaterialType
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — Teacher Toolkit Hub
 *
 * Dedicated teacher-first workspace:
 * - Quick Translate (Hindi -> Santali)
 * - Saved Materials & Authoring
 * - FLN Flashcards & Custom Decks
 * - NIPUN Worksheets & Generator
 * - PDF & Image Import / OCR Review
 * - Classroom Quick Tools
 * - Offline Local Search
 */
@Composable
fun TeacherToolkitScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val materials by container.teacherMaterialRepository.materials.collectAsState()
    val history by container.teacherMaterialRepository.history.collectAsState()
    val activePack by container.languagePackRepository.activePack.collectAsState()

    val recentMaterials = remember(materials) { materials.take(5) }
    val favoriteMaterials = remember(materials) { materials.filter { it.isFavorite } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
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
                    Text("← DASHBOARD", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "● 100% OFFLINE",
                        color = Color(0xFF2E7D32),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Header Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF004D40)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🧰 शिक्षक टूलकिट (Teacher Toolkit)",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "पाठ्य सामग्री तैयार करें, अनुवाद करें और ऑफ़लाइन उपयोग करें",
                        color = Color(0xFFB2DFDB),
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Search Bar button
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(LocalSearchDest()) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🔍", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "खोजें: पाठ, मुहावरे, शब्दकोश, वर्कशीट...",
                                color = Color(0xFFE0F2F1),
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // Quick Tools Hero Grid (Section 3 of specs)
        item {
            Text(
                text = "Classroom Utilities & Tools",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolkitTile(
                    title = "त्वरित अनुवाद",
                    subtitle = "Quick Translate",
                    icon = "💬",
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(QuickTranslateDest) }

                ToolkitTile(
                    title = "कक्षा उपकरण",
                    subtitle = "Quick Tools",
                    icon = "🔢",
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(ClassroomQuickToolsDest) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolkitTile(
                    title = "फ्लैशकार्ड डेक",
                    subtitle = "Flashcards",
                    icon = "🎴",
                    color = Color(0xFFEDE7F6),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(FlashcardDecksDest) }

                ToolkitTile(
                    title = "वर्कशीट निर्माता",
                    subtitle = "Worksheet Builder",
                    icon = "📝",
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(WorksheetBuilderDest) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolkitTile(
                    title = "चित्र आयात / OCR",
                    subtitle = "Import Image",
                    icon = "📷",
                    color = Color(0xFFFCE4EC),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(ImportImageDest) }

                ToolkitTile(
                    title = "PDF दस्तावेज़",
                    subtitle = "Import PDF",
                    icon = "📄",
                    color = Color(0xFFE0F7FA),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(ImportPdfDest) }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToolkitTile(
                    title = "सुरक्षित सामग्री",
                    subtitle = "Saved Materials",
                    icon = "📁",
                    color = Color(0xFFF1F8E9),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(SavedMaterialsDest) }

                ToolkitTile(
                    title = "नई सामग्री बनाएँ",
                    subtitle = "Create Content",
                    icon = "✍️",
                    color = Color(0xFFFFF8E1),
                    modifier = Modifier.weight(1f)
                ) { onNavigate(CreateMaterialDest) }
            }
        }

        // Recent Teaching Materials
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Teaching Materials (${recentMaterials.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121)
                )
                if (recentMaterials.isNotEmpty()) {
                    Text(
                        text = "View All →",
                        fontSize = 12.sp,
                        color = Color(0xFF004D40),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigate(SavedMaterialsDest) }
                    )
                }
            }
        }

        if (recentMaterials.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📂", fontSize = 28.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("अभी कोई सुरक्षित सामग्री नहीं है", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            "त्वरित अनुवाद, वर्कशीट या फ्लैशकार्ड बनाकर यहाँ सुरक्षित करें।",
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
            }
        } else {
            items(recentMaterials) { mat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(SavedMaterialsDest) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mat.type.icon, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mat.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                "${mat.type.displayName} • ${mat.gradeLevel.displayLabel}",
                                fontSize = 11.sp,
                                color = Color(0xFF616161)
                            )
                        }
                        Surface(
                            color = Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(6.dp)
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
                }
            }
        }
    }
}

@Composable
fun ToolkitTile(
    title: String,
    subtitle: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF212121))
            Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF555555))
        }
    }
}

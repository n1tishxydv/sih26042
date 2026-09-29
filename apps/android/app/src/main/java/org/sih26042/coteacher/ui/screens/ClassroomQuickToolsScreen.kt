package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — ClassroomQuickToolsScreen
 *
 * Lightweight classroom utilities driven by verified language pack assets:
 * - Counting helper (1-10 with Ol Chiki numerals ᱑-᱑᱐ and audio)
 * - Letter cards (Ol Chiki alphabet ᱚ ᱛ ᱜ ᱝ ᱞ...)
 * - Shape cards
 * - Color cards
 * - Body parts
 * - Repeat-after-me pronunciation trainer
 */
@Composable
fun ClassroomQuickToolsScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val flnVocab by container.classroomRepository.flnVocabulary.collectAsState()
    val toolTabs = listOf("गिनती (1-10)", "वर्णमाला (Letters)", "आकार (Shapes)", "रंग (Colors)", "शरीर के अंग (Body)")
    var selectedTab by remember { mutableStateOf(0) }

    val numbersList = remember {
        listOf(
            Triple("1 (एक)", "ᱢᱤᱫ (᱑)", "fln_num_01.wav"),
            Triple("2 (दो)", "ᱵᱟᱨ (᱒)", "fln_num_02.wav"),
            Triple("3 (तीन)", "ᱯᱮ (᱓)", "fln_num_03.wav"),
            Triple("4 (चार)", "ᱯᱳᱱ (᱔)", "fln_num_04.wav"),
            Triple("5 (पाँच)", "ᱢᱚᱬᱮ (᱕)", "fln_num_05.wav"),
            Triple("6 (छह)", "ᱛᱩᱨᱩᱭ (᱖)", "fln_num_06.wav"),
            Triple("7 (सात)", "ᱮᱭᱟᱭ (᱗)", "fln_num_07.wav"),
            Triple("8 (आठ)", "ᱤᱨᱟᱹᱞ (᱘)", "fln_num_08.wav"),
            Triple("9 (नौ)", "ᱟᱨᱮ (᱙)", "fln_num_09.wav"),
            Triple("10 (दस)", "ᱜᱮᱞ (᱑᱐)", "fln_num_10.wav")
        )
    }

    val olChikiAlphabet = remember {
        listOf(
            Pair("ᱚ", "LA (ᱚ)"), Pair("ᱛ", "AT (ᱛ)"), Pair("ᱜ", "AG (ᱜ)"), Pair("ᱝ", "ANG (ᱝ)"),
            Pair("ᱞ", "AL (ᱞ)"), Pair("ᱟ", "LAA (ᱟ)"), Pair("ᱠ", "AAK (ᱠ)"), Pair("ᱡ", "AAJ (ᱡ)"),
            Pair("ᱢ", "AAM (ᱢ)"), Pair("ᱣ", "AAW (ᱣ)"), Pair("ᱤ", "LI (ᱤ)"), Pair("ᱥ", "IS (ᱥ)"),
            Pair("ᱦ", "IH (ᱦ)"), Pair("ᱧ", "INY (ᱧ)"), Pair("ᱨ", "IR (ᱨ)"), Pair("ᱩ", "LU (ᱩ)")
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Nav
        item {
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEEEEEE)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("← TOOLKIT", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Text(
                text = "🔢 कक्षा त्वरित उपकरण (Classroom Quick Tools)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "कक्षा में तुरंत उपयोग के लिए डिजिटल चार्ट्स और उच्चारण सहायक",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(toolTabs.indices.toList()) { idx ->
                    FilterChip(
                        selected = (selectedTab == idx),
                        onClick = { selectedTab = idx },
                        label = { Text(toolTabs[idx], fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Content based on tab
        when (selectedTab) {
            0 -> {
                // Counting Helper
                items(numbersList) { (hi, sat, audio) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(hi, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF212121))
                                Text(sat, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF004D40))
                            }

                            Button(
                                onClick = { container.audioPlayerService.playAudio(audio) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("🔊 उच्चारण", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            1 -> {
                // Alphabet Cards
                item {
                    Text("ओल चिकी वर्णमाला (Ol Chiki Script Letters):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        olChikiAlphabet.chunked(4).forEach { rowLetters ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowLetters.forEach { (char, name) ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        shape = RoundedCornerShape(10.dp),
                                        elevation = CardDefaults.cardElevation(2.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(char, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color(0xFF004D40))
                                            Spacer(Modifier.height(4.dp))
                                            Text(name, fontSize = 10.sp, color = Color(0xFF616161))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // Vocab categories: shapes, colors, body
                val categoryName = when (selectedTab) {
                    2 -> "classroom_objects"
                    3 -> "colors"
                    else -> "body_parts"
                }
                val itemsForCat = flnVocab.filter { it.category == categoryName }

                if (itemsForCat.isEmpty()) {
                    item {
                        Text("इस श्रेणी के शब्द लोड हो रहे हैं...", color = Color.Gray)
                    }
                } else {
                    items(itemsForCat) { v ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(v.hindiWord, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(v.targetNativeScript, fontSize = 18.sp, color = Color(0xFF004D40), fontWeight = FontWeight.Bold)
                                    Text(v.targetTransliteration, fontSize = 11.sp, color = Color(0xFF757575))
                                }

                                if (v.audioPath != null) {
                                    OutlinedButton(
                                        onClick = { container.audioPlayerService.playAudio(v.audioPath) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("🔊 सुनें", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

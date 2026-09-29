package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.sih26042.coteacher.di.AppContainer

enum class ValidatorStatusFilter {
    ALL, PENDING, APPROVED, REJECTED
}

data class ReviewablePhrase(
    val id: String,
    val hindi: String,
    val olChiki: String,
    val latin: String,
    val category: String,
    val dialect: String,
    var status: String,
    val hasAudio: Boolean,
    var notes: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ValidatorScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf(ValidatorStatusFilter.ALL) }
    var selectedDialect by remember { mutableStateOf("ALL") }

    // Mock initial state representing real validation catalog
    val phraseList = remember {
        mutableStateListOf(
            ReviewablePhrase(
                id = "ph_sit_down_01",
                hindi = "बैठ जाओ",
                olChiki = "ᱫᱩᱲᱩᱵ ᱢᱮ",
                latin = "durb me",
                category = "CLASSROOM_MANAGEMENT",
                dialect = "mayurbhanj",
                status = "APPROVED",
                hasAudio = true,
                notes = "Audio verified against Mayurbhanj phonology. Native educator approved."
            ),
            ReviewablePhrase(
                id = "ph_stand_up_02",
                hindi = "खड़े हो जाओ",
                olChiki = "ᱛᱤᱸᱜᱩᱱ ᱢᱮ",
                latin = "tingun me",
                category = "CLASSROOM_MANAGEMENT",
                dialect = "generic_santali",
                status = "PENDING",
                hasAudio = true,
                notes = ""
            ),
            ReviewablePhrase(
                id = "ph_come_here_03",
                hindi = "यहाँ आओ",
                olChiki = "ᱱᱚᱰᱮ ᱦᱤᱡᱩᱜ ᱢᱮ",
                latin = "node hijug me",
                category = "CLASSROOM_MANAGEMENT",
                dialect = "santhal_pargana",
                status = "PENDING",
                hasAudio = false,
                notes = "Awaiting studio WAV recording."
            ),
            ReviewablePhrase(
                id = "ph_open_book_04",
                hindi = "किताब खोलो",
                olChiki = "ᱯᱩᱛᱷᱤ ᱡᱷᱤᱡᱽ ᱢᱮ",
                latin = "puthi jhij me",
                category = "TEACHING_INSTRUCTION",
                dialect = "generic_santali",
                status = "PENDING",
                hasAudio = false,
                notes = ""
            )
        )
    }

    val filteredList = phraseList.filter {
        val matchesFilter = when (selectedFilter) {
            ValidatorStatusFilter.ALL -> true
            ValidatorStatusFilter.PENDING -> it.status == "PENDING"
            ValidatorStatusFilter.APPROVED -> it.status == "APPROVED"
            ValidatorStatusFilter.REJECTED -> it.status == "REJECTED"
        }
        val matchesDialect = if (selectedDialect == "ALL") true else it.dialect.equals(selectedDialect, ignoreCase = true)
        matchesFilter && matchesDialect
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Santali Linguistic Validator Mode", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text("Protected Educator Review Interface (Internal Only)", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("← BACK", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F6F9))
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "⚖️ Linguistic Approval Rule Notice",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "A phrase is promoted to runtime VERIFIED ONLY when: Source Hindi, Ol Chiki script, intent, audio WAV, and reviewer identity are verified. Missing items remain PENDING.",
                            fontSize = 11.sp,
                            color = Color(0xFFBF360C)
                        )
                    }
                }
            }

            // Dialect & Status Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ValidatorStatusFilter.ALL to "All (${phraseList.size})",
                        ValidatorStatusFilter.PENDING to "Pending (${phraseList.count { it.status == "PENDING" }})",
                        ValidatorStatusFilter.APPROVED to "Approved (${phraseList.count { it.status == "APPROVED" }})",
                        ValidatorStatusFilter.REJECTED to "Rejected (${phraseList.count { it.status == "REJECTED" }})"
                    ).forEach { (filter, label) ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(label, fontSize = 11.sp) }
                        )
                    }
                }
            }

            items(filteredList) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
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
                                text = item.id,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            val statusColor = when (item.status) {
                                "APPROVED" -> Color(0xFF2E7D32)
                                "REJECTED" -> Color(0xFFC62828)
                                else -> Color(0xFFF57C00)
                            }
                            Text(
                                text = item.status,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Hindi: ${item.hindi}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = "Ol Chiki: ${item.olChiki}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = "Latin: ${item.latin}",
                            fontSize = 12.sp,
                            color = Color(0xFF555555)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Dialect: ${item.dialect}", fontSize = 10.sp) }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(if (item.hasAudio) "Audio: Available" else "Audio: Missing", fontSize = 10.sp) }
                            )
                        }

                        if (item.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Note: ${item.notes}",
                                fontSize = 11.sp,
                                color = Color(0xFF616161)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { item.status = "APPROVED" },
                                enabled = item.hasAudio,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("APPROVE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { item.status = "REJECTED" },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("REJECT", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

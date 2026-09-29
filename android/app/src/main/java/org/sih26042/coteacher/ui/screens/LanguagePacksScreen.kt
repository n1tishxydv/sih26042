package org.sih26042.coteacher.ui.screens

import androidx.compose.foundation.background
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
import org.sih26042.coteacher.core.model.LanguagePackInfo
import org.sih26042.coteacher.di.AppContainer

@Composable
fun LanguagePacksScreen(
    container: AppContainer,
    onBack: () -> Unit
) {
    val availablePacks by container.managePacksUseCase.availablePacks.collectAsState()
    val activePack by container.managePacksUseCase.activePack.collectAsState()

    var verificationStatus by remember { mutableStateOf<String?>(null) }

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
                text = "Language Packs Manager",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A237E)
            )
            Text(
                text = "Manage offline language packs, switch active classroom language, and verify cryptographic checksums",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        if (verificationStatus != null) {
            item {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = verificationStatus ?: "",
                        color = Color(0xFF2E7D32),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        items(availablePacks) { pack ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${pack.languageName} (${pack.nativeName})",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF212121)
                            )
                            Text(
                                text = "Code: ${pack.languageCode} | Script: ${pack.primaryScript} | v${pack.version}",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                        }

                        if (pack.isActive) {
                            Surface(
                                color = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "Phrases: ${pack.phrasesCount}", fontSize = 12.sp, color = Color(0xFF424242))
                        Text(text = "FLN Vocab: ${pack.flnVocabCount}", fontSize = 12.sp, color = Color(0xFF424242))
                        Text(text = "Worksheets: ${pack.worksheetsCount}", fontSize = 12.sp, color = Color(0xFF424242))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "SHA-256: ${pack.sha256Checksum.take(24)}...",
                        fontSize = 10.sp,
                        color = Color(0xFF9E9E9E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!pack.isActive) {
                            Button(
                                onClick = {
                                    container.managePacksUseCase.switchPack(pack.packId)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A237E)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("SET AS ACTIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val valid = container.managePacksUseCase.verifyIntegrity(pack.packId)
                                verificationStatus = if (valid) {
                                    "✓ Checksum verified for ${pack.languageName}: SHA-256 integrity intact."
                                } else {
                                    "✗ Checksum mismatch in ${pack.languageName} files!"
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = if (pack.isActive) Modifier.fillMaxWidth() else Modifier.weight(1f)
                        ) {
                            Text("VERIFY CHECKSUM", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Import Local Pack
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Install New Offline Pack (.slp)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5E35B1)
                    )
                    Text(
                        text = "Import verified language pack archives from SD Card or Bluetooth transfer",
                        fontSize = 11.sp,
                        color = Color(0xFF7E57C2)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            verificationStatus = "Ready to import .slp from device internal storage."
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5E35B1)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("SELECT .SLP ARCHIVE 📂", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

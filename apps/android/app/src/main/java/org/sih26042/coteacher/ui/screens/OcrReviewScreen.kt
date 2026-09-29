package org.sih26042.coteacher.ui.screens

import android.graphics.BitmapFactory
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
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.launch
import org.sih26042.coteacher.*
import org.sih26042.coteacher.core.model.*
import org.sih26042.coteacher.di.AppContainer
import java.io.File

/**
 * PHASE 6 — OcrReviewScreen
 *
 * Editable review workbench for extracted or entered text:
 * - Shows extracted text with full editing capability
 * - Translates into Santali Ol Chiki via local MT
 * - Saves as TeacherMaterial
 * - Navigates to Worksheet or Flashcard creation
 */
@Composable
fun OcrReviewScreen(
    imagePath: String?,
    initialText: String,
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var editableText by remember { mutableStateOf(initialText) }
    var translatedSantali by remember { mutableStateOf<String?>(null) }
    var translatedLatin by remember { mutableStateOf<String?>(null) }
    var isTranslating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Run honest OCR capability check if imagePath provided
    LaunchedEffect(imagePath) {
        if (imagePath != null && editableText.isBlank()) {
            val file = File(imagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    val res = container.offlineOcrService.processImage(bitmap, "Devanagari")
                    if (res.extractedText.isNotBlank()) {
                        editableText = res.extractedText
                    } else if (res.warningMessage != null) {
                        statusMessage = res.warningMessage
                    }
                    bitmap.recycle()
                }
            }
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
                text = "📝 पाठ समीक्षा एवं संपादन (Text Review & Edit)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "निकाले गए पाठ को संशोधित करें और अनुवाद या वर्कशीट बनाएँ",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        if (statusMessage != null) {
            item {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ️", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(statusMessage!!, color = Color(0xFFE65100), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Editable Area
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("समीक्षा हेतु पाठ (Extracted Text - Editable):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editableText,
                        onValueChange = { editableText = it },
                        placeholder = { Text("पाठ यहाँ टाइप या संपादित करें...") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(14.dp))

                    Button(
                        onClick = {
                            if (editableText.isNotBlank()) {
                                isTranslating = true
                                coroutineScope.launch {
                                    val res = container.orchestratedTranslationEngine.process(
                                        normalizedHindi = editableText.trim(),
                                        targetLanguageCode = "sat",
                                        asrLatencyMs = 0L
                                    )
                                    translatedSantali = res.outputNativeScript
                                    translatedLatin = res.outputTransliteration
                                    isTranslating = false
                                }
                            }
                        },
                        enabled = editableText.isNotBlank() && !isTranslating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isTranslating) "अनुवाद हो रहा है..." else "संताली में अनुवाद करें (Translate to Santali) ➔",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Translated Result
        if (translatedSantali != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("संताली अनुवाद (Ol Chiki):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "MACHINE_GENERATED",
                                    color = Color(0xFFE65100),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = translatedSantali!!,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D40)
                        )

                        if (translatedLatin != null && translatedLatin!!.isNotBlank()) {
                            Text(translatedLatin!!, fontSize = 13.sp, color = Color(0xFF616161))
                        }

                        Spacer(Modifier.height(14.dp))
                        Divider(color = Color(0xFFEEEEEE))
                        Spacer(Modifier.height(12.dp))

                        // Actions: Save as material, Create Worksheet, Create Flashcard
                        Text("आगे की क्रियाएँ (Next Actions):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val mat = TeacherMaterial(
                                        title = editableText.take(40),
                                        type = MaterialType.IMPORTED_PAGE,
                                        contentJson = """{"hindi":"$editableText","santali":"$translatedSantali"}""",
                                        notes = "Imported / OCR text"
                                    )
                                    container.teacherMaterialRepository.saveMaterial(mat)
                                    statusMessage = "सामग्री सुरक्षित की गई!"
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("💾 सुरक्षित करें", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { onNavigate(WorksheetBuilderDest) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("📝 वर्कशीट बनाएँ", color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

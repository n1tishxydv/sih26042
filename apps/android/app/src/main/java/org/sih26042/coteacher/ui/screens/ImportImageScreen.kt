package org.sih26042.coteacher.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import org.sih26042.coteacher.OcrReviewDest
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — ImportImageScreen
 *
 * Safe image picker for textbooks and teaching aids:
 * - Uses modern Android activity result contract
 * - Enforces memory safety and downsampling
 * - Navigates to OCR review area
 */
@Composable
fun ImportImageScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            val result = container.imageIngestionService.ingestImage(uri)
            isProcessing = false
            if (result.success && result.localFile != null) {
                onNavigate(OcrReviewDest(imagePath = result.localFile.absolutePath))
            } else {
                errorMessage = result.errorMessage ?: "छवि आयात करने में असमर्थ"
            }
        }
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
                text = "📷 चित्र आयात और पाठ्य समीक्षा (Image Import)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "पाठ्यपुस्तक पृष्ठ या शिक्षण सामग्री की तस्वीर से पाठ निकालें और अनुवाद करें",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

        // Action Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📸", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "डिवाइस से चित्र चुनें (Select Image)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "केवल स्थानीय डिवाइस पर प्रोसेस होता है। क्लाउड पर कोई डेटा नहीं भेजा जाता।",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        enabled = !isProcessing,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isProcessing) "चित्र प्रोसेस हो रहा है..." else "📁 गैलरी / फ़ाइल से चुनें (Browse Image)",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (errorMessage != null) {
                        Spacer(Modifier.height(12.dp))
                        Text("❌ $errorMessage", color = Color(0xFFD32F2F), fontSize = 12.sp)
                    }
                }
            }
        }

        // Direct Text Option
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(OcrReviewDest(imagePath = null)) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✏️", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("सीधे पाठ दर्ज करें (Enter Text Directly)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("बिना फोटो के सीधे पाठ लिखकर अनुवाद या वर्कशीट बनाएँ", fontSize = 11.sp, color = Color(0xFF555555))
                    }
                    Text("➔", color = Color(0xFF004D40), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

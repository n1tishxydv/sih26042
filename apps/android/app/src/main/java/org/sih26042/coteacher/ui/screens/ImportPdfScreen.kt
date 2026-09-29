package org.sih26042.coteacher.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import org.sih26042.coteacher.PdfViewerDest
import org.sih26042.coteacher.di.AppContainer

/**
 * PHASE 6 — ImportPdfScreen
 *
 * Safe PDF document ingestion with size bounds and page counting.
 */
@Composable
fun ImportPdfScreen(
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isIngesting by remember { mutableStateOf(false) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isIngesting = true
            val metadata = container.pdfIngestionService.ingestPdf(uri)
            isIngesting = false

            if (metadata.success && metadata.localFile != null) {
                onNavigate(PdfViewerDest(pdfPath = metadata.localFile.absolutePath))
            } else {
                errorMessage = metadata.errorMessage ?: "PDF लोड करने में असमर्थ"
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
                text = "📄 PDF दस्तावेज़ आयात (Import PDF)",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF004D40)
            )
            Text(
                text = "पाठ्यपुस्तक या शिक्षण सामग्री की PDF फ़ाइल का ऑफ़लाइन अध्ययन करें",
                fontSize = 12.sp,
                color = Color(0xFF616161)
            )
        }

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
                    Text("📑", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "स्थानीय PDF चुनें (Select Local PDF)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    Text(
                        text = "पेज-दर-पेज सुरक्षित रेंडरिंग (अधिकतम 25 MB)।",
                        fontSize = 11.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(Modifier.height(20.dp))

                    Button(
                        onClick = { pdfPickerLauncher.launch("application/pdf") },
                        enabled = !isIngesting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isIngesting) "PDF जाँची जा रही है..." else "📁 PDF फ़ाइल चुनें (Browse PDF)",
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
    }
}

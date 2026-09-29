package org.sih26042.coteacher.ui.screens

import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import android.graphics.pdf.PdfRenderer
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import org.sih26042.coteacher.OcrReviewDest
import org.sih26042.coteacher.core.model.MaterialType
import org.sih26042.coteacher.core.model.TeacherMaterial
import org.sih26042.coteacher.di.AppContainer
import java.io.File

/**
 * PHASE 6 — PdfViewerScreen
 *
 * Incremental, page-by-page PDF viewer:
 * - Renders requested page to bitmap
 * - Recycles bitmap when navigating pages
 * - Sends selected page to text review or saves as material
 */
@Composable
fun PdfViewerScreen(
    pdfPath: String,
    container: AppContainer,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val file = remember(pdfPath) { File(pdfPath) }
    var pageCount by remember { mutableStateOf(1) }
    var currentPageIndex by remember { mutableStateOf(0) }
    var currentBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    // Read total page count safely
    LaunchedEffect(file) {
        if (file.exists()) {
            try {
                val pfd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(pfd)
                pageCount = renderer.pageCount
                renderer.close()
                pfd.close()
            } catch (e: Exception) {
                statusMessage = "PDF खोलने में त्रुटि: ${e.message}"
            }
        }
    }

    // Render current page
    LaunchedEffect(currentPageIndex, file) {
        if (file.exists() && pageCount > 0) {
            val res = container.pdfIngestionService.renderPage(file, currentPageIndex)
            if (res.success && res.bitmap != null) {
                currentBitmap?.recycle() // Free old page native memory
                currentBitmap = res.bitmap
            } else {
                statusMessage = res.errorMessage
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentBitmap?.recycle()
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
                    Text("← IMPORT PDF", color = Color(0xFF212121), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "पेज ${currentPageIndex + 1} / $pageCount",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF004D40)
                )
            }
        }

        item {
            Text(
                text = "📑 ${file.name}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121)
            )
        }

        // Rendered Page Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentBitmap != null) {
                        Image(
                            bitmap = currentBitmap!!.asImageBitmap(),
                            contentDescription = "PDF Page ${currentPageIndex + 1}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 350.dp, max = 500.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF004D40))
                        }
                    }
                }
            }
        }

        // Page Navigation Controls
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { if (currentPageIndex > 0) currentPageIndex-- },
                    enabled = currentPageIndex > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF37474F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("⯇ पिछला पेज (Prev)", fontSize = 11.sp, color = Color.White)
                }

                Button(
                    onClick = { if (currentPageIndex < pageCount - 1) currentPageIndex++ },
                    enabled = currentPageIndex < pageCount - 1,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("अगला पेज (Next) ⯈", fontSize = 11.sp, color = Color.White)
                }
            }
        }

        // Page Actions
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("इस पेज पर क्रियाएँ (Page Actions):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onNavigate(OcrReviewDest(initialText = "पेज ${currentPageIndex + 1} से पाठ"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF004D40)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📝 पाठ समीक्षा / OCR", color = Color.White, fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val mat = TeacherMaterial(
                                    title = "${file.name} (Page ${currentPageIndex + 1})",
                                    type = MaterialType.IMPORTED_PAGE,
                                    notes = "Page ${currentPageIndex + 1} of ${file.name}"
                                )
                                container.teacherMaterialRepository.saveMaterial(mat)
                                statusMessage = "पेज सामग्री के रूप में सुरक्षित हुआ!"
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💾 पेज सुरक्षित करें", fontSize = 11.sp)
                        }
                    }

                    if (statusMessage != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(statusMessage!!, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

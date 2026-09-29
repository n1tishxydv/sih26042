package org.sih26042.coteacher.core.worksheet

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import org.sih26042.coteacher.core.model.GeneratedWorksheet
import java.io.File
import java.io.FileOutputStream

/**
 * PHASE 6 — WorksheetPdfExporter
 *
 * Renders GeneratedWorksheet into a standard printable A4 PDF using Android's native PdfDocument.
 * Strictly runs offline with zero external network or cloud rendering.
 * Enforces memory safety by closing documents and cleaning streams.
 */
class WorksheetPdfExporter(private val context: Context) {

    fun exportToPdf(worksheet: GeneratedWorksheet): File {
        val outputDir = File(context.filesDir, "exported_worksheets").apply { mkdirs() }
        val outputFile = File(outputDir, "worksheet_${worksheet.id.take(8)}.pdf")

        val document = PdfDocument()
        try {
            // Standard A4 dimensions at 72 dpi: 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.BLACK
                textSize = 18f
                isFakeBoldText = true
            }

            val subTitlePaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 12f
            }

            val bodyPaint = Paint().apply {
                color = Color.BLACK
                textSize = 11f
            }

            val badgePaint = Paint().apply {
                color = Color.rgb(230, 81, 0)
                textSize = 9f
                isFakeBoldText = true
            }

            var y = 50f

            // Header Banner
            canvas.drawText("SIH26042 • NIPUN FLN BILINGUAL WORKSHEET", 40f, y, subTitlePaint)
            y += 24f
            canvas.drawText(worksheet.title, 40f, y, titlePaint)
            y += 18f
            canvas.drawText("Grade: ${worksheet.gradeLevel.displayLabel}  |  Domain: ${worksheet.domain.displayLabel}", 40f, y, subTitlePaint)
            y += 16f
            canvas.drawText("Status: TEACHER_CREATED (Not Native-Speaker Validated)", 40f, y, badgePaint)
            y += 20f

            // Student Info line
            canvas.drawLine(40f, y, 555f, y, subTitlePaint)
            y += 20f
            canvas.drawText("Student Name: _______________________   Roll No: _______   Date: _________", 40f, y, bodyPaint)
            y += 25f

            // Instructions
            canvas.drawText("Instructions: ${worksheet.instructionsHindi}", 40f, y, bodyPaint)
            y += 16f
            canvas.drawText("Santali: ${worksheet.instructionsSantali}", 40f, y, bodyPaint)
            y += 25f

            canvas.drawLine(40f, y, 555f, y, subTitlePaint)
            y += 25f

            // Questions
            worksheet.questions.forEachIndexed { idx, q ->
                if (y > 780f) return@forEachIndexed // Simple page height guard

                canvas.drawText("Q${idx + 1}. ${q.promptHindi} (${q.promptTargetNative})", 40f, y, titlePaint.apply { textSize = 12f })
                y += 18f

                if (q.options.isNotEmpty()) {
                    val optionsStr = q.options.joinToString("      ") { "[  ] $it" }
                    canvas.drawText(optionsStr, 60f, y, bodyPaint)
                    y += 22f
                } else {
                    canvas.drawText("Answer / ᱛᱮᱞᱟ: ___________________________________", 60f, y, bodyPaint)
                    y += 22f
                }
            }

            // Footer
            canvas.drawText("Generated offline by SIH26042 Co-Teacher Assistant", 40f, 810f, subTitlePaint.apply { textSize = 9f })

            document.finishPage(page)

            FileOutputStream(outputFile).use { fos ->
                document.writeTo(fos)
                fos.flush()
            }
        } finally {
            document.close()
        }

        return outputFile
    }
}

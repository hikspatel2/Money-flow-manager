package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.CashTransaction
import com.example.data.CashbookCategory
import com.example.data.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generateAndSharePdf(
        context: Context,
        userProfile: UserProfile?,
        transactions: List<CashTransaction>,
        cashbooks: List<CashbookCategory>,
        timeframeName: String,
        selectedCategoryName: String,
        openingBalance: Double
    ) {
        try {
            val bizName = userProfile?.businessName?.ifEmpty { "Money Flow Manager" } ?: "Money Flow Manager"
            val pdfDocument = PdfDocument()

            // A4 Dimensions in points (72 dpi): 595 x 842
            val width = 595
            val height = 842

            // Running balance logic (from oldest to newest)
            val sortedListOldestToNewest = transactions.sortedBy { it.date }
            val runningBalancesList = mutableListOf<Double>()
            var currentRunningBal = openingBalance
            for (t in sortedListOldestToNewest) {
                currentRunningBal += if (t.type == "IN") t.amount else -t.amount
                runningBalancesList.add(currentRunningBal)
            }

            // Group transactions into pages. Page 1 can fit around 14 rows, subsequent pages fit 22 rows.
            val totalTransactions = transactions.size
            val rowsPerPageFirstPage = 14
            val rowsPerPageOtherPages = 22

            val pagesOfTransactions = mutableListOf<List<CashTransaction>>()
            val pagesOfBalances = mutableListOf<List<Double>>()

            var currentIndex = 0
            var isFirstPage = true

            while (currentIndex < totalTransactions) {
                val limit = if (isFirstPage) rowsPerPageFirstPage else rowsPerPageOtherPages
                val end = (currentIndex + limit).coerceAtMost(totalTransactions)

                pagesOfTransactions.add(sortedListOldestToNewest.subList(currentIndex, end))
                pagesOfBalances.add(runningBalancesList.subList(currentIndex, end))

                currentIndex = end
                isFirstPage = false
            }

            // If empty, create at least one page
            if (pagesOfTransactions.isEmpty()) {
                pagesOfTransactions.add(emptyList())
                pagesOfBalances.add(emptyList())
            }

            val totalPagesCount = pagesOfTransactions.size

            // Paints
            val bannerPaint = Paint().apply {
                color = Color.rgb(26, 54, 93) // Professional Dark Blue (#1A365D)
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val whiteHeaderPaint = Paint().apply {
                color = Color.WHITE
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val whiteHeaderRightPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            val footerTextPaintLeft = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val footerTextPaintRight = Paint().apply {
                color = Color.WHITE
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            val titlePaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Slate 900
                textSize = 15f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(71, 85, 105) // Slate 600
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val rightSubtitlePaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.rgb(241, 245, 249) // Slate 100 Background
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            val tableHeaderLabelPaint = Paint().apply {
                color = Color.rgb(51, 65, 85) // Slate 700 Text
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val tableRowPaint = Paint().apply {
                color = Color.rgb(51, 65, 85)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val tableRowBoldPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val redIndicatorPaint = Paint().apply {
                color = Color.rgb(225, 29, 72) // red-600
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            val greenIndicatorPaint = Paint().apply {
                color = Color.rgb(22, 163, 74) // green-600
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
                isAntiAlias = true
            }

            val gridLinePaint = Paint().apply {
                color = Color.rgb(226, 232, 240) // Slate 200
                strokeWidth = 0.5f
                style = Paint.Style.STROKE
                isAntiAlias = true
            }

            val summaryBoxPaint = Paint().apply {
                color = Color.rgb(248, 250, 252) // Slate 50 Background
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            var globalTxIndex = 0

            for (pageNumber in 0 until totalPagesCount) {
                val pageInfo = PdfDocument.PageInfo.Builder(width, height, pageNumber + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas: Canvas = page.canvas

                // 1. Header Banner: Height 50
                canvas.drawRect(0f, 0f, width.toFloat(), 50f, bannerPaint)

                canvas.drawText(bizName, 25f, 31f, whiteHeaderPaint)
                canvas.drawText("Rudees Digital", (width - 25).toFloat(), 31f, whiteHeaderRightPaint)

                // 2. Report Description (Only on First Page) or Compact version
                val startContentY: Float
                if (pageNumber == 0) {
                    canvas.drawText("CASHBOOK STATEMENT INDEX & MONITOR", 25f, 80f, titlePaint)
                    canvas.drawText("Timeframe: $timeframeName | Ledger: $selectedCategoryName", 25f, 96f, subtitlePaint)

                    val formatStr = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault()).format(Date())
                    canvas.drawText("Generated on: $formatStr", (width - 25).toFloat(), 96f, rightSubtitlePaint)

                    // 3. Summary Box (Only on Page 1)
                    val boxTop = 115f
                    val boxHeight = 50f
                    canvas.drawRoundRect(25f, boxTop, (width - 25).toFloat(), boxTop + boxHeight, 8f, 8f, summaryBoxPaint)
                    canvas.drawRoundRect(25f, boxTop, (width - 25).toFloat(), boxTop + boxHeight, 8f, 8f, gridLinePaint)

                    // Calculations
                    var totalIn = 0.0
                    var totalOut = 0.0
                    for (t in transactions) {
                        if (t.type == "IN") totalIn += t.amount else totalOut += t.amount
                    }
                    val netBalance = openingBalance + totalIn - totalOut

                    val cardWidth = (width - 50) / 4.toFloat()

                    // Col 1: Opening Balance
                    canvas.drawText("Opening Bal.", 35f, boxTop + 18f, subtitlePaint)
                    canvas.drawText(Utils.formatCurrencyRs(openingBalance), 35f, boxTop + 36f, tableRowBoldPaint)

                    // Col 2: Total Credit (Credit is Green + Receipt)
                    canvas.drawText("Total Credit (+)", 35f + cardWidth, boxTop + 18f, subtitlePaint)
                    val crPaint = Paint(tableRowBoldPaint).apply { color = Color.rgb(22, 163, 74) }
                    canvas.drawText(Utils.formatCurrencyRs(totalIn), 35f + cardWidth, boxTop + 36f, crPaint)

                    // Col 3: Total Debit (Debit is Red - Payments)
                    canvas.drawText("Total Debit (-)", 35f + cardWidth * 2, boxTop + 18f, subtitlePaint)
                    val drPaint = Paint(tableRowBoldPaint).apply { color = Color.rgb(225, 29, 72) }
                    canvas.drawText(Utils.formatCurrencyRs(totalOut), 35f + cardWidth * 2, boxTop + 36f, drPaint)

                    // Col 4: Net Balance
                    canvas.drawText("Net Balance", 35f + cardWidth * 3, boxTop + 18f, subtitlePaint)
                    val netColorPaint = Paint(tableRowBoldPaint).apply {
                        color = if (netBalance >= 0) Color.rgb(22, 163, 74) else Color.rgb(225, 29, 72)
                    }
                    canvas.drawText(Utils.formatCurrencyRs(netBalance), 35f + cardWidth * 3, boxTop + 36f, netColorPaint)

                    startContentY = 195f
                } else {
                    canvas.drawText("CONTINUED LEDGER STATEMENT", 25f, 75f, titlePaint)
                    canvas.drawText("Ledger: $selectedCategoryName | Page ${pageNumber + 1}", 25f, 90f, subtitlePaint)
                    startContentY = 110f
                }

                // 4. Ledger Table Header
                val thTop = startContentY
                val thHeight = 22f
                canvas.drawRect(25f, thTop, (width - 25).toFloat(), thTop + thHeight, tableHeaderPaint)
                canvas.drawRect(25f, thTop, (width - 25).toFloat(), thTop + thHeight, gridLinePaint)

                // Col margins
                val colXDate = 32f
                val colXDetails = 115f
                val colXDebit = 320f
                val colXCredit = 410f
                val colXBalance = 510f

                canvas.drawText("DATE", colXDate, thTop + 14f, tableHeaderLabelPaint)
                canvas.drawText("PARTY / DETAILS", colXDetails, thTop + 14f, tableHeaderLabelPaint)

                tableHeaderLabelPaint.textAlign = Paint.Align.RIGHT
                canvas.drawText("DEBIT (-)", colXDebit, thTop + 14f, tableHeaderLabelPaint)
                canvas.drawText("CREDIT (+)", colXCredit, thTop + 14f, tableHeaderLabelPaint)
                canvas.drawText("BAL (Dr/Cr)", colXBalance, thTop + 14f, tableHeaderLabelPaint)
                tableHeaderLabelPaint.textAlign = Paint.Align.LEFT // Restore

                // 5. Draw table row contents for current page
                val transList = pagesOfTransactions[pageNumber]
                val balList = pagesOfBalances[pageNumber]

                var rowY = thTop + thHeight

                for (i in transList.indices) {
                    val t = transList[i]
                    val bal = balList[i]

                    // Row background lines
                    canvas.drawLine(25f, rowY + 24f, (width - 25).toFloat(), rowY + 24f, gridLinePaint)

                    // Add date text
                    val dateFormatted = Utils.formatDate(t.date)
                    canvas.drawText(dateFormatted, colXDate, rowY + 16f, tableRowPaint)

                    // Add details
                    val detailsPreview = if (t.partyName.isNotBlank()) {
                        "${t.description} (${t.partyName})"
                    } else {
                        t.description
                    }
                    val detailTruncated = if (detailsPreview.length > 36) {
                        detailsPreview.substring(0, 33) + "..."
                    } else {
                        detailsPreview
                    }
                    canvas.drawText(detailTruncated, colXDetails, rowY + 16f, tableRowBoldPaint)

                    // Debit / Credit fields
                    if (t.type == "OUT") {
                        val drFormatted = Utils.formatCurrency(t.amount, true)
                        tableRowPaint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(drFormatted, colXDebit, rowY + 16f, tableRowPaint)
                        tableRowPaint.textAlign = Paint.Align.LEFT
                    } else {
                        val crFormatted = Utils.formatCurrency(t.amount, true)
                        tableRowPaint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(crFormatted, colXCredit, rowY + 16f, tableRowPaint)
                        tableRowPaint.textAlign = Paint.Align.LEFT
                    }

                    // Balance column
                    val prefixBal = Utils.formatCurrency(Math.abs(bal), true)
                    val statusText = if (bal >= 0) "Cr" else "Dr"
                    val statusPaint = if (bal >= 0) greenIndicatorPaint else redIndicatorPaint

                    // Draw balance amount, and next to it dynamic Dr/Cr
                    statusPaint.textAlign = Paint.Align.RIGHT
                    canvas.drawText("$prefixBal $statusText", colXBalance, rowY + 16f, statusPaint)

                    globalTxIndex++
                    rowY += 24f
                }

                // 6. Draw vertical border lines left and right of statement table
                canvas.drawLine(25f, thTop, 25f, rowY, gridLinePaint)
                canvas.drawLine((width - 25).toFloat(), thTop, (width - 25).toFloat(), rowY, gridLinePaint)

                // 7. Footer Banner at bottom: Height 40 (y: height - 60 to height - 20)
                val footerTop = (height - 60).toFloat()
                canvas.drawRect(0f, footerTop, width.toFloat(), (height - 20).toFloat(), bannerPaint)

                canvas.drawText("Start Using Money Flow Manager Now", 25f, footerTop + 24f, footerTextPaintLeft)

                val supportEmails = "info@rudeesdigital.dev"
                val supportWebsite = "www.rudeesdigital.dev"
                canvas.drawText(supportWebsite, (width - 25).toFloat(), footerTop + 18f, footerTextPaintRight)
                canvas.drawText(supportEmails, (width - 25).toFloat(), footerTop + 30f, footerTextPaintRight)

                // Page Number Indicator right below footer or on bottom margin border
                val pageIndicatorStr = "Page ${pageNumber + 1} of $totalPagesCount"
                val pageIndicatorPaint = Paint().apply {
                    color = Color.rgb(100, 116, 139)
                    textSize = 8.5f
                    typeface = Typeface.DEFAULT
                    textAlign = Paint.Align.RIGHT
                    isAntiAlias = true
                }
                canvas.drawText(pageIndicatorStr, (width - 25).toFloat(), (height - 6).toFloat(), pageIndicatorPaint)

                pdfDocument.finishPage(page)
            }

            // Save PDF into Cache Directory
            val pdfDir = File(context.cacheDir, "reports")
            if (!pdfDir.exists()) {
                pdfDir.mkdirs()
            }
            val appTitleSanitized = bizName.replace(" ", "_")
            val fileName = "MoneyFlow_Report_${appTitleSanitized}_${System.currentTimeMillis()}.pdf"
            val file = File(pdfDir, fileName)

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            // Open Share Intent
            shareFileWithIntent(context, file, "application/pdf", "Share PDF Statement")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF report: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    fun generateAndShareCsv(
        context: Context,
        userProfile: UserProfile?,
        transactions: List<CashTransaction>,
        cashbooks: List<CashbookCategory>,
        openingBalance: Double
    ) {
        try {
            val sb = java.lang.StringBuilder()

            // Document branding headers first compatibility-row
            val bizName = userProfile?.businessName?.ifEmpty { "Money Flow Manager" } ?: "Money Flow Manager"
            sb.append("\"Ledger Statement for $bizName\"\n")
            sb.append("\"Branded & Generated powered by Rudees Digital (www.rudeesdigital.dev)\"\n\n")

            // Ledger Metadata
            sb.append("\"Opening Cash Balance:\",\"${Utils.formatCurrency(openingBalance, false)}\"\n\n")

            // Main headers
            sb.append("Date,Details,Party,Type,Mode,Debit(-),Credit(+),Balance,Status\n")

            // Running balance logic (from oldest to newest)
            val sortedListOldestToNewest = transactions.sortedBy { it.date }
            var runningBal = openingBalance

            for (t in sortedListOldestToNewest) {
                val isIncome = t.type == "IN"
                val debit = if (!isIncome) t.amount else 0.0
                val credit = if (isIncome) t.amount else 0.0
                runningBal += if (isIncome) t.amount else -t.amount

                val dateStr = Utils.formatDate(t.date)
                val descriptionStr = t.description.replace("\"", "\"\"").replace("\n", " ")
                val partyStr = t.partyName.replace("\"", "\"\"").replace("\n", " ")
                val typeStr = t.type
                val modeStr = t.mode

                val debitStr = if (debit > 0) debit.toString() else ""
                val creditStr = if (credit > 0) credit.toString() else ""
                val balStr = Math.abs(runningBal).toString()
                val balStatus = if (runningBal >= 0) "Cr" else "Dr"

                sb.append("\"$dateStr\",\"$descriptionStr\",\"$partyStr\",\"$typeStr\",\"$modeStr\",$debitStr,$creditStr,$balStr,\"$balStatus\"\n")
            }

            // Save CSV file inside cache dir
            val reportDir = File(context.cacheDir, "reports")
            if (!reportDir.exists()) {
                reportDir.mkdirs()
            }
            val appTitleSanitized = bizName.replace(" ", "_")
            val fileName = "MoneyFlow_Ledger_${appTitleSanitized}_${System.currentTimeMillis()}.csv"
            val file = File(reportDir, fileName)

            FileOutputStream(file).use { out ->
                out.write(sb.toString().toByteArray())
            }

            // Run Android share intent
            shareFileWithIntent(context, file, "text/csv", "Share Excel Ledger Statement")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting Excel statement: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFileWithIntent(context: Context, file: File, mimeType: String, actionTitle: String) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(intent, actionTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }
}

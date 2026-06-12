package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.ReportExporter
import com.example.util.Utils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: CashbookViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val cashbooks by viewModel.cashbooks.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val customDateRange by viewModel.customDateRange.collectAsState()
    val selectedStartDate = customDateRange.first
    val selectedEndDate = customDateRange.second
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedBusinessId by viewModel.selectedBusinessId.collectAsState()

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Calendar Pickers triggers
    val calendarInstance = Calendar.getInstance()

    val startDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendarInstance.set(Calendar.YEAR, year)
            calendarInstance.set(Calendar.MONTH, month)
            calendarInstance.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendarInstance.set(Calendar.HOUR_OF_DAY, 0)
            calendarInstance.set(Calendar.MINUTE, 0)
            calendarInstance.set(Calendar.SECOND, 0)
            viewModel.customDateRange.value = Pair(calendarInstance.timeInMillis, selectedEndDate)
        },
        calendarInstance.get(Calendar.YEAR),
        calendarInstance.get(Calendar.MONTH),
        calendarInstance.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendarInstance.set(Calendar.YEAR, year)
            calendarInstance.set(Calendar.MONTH, month)
            calendarInstance.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendarInstance.set(Calendar.HOUR_OF_DAY, 23)
            calendarInstance.set(Calendar.MINUTE, 59)
            calendarInstance.set(Calendar.SECOND, 59)
            viewModel.customDateRange.value = Pair(selectedStartDate, calendarInstance.timeInMillis)
        },
        calendarInstance.get(Calendar.YEAR),
        calendarInstance.get(Calendar.MONTH),
        calendarInstance.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DesignBackground)
    ) {
        // Appbar Header styled cohesively with Dashboard Screen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DesignBackground)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.padding(end = 8.dp).size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, // Or AutoMirrored.Filled.ArrowBack if you want
                        contentDescription = "Back",
                        tint = Slate900
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Analytics,
                contentDescription = "Reports icon",
                tint = Blue600,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Reports & Statements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }

        // Configuration Scroll Pane Layout
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Option Segment: Date Range Filter Card widget
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Slate200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "CHOOSE DATE INTERVAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue600,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Start Date
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { startDatePickerDialog.show() }
                                .testTag("reports_start_date_picker"),
                            shape = RoundedCornerShape(10.dp),
                            color = Slate50,
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Starting From", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(Utils.formatDate(selectedStartDate), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }

                        // End Date
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { endDatePickerDialog.show() }
                                .testTag("reports_end_date_picker"),
                            shape = RoundedCornerShape(10.dp),
                            color = Slate50,
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Ending At", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(Utils.formatDate(selectedEndDate), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            }
                        }
                    }
                }
            }

            // Option Segment: Target Cashbook Category Dropdown Selection
            Column {
                Text(
                    text = "TARGET CASHBOOK FILTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate600,
                    modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
                    letterSpacing = 0.5.sp
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    val availableCashbooks = if (selectedBusinessId != null) {
                        cashbooks.filter { it.businessId == selectedBusinessId }
                    } else cashbooks

                    val filterNameLabel = if (selectedCategoryFilter == null) {
                        "All Cashbooks (Unified Statement)"
                    } else {
                        availableCashbooks.find { it.id == selectedCategoryFilter }?.name ?: "Selected Category"
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCategoryDropdownExpanded = true }
                            .testTag("reports_ledger_selector"),
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = filterNameLabel,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown icon", tint = Slate600)
                        }
                    }

                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Cashbooks (Unified)", fontWeight = FontWeight.Bold) },
                            onClick = {
                                viewModel.selectedCategoryFilter.value = null
                                isCategoryDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("ledger_item_all")
                        )
                        availableCashbooks.forEach { cb ->
                            DropdownMenuItem(
                                text = { Text(cb.name, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    viewModel.selectedCategoryFilter.value = cb.id
                                    isCategoryDropdownExpanded = false
                                },
                                modifier = Modifier.testTag("ledger_item_${cb.id}")
                            )
                        }
                    }
                }
            }

            // Calculations live summary Box card (Opening Balance, Dr/Cr sums, Net Closing Balance)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900), // High density premium Slate card
                border = BorderStroke(1.dp, Slate950_OR_Color_Black_Alpha)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Title
                    Text(
                        text = "LIVE AUDITED SUMMARY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 0.5.sp
                    )

                    // Triple columns metrics showing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Opening Balance (computed of all past transactions before selectedStartDate)
                        val openingBalance = remember(selectedStartDate, selectedCategoryFilter, allTransactions, selectedBusinessId, cashbooks) {
                            allTransactions.filter { t ->
                                val cashbook = cashbooks.find { it.id == t.cashbookId }
                                val matchesBusiness = selectedBusinessId == null || cashbook?.businessId == selectedBusinessId
                                val matchesCategory = selectedCategoryFilter == null || t.cashbookId == selectedCategoryFilter
                                matchesCategory && matchesBusiness && t.date < selectedStartDate
                            }.sumOf { t ->
                                if (t.type == "IN") t.amount else -t.amount
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Opening Bal.", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Utils.formatCurrency(openingBalance),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Total Debit (Expense)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total Out (Dr)", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Utils.formatCurrency(stats.totalOut),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Rose500 // Beautiful bright Red Expense
                            )
                        }

                        // Total Credit (Income)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Total In (Cr)", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Utils.formatCurrency(stats.totalIn),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Emerald500 // Beautiful bright Green Income
                            )
                        }
                    }

                    Divider(color = Color.White.copy(alpha = 0.12f))

                    // Final Net Closing balance row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val openingBalance = remember(selectedStartDate, selectedCategoryFilter, allTransactions, selectedBusinessId, cashbooks) {
                            allTransactions.filter { t ->
                                val cashbook = cashbooks.find { it.id == t.cashbookId }
                                val matchesBusiness = selectedBusinessId == null || cashbook?.businessId == selectedBusinessId
                                val matchesCategory = selectedCategoryFilter == null || t.cashbookId == selectedCategoryFilter
                                matchesCategory && matchesBusiness && t.date < selectedStartDate
                            }.sumOf { t ->
                                if (t.type == "IN") t.amount else -t.amount
                            }
                        }
                        val closingBalance = openingBalance + stats.netBalance

                        Text(
                            text = "Net Closing Balance",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = Utils.formatCurrency(closingBalance),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (closingBalance >= 0.0) Emerald500 else Rose500
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Professional Brand PDF Statement Exporter Actions block
            Text(
                text = "EXPORT STATEMENTS (RUDEES DIGITAL)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Slate600,
                letterSpacing = 0.5.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // PDF Button
                val openingBalance = remember(selectedStartDate, selectedCategoryFilter, allTransactions, selectedBusinessId, cashbooks) {
                    allTransactions.filter { t ->
                        val cashbook = cashbooks.find { it.id == t.cashbookId }
                        val matchesBusiness = selectedBusinessId == null || cashbook?.businessId == selectedBusinessId
                        val matchesCategory = selectedCategoryFilter == null || t.cashbookId == selectedCategoryFilter
                        matchesCategory && matchesBusiness && t.date < selectedStartDate
                    }.sumOf { t ->
                        if (t.type == "IN") t.amount else -t.amount
                    }
                }
                Button(
                    onClick = {
                        val categoryName = if (selectedCategoryFilter == null) {
                            "All Cashbooks"
                        } else {
                            cashbooks.find { it.id == selectedCategoryFilter }?.name ?: "Selected"
                        }
                        ReportExporter.generateAndSharePdf(
                            context = context,
                            userProfile = userProfile,
                            transactions = transactions,
                            cashbooks = cashbooks,
                            timeframeName = "Custom Selection",
                            selectedCategoryName = categoryName,
                            openingBalance = openingBalance
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("export_pdf_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Rose600) // Beautiful high contrast Rose
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF icon", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("PDF Report", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                // Excel CSV Button
                Button(
                    onClick = {
                        ReportExporter.generateAndShareCsv(
                            context = context,
                            userProfile = userProfile,
                            transactions = transactions,
                            cashbooks = cashbooks,
                            openingBalance = openingBalance
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("export_excel_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600) // Beautiful Emerald Green
                ) {
                    Icon(Icons.Default.GridOn, contentDescription = "Excel grid icon", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Excel CSV", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Low level color constraint for audited block
private val Slate950_OR_Color_Black_Alpha = Color(0xFF020617)

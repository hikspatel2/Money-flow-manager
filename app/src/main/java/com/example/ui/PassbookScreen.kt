package com.example.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CashTransaction
import com.example.ui.theme.*
import com.example.util.ReportExporter
import com.example.util.Utils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PassbookScreen(
    viewModel: CashbookViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val cashbooks by viewModel.cashbooks.collectAsState()
    val allBusinesses by viewModel.allBusinesses.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val customDateRange by viewModel.customDateRange.collectAsState()
    val selectedStartDate = customDateRange.first
    val selectedEndDate = customDateRange.second
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val selectedBusinessId by viewModel.selectedBusinessId.collectAsState()
    val selectedEntryType by viewModel.selectedEntryType.collectAsState()
    val selectedPaymentMode by viewModel.selectedPaymentMode.collectAsState()

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
            viewModel.selectedTimeframe.value = "Custom"
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
            viewModel.selectedTimeframe.value = "Custom"
        },
        calendarInstance.get(Calendar.YEAR),
        calendarInstance.get(Calendar.MONTH),
        calendarInstance.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Appbar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Slate900,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Reports",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Filtered Total Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Blue600)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "FILTERED TOTAL",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                                Surface(
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.ShowChart, contentDescription = "Live", tint = Color.White, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Live", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = Utils.formatCurrency(stats.netBalance),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "${transactions.size} transactions • $selectedTimeframe",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(24.dp).background(Emerald500, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.CallReceived, contentDescription = "In", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Cash In", fontSize = 10.sp, color = Color.White.copy(alpha=0.7f))
                                            Text(Utils.formatCurrency(stats.totalIn), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Surface(
                                    modifier = Modifier.weight(1f),
                                    color = Color.White.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier.size(24.dp).background(Rose500, RoundedCornerShape(6.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.AutoMirrored.Filled.CallMade, contentDescription = "Out", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("Cash Out", fontSize = 10.sp, color = Color.White.copy(alpha=0.7f))
                                            Text(Utils.formatCurrency(stats.totalOut), fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Filters
                    FilterSection("DATE RANGE") {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Today", "7 Days", "30 Days", "6 Months", "This Year", "All").forEach { period ->
                                FilterChip(
                                    label = if (period == "All") "All Time" else period,
                                    selected = selectedTimeframe == period,
                                    onClick = { 
                                        if (period == "6 Months") {
                                            val cal = Calendar.getInstance()
                                            cal.add(Calendar.MONTH, -6)
                                            viewModel.customDateRange.value = Pair(cal.timeInMillis, System.currentTimeMillis())
                                        }
                                        viewModel.selectedTimeframe.value = period 
                                    }
                                )
                            }
                            FilterChip(
                                label = "Select Dates",
                                selected = selectedTimeframe == "Custom",
                                onClick = { startDatePickerDialog.show() }
                            )
                        }
                    }

                    FilterSection("ENTRY TYPE") {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All", "IN", "OUT").forEach { type ->
                                val label = when(type) { "IN" -> "Cash In"; "OUT" -> "Cash Out"; else -> "All" }
                                FilterChip(
                                    label = label,
                                    selected = selectedEntryType == type,
                                    onClick = { viewModel.selectedEntryType.value = type }
                                )
                            }
                        }
                    }

                    FilterSection("PAYMENT MODE") {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("All Modes", "Cash", "UPI", "Bank").forEach { mode ->
                                FilterChip(
                                    label = mode,
                                    selected = selectedPaymentMode == mode,
                                    onClick = { viewModel.selectedPaymentMode.value = mode }
                                )
                            }
                        }
                    }

                    FilterSection("BUSINESS") {
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                label = "All",
                                selected = selectedBusinessId == null,
                                onClick = { viewModel.selectedBusinessId.value = null }
                            )
                            allBusinesses.forEach { biz ->
                                FilterChip(
                                    label = biz.name,
                                    selected = selectedBusinessId == biz.id,
                                    onClick = { viewModel.selectedBusinessId.value = biz.id }
                                )
                            }
                        }
                    }

                    FilterSection("CASHBOOK") {
                        val availableCashbooks = if (selectedBusinessId != null) {
                            cashbooks.filter { it.businessId == selectedBusinessId }
                        } else cashbooks
                        
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                label = "All",
                                selected = selectedCategoryFilter == null,
                                onClick = { viewModel.selectedCategoryFilter.value = null }
                            )
                            availableCashbooks.forEach { cb ->
                                FilterChip(
                                    label = cb.name,
                                    selected = selectedCategoryFilter == cb.id,
                                    onClick = { viewModel.selectedCategoryFilter.value = cb.id }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Export Buttons
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
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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
                                    timeframeName = selectedTimeframe,
                                    selectedCategoryName = categoryName,
                                    openingBalance = openingBalance
                                )
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF icon", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export PDF", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

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
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Slate900),
                            border = BorderStroke(1.dp, Slate200)
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = "Excel icon", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Excel", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "PREVIEW (${transactions.size} ENTRIES)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate500,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            items(transactions.sortedByDescending { it.date }) { tx ->
                val cashbook = cashbooks.find { it.id == tx.cashbookId }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(
                                if (tx.type == "IN") Emerald50 else Rose50, 
                                RoundedCornerShape(10.dp)
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (tx.type == "IN") Icons.AutoMirrored.Filled.CallReceived else Icons.AutoMirrored.Filled.CallMade,
                                contentDescription = tx.type,
                                tint = if (tx.type == "IN") Emerald500 else Rose500,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = tx.description.ifEmpty { "Transaction" },
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate900,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Utils.formatDateTime(tx.date),
                                    fontSize = 12.sp,
                                    color = Slate500
                                )
                                Text(" • ", fontSize = 12.sp, color = Slate400)
                                Icon(
                                    imageVector = when(tx.mode.uppercase()) {
                                        "UPI" -> Icons.Default.QrCode
                                        "BANK" -> Icons.Default.AccountBalance
                                        else -> Icons.Default.Money
                                    },
                                    contentDescription = tx.mode,
                                    tint = Slate400,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(tx.mode, fontSize = 12.sp, color = Slate500)
                            }
                            if (tx.partyName.isNotBlank() || cashbook != null) {
                                val partyText = if (tx.partyName.isNotBlank()) tx.partyName else "Unknown"
                                val cbText = cashbook?.name ?: "Deleted Cashbook"
                                Text(
                                    text = "$partyText • $cbText",
                                    fontSize = 12.sp,
                                    color = Slate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                    Text(
                        text = "${if (tx.type == "IN") "+" else "-"}${Utils.formatCurrency(tx.amount)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (tx.type == "IN") Emerald500 else Rose500
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = Slate100)
            }
        }
    }
}

@Composable
fun FilterSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Slate500,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Blue600 else Color.White,
        border = BorderStroke(1.dp, if (selected) Blue600 else Slate200)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else Slate900,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

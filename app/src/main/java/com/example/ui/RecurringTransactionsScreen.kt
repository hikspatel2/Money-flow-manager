package com.example.ui

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.RecurringTransaction
import com.example.ui.theme.*
import com.example.util.Utils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsScreen(
    viewModel: CashbookViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recurringTransactions by viewModel.allRecurringTransactions.collectAsState()
    val cashbooks by viewModel.cashbooks.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }

    // Form states
    var isIncome by remember { mutableStateOf(true) }
    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var partyName by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf("CASH") }
    var selectedFrequency by remember { mutableStateOf("MONTHLY") }

    // Date Bounds states
    val calendarInstance = Calendar.getInstance()
    var startDateMs by remember { mutableStateOf(System.currentTimeMillis()) }
    // End date defaults to 1 month from now
    var endDateMs by remember { 
        mutableStateOf(Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.timeInMillis) 
    }

    val initialCashId = cashbooks.firstOrNull()?.id ?: 1
    var selectedCashbookId by remember { mutableIntStateOf(initialCashId) }

    // Dropdowns UI trigger
    var isCashbookDropdownExpanded by remember { mutableStateOf(false) }
    var isFrequencyDropdownExpanded by remember { mutableStateOf(false) }
    var inputErrorText by remember { mutableStateOf("") }

    // Reset Form Helpers
    fun resetForm() {
        isIncome = true
        amountStr = ""
        description = ""
        partyName = ""
        selectedMode = "CASH"
        selectedFrequency = "MONTHLY"
        startDateMs = System.currentTimeMillis()
        endDateMs = Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.timeInMillis
        selectedCashbookId = cashbooks.firstOrNull()?.id ?: 1
        inputErrorText = ""
    }

    // Pickers Dialogs
    val startDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            // Start of day
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            startDateMs = cal.timeInMillis
        },
        calendarInstance.get(Calendar.YEAR),
        calendarInstance.get(Calendar.MONTH),
        calendarInstance.get(Calendar.DAY_OF_MONTH)
    )

    val endDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            // End of day
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            endDateMs = cal.timeInMillis
        },
        calendarInstance.get(Calendar.YEAR),
        calendarInstance.get(Calendar.MONTH),
        calendarInstance.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (showAddForm) "Schedule Cashflow" else "Recurring Transactions",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showAddForm) {
                                showAddForm = false
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("recurring_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    if (!showAddForm) {
                        IconButton(
                            onClick = {
                                viewModel.triggerRecurringSync()
                                Toast.makeText(context, "Schedules audited & synchronized!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("recurring_sync_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = "Audit Schedules Now", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Blue600)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DesignBackground)
        ) {
            if (!showAddForm) {
                // Rule list & Explainer
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header visual explainer card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Blue100),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Autorenew, contentDescription = "Auto Logo", tint = Blue600, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Automated Cashflow Rules",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate900
                                )
                                Text(
                                    text = "Program standard entries to post automatically on specified dates.",
                                    fontSize = 11.sp,
                                    color = Slate600
                                )
                            }
                        }
                    }

                    // Schedule item results list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (recurringTransactions.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventNote,
                                        contentDescription = "No schedule",
                                        tint = Slate300,
                                        modifier = Modifier.size(64.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Recurring Transactions Set Up",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Slate800
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Automate bills, subscriptions, wages, and periodic customer installments.",
                                        fontSize = 11.sp,
                                        color = Slate400,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    )
                                }
                            }
                        } else {
                            items(
                                items = recurringTransactions,
                                key = { it.id }
                            ) { rt ->
                                val currentCashbookLabel = cashbooks.find { it.id == rt.cashbookId }?.name ?: "Unknown Ledger"
                                val isRtIncome = rt.type == "IN"
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("recurring_item_${rt.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = BorderStroke(1.dp, Slate100),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Dynamic indicator circle
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isRtIncome) Emerald50 else Rose50),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isRtIncome) Icons.Default.VerticalAlignBottom else Icons.Default.VerticalAlignTop,
                                                contentDescription = if (isRtIncome) "Auto Inflow" else "Auto Outflow",
                                                tint = if (isRtIncome) Emerald600 else Rose600,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = rt.description + if (rt.partyName.isNotBlank()) " (${rt.partyName})" else "",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(Blue100)
                                                        .padding(horizontal = 5.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = rt.frequency,
                                                        fontSize = 9.sp,
                                                        color = Blue600,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Text(
                                                    text = " • ",
                                                    fontSize = 11.sp,
                                                    color = Slate300
                                                )
                                                Text(
                                                    text = currentCashbookLabel,
                                                    fontSize = 11.sp,
                                                    color = Slate600
                                                )
                                                Text(
                                                    text = " • ",
                                                    fontSize = 11.sp,
                                                    color = Slate300
                                                )
                                                Text(
                                                    text = "Mode: ${rt.mode}",
                                                    fontSize = 11.sp,
                                                    color = Slate400
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Boundary: ${Utils.formatDate(rt.startDate)} to ${Utils.formatDate(rt.endDate)}",
                                                fontSize = 10.sp,
                                                color = Slate400
                                            )
                                            if (rt.lastGeneratedDate > 0L) {
                                                Text(
                                                    text = "Last Processed: ${Utils.formatDate(rt.lastGeneratedDate)}",
                                                    fontSize = 10.sp,
                                                    color = Emerald600,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }

                                        Column(
                                            horizontalAlignment = Alignment.End,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = (if (isRtIncome) "+" else "-") + Utils.formatCurrency(rt.amount, true),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isRtIncome) Emerald600 else Rose600
                                            )
                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteRecurringTransaction(rt)
                                                    Toast.makeText(context, "Recurring schedule deleted successfully.", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("delete_recurring_${rt.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Rose600,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    // Create Floating rule bottom container
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Transparent)
                            .padding(20.dp)
                    ) {
                        Button(
                            onClick = {
                                resetForm()
                                showAddForm = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_create_recurring_form"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Schedule Plus")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Setup Automated Cashflow Rule", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // Form layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type switch IN vs OUT
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isIncome = true }
                                .testTag("form_type_in"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isIncome) Emerald50 else Color.White,
                            border = BorderStroke(1.dp, if (isIncome) Emerald600 else Slate200)
                        ) {
                            Text(
                                "CREDIT (+ IN)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isIncome) Emerald600 else Slate600,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(14.dp)
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isIncome = false }
                                .testTag("form_type_out"),
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isIncome) Rose50 else Color.White,
                            border = BorderStroke(1.dp, if (!isIncome) Rose600 else Slate200)
                        ) {
                            Text(
                                "DEBIT (- OUT)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isIncome) Rose600 else Slate600,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Amount card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ENTER RULE AMOUNT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = amountStr,
                                onValueChange = { input ->
                                    if (input.isEmpty() || input.toDoubleOrNull() != null || input.endsWith(".")) {
                                        amountStr = input
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("form_amount_input"),
                                placeholder = { Text("0.00", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Slate400) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                textStyle = LocalTextStyle.current.copy(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Blue600),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Blue600,
                                    unfocusedBorderColor = Slate300,
                                    cursorColor = Blue600
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    // Contact / Note Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                Text("PARTY / CUSTOMER", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = partyName,
                                    onValueChange = { partyName = it },
                                    placeholder = { Text("Who is this linked with?", color = Slate400) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("form_party_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue600,
                                        unfocusedBorderColor = Slate300,
                                        focusedTextColor = Slate900,
                                        unfocusedTextColor = Slate900,
                                        cursorColor = Slate900
                                    )
                                )
                            }

                            Column {
                                Text("RULE DESCRIPTION (REQUIRED)", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    placeholder = { Text("e.g. Monthly cloud server, Weekly office snacks", color = Slate400) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("form_description_input"),
                                    singleLine = false,
                                    maxLines = 2,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Blue600,
                                        unfocusedBorderColor = Slate300,
                                        focusedTextColor = Slate900,
                                        unfocusedTextColor = Slate900,
                                        cursorColor = Slate900
                                    )
                                )
                            }
                        }
                    }

                    // Frequency & Cashbook Selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Frequency Selection Dropdown
                            Column {
                                Text("RECURRING FREQUENCY", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isFrequencyDropdownExpanded = true }
                                            .testTag("form_frequency_selector"),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Slate50,
                                        border = BorderStroke(1.dp, Slate200)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedFrequency,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop", tint = Slate600)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isFrequencyDropdownExpanded,
                                        onDismissRequest = { isFrequencyDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        val frequencies = listOf("DAILY", "WEEKLY", "MONTHLY", "YEARLY")
                                        frequencies.forEach { freq ->
                                            DropdownMenuItem(
                                                text = { Text(freq, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                                onClick = {
                                                    selectedFrequency = freq
                                                    isFrequencyDropdownExpanded = false
                                                },
                                                modifier = Modifier.testTag("freq_item_$freq")
                                            )
                                        }
                                    }
                                }
                            }

                            // Target Ledger Cashbook Category
                            Column {
                                Text("ASSIGN LEDGER CASHBOOK", fontSize = 11.sp, color = Slate600, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                Box(modifier = Modifier.fillMaxWidth()) {
                                    val assignedName = cashbooks.find { it.id == selectedCashbookId }?.name ?: "Main Cashbook"
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isCashbookDropdownExpanded = true }
                                            .testTag("form_cashbook_selector"),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Slate50,
                                        border = BorderStroke(1.dp, Slate200)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = assignedName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Slate900
                                            )
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Drop", tint = Slate600)
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = isCashbookDropdownExpanded,
                                        onDismissRequest = { isCashbookDropdownExpanded = false },
                                        modifier = Modifier.fillMaxWidth(0.8f)
                                    ) {
                                        cashbooks.forEach { cb ->
                                            DropdownMenuItem(
                                                text = { Text(cb.name, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                                                onClick = {
                                                    selectedCashbookId = cb.id
                                                    isCashbookDropdownExpanded = false
                                                },
                                                modifier = Modifier.testTag("form_ledger_item_${cb.id}")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Mode Row Selector
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "DEFAULT PAYMENT MODE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val modes = listOf("CASH", "UPI", "BANK")
                                modes.forEach { mode ->
                                    val isModeSelected = selectedMode == mode
                                    val activeBg = if (isModeSelected) Blue600 else Slate50
                                    val activeText = if (isModeSelected) Color.White else Slate600
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(activeBg)
                                            .clickable { selectedMode = mode }
                                            .padding(vertical = 12.dp)
                                            .border(
                                                width = 1.dp,
                                                color = if (isModeSelected) Color.Transparent else Slate200,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .testTag("form_mode_$mode"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = mode,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = activeText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Start/End Dates Card Boundaries Picker
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Slate200)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "DATE ACTIVE INTERVAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Blue600
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Start Date picker
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { startDatePicker.show() }
                                        .testTag("form_start_date_picker"),
                                    color = Slate50,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Slate200)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Starts On", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(Utils.formatDate(startDateMs), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    }
                                }

                                // End Date picker
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { endDatePicker.show() }
                                        .testTag("form_end_date_picker"),
                                    color = Slate50,
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Slate200)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("Ends On", fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(Utils.formatDate(endDateMs), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                    }
                                }
                            }
                        }
                    }

                    if (inputErrorText.isNotBlank()) {
                        Text(
                            text = inputErrorText,
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Save Automated Button
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                inputErrorText = "Enter a valid amount greater than zero."
                                return@Button
                            }
                            if (description.isBlank()) {
                                inputErrorText = "Enter a rule description details."
                                return@Button
                            }
                            if (endDateMs <= startDateMs) {
                                inputErrorText = "End date must be after start date."
                                return@Button
                            }

                            viewModel.addRecurringTransaction(
                                type = if (isIncome) "IN" else "OUT",
                                amount = amt,
                                description = description.trim(),
                                partyName = partyName.trim(),
                                mode = selectedMode,
                                cashbookId = selectedCashbookId,
                                frequency = selectedFrequency,
                                startDate = startDateMs,
                                endDate = endDateMs
                            )
                            Toast.makeText(context, "Recurring automated rule configured!", Toast.LENGTH_SHORT).show()
                            showAddForm = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("form_save_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Schedule Save")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Automated Cashflow Rule", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

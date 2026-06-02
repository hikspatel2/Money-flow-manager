package com.example.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CashTransaction
import com.example.util.Utils
import java.util.*
import com.example.ui.theme.Blue600
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate900
import com.example.ui.theme.Emerald500
import com.example.ui.theme.Emerald50
import com.example.ui.theme.Rose500
import com.example.ui.theme.Rose50
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    viewModel: CashbookViewModel,
    initialType: String,
    editingTransaction: CashTransaction?,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allCashbooks by viewModel.cashbooks.collectAsState()
    val selectedBusinessId by viewModel.selectedBusinessId.collectAsState()
    val cashbooks = if (selectedBusinessId != null) {
        allCashbooks.filter { it.businessId == selectedBusinessId }
    } else {
        allCashbooks
    }

    // Form inputs state
    val isEditMode = editingTransaction != null
    var entryType by remember { mutableStateOf(editingTransaction?.type ?: initialType) }
    val isIncome = entryType == "IN"

    val headerTitle = if (isEditMode) "Edit Entry" else "New Entry"

    var amountStr by remember { mutableStateOf(editingTransaction?.amount?.toString()?.removeSuffix(".0") ?: "") }
    var description by remember { mutableStateOf(editingTransaction?.description ?: "") }
    var partyName by remember { mutableStateOf(editingTransaction?.partyName ?: "") }
    var selectedMode by remember { mutableStateOf(editingTransaction?.mode ?: "Cash") }
    var receiptUri by remember { mutableStateOf(editingTransaction?.receiptUri) }
    
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> 
        uri?.let { receiptUri = it.toString() } 
    }

    // Resolve initial cashbook reference
    val initialCashbookId = editingTransaction?.cashbookId ?: (cashbooks.firstOrNull()?.id ?: 1)
    var selectedCashbookId by remember { mutableIntStateOf(initialCashbookId) }

    // Date/Time state
    var selectedDateMs by remember { mutableStateOf(editingTransaction?.date ?: System.currentTimeMillis()) }

    // UI state alerts
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var inputErrorText by remember { mutableStateOf("") }

    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    val calendarInstance = Calendar.getInstance().apply { timeInMillis = selectedDateMs }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendarInstance.set(Calendar.YEAR, year)
            calendarInstance.set(Calendar.MONTH, month)
            calendarInstance.set(Calendar.DAY_OF_YEAR, calendarInstance.get(Calendar.DAY_OF_YEAR))
            calendarInstance.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendarInstance.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendarInstance.set(Calendar.MINUTE, minute)
                    selectedDateMs = calendarInstance.timeInMillis
                },
                calendarInstance.get(Calendar.HOUR_OF_DAY),
                calendarInstance.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendarInstance.get(Calendar.YEAR),
        calendarInstance.get(Calendar.MONTH),
        calendarInstance.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF4F5F9), // Match the light grayish background
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = headerTitle,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = Slate900)
                    }
                },
                actions = {
                    if (isEditMode) {
                        IconButton(
                            onClick = { showDeleteConfirm = true }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete transaction", tint = Rose500)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFF4F5F9))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Cashbook Selector
            Card(
                modifier = Modifier.fillMaxWidth().clickable { isCategoryDropdownExpanded = true },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Box {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "CASHBOOK", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Slate600)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cashbooks.find { it.id == selectedCashbookId }?.name ?: "Select Cashbook",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                    }
                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        cashbooks.forEach { cb ->
                            DropdownMenuItem(
                                text = { Text(cb.name, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    selectedCashbookId = cb.id
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // In / Out toggles
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { entryType = "IN" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIncome) Emerald500 else Color.White,
                        contentColor = if (isIncome) Color.White else Slate900
                    ),
                    border = BorderStroke(1.dp, if (isIncome) Emerald500 else Color(0xFFE2E8F0))
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CASH IN", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { entryType = "OUT" },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!isIncome) Rose500 else Color.White,
                        contentColor = if (!isIncome) Color.White else Slate900
                    ),
                    border = BorderStroke(1.dp, if (!isIncome) Rose500 else Color(0xFFE2E8F0))
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("CASH OUT", fontWeight = FontWeight.Bold)
                }
            }

            // Amount field with quick add
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if (isIncome) Emerald50 else Rose50),
                border = BorderStroke(1.dp, if (isIncome) Emerald500.copy(alpha=0.3f) else Rose500.copy(alpha=0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AMOUNT (₹)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isIncome) Emerald500.copy(alpha=0.8f) else Rose500.copy(alpha=0.8f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    TextField(
                        value = amountStr,
                        onValueChange = { input: String -> if (input.isEmpty() || input.toDoubleOrNull() != null || input.endsWith(".")) amountStr = input },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Slate600.copy(alpha=0.5f)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = LocalTextStyle.current.copy(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = if (isIncome) Emerald500 else Rose500),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = if (isIncome) Emerald500 else Rose500
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val quickAddValues = listOf(100, 500, 1000, 5000)
                        val btnColor = if (isIncome) Emerald500 else Rose500
                        quickAddValues.forEach { v ->
                            OutlinedButton(
                                onClick = {
                                    val current = amountStr.toDoubleOrNull() ?: 0.0
                                    amountStr = (current + v).toString().removeSuffix(".0")
                                },
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, btnColor.copy(alpha=0.5f)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("+₹$v", fontSize = 12.sp, color = btnColor, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Payment Mode selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PAYMENT MODE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val paymentModesList = listOf(
                            Triple("Cash", Icons.Default.AttachMoney, "Cash"), 
                            Triple("UPI", Icons.Default.QrCodeScanner, "UPI"), 
                            Triple("Bank", Icons.Default.AccountBalance, "Bank")
                        )
                        
                        paymentModesList.forEach { (mode, icon, dbMode) ->
                            val isSelected = selectedMode.equals(dbMode, ignoreCase = true)
                            val containerCl = if (isSelected) Blue600 else Slate100
                            val textCl = if (isSelected) Color.White else Slate900

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(containerCl)
                                    .clickable { selectedMode = dbMode }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = textCl, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = mode, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textCl)
                                }
                            }
                        }
                    }
                }
            }

            // Description and Party
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("What's it for? (e.g. Counter sale)", color = Slate400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Blue600,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        cursorColor = Slate900,
                        focusedLabelColor = Blue600,
                        unfocusedLabelColor = Slate700
                    ),
                    label = { Text("DESCRIPTION", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )

                OutlinedTextField(
                    value = partyName,
                    onValueChange = { partyName = it },
                    placeholder = { Text("Name of person or company", color = Slate400) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Blue600,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedTextColor = Slate900,
                        unfocusedTextColor = Slate900,
                        cursorColor = Slate900,
                        focusedLabelColor = Blue600,
                        unfocusedLabelColor = Slate700
                    ),
                    label = { Text("PARTY / CUSTOMER (OPTIONAL)", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
            
            // Receipt and Extras
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Upload Receipt
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { launcher.launch("image/*") },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (receiptUri == null) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Blue600.copy(alpha=0.1f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Upload Receipt", tint = Blue600)
                            }
                        } else {
                            Image(
                                painter = rememberAsyncImagePainter(receiptUri),
                                contentDescription = "Receipt Image",
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(if (receiptUri == null) "Upload Receipt/Photo" else "Receipt Uploaded", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text(if (receiptUri == null) "Attach a bill or invoice" else "Click to change", fontSize = 12.sp, color = Slate600)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.UploadFile, contentDescription = "Add", tint = Slate400)
                    }
                }

                // Date Picker
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Date Options", tint = Slate600)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Date & Time", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                            Text(Utils.formatDateTime(selectedDateMs), fontSize = 12.sp, color = Slate600)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, contentDescription = "Edit Date", tint = Slate400)
                    }
                }

                // Recurring Toggle
                var isRecurring by remember { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { isRecurring = !isRecurring },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Autorenew, contentDescription = "Recurring", tint = Color(0xFFD97706))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Make this Recurring", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                Text("Repeat this entry periodically", fontSize = 12.sp, color = Slate600)
                            }
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Blue600)
                        )
                    }
                }
            }

            if (inputErrorText.isNotBlank()) {
                Text(
                    text = inputErrorText,
                    color = Rose500,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            // Save Entry Action Bar
            Button(
                onClick = {
                    val amountVal = amountStr.toDoubleOrNull()
                    if (amountVal == null || amountVal <= 0.0) {
                        inputErrorText = "Please enter a valid cash amount greater than zero."
                        return@Button
                    }

                    viewModel.saveTransaction(
                        id = editingTransaction?.id ?: 0,
                        type = entryType,
                        amount = amountVal,
                        description = description.trim().ifEmpty { "Uncategorized" },
                        partyName = partyName.trim(),
                        date = selectedDateMs,
                        mode = selectedMode.uppercase(),
                        cashbookId = selectedCashbookId,
                        receiptUri = receiptUri
                    )
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) Emerald500 else Rose500)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Save icon")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isIncome) "Save Cash In" else "Save Cash Out",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showDeleteConfirm && editingTransaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry", fontWeight = FontWeight.Bold) },
            text = { Text("Are you absolutely sure you want to delete this recorded entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTransaction(editingTransaction)
                        showDeleteConfirm = false
                        onNavigateBack()
                    }
                ) {
                    Text("Delete Entry", color = Rose500, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


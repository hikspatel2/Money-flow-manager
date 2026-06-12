package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CashTransaction
import com.example.data.CashbookCategory
import com.example.ui.theme.*
import com.example.util.Utils
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDetailScreen(
    viewModel: CashbookViewModel,
    cashbook: CashbookCategory,
    onNavigateBack: () -> Unit,
    onEditEntry: (CashTransaction) -> Unit = {},
    onAddEntry: (String) -> Unit = {},
    onNavigateToReports: () -> Unit = {}
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    var selectedPartyFilter by remember { mutableStateOf<String?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) } // Not effectively used here unless we categorize transactions inside ledger
    var selectedPaymentMode by remember { mutableStateOf<String?>(null) }

    var partyFilterExpanded by remember { mutableStateOf(false) }
    var categoryFilterExpanded by remember { mutableStateOf(false) }
    var paymentModeFilterExpanded by remember { mutableStateOf(false) }
    var moreMenuExpanded by remember { mutableStateOf(false) }
    
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var newMemberName by remember { mutableStateOf("") }
    var newMemberPhone by remember { mutableStateOf("") }

    val rawTransactions = allTransactions.filter { it.cashbookId == cashbook.id }.sortedBy { it.date }
    val parties = rawTransactions.map { it.partyName }.filter { it.isNotBlank() }.distinct()
    val categories = rawTransactions.map { it.description }.filter { it.isNotBlank() }.distinct() // Using desc as category pseudo
    val modes = listOf("Cash", "Online", "Bank", "Cheque")

    val ledgerTransactionsAsc = rawTransactions.filter { tx ->
        val matchesParty = selectedPartyFilter == null || tx.partyName == selectedPartyFilter
        val matchesCategory = selectedCategoryFilter == null || tx.description == selectedCategoryFilter
        val matchesMode = selectedPaymentMode == null || tx.mode.equals(selectedPaymentMode, ignoreCase = true)
        matchesParty && matchesCategory && matchesMode
    }
    
    var currentBalance = 0.0
    val txWithBalances = ledgerTransactionsAsc.map { tx ->
        val amt = if (tx.type == "IN") tx.amount else -tx.amount
        currentBalance += amt
        tx to currentBalance
    }.reversed()
    
    val totalIn = txWithBalances.filter { it.first.type == "IN" }.sumOf { it.first.amount }
    val totalOut = txWithBalances.filter { it.first.type == "OUT" }.sumOf { it.first.amount }
    val balance = totalIn - totalOut

    val grouped = txWithBalances.groupBy { Utils.formatDate(it.first.date) }
    val context = androidx.compose.ui.platform.LocalContext.current

    var showRenameDialog by remember { mutableStateOf(false) }
    var newCashbookName by remember { mutableStateOf(cashbook.name) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column(modifier = Modifier.clickable {
                        showRenameDialog = true
                    }) {
                        Text(cashbook.name, fontWeight = FontWeight.Bold, color = Slate900, fontSize = 18.sp)
                        Text("Add Member, Book Activity etc", color = Slate500, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate900)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddMemberDialog = true }) { Icon(Icons.Default.PersonAddAlt1, contentDescription = "Member", tint = Blue600) }
                    IconButton(onClick = {
                        val openingBalance = 0.0
                        com.example.util.ReportExporter.generateAndSharePdf(
                            context = context,
                            userProfile = userProfile,
                            transactions = ledgerTransactionsAsc.reversed(),
                            cashbooks = listOf(cashbook),
                            timeframeName = "All time",
                            selectedCategoryName = cashbook.name,
                            openingBalance = openingBalance
                        )
                    }) { Icon(Icons.Default.PictureAsPdf, contentDescription = "PDF", tint = Blue600) }
                    Box {
                        IconButton(onClick = { moreMenuExpanded = true }) { Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Blue600) }
                        DropdownMenu(expanded = moreMenuExpanded, onDismissRequest = { moreMenuExpanded = false }) {
                            DropdownMenuItem(text = { Text("Edit Cashbook Name") }, onClick = {
                                moreMenuExpanded = false
                                showRenameDialog = true
                            })
                            DropdownMenuItem(text = { Text("Delete Cashbook") }, onClick = {
                                viewModel.deleteCashbookCategory(cashbook)
                                moreMenuExpanded = false
                                onNavigateBack()
                            })
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Slate50
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Chips Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box {
                        FilterChip(
                            selected = selectedPartyFilter != null,
                            onClick = { partyFilterExpanded = true },
                            label = { Text(selectedPartyFilter ?: "Party") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription=null) }
                        )
                        DropdownMenu(expanded = partyFilterExpanded, onDismissRequest = { partyFilterExpanded = false }) {
                            DropdownMenuItem(text = { Text("All Parties") }, onClick = { selectedPartyFilter = null; partyFilterExpanded = false })
                            parties.forEach { p ->
                                DropdownMenuItem(text = { Text(p) }, onClick = { selectedPartyFilter = p; partyFilterExpanded = false })
                            }
                        }
                    }
                    Box {
                        FilterChip(
                            selected = selectedCategoryFilter != null,
                            onClick = { categoryFilterExpanded = true },
                            label = { Text(selectedCategoryFilter ?: "Category") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription=null) }
                        )
                        DropdownMenu(expanded = categoryFilterExpanded, onDismissRequest = { categoryFilterExpanded = false }) {
                            DropdownMenuItem(text = { Text("All Categories") }, onClick = { selectedCategoryFilter = null; categoryFilterExpanded = false })
                            categories.forEach { c ->
                                DropdownMenuItem(text = { Text(c) }, onClick = { selectedCategoryFilter = c; categoryFilterExpanded = false })
                            }
                        }
                    }
                    Box {
                        FilterChip(
                            selected = selectedPaymentMode != null,
                            onClick = { paymentModeFilterExpanded = true },
                            label = { Text(selectedPaymentMode ?: "Payment Mode") },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription=null) }
                        )
                        DropdownMenu(expanded = paymentModeFilterExpanded, onDismissRequest = { paymentModeFilterExpanded = false }) {
                            DropdownMenuItem(text = { Text("All Modes") }, onClick = { selectedPaymentMode = null; paymentModeFilterExpanded = false })
                            modes.forEach { m ->
                                DropdownMenuItem(text = { Text(m) }, onClick = { selectedPaymentMode = m; paymentModeFilterExpanded = false })
                            }
                        }
                    }
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Net Balance Summary
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(1.dp)
                        ) {
                            Column {
                                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Net Balance", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
                                    Text(
                                        "${if(balance < 0) "-" else ""}₹${Utils.formatCurrency(kotlin.math.abs(balance), false)}",
                                        fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900
                                    )
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total In (+)", fontSize = 14.sp, color = Slate700)
                                    Text("₹${Utils.formatCurrency(totalIn, false)}", fontSize = 14.sp, color = Emerald600)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Total Out (-)", fontSize = 14.sp, color = Slate700)
                                    Text("₹${Utils.formatCurrency(totalOut, false)}", fontSize = 14.sp, color = Rose600)
                                }
                                HorizontalDivider(color = Slate200)
                                TextButton(
                                    onClick = onNavigateToReports,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("VIEW REPORTS", color = Blue600, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Blue600)
                                }
                            }
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Slate300)
                            Text("Showing ${txWithBalances.size} entries", modifier = Modifier.padding(horizontal = 8.dp), fontSize = 12.sp, color = Slate500)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Slate300)
                        }
                    }
                    
                    grouped.forEach { (dateStr, txs) ->
                        item {
                            Box(modifier = Modifier.fillMaxWidth().background(Slate100).padding(horizontal = 16.dp, vertical = 8.dp)) {
                                Text(dateStr, fontWeight = FontWeight.Bold, color = Slate600, fontSize = 13.sp)
                            }
                        }
                        items(txs) { (tx, bal) ->
                            TransactionItemSimple(
                                transaction = tx,
                                runningBalance = bal,
                                onClick = { onEditEntry(tx) }
                            )
                        }
                    }
                }
            }
            
            // Bottom Action buttons
            Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color.White).padding(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = { onAddEntry("IN") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("CASH IN")
                    }
                    Spacer(Modifier.width(64.dp)) // Space for mic
                    Button(
                        onClick = { onAddEntry("OUT") },
                        modifier = Modifier.weight(1f).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Rose600),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("CASH OUT")
                    }
                }
                
                FloatingActionButton(
                    onClick = {},
                    containerColor = Blue600,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = (-24).dp),
                    elevation = FloatingActionButtonDefaults.elevation(4.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Entry")
                }
            }
        }
    }

    if (showAddMemberDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemberDialog = false },
            title = { Text("Add Member") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newMemberName,
                        onValueChange = { newMemberName = it },
                        label = { Text("Member Name") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = newMemberPhone,
                        onValueChange = { newMemberPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newMemberName.isNotBlank()) {
                        viewModel.addMember(newMemberName, newMemberPhone)
                        showAddMemberDialog = false
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemberDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Cashbook") },
            text = {
                OutlinedTextField(
                    value = newCashbookName,
                    onValueChange = { newCashbookName = it },
                    label = { Text("Cashbook Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCashbookName.isNotBlank() && newCashbookName != cashbook.name) {
                        viewModel.updateCashbookCategory(cashbook.copy(name = newCashbookName))
                    }
                    showRenameDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TransactionItemSimple(transaction: CashTransaction, runningBalance: Double? = null, onClick: () -> Unit = {}) {
    val isIn = transaction.type == "IN"
    val color = if (isIn) Emerald600 else Rose600
    
    val timeStr = remember(transaction.date) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(transaction.date)).lowercase()
    }

    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White).clickable { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Badge
                Box(modifier = Modifier.background(Blue50, RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(transaction.mode.lowercase().replaceFirstChar{it.uppercase()}, color = DarkBlue, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
                
                // Amount & Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(Utils.formatCurrency(transaction.amount, false), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
                    if (runningBalance != null) {
                        Text("Balance: ${Utils.formatCurrency(runningBalance, false)}", fontSize = 12.sp, color = Slate500)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val desc = if (transaction.partyName.isNotBlank()) {
                "${transaction.partyName} - ${transaction.description}"
            } else {
                transaction.description
            }
            Text(desc, fontSize = 14.sp, color = Slate800, modifier = Modifier.padding(end = 40.dp))
            
            Spacer(modifier = Modifier.height(12.dp))
            
            HorizontalDivider(color = Slate100)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Entry by ", fontSize = 12.sp, color = Slate500)
                Text("You", fontSize = 12.sp, color = Blue600, fontWeight = FontWeight.Medium)
                Text(" at $timeStr", fontSize = 12.sp, color = Slate500)
            }
            
            if (transaction.receiptUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(transaction.receiptUri),
                    contentDescription = "Receipt",
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


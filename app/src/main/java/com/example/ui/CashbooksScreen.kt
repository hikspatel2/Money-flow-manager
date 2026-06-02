package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.Utils
import com.example.data.CashbookCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashbooksScreen(
    viewModel: CashbookViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val cashbooks by viewModel.cashbooks.collectAsState()
    val cashbookBalances by viewModel.cashbookBalances.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()

    val allBusinesses by viewModel.allBusinesses.collectAsState()
    val selectedBusinessId by viewModel.selectedBusinessId.collectAsState()
    val selectedBusiness = allBusinesses.find { it.id == selectedBusinessId }

    var expandedSwitcher by remember { mutableStateOf(false) }
    var showAddBusinessDialog by remember { mutableStateOf(false) }
    var newBusinessName by remember { mutableStateOf("") }
    
    val currentCashbooks = if (selectedBusinessId != null) {
        cashbooks.filter { it.businessId == selectedBusinessId }
    } else {
        cashbooks
    }

    if (showAddBusinessDialog) {
        AlertDialog(
            onDismissRequest = { showAddBusinessDialog = false },
            title = { Text("Add Business") },
            text = {
                OutlinedTextField(
                    value = newBusinessName,
                    onValueChange = { newBusinessName = it },
                    label = { Text("Business Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addBusiness(newBusinessName)
                    showAddBusinessDialog = false
                    newBusinessName = ""
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showAddBusinessDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF4F5F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Namaste 👋", fontSize = 14.sp, color = Slate500, fontWeight = FontWeight.Medium)
                        Text("Bhojani Cashbook", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    }
                    IconButton(onClick = { /* Stats */ }, modifier = Modifier.background(Color.White, CircleShape)) {
                        Icon(Icons.Default.BarChart, contentDescription = "Stats", tint = Slate600)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { expandedSwitcher = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Box {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(Blue600, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initial = selectedBusiness?.name?.firstOrNull()?.toString()?.uppercase() ?: "A"
                                    Text(initial, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("BUSINESS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Slate500)
                                    Text(selectedBusiness?.name ?: "All Businesses", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                                }
                            }
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Dropdown", tint = Slate500)
                        }
                        
                        DropdownMenu(
                            expanded = expandedSwitcher,
                            onDismissRequest = { expandedSwitcher = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Businesses") },
                                onClick = { 
                                    viewModel.selectedBusinessId.value = null
                                    expandedSwitcher = false
                                }
                            )
                            Divider()
                            allBusinesses.forEach { biz ->
                                DropdownMenuItem(
                                    text = { Text(biz.name) },
                                    onClick = { 
                                        viewModel.selectedBusinessId.value = biz.id
                                        expandedSwitcher = false
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("+ Add Business") },
                                onClick = { 
                                    expandedSwitcher = false
                                    showAddBusinessDialog = true
                                }
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue600),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("NET BALANCE", color = Color.White.copy(alpha=0.9f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Box(modifier = Modifier.background(Color.White.copy(alpha=0.2f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Live", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${if (stats.netBalance < 0) "-" else ""}₹${Utils.formatCurrency(kotlin.math.abs(stats.netBalance), false)}",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = "${currentCashbooks.size} books • ${transactions.size} entries",
                            color = Color.White.copy(alpha=0.9f),
                            fontSize = 12.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha=0.15f), RoundedCornerShape(12.dp)).padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "In", tint = Emerald500, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Cash In", color = Color.White.copy(alpha=0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("₹${Utils.formatCurrency(stats.totalIn, false)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Divider(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha=0.3f)))
                            Row(modifier = Modifier.weight(1f).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Out", tint = Rose500, modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Cash Out", color = Color.White.copy(alpha=0.9f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                    Text("₹${Utils.formatCurrency(stats.totalOut, false)}", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    placeholder = { Text("Search cashbooks...", fontSize = 14.sp, color = Slate500) },
                    leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = "Search", tint = Slate500) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Cashbooks", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Button(
                        onClick = { 
                            if (selectedBusinessId != null) {
                                viewModel.addCashbookCategory("New Cashbook", selectedBusinessId!!) 
                            } else {
                                // Provide user feedback, for now just default to business logic handled previously
                            }
                        }, 
                        colors = ButtonDefaults.buttonColors(containerColor = Blue100, contentColor = Blue600),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("+ New Book", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            
            items(currentCashbooks) { cb ->
                val balance = cashbookBalances[cb.id] ?: 0.0
                CashbookListItem(
                    cashbook = cb,
                    balance = balance,
                    onClick = {}
                )
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun CashbookListItem(
    cashbook: CashbookCategory,
    balance: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(40.dp).background(Blue100, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Home, contentDescription = null, tint = Blue600)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(cashbook.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text("Customer udhaar ledger", fontSize = 12.sp, color = Slate500)
                }
                Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = Slate400)
            }
            
            val isNegative = balance < 0
            val balanceColor = if (isNegative) Rose500 else Emerald500
            val bgColor = if (isNegative) Rose50 else Emerald50
            
            Row(
                modifier = Modifier.fillMaxWidth().background(bgColor).padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("NET BALANCE", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = balanceColor.copy(alpha=0.8f))
                Text(
                    text = "${if (isNegative) "-" else ""}₹${Utils.formatCurrency(kotlin.math.abs(balance), false)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = balanceColor
                )
            }
        }
    }
}

package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.Utils
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun PaymentsScreen(
    viewModel: CashbookViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.stats.collectAsState()
    val transactions by viewModel.filteredTransactions.collectAsState()
    val cashbooks by viewModel.cashbooks.collectAsState()
    
    var selectedFilter by remember { mutableStateOf("All Entries") }
    val filters = listOf("All Entries", "Party-wise Entries", "Ledger-wise Entries")
    var expandedFilter by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF4F5F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text("Payments", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Breakdown and filters • My Shop", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Medium)
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Blue600),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    // ... the business total code is same, keep it simple by rebuilding or pasting ...
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("BUSINESS TOTAL", color = Color.White.copy(alpha=0.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                            text = "${transactions.size} transactions",
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
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "In", tint = Color(0xFF4ADE80), modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Cash In", color = Color.White.copy(alpha=0.9f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text("₹${Utils.formatCurrency(stats.totalIn, false)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            HorizontalDivider(modifier = Modifier.width(1.dp).height(24.dp).background(Color.White.copy(alpha=0.3f)))
                            Row(modifier = Modifier.weight(1f).padding(start = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(24.dp).background(Color.White.copy(alpha=0.2f), CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Out", tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Cash Out", color = Color.White.copy(alpha=0.9f), fontSize = 10.sp, fontWeight = FontWeight.Medium)
                                    Text("₹${Utils.formatCurrency(stats.totalOut, false)}", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            
            // Filter Selector
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View Mode", fontWeight = FontWeight.Bold, color = Slate900)
                    Box {
                        TextButton(onClick = { expandedFilter = true }) {
                            Text(selectedFilter)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = expandedFilter, onDismissRequest = { expandedFilter = false }) {
                            filters.forEach { filter ->
                                DropdownMenuItem(
                                    text = { Text(filter) },
                                    onClick = { selectedFilter = filter; expandedFilter = false }
                                )
                            }
                        }
                    }
                }
            }

            // List area based on filter
            if (selectedFilter == "All Entries") {
                val sortedTx = transactions.sortedByDescending { it.date }
                if (sortedTx.isEmpty()) {
                    item { Text("No entries.", color = Slate500, modifier = Modifier.padding(16.dp)) }
                } else {
                    items(sortedTx.size) { i ->
                        com.example.ui.TransactionItemSimple(sortedTx[i])
                    }
                }
            } else if (selectedFilter == "Party-wise Entries") {
                val partyGroups = transactions.groupBy { it.partyName }
                if (partyGroups.isEmpty()) {
                    item { Text("No party entries.", color = Slate500, modifier = Modifier.padding(16.dp)) }
                } else {
                    items(partyGroups.keys.toList()) { partyName ->
                        val txs = partyGroups[partyName] ?: emptyList()
                        val mTotalIn = txs.filter{ it.type == "IN" }.sumOf{ it.amount }
                        val mTotalOut = txs.filter{ it.type == "OUT" }.sumOf{ it.amount }
                        val mNet = mTotalIn - mTotalOut
                        PaymentModeCard(partyName.ifBlank { "Unknown Party" }, Icons.Default.Person, txs.size, mNet, mTotalIn, mTotalOut)
                    }
                }
            } else if (selectedFilter == "Ledger-wise Entries") {
                val ledgerGroups = transactions.groupBy { it.cashbookId }
                if (ledgerGroups.isEmpty()) {
                    item { Text("No ledger entries.", color = Slate500, modifier = Modifier.padding(16.dp)) }
                } else {
                    items(ledgerGroups.keys.toList()) { bookId ->
                        val txs = ledgerGroups[bookId] ?: emptyList()
                        val bookName = cashbooks.find { it.id == bookId }?.name ?: "Unknown Ledger"
                        val mTotalIn = txs.filter{ it.type == "IN" }.sumOf{ it.amount }
                        val mTotalOut = txs.filter{ it.type == "OUT" }.sumOf{ it.amount }
                        val mNet = mTotalIn - mTotalOut
                        PaymentModeCard(bookName, Icons.Default.LibraryBooks, txs.size, mNet, mTotalIn, mTotalOut)
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun PaymentModeCard(
    title: String,
    icon: ImageVector,
    entryCount: Int,
    netBalance: Double,
    totalIn: Double,
    totalOut: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).background(Blue100.copy(alpha=0.5f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = title, tint = Blue600)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                        Text("$entryCount entries", fontSize = 12.sp, color = Slate500, fontWeight = FontWeight.Medium)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    val isNegative = netBalance < 0
                    val bColor = if (isNegative) Rose500 else Emerald500
                    Text(
                        text = "${if (isNegative) "-" else ""}₹${Utils.formatCurrency(kotlin.math.abs(netBalance), false)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = bColor
                    )
                    Text("Net Balance", fontSize = 11.sp, color = Slate500)
                }
            }
            
            Divider(color = Slate100)
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CallReceived, contentDescription = "In", tint = Emerald500, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("In", fontSize = 12.sp, color = Slate600)
                    Spacer(modifier = Modifier.width(28.dp))
                    Text("₹${Utils.formatCurrency(totalIn, false)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CallMade, contentDescription = "Out", tint = Rose500, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Out", fontSize = 12.sp, color = Slate600)
                    Spacer(modifier = Modifier.width(28.dp))
                    Text("₹${Utils.formatCurrency(totalOut, false)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
            }
        }
    }
}

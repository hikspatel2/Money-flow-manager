package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CashTransaction
import com.example.data.CashbookCategory
import com.example.ui.theme.*
import com.example.util.Utils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerDetailScreen(
    viewModel: CashbookViewModel,
    cashbook: CashbookCategory,
    onNavigateBack: () -> Unit,
    onEditEntry: (CashTransaction) -> Unit = {}
) {
    val allTransactions by viewModel.allTransactions.collectAsState()
    val ledgerTransactions = allTransactions.filter { it.cashbookId == cashbook.id }.sortedByDescending { it.date }
    
    val totalIn = ledgerTransactions.filter { it.type == "IN" }.sumOf { it.amount }
    val totalOut = ledgerTransactions.filter { it.type == "OUT" }.sumOf { it.amount }
    val balance = totalIn - totalOut

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cashbook.name, fontWeight = FontWeight.Bold, color = Slate900) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Slate900)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF4F5F9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Stats Header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isNegative = balance < 0
                    val balanceColor = if (isNegative) Rose600 else Emerald600
                    Text("Net Balance", fontSize = 12.sp, color = Slate500)
                    Text(
                        "${if (isNegative) "-" else ""}₹${Utils.formatCurrency(kotlin.math.abs(balance), false)}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = balanceColor
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Total In (+)", fontSize = 12.sp, color = Slate500)
                            Text("₹${Utils.formatCurrency(totalIn, false)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Emerald600)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Total Out (-)", fontSize = 12.sp, color = Slate500)
                            Text("₹${Utils.formatCurrency(totalOut, false)}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Rose600)
                        }
                    }
                }
            }

            // Entries List
            Text(
                "Recent Entries",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (ledgerTransactions.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No entries found in this ledger.", color = Slate500)
                        }
                    }
                } else {
                    items(ledgerTransactions) { tx ->
                        TransactionItemSimple(tx, onClick = { onEditEntry(tx) })
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionItemSimple(transaction: CashTransaction, onClick: () -> Unit = {}) {
    val isIn = transaction.type == "IN"
    val color = if (isIn) Emerald600 else Rose600
    val sign = if (isIn) "+" else "-"

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(transaction.partyName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                    Text(transaction.description, fontSize = 12.sp, color = Slate500)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("$sign₹${Utils.formatCurrency(transaction.amount, false)}", fontWeight = FontWeight.Bold, color = color)
                    Text(Utils.formatDate(transaction.date), fontSize = 10.sp, color = Slate400)
                }
            }
            if (transaction.receiptUri != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(transaction.receiptUri),
                    contentDescription = "Receipt Image",
                    modifier = Modifier.fillMaxWidth().height(100.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

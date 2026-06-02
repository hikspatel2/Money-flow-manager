package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CashbookCategory
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageLedgersScreen(
    viewModel: CashbookViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cashbooks by viewModel.cashbooks.collectAsState()
    
    // Modal dialogue flows
    var showAddLedgerDialog by remember { mutableStateOf(false) }
    var ledgerNameInput by remember { mutableStateOf("") }

    var editingCategory by remember { mutableStateOf<CashbookCategory?>(null) }
    var showEditLedgerDialog by remember { mutableStateOf(false) }
    var editLedgerInput by remember { mutableStateOf("") }

    var deletingCategory by remember { mutableStateOf<CashbookCategory?>(null) }
    var showDeleteLedgerDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DesignBackground)
    ) {
        // Appbar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DesignBackground)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(26.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Slate900
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Manage Ledgers",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Customize and manage the ledgers in your cashbook manager.",
                color = Slate600,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "YOUR LEDGERS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Blue600,
                    letterSpacing = 0.5.sp
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Blue50,
                    modifier = Modifier.clickable {
                        ledgerNameInput = ""
                        showAddLedgerDialog = true
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "add icon", modifier = Modifier.size(16.dp), tint = Blue600)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Ledger", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Blue600)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                cashbooks.forEach { cb ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Slate200), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(40.dp).background(Blue100, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cb.name.firstOrNull()?.toString()?.uppercase() ?: "L",
                                    color = Blue600,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = cb.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate800,
                            )
                        }
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Slate50,
                                modifier = Modifier.clickable {
                                    editingCategory = cb
                                    editLedgerInput = cb.name
                                    showEditLedgerDialog = true
                                }.border(BorderStroke(1.dp, Slate200), CircleShape)
                            ) {
                                Box(modifier = Modifier.padding(8.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename category", tint = Slate600, modifier = Modifier.size(18.dp))
                                }
                            }

                            if (cashbooks.size > 1) {
                                Surface(
                                    shape = CircleShape,
                                    color = Rose50,
                                    modifier = Modifier.clickable {
                                        deletingCategory = cb
                                        showDeleteLedgerDialog = true
                                    }.border(BorderStroke(1.dp, Rose50), CircleShape)
                                ) {
                                    Box(modifier = Modifier.padding(8.dp)) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete category", tint = Rose500, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    
    // Dialogs: Creating cashbooks categories
    if (showAddLedgerDialog) {
        AlertDialog(
            onDismissRequest = { showAddLedgerDialog = false },
            title = { Text("Add Ledger Cashbook", fontWeight = FontWeight.Bold, color = Slate900) },
            text = {
                Column {
                    Text("Provide a custom descriptive name for your ledger cashbook:", color = Slate600, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = ledgerNameInput,
                        onValueChange = { ledgerNameInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_ledger_input"),
                        placeholder = { Text("e.g., Office Supplies, Workshop B", color = Slate400) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue600,
                            unfocusedBorderColor = Slate200,
                            cursorColor = Blue600
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (ledgerNameInput.isNotBlank()) {
                            val activeBiz = viewModel.selectedBusinessId.value ?: 1
                            viewModel.addCashbookCategory(ledgerNameInput.trim(), activeBiz)
                            showAddLedgerDialog = false
                        }
                    },
                    modifier = Modifier.testTag("dialog_confirm_add"),
                    colors = ButtonDefaults.textButtonColors(contentColor = Blue600)
                ) {
                    Text("Add Ledger", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddLedgerDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate600)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialogs: Editing cashbook catalog Name
    if (showEditLedgerDialog && editingCategory != null) {
        AlertDialog(
            onDismissRequest = { showEditLedgerDialog = false },
            title = { Text("Rename Ledger Category", fontWeight = FontWeight.Bold, color = Slate900) },
            text = {
                Column {
                    Text("Update title label for ${editingCategory?.name}:", color = Slate600, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editLedgerInput,
                        onValueChange = { editLedgerInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dialog_edit_ledger_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Blue600,
                            unfocusedBorderColor = Slate200,
                            cursorColor = Blue600
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val currentCategory = editingCategory
                        if (editLedgerInput.isNotBlank() && currentCategory != null) {
                            viewModel.updateCashbookCategory(currentCategory.copy(name = editLedgerInput.trim()))
                            showEditLedgerDialog = false
                            editingCategory = null
                        }
                    },
                    modifier = Modifier.testTag("dialog_confirm_edit"),
                    colors = ButtonDefaults.textButtonColors(contentColor = Blue600)
                ) {
                    Text("Rename", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditLedgerDialog = false
                        editingCategory = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate600)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialogs: Category permanent purge
    if (showDeleteLedgerDialog && deletingCategory != null) {
        AlertDialog(
            onDismissRequest = { showDeleteLedgerDialog = false },
            title = { Text("Remove Ledger Cashbook", fontWeight = FontWeight.Bold, color = Slate900) },
            text = { Text("Are you sure you want to delete ledger book \"${deletingCategory?.name}\"? All transactions under this category will also be permanently deleted. This action cannot be undone.", color = Slate600, fontSize = 14.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cat = deletingCategory
                        if (cat != null) {
                            viewModel.deleteCashbookCategory(cat)
                            showDeleteLedgerDialog = false
                            deletingCategory = null
                        }
                    },
                    modifier = Modifier.testTag("dialog_confirm_delete_ledger"),
                    colors = ButtonDefaults.textButtonColors(contentColor = Rose600)
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteLedgerDialog = false
                        deletingCategory = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Slate600)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

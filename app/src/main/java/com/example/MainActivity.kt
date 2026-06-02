package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.CashTransaction
import com.example.data.CashbookRepository
import com.example.ui.AddEditEntryScreen
import com.example.ui.CashbookViewModel
import com.example.ui.CashbookViewModelFactory
import com.example.ui.CashbooksScreen
import com.example.ui.PaymentsScreen
import com.example.ui.PassbookScreen
import com.example.ui.SettingsScreen
import com.example.ui.RecurringTransactionsScreen
import com.example.ui.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Blue100
import com.example.ui.theme.Blue600
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = CashbookRepository(this)
        val viewModelFactory = CashbookViewModelFactory(application, repository)
        
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        setContent {
            MyApplicationTheme {
                val viewModel: CashbookViewModel = viewModel(factory = viewModelFactory)
                MainAppContainer(
                    viewModel = viewModel, 
                    sharedPrefs = sharedPrefs
                )
            }
        }
    }
}

enum class NavigationTab {
    CASHBOOKS, PAYMENTS, PASSBOOK, SETTINGS
}

@Composable
fun MainAppContainer(viewModel: CashbookViewModel, sharedPrefs: android.content.SharedPreferences) {
    var showSplash by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(sharedPrefs.getBoolean("is_logged_in", false)) }
    var activeTab by remember { mutableStateOf(NavigationTab.CASHBOOKS) }
    var activeEditingTransaction by remember { mutableStateOf<CashTransaction?>(null) }
    var addingEntryType by remember { mutableStateOf<String?>(null) }

    if (showSplash) {
        com.example.ui.SplashScreen(onTimeout = { showSplash = false })
    } else if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = {
                sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
                isLoggedIn = true
            }
        )
    } else if (activeEditingTransaction != null) {
        AddEditEntryScreen(
            viewModel = viewModel,
            initialType = "IN",
            editingTransaction = activeEditingTransaction,
            onNavigateBack = { activeEditingTransaction = null }
        )
    } else if (addingEntryType != null) {
        AddEditEntryScreen(
            viewModel = viewModel,
            initialType = addingEntryType ?: "IN",
            editingTransaction = null,
            onNavigateBack = { addingEntryType = null }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                if (activeTab == NavigationTab.CASHBOOKS || activeTab == NavigationTab.PASSBOOK) {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = { addingEntryType = "IN" },
                        containerColor = Blue600,
                        contentColor = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier.testTag("fab_add_entry")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Entry")
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars).testTag("bottom_nav_bar"),
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = activeTab == NavigationTab.CASHBOOKS,
                        onClick = { activeTab = NavigationTab.CASHBOOKS },
                        icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Cashbooks") },
                        label = { Text("Cashbooks", fontSize = 11.sp, fontWeight = FontWeightMedium(activeTab == NavigationTab.CASHBOOKS)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue600,
                            selectedTextColor = Blue600,
                            indicatorColor = Blue100,
                            unselectedTextColor = Slate600,
                            unselectedIconColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_dashboard")
                    )

                    NavigationBarItem(
                        selected = activeTab == NavigationTab.PAYMENTS,
                        onClick = { activeTab = NavigationTab.PAYMENTS },
                        icon = { Icon(Icons.Default.CreditCard, contentDescription = "Payments") },
                        label = { Text("Payments", fontSize = 11.sp, fontWeight = FontWeightMedium(activeTab == NavigationTab.PAYMENTS)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue600,
                            selectedTextColor = Blue600,
                            indicatorColor = Blue100,
                            unselectedTextColor = Slate600,
                            unselectedIconColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_payments")
                    )

                    NavigationBarItem(
                        selected = activeTab == NavigationTab.PASSBOOK,
                        onClick = { activeTab = NavigationTab.PASSBOOK },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Passbook") },
                        label = { Text("Passbook", fontSize = 11.sp, fontWeight = FontWeightMedium(activeTab == NavigationTab.PASSBOOK)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue600,
                            selectedTextColor = Blue600,
                            indicatorColor = Blue100,
                            unselectedTextColor = Slate600,
                            unselectedIconColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_passbook")
                    )

                    NavigationBarItem(
                        selected = activeTab == NavigationTab.SETTINGS,
                        onClick = { activeTab = NavigationTab.SETTINGS },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeightMedium(activeTab == NavigationTab.SETTINGS)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Blue600,
                            selectedTextColor = Blue600,
                            indicatorColor = Blue100,
                            unselectedTextColor = Slate600,
                            unselectedIconColor = Slate400
                        ),
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    NavigationTab.CASHBOOKS -> com.example.ui.CashbooksScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.PAYMENTS -> com.example.ui.PaymentsScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.PASSBOOK -> com.example.ui.PassbookScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.SETTINGS -> com.example.ui.SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToRecurring = { /* We could set up navigation to recurring screen */ },
                        onLogout = {
                            sharedPrefs.edit().putBoolean("is_logged_in", false).apply()
                            isLoggedIn = false
                        }
                    )
                }
            }
        }
    }
}

// Small font weight helper
@Composable
private fun FontWeightMedium(selected: Boolean): FontWeight {
    return if (selected) FontWeight.Bold else FontWeight.Medium
}

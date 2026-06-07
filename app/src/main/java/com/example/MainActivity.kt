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
import androidx.compose.runtime.LaunchedEffect
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

import androidx.compose.foundation.verticalScroll

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.workers.BackupWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()

            // Setup daily backup worker
            val constraints = Constraints.Builder()
                .setRequiresDeviceIdle(false)
                .setRequiresCharging(false)
                .build()
                
            val backupWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()
                
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "daily_database_backup",
                ExistingPeriodicWorkPolicy.KEEP,
                backupWorkRequest
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }

        val repository = CashbookRepository(this)
        val viewModelFactory = CashbookViewModelFactory(application, repository)
        
        val sharedPrefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        try {
            setContent {
                MyApplicationTheme {
                    val viewModel: CashbookViewModel = viewModel(factory = viewModelFactory)
                    MainAppContainer(
                        viewModel = viewModel, 
                        sharedPrefs = sharedPrefs
                    )
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            finish()
        }
    }
}

enum class NavigationTab {
    CASHBOOKS, PAYMENTS, PASSBOOK, SETTINGS
}

@Composable
fun MainAppContainer(viewModel: CashbookViewModel, sharedPrefs: android.content.SharedPreferences) {
    var showSplash by remember { mutableStateOf(true) }
    var isLoggedIn by remember { 
        val fireAuthLogged = try {
            com.example.FirebaseAuthService.getInstance()?.currentUser != null
        } catch (e: Throwable) {
            true // Fallback to true or false, but let's just let sharedPrefs decide if it crashed
        }
        mutableStateOf(sharedPrefs.getBoolean("is_logged_in", false) && fireAuthLogged) 
    }
    
    var loginProvider by remember { mutableStateOf(sharedPrefs.getString("login_provider", "")) }
    var storedMpin by remember { mutableStateOf(sharedPrefs.getString("mpin_hash", null)) }
    var isMpinUnlocked by remember { mutableStateOf(false) }
    var wrongMpinAttempts by remember { mutableStateOf(0) }
    
    var activeTab by remember { mutableStateOf(NavigationTab.CASHBOOKS) }
    var activeEditingTransaction by remember { mutableStateOf<CashTransaction?>(null) }
    var addingEntryType by remember { mutableStateOf<String?>(null) }
    var activeCashbook by remember { mutableStateOf<com.example.data.CashbookCategory?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(isLoggedIn, isMpinUnlocked) {
        if (isLoggedIn && (storedMpin == null || isMpinUnlocked)) {
            viewModel.syncWithFirestore()
        }
    }

    if (showSplash) {
        com.example.ui.SplashScreen(onTimeout = { showSplash = false })
    } else if (!isLoggedIn) {
        LoginScreen(
            onLoginSuccess = { provider ->
                sharedPrefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("login_provider", provider)
                    .apply()
                loginProvider = provider
                isLoggedIn = true
            }
        )
    } else if (storedMpin == null) {
        com.example.ui.MpinScreen(
            mode = com.example.ui.MpinMode.CREATE,
            onSuccess = { mpin ->
                val hash = SecurityUtil.hashMpin(mpin)
                sharedPrefs.edit().putString("mpin_hash", hash).apply()
                storedMpin = hash
                isMpinUnlocked = true
            }
        )
    } else if (!isMpinUnlocked) {
        com.example.ui.MpinScreen(
            mode = com.example.ui.MpinMode.UNLOCK,
            onSuccess = { mpin ->
                val hash = SecurityUtil.hashMpin(mpin)
                if (hash == storedMpin) {
                    isMpinUnlocked = true
                    wrongMpinAttempts = 0
                } else {
                    wrongMpinAttempts++
                    if (wrongMpinAttempts >= 5) {
                        android.widget.Toast.makeText(context, "Too many wrong attempts. Security Logout.", android.widget.Toast.LENGTH_LONG).show()
                        com.example.FirebaseAuthService.getInstance()?.signOut()
                        sharedPrefs.edit()
                            .putBoolean("is_logged_in", false)
                            .remove("mpin_hash") // Optional: remove mpin on too many wrong attempts or just force re-auth
                            .apply()
                        isLoggedIn = false
                        storedMpin = null
                        wrongMpinAttempts = 0
                    } else {
                        android.widget.Toast.makeText(context, "Wrong MPIN ($wrongMpinAttempts/5)", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onForgotMpin = {
                // If they forgot MPIN, require re-authentication by logging out
                com.example.FirebaseAuthService.getInstance()?.signOut()
                sharedPrefs.edit()
                    .putBoolean("is_logged_in", false)
                    .remove("mpin_hash")
                    .apply()
                isLoggedIn = false
                storedMpin = null
                android.widget.Toast.makeText(context, "Please login again to reset MPIN", android.widget.Toast.LENGTH_LONG).show()
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
    } else if (activeCashbook != null) {
        com.example.ui.LedgerDetailScreen(
            viewModel = viewModel,
            cashbook = activeCashbook!!,
            onNavigateBack = { activeCashbook = null },
            onEditEntry = { activeEditingTransaction = it },
            onAddEntry = { addingEntryType = it }
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
                        viewModel = viewModel,
                        onLedgerClick = { activeCashbook = it }
                    )
                    NavigationTab.PAYMENTS -> com.example.ui.PaymentsScreen(
                        viewModel = viewModel,
                        onEditEntry = { activeEditingTransaction = it }
                    )
                    NavigationTab.PASSBOOK -> com.example.ui.PassbookScreen(
                        viewModel = viewModel
                    )
                    NavigationTab.SETTINGS -> com.example.ui.SettingsScreen(
                        viewModel = viewModel,
                        onNavigateToRecurring = { /* We could set up navigation to recurring screen */ },
                        onLogout = {
                            com.example.FirebaseAuthService.getInstance()?.signOut()
                            sharedPrefs.edit()
                                .putBoolean("is_logged_in", false)
                                .remove("login_provider")
                                .remove("mpin_hash")
                                .apply()
                            isLoggedIn = false
                            storedMpin = null
                            isMpinUnlocked = false
                            activeTab = NavigationTab.CASHBOOKS
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

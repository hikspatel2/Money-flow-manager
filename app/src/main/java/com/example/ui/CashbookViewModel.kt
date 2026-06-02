package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.Utils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class CashbookViewModel(
    application: Application,
    private val repository: CashbookRepository
) : AndroidViewModel(application) {

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allBusinesses: StateFlow<List<Business>> = repository.allBusinesses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cashbooks: StateFlow<List<CashbookCategory>> = repository.allCashbooks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<CashTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allRecurringTransactions: StateFlow<List<RecurringTransaction>> = repository.allRecurringTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filter states
    val selectedTimeframe = MutableStateFlow("All") // "Today", "7 Days", "30 Days", "This Year", "All", "Custom"
    val customDateRange = MutableStateFlow<Pair<Long, Long>>(
        Pair(
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0) }.timeInMillis,
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }.timeInMillis
        )
    )
    val selectedBusinessId = MutableStateFlow<Int?>(null) // null = All businesses
    val selectedCategoryFilter = MutableStateFlow<Int?>(null) // null = All
    val selectedEntryType = MutableStateFlow("All") // "All", "IN", "OUT"
    val selectedPaymentMode = MutableStateFlow("All Modes") // "All Modes", "Cash", "UPI", "Bank"
    val searchQuery = MutableStateFlow("")

    init {
        // Run seeding and sync asynchronously on startup
        viewModelScope.launch {
            // repository.checkAndSeedDefaults()
            // repository.syncRecurringTransactions()
        }
    }

    // Active screen state or simple state transitions for robust single-activity stack
    // We can also have an indicator of the active transaction being edited
    val editingTransaction = MutableStateFlow<CashTransaction?>(null)

    private data class FilterState(
        val timeframe: String,
        val dateRange: Pair<Long, Long>,
        val catFilter: Int?,
        val query: String,
        val bizFilter: Int?,
        val entryType: String,
        val paymentMode: String
    )

    private data class FilterGroup1(
        val t: String,
        val d: Pair<Long, Long>,
        val c: Int?,
        val q: String
    )

    private val filtersFlow = combine(
        combine(selectedTimeframe, customDateRange, selectedCategoryFilter, searchQuery) { t, d, c, q ->
            FilterGroup1(t, d, c, q)
        },
        selectedBusinessId,
        selectedEntryType,
        selectedPaymentMode
    ) { group1, biz, type, mode ->
        FilterState(
            timeframe = group1.t,
            dateRange = group1.d,
            catFilter = group1.c,
            query = group1.q,
            bizFilter = biz,
            entryType = type,
            paymentMode = mode
        )
    }

    // Derived State Flow for Filtered Transactions
    val filteredTransactions: StateFlow<List<CashTransaction>> = combine(
        repository.allTransactions,
        cashbooks,
        filtersFlow
    ) { trans, allCash, filters ->
        trans.filter { t ->
            val cashbook = allCash.find { it.id == t.cashbookId }
            val matchesBusiness = filters.bizFilter == null || cashbook?.businessId == filters.bizFilter
            val matchesCategory = filters.catFilter == null || t.cashbookId == filters.catFilter
            val matchesQuery = filters.query.isEmpty() ||
                    t.description.contains(filters.query, ignoreCase = true) ||
                    t.partyName.contains(filters.query, ignoreCase = true) ||
                    Utils.formatCurrency(t.amount).contains(filters.query)

            val matchesDate = when (filters.timeframe) {
                "Today" -> Utils.isToday(t.date)
                "7 Days" -> Utils.isPastDays(t.date, 7)
                "30 Days" -> Utils.isPastDays(t.date, 30)
                "This Year" -> Utils.isThisYear(t.date)
                "Custom" -> t.date in (filters.dateRange.first..filters.dateRange.second)
                else -> true // "All"
            }
            
            val matchesType = filters.entryType == "All" || 
                              (filters.entryType == "IN" && t.type == "IN") || 
                              (filters.entryType == "OUT" && t.type == "OUT")
            
            val matchesMode = filters.paymentMode == "All Modes" || t.mode.equals(filters.paymentMode, ignoreCase = true)

            matchesBusiness && matchesCategory && matchesQuery && matchesDate && matchesType && matchesMode
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // State flows for dashboard/reports statistics based on selected options
    val stats = filteredTransactions.map { list ->
        var totalIn = 0.0
        var totalOut = 0.0
        for (t in list) {
            if (t.type == "IN") {
                totalIn += t.amount
            } else {
                totalOut += t.amount
            }
        }
        val netBalance = totalIn - totalOut
        TransactionStats(totalIn, totalOut, netBalance)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TransactionStats())

    // All-time statistics for each ledger category
    val cashbookBalances: StateFlow<Map<Int, Double>> = repository.allTransactions.map { list ->
        val balances = mutableMapOf<Int, Double>()
        for (t in list) {
            val amount = if (t.type == "IN") t.amount else -t.amount
            balances[t.cashbookId] = (balances[t.cashbookId] ?: 0.0) + amount
        }
        balances
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Database manipulation functions wrapped in viewModelScope
    fun upsertUserProfile(email: String, name: String, businessName: String, phone: String) {
        viewModelScope.launch {
            repository.saveUserProfile(UserProfile(email, name, businessName, phone))
        }
    }

    fun addCashbookCategory(name: String, businessId: Int = 1) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addCashbook(name.trim(), businessId)
            }
        }
    }

    fun updateCashbookCategory(category: CashbookCategory) {
        viewModelScope.launch {
            repository.updateCashbook(category)
        }
    }

    fun deleteCashbookCategory(category: CashbookCategory) {
        viewModelScope.launch {
            repository.deleteCashbook(category)
        }
    }

    fun addBusiness(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.addBusiness(name.trim())
            }
        }
    }

    fun deleteBusiness(business: Business) {
        viewModelScope.launch {
            repository.deleteBusiness(business)
        }
    }

    fun saveTransaction(
        id: Int = 0,
        type: String,
        amount: Double,
        description: String,
        partyName: String,
        date: Long,
        mode: String,
        cashbookId: Int,
        receiptUri: String? = null
    ) {
        viewModelScope.launch {
            val tx = CashTransaction(
                id = id,
                type = type,
                amount = amount,
                description = description,
                partyName = partyName,
                date = date,
                mode = mode,
                cashbookId = cashbookId,
                receiptUri = receiptUri
            )
            if (id == 0) {
                repository.addTransaction(tx)
            } else {
                repository.updateTransaction(tx)
            }
        }
    }

    fun deleteTransaction(transaction: CashTransaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addRecurringTransaction(
        type: String,
        amount: Double,
        description: String,
        partyName: String,
        mode: String,
        cashbookId: Int,
        frequency: String,
        startDate: Long,
        endDate: Long
    ) {
        viewModelScope.launch {
            val rt = RecurringTransaction(
                type = type,
                amount = amount,
                description = description,
                partyName = partyName,
                mode = mode,
                cashbookId = cashbookId,
                frequency = frequency,
                startDate = startDate,
                endDate = endDate
            )
            repository.addRecurringTransaction(rt)
            // Immediately sync transactions so any due transactions are created
            repository.syncRecurringTransactions()
        }
    }

    fun deleteRecurringTransaction(rt: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(rt)
        }
    }

    fun triggerRecurringSync() {
        viewModelScope.launch {
            repository.syncRecurringTransactions()
        }
    }

    fun getBackupJson(): String? {
        return repository.exportData()
    }

    suspend fun restoreBackupJson(json: String): Boolean {
        return repository.importData(json)
    }
}

data class TransactionStats(
    val totalIn: Double = 0.0,
    val totalOut: Double = 0.0,
    val netBalance: Double = 0.0
)

class CashbookViewModelFactory(
    private val application: Application,
    private val repository: CashbookRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CashbookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CashbookViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

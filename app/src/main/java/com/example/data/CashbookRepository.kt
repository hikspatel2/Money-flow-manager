package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.util.Calendar

class CashbookRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("cashbook_prefs", Context.MODE_PRIVATE)
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Using Moshi to serialize lists
    private val profileAdapter = moshi.adapter(UserProfile::class.java)
    private val cashbookListAdapter = moshi.adapter<List<CashbookCategory>>(Types.newParameterizedType(List::class.java, CashbookCategory::class.java))
    private val transactionListAdapter = moshi.adapter<List<CashTransaction>>(Types.newParameterizedType(List::class.java, CashTransaction::class.java))
    private val recurringListAdapter = moshi.adapter<List<RecurringTransaction>>(Types.newParameterizedType(List::class.java, RecurringTransaction::class.java))
    private val businessListAdapter = moshi.adapter<List<Business>>(Types.newParameterizedType(List::class.java, Business::class.java))

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    private val _allBusinesses = MutableStateFlow<List<Business>>(emptyList())
    val allBusinesses: StateFlow<List<Business>> = _allBusinesses

    private val _allCashbooks = MutableStateFlow<List<CashbookCategory>>(emptyList())
    val allCashbooks: StateFlow<List<CashbookCategory>> = _allCashbooks

    private val _allTransactions = MutableStateFlow<List<CashTransaction>>(emptyList())
    val allTransactions: StateFlow<List<CashTransaction>> = _allTransactions

    private val _allRecurringTransactions = MutableStateFlow<List<RecurringTransaction>>(emptyList())
    val allRecurringTransactions: StateFlow<List<RecurringTransaction>> = _allRecurringTransactions

    init {
        loadData()
    }

    private fun loadData() {
        try {
            // Load User Profile
            val profileJson = prefs.getString("user_profile", null)
            if (profileJson != null) {
                _userProfile.value = profileAdapter.fromJson(profileJson)
            }

            // Load Businesses
            val businessJson = prefs.getString("businesses", null)
            if (businessJson != null) {
                _allBusinesses.value = businessListAdapter.fromJson(businessJson) ?: emptyList()
            }

            // Load Cashbooks
            val cashbooksJson = prefs.getString("cashbooks", null)
            if (cashbooksJson != null) {
                _allCashbooks.value = cashbookListAdapter.fromJson(cashbooksJson) ?: emptyList()
            }

            // Load Transactions
            val transactionsJson = prefs.getString("transactions", null)
            if (transactionsJson != null) {
                _allTransactions.value = transactionListAdapter.fromJson(transactionsJson) ?: emptyList()
            }

            // Load Recurring Transactions
            val recurringJson = prefs.getString("recurring_transactions", null)
            if (recurringJson != null) {
                _allRecurringTransactions.value = recurringListAdapter.fromJson(recurringJson) ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Clear corrupted or old schema data to prevent recurring crashes
            prefs.edit().clear().apply()
            _userProfile.value = null
            _allBusinesses.value = emptyList()
            _allCashbooks.value = emptyList()
            _allTransactions.value = emptyList()
            _allRecurringTransactions.value = emptyList()
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        _userProfile.value = profile
        prefs.edit().putString("user_profile", profileAdapter.toJson(profile)).apply()
    }

    suspend fun addBusiness(name: String): Long = withContext(Dispatchers.IO) {
        val current = _allBusinesses.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newBusiness = Business(id = newId, name = name)
        current.add(newBusiness)
        _allBusinesses.value = current
        prefs.edit().putString("businesses", businessListAdapter.toJson(current)).apply()
        newId.toLong()
    }

    suspend fun updateBusiness(business: Business) = withContext(Dispatchers.IO) {
        val current = _allBusinesses.value.toMutableList()
        val index = current.indexOfFirst { it.id == business.id }
        if (index != -1) {
            current[index] = business
            _allBusinesses.value = current
            prefs.edit().putString("businesses", businessListAdapter.toJson(current)).apply()
        }
    }

    suspend fun deleteBusiness(business: Business) = withContext(Dispatchers.IO) {
        val current = _allBusinesses.value.toMutableList()
        current.removeAll { it.id == business.id }
        _allBusinesses.value = current
        prefs.edit().putString("businesses", businessListAdapter.toJson(current)).apply()

        // Optionally, delete related cashbooks and transactions...
        // For simplicity now, we just let them be orphaned or we can enforce constraints later.
    }

    suspend fun addCashbook(name: String, businessId: Int = 1): Long = withContext(Dispatchers.IO) {
        val current = _allCashbooks.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newCategory = CashbookCategory(id = newId, name = name, businessId = businessId)
        current.add(newCategory)
        _allCashbooks.value = current
        prefs.edit().putString("cashbooks", cashbookListAdapter.toJson(current)).apply()
        newId.toLong()
    }

    suspend fun updateCashbook(cashbook: CashbookCategory) = withContext(Dispatchers.IO) {
        val current = _allCashbooks.value.toMutableList()
        val index = current.indexOfFirst { it.id == cashbook.id }
        if (index != -1) {
            current[index] = cashbook
            _allCashbooks.value = current
            prefs.edit().putString("cashbooks", cashbookListAdapter.toJson(current)).apply()
        }
    }

    suspend fun deleteCashbook(cashbook: CashbookCategory) = withContext(Dispatchers.IO) {
        val current = _allCashbooks.value.toMutableList()
        current.removeAll { it.id == cashbook.id }
        _allCashbooks.value = current
        prefs.edit().putString("cashbooks", cashbookListAdapter.toJson(current)).apply()

        // Also delete transactions in this cashbook
        val currentTx = _allTransactions.value.toMutableList()
        currentTx.removeAll { it.cashbookId == cashbook.id }
        _allTransactions.value = currentTx
        prefs.edit().putString("transactions", transactionListAdapter.toJson(currentTx)).apply()
    }

    suspend fun addTransaction(transaction: CashTransaction): Long = withContext(Dispatchers.IO) {
        val current = _allTransactions.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newTx = transaction.copy(id = newId)
        current.add(newTx)
        current.sortByDescending { it.date }
        _allTransactions.value = current
        prefs.edit().putString("transactions", transactionListAdapter.toJson(current)).apply()
        newId.toLong()
    }

    suspend fun updateTransaction(transaction: CashTransaction) = withContext(Dispatchers.IO) {
        val current = _allTransactions.value.toMutableList()
        val index = current.indexOfFirst { it.id == transaction.id }
        if (index != -1) {
            current[index] = transaction
            current.sortByDescending { it.date }
            _allTransactions.value = current
            prefs.edit().putString("transactions", transactionListAdapter.toJson(current)).apply()
        }
    }

    suspend fun deleteTransaction(transaction: CashTransaction) = withContext(Dispatchers.IO) {
        val current = _allTransactions.value.toMutableList()
        current.removeAll { it.id == transaction.id }
        _allTransactions.value = current
        prefs.edit().putString("transactions", transactionListAdapter.toJson(current)).apply()
    }

    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction): Long = withContext(Dispatchers.IO) {
        val current = _allRecurringTransactions.value.toMutableList()
        val newId = (current.maxOfOrNull { it.id } ?: 0) + 1
        val newRt = recurringTransaction.copy(id = newId)
        current.add(newRt)
        _allRecurringTransactions.value = current
        prefs.edit().putString("recurring_transactions", recurringListAdapter.toJson(current)).apply()
        newId.toLong()
    }

    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) = withContext(Dispatchers.IO) {
        val current = _allRecurringTransactions.value.toMutableList()
        val index = current.indexOfFirst { it.id == recurringTransaction.id }
        if (index != -1) {
            current[index] = recurringTransaction
            _allRecurringTransactions.value = current
            prefs.edit().putString("recurring_transactions", recurringListAdapter.toJson(current)).apply()
        }
    }

    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) = withContext(Dispatchers.IO) {
        val current = _allRecurringTransactions.value.toMutableList()
        current.removeAll { it.id == recurringTransaction.id }
        _allRecurringTransactions.value = current
        prefs.edit().putString("recurring_transactions", recurringListAdapter.toJson(current)).apply()
    }

    suspend fun syncRecurringTransactions() = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        var txAdded = false
        val rtList = _allRecurringTransactions.value.toMutableList()
        
        for (i in rtList.indices) {
            val rt = rtList[i]
            val limitTime = if (rt.endDate < currentTime) rt.endDate else currentTime
            val cal = Calendar.getInstance()
            cal.timeInMillis = rt.startDate

            var lastGen = rt.lastGeneratedDate
            var modified = false
            var guardCounter = 0

            while (cal.timeInMillis <= limitTime && guardCounter < 500) {
                val occurrenceTime = cal.timeInMillis
                if (occurrenceTime > lastGen) {
                    addTransaction(
                        CashTransaction(
                            type = rt.type,
                            amount = rt.amount,
                            description = rt.description,
                            partyName = rt.partyName,
                            date = occurrenceTime,
                            mode = rt.mode,
                            cashbookId = rt.cashbookId
                        )
                    )
                    lastGen = occurrenceTime
                    modified = true
                    txAdded = true
                }

                when (rt.frequency.uppercase()) {
                    "DAILY" -> cal.add(Calendar.DAY_OF_YEAR, 1)
                    "WEEKLY" -> cal.add(Calendar.WEEK_OF_YEAR, 1)
                    "MONTHLY" -> cal.add(Calendar.MONTH, 1)
                    "YEARLY" -> cal.add(Calendar.YEAR, 1)
                    else -> cal.add(Calendar.DAY_OF_YEAR, 1)
                }
                guardCounter++
            }

            if (modified) {
                rtList[i] = rt.copy(lastGeneratedDate = lastGen)
            }
        }
        
        if (txAdded) {
            _allRecurringTransactions.value = rtList
            prefs.edit().putString("recurring_transactions", recurringListAdapter.toJson(rtList)).apply()
        }
    }

    fun exportData(): String? {
        return try {
            val backup = AppBackup(
                userProfile = _userProfile.value,
                businesses = _allBusinesses.value,
                cashbooks = _allCashbooks.value,
                transactions = _allTransactions.value,
                recurringTransactions = _allRecurringTransactions.value
            )
            val adapter = moshi.adapter(AppBackup::class.java)
            adapter.toJson(backup)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun importData(json: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val adapter = moshi.adapter(AppBackup::class.java)
            val backup = adapter.fromJson(json)
            if (backup != null) {
                _userProfile.value = backup.userProfile
                _allBusinesses.value = backup.businesses
                _allCashbooks.value = backup.cashbooks
                _allTransactions.value = backup.transactions
                _allRecurringTransactions.value = backup.recurringTransactions

                val editor = prefs.edit()
                if (backup.userProfile != null) editor.putString("user_profile", profileAdapter.toJson(backup.userProfile))
                editor.putString("businesses", businessListAdapter.toJson(backup.businesses))
                editor.putString("cashbooks", cashbookListAdapter.toJson(backup.cashbooks))
                editor.putString("transactions", transactionListAdapter.toJson(backup.transactions))
                editor.putString("recurring_transactions", recurringListAdapter.toJson(backup.recurringTransactions))
                editor.apply()
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun checkAndSeedDefaults() = withContext(Dispatchers.IO) {
        if (_allBusinesses.value.isEmpty()) {
            addBusiness("My Business")
        }
        if (_allCashbooks.value.isEmpty()) {
            val defaultBizId = _allBusinesses.value.firstOrNull()?.id ?: 1
            addCashbook("Business", defaultBizId)
            addCashbook("Salary", defaultBizId)
            addCashbook("Food", defaultBizId)
            addCashbook("Travel", defaultBizId)
            addCashbook("Shopping", defaultBizId)
            addCashbook("Bills", defaultBizId)
            addCashbook("Other", defaultBizId)
        }
        if (_userProfile.value == null) {
            saveUserProfile(UserProfile())
        }
        
        if (_allTransactions.value.isEmpty()) {
            val currentCashbooks = _allCashbooks.value
            val defaultCashbookId = currentCashbooks.firstOrNull()?.id ?: 1
            addTransaction(
                CashTransaction(
                    type = "IN",
                    amount = 50000.0,
                    description = "Initial Funding",
                    partyName = "Self",
                    date = System.currentTimeMillis() - 86400000 * 2,
                    mode = "BANK",
                    cashbookId = defaultCashbookId
                )
            )
        }
    }
}

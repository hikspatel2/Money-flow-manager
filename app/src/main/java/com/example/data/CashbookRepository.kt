package com.example.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.Calendar

class CashbookRepository(private val context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "cashbook_database"
    ).build()
    
    private val dao = db.appDao()

    private val prefs: SharedPreferences = context.getSharedPreferences("cashbook_prefs", Context.MODE_PRIVATE)
    
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    private val profileAdapter = moshi.adapter(UserProfile::class.java)

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile

    val allBusinesses: Flow<List<Business>> = dao.getAllBusinesses()
    val allCashbooks: Flow<List<CashbookCategory>> = dao.getAllCashbooks()
    val allTransactions: Flow<List<CashTransaction>> = dao.getAllTransactions()
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = dao.getAllRecurringTransactions()

    // New tables
    val allMembers: Flow<List<Member>> = dao.getAllMembers()
    val allTransactionCategories: Flow<List<TransactionCategory>> = dao.getAllTransactionCategories()

    init {
        loadData()
        setupFirestoreSyncListener()
    }

    private var firestoreListener: com.google.firebase.firestore.ListenerRegistration? = null
    
    fun setupFirestoreSyncListener() {
        try {
            val user = com.example.FirebaseAuthService.getInstance()?.currentUser
            if (user != null) {
                val dbFs = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                firestoreListener?.remove()
                firestoreListener = dbFs.collection("users").document(user.uid).collection("transactions")
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) return@addSnapshotListener
                        if (snapshot != null) {
                            kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                                val localTx = dao.getTransactionsSync().toMutableList()
                                val remoteTx = mutableListOf<CashTransaction>()
                                for (doc in snapshot.documents) {
                                    val id = doc.getLong("id")?.toInt() ?: continue
                                    val type = doc.getString("type") ?: ""
                                    val amount = doc.getDouble("amount") ?: 0.0
                                    val description = doc.getString("description") ?: ""
                                    val partyName = doc.getString("partyName") ?: ""
                                    val date = doc.getLong("date") ?: 0L
                                    val mode = doc.getString("mode") ?: ""
                                    val cashbookId = doc.getLong("cashbookId")?.toInt() ?: 1
                                    val receiptUri = doc.getString("receiptUri")?.takeIf { it.isNotEmpty() }
                                    remoteTx.add(CashTransaction(id, type, amount, description, partyName, date, mode, cashbookId, receiptUri))
                                }
                                
                                var changed = false
                                for (rt in remoteTx) {
                                    val existingIndex = localTx.indexOfFirst { it.id == rt.id }
                                    if (existingIndex == -1) {
                                        dao.insertTransaction(rt)
                                        changed = true
                                    } else if (localTx[existingIndex] != rt) {
                                        dao.updateTransaction(rt)
                                        changed = true
                                    }
                                }
                                
                                // Upload local that don't exist remotely
                                for (lt in localTx) {
                                    if (remoteTx.none { it.id == lt.id }) {
                                        saveTransactionToFirestore(lt)
                                    }
                                }
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveTransactionToFirestore(tx: CashTransaction) {
        try {
            val user = com.example.FirebaseAuthService.getInstance()?.currentUser
            if (user != null) {
                val dbFs = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val map = mapOf(
                    "id" to tx.id,
                    "type" to tx.type,
                    "amount" to tx.amount,
                    "description" to tx.description,
                    "partyName" to tx.partyName,
                    "date" to tx.date,
                    "mode" to tx.mode,
                    "cashbookId" to tx.cashbookId,
                    "receiptUri" to (tx.receiptUri ?: "")
                )
                dbFs.collection("users").document(user.uid).collection("transactions").document(tx.id.toString()).set(map)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun deleteTransactionFromFirestore(txId: Int) {
         try {
            val user = com.example.FirebaseAuthService.getInstance()?.currentUser
            if (user != null) {
                val dbFs = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                dbFs.collection("users").document(user.uid).collection("transactions").document(txId.toString()).delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadData() {
        try {
            val profileJson = prefs.getString("user_profile", null)
            if (profileJson != null) {
                _userProfile.value = profileAdapter.fromJson(profileJson)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        _userProfile.value = profile
        prefs.edit().putString("user_profile", profileAdapter.toJson(profile)).apply()
    }

    suspend fun addBusiness(name: String): Long = withContext(Dispatchers.IO) {
        dao.insertBusiness(Business(name = name))
    }

    suspend fun updateBusiness(business: Business) = withContext(Dispatchers.IO) {
        dao.updateBusiness(business)
    }

    suspend fun deleteBusiness(business: Business) = withContext(Dispatchers.IO) {
        dao.deleteBusiness(business)
    }

    suspend fun addCashbook(name: String, businessId: Int = 1): Long = withContext(Dispatchers.IO) {
        dao.insertCashbook(CashbookCategory(name = name, businessId = businessId))
    }

    suspend fun updateCashbook(cashbook: CashbookCategory) = withContext(Dispatchers.IO) {
        dao.updateCashbook(cashbook)
    }

    suspend fun deleteCashbook(cashbook: CashbookCategory) = withContext(Dispatchers.IO) {
        dao.deleteCashbook(cashbook)
    }

    suspend fun addTransaction(transaction: CashTransaction): Long = withContext(Dispatchers.IO) {
        val newId = dao.insertTransaction(transaction)
        saveTransactionToFirestore(transaction.copy(id = newId.toInt()))
        newId.toLong()
    }

    suspend fun updateTransaction(transaction: CashTransaction) = withContext(Dispatchers.IO) {
        dao.updateTransaction(transaction)
        saveTransactionToFirestore(transaction)
    }

    suspend fun deleteTransaction(transaction: CashTransaction) = withContext(Dispatchers.IO) {
        dao.deleteTransaction(transaction)
        deleteTransactionFromFirestore(transaction.id)
    }

    suspend fun addMember(name: String, phone: String = ""): Long = withContext(Dispatchers.IO) {
        dao.insertMember(Member(name = name, phone = phone))
    }
    suspend fun updateMember(member: Member) = withContext(Dispatchers.IO) { dao.updateMember(member) }
    suspend fun deleteMember(member: Member) = withContext(Dispatchers.IO) { dao.deleteMember(member) }

    suspend fun addTransactionCategory(name: String, type: String = "IN"): Long = withContext(Dispatchers.IO) {
        dao.insertTransactionCategory(TransactionCategory(name = name, type = type))
    }
    suspend fun updateTransactionCategory(category: TransactionCategory) = withContext(Dispatchers.IO) { dao.updateTransactionCategory(category) }
    suspend fun deleteTransactionCategory(category: TransactionCategory) = withContext(Dispatchers.IO) { dao.deleteTransactionCategory(category) }

    suspend fun addRecurringTransaction(recurringTransaction: RecurringTransaction): Long = withContext(Dispatchers.IO) {
        dao.insertRecurringTransaction(recurringTransaction)
    }

    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction) = withContext(Dispatchers.IO) {
        dao.updateRecurringTransaction(recurringTransaction)
    }

    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) = withContext(Dispatchers.IO) {
        dao.deleteRecurringTransaction(recurringTransaction)
    }

    suspend fun syncRecurringTransactions() = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val rtList = dao.getRecurringTransactionsSync()
        
        for (rt in rtList) {
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
                dao.updateRecurringTransaction(rt.copy(lastGeneratedDate = lastGen))
            }
        }
    }

    suspend fun exportData(): String? = withContext(Dispatchers.IO) {
        try {
            val backup = AppBackup(
                userProfile = _userProfile.value,
                businesses = dao.getBusinessesSync(),
                cashbooks = dao.getCashbooksSync(),
                transactions = dao.getTransactionsSync(),
                recurringTransactions = dao.getRecurringTransactionsSync()
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
                if (backup.userProfile != null) {
                    saveUserProfile(backup.userProfile)
                }
                for (biz in backup.businesses) dao.insertBusiness(biz)
                for (cb in backup.cashbooks) dao.insertCashbook(cb)
                for (tx in backup.transactions) dao.insertTransaction(tx)
                for (rt in backup.recurringTransactions) dao.insertRecurringTransaction(rt)
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }

    suspend fun checkAndSeedDefaults() = withContext(Dispatchers.IO) {
        val bizCount = dao.getBusinessesSync().size
        if (bizCount == 0) {
            addBusiness("My Business")
        }
        val cbCount = dao.getCashbooksSync().size
        if (cbCount == 0) {
            val firstBiz = dao.getBusinessesSync().firstOrNull()?.id ?: 1
            addCashbook("Business", firstBiz)
            addCashbook("Salary", firstBiz)
            addCashbook("Food", firstBiz)
        }
        if (_userProfile.value == null) {
            saveUserProfile(UserProfile())
        }
    }
}

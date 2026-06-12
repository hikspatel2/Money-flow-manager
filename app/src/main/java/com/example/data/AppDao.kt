package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Businesses
    @Query("SELECT * FROM businesses")
    fun getAllBusinesses(): Flow<List<Business>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusiness(business: Business): Long

    @Update
    suspend fun updateBusiness(business: Business)

    @Delete
    suspend fun deleteBusiness(business: Business)

    // Cashbooks / Ledgers
    @Query("SELECT * FROM cashbooks")
    fun getAllCashbooks(): Flow<List<CashbookCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCashbook(cashbook: CashbookCategory): Long

    @Update
    suspend fun updateCashbook(cashbook: CashbookCategory)

    @Delete
    suspend fun deleteCashbook(cashbook: CashbookCategory)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<CashTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: CashTransaction): Long

    @Update
    suspend fun updateTransaction(transaction: CashTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: CashTransaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // Recurring Transactions
    @Query("SELECT * FROM recurring_transactions")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction): Long

    @Update
    suspend fun updateRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)

    // Members
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun getAllMembers(): Flow<List<Member>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: Member): Long

    @Update
    suspend fun updateMember(member: Member)

    @Delete
    suspend fun deleteMember(member: Member)

    // Transaction Categories
    @Query("SELECT * FROM transaction_categories ORDER BY name ASC")
    fun getAllTransactionCategories(): Flow<List<TransactionCategory>>

    @Query("SELECT * FROM businesses")
    suspend fun getBusinessesSync(): List<Business>
    @Query("SELECT * FROM cashbooks")
    suspend fun getCashbooksSync(): List<CashbookCategory>
    @Query("SELECT * FROM transactions")
    suspend fun getTransactionsSync(): List<CashTransaction>
    @Query("SELECT * FROM recurring_transactions")
    suspend fun getRecurringTransactionsSync(): List<RecurringTransaction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionCategory(category: TransactionCategory): Long

    @Update
    suspend fun updateTransactionCategory(category: TransactionCategory)

    @Delete
    suspend fun deleteTransactionCategory(category: TransactionCategory)
}

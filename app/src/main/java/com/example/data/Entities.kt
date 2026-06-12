package com.example.data

import com.squareup.moshi.JsonClass
import androidx.room.Entity
import androidx.room.PrimaryKey

@JsonClass(generateAdapter = true)
data class UserProfile(
    val email: String = "info@example.com",
    val name: String = "User",
    val businessName: String = "My Business",
    val phone: String = "+123456789",
    val address: String = ""
)

@Entity(tableName = "businesses")
@JsonClass(generateAdapter = true)
data class Business(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cashbooks")
@JsonClass(generateAdapter = true)
data class CashbookCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val businessId: Int = 1, // Defaulting to 1 for backwards compatibility
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
@JsonClass(generateAdapter = true)
data class CashTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // 'IN' or 'OUT'
    val amount: Double,
    val description: String,
    val partyName: String,
    val date: Long, // timestamp
    val mode: String, // 'CASH', 'UPI', 'BANK'
    val cashbookId: Int, // reference to cashbooks.id
    val receiptUri: String? = null
)

@Entity(tableName = "members")
@JsonClass(generateAdapter = true)
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transaction_categories")
@JsonClass(generateAdapter = true)
data class TransactionCategory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String = "IN", // IN, OUT or BOTH
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class AppBackup(
    val userProfile: UserProfile?,
    val businesses: List<Business>,
    val cashbooks: List<CashbookCategory>,
    val transactions: List<CashTransaction>,
    val recurringTransactions: List<RecurringTransaction>
)

@Entity(tableName = "recurring_transactions")
@JsonClass(generateAdapter = true)
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // 'IN' or 'OUT'
    val amount: Double,
    val description: String,
    val partyName: String,
    val mode: String, // 'CASH', 'UPI', 'BANK'
    val cashbookId: Int, // reference to cashbooks.id
    val frequency: String, // 'DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY'
    val startDate: Long, // timestamp
    val endDate: Long, // timestamp
    val lastGeneratedDate: Long = 0L // timestamp of last generated occurrence
)

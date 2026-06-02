package com.example.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserProfile(
    val email: String = "info@example.com",
    val name: String = "User",
    val businessName: String = "My Business",
    val phone: String = "+123456789",
    val address: String = ""
)

@JsonClass(generateAdapter = true)
data class Business(
    val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class CashbookCategory(
    val id: Int = 0,
    val name: String,
    val businessId: Int = 1, // Defaulting to 1 for backwards compatibility
    val createdAt: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class CashTransaction(
    val id: Int = 0,
    val type: String, // 'IN' or 'OUT'
    val amount: Double,
    val description: String,
    val partyName: String,
    val date: Long, // timestamp
    val mode: String, // 'CASH', 'UPI', 'BANK'
    val cashbookId: Int // reference to cashbooks.id
)

@JsonClass(generateAdapter = true)
data class RecurringTransaction(
    val id: Int = 0,
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

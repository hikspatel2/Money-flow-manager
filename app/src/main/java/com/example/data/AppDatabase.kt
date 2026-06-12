package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Business::class,
        CashbookCategory::class,
        CashTransaction::class,
        RecurringTransaction::class,
        Member::class,
        TransactionCategory::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao
}

package com.example.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.CashbookRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = CashbookRepository(applicationContext)
            val jsonStr = repository.exportData()
            if (jsonStr != null) {
                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                val fileName = "auto_backup_${dateFormat.format(Date())}.json"
                val dir = File(applicationContext.filesDir, "auto_backups")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(dir, fileName)
                file.writeText(jsonStr)
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}

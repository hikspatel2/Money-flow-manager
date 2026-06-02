package com.example

import android.app.Application
import com.google.firebase.FirebaseApp

class CashbookApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                val file = java.io.File(filesDir, "crash_log.txt")
                file.writeText(android.util.Log.getStackTraceString(throwable))
            } catch (e: Exception) {}
            defaultHandler?.uncaughtException(thread, throwable)
        }
        
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}

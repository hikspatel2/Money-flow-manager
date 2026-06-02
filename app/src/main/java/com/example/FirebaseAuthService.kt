package com.example

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthService {
    fun getInstance(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: IllegalStateException) {
            null
        } catch (e: Throwable) {
            null
        }
    }
}

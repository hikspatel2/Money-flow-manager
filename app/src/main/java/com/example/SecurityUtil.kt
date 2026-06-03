package com.example

import java.security.MessageDigest

object SecurityUtil {
    fun hashMpin(mpin: String): String {
        val bytes = mpin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}

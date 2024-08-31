package com.vaultmessenger.modules

import java.security.MessageDigest

class Encoder {
    companion object {
        // Static method to encode a string with SHA-256
        fun encodeWithSHA256(input: String?): String? {
            // Create a MessageDigest instance for SHA-256
            val digest = MessageDigest.getInstance("SHA-256")

            // Perform the hash computation
            val hashBytes = input?.toByteArray()?.let { digest.digest(it) }

            // Convert the byte array to a hexadecimal string
            return hashBytes?.joinToString("") { "%02x".format(it) }
        }
    }
}
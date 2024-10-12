package com.vaultmessenger.modules

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage

object FirebaseService {

    // Initialize the service with application context
    fun initialize( useEmulators: Boolean) {

        if (useEmulators) {
            setupEmulators()
        }
    }

    private fun setupEmulators() {
        val host = if (isRunningOnAndroidEmulator()) "10.0.2.2" else "10.0.2.2"
        auth.useEmulator(host, 9099)
        firestore.useEmulator(host, 8080)
        storage.useEmulator(host, 9199)
        functions.useEmulator(host, 5001)
    }

    private fun isRunningOnAndroidEmulator(): Boolean {
        return android.os.Build.FINGERPRINT.contains("generic") ||
                android.os.Build.MODEL.contains("Emulator") ||
                android.os.Build.MODEL.contains("Android SDK built for x86")
    }

    // Access Firebase instances
    val auth: FirebaseAuth
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error getting FirebaseAuth instance: ${e.message}", e)
            // Handle the exception (e.g., return a fallback or rethrow the exception)
            FirebaseAuth.getInstance() // As a fallback, retry getting the instance
        }finally {

        }

    val firestore: FirebaseFirestore
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error getting firebaseFireStore instance: ${e.message}", e)
            // Handle the exception (e.g., return a fallback or rethrow the exception)
            FirebaseFirestore.getInstance() // As a fallback, retry getting the instance
        }finally {

        }

    val storage: FirebaseStorage
        get() = try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error getting FirebaseStorage instance: ${e.message}", e)
            // Handle the exception (e.g., return a fallback or rethrow the exception)
            FirebaseStorage.getInstance() // As a fallback, retry getting the instance
        }finally {

        }

    val functions: FirebaseFunctions
        get() = try {
            FirebaseFunctions.getInstance()
        } catch (e: Exception) {
            Log.e("FirebaseAuth", "Error getting FirebaseFunctions instance: ${e.message}", e)
            // Handle the exception (e.g., return a fallback or rethrow the exception)
            FirebaseFunctions.getInstance() // As a fallback, retry getting the instance
        }finally {

        }
}

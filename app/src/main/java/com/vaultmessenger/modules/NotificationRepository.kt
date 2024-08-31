package com.vaultmessenger.modules

import android.util.Log
import com.vaultmessenger.model.NotificationToken
import kotlinx.coroutines.tasks.await
import java.net.URL

class NotificationRepository {

    private val firestore = FirebaseService.firestore
    private val functions = FirebaseService.functions

    // Fetch the current token from Firestore
    fun getToken(userId: String, onResult: (String?) -> Unit) {
        try {
            firestore.collection("notifications")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentToken = document.getString("token")
                        onResult(currentToken)
                    } else {
                        onResult(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("NotificationRepository", "Error getting token", e)
                    onResult(null)
                }
        }catch (e:Exception){
            Log.d("NotificationRepository Error", "${e.message}")
        }
    }

    // Save the token to Firestore
    fun saveToken(userId: String, token: String) {
        val notificationToken = NotificationToken(token, userId)
        try {
            firestore.collection("notifications")
                .document(userId)
                .set(notificationToken)
                .addOnSuccessListener {
                    Log.d("NotificationRepository", "Token successfully written!")
                }
                .addOnFailureListener { e ->
                    Log.w("NotificationRepository", "Error writing token", e)
                }
        }catch (e:Exception){
            Log.d("NotificationRepository Error", "${e.message}")
        }
    }
    suspend fun sendNotification(token: String, title: String, body: String, imageUrl: String): Result<Map<String, Any>> {
        return try {
            val data = mapOf(
                "token" to token,
                "title" to title,
                "body" to body,
                "imageUrl" to imageUrl // Include image URL
            )

            val endpointSendNotifications = URL(
                "https://europe-west3-vaultmessengerdev.cloudfunctions.net/sendNotification"
            )

            val result = functions
                .getHttpsCallableFromUrl(endpointSendNotifications)
                .call(data)
                .await()

            // Ensure result.data is a Map<String, Any>
            val resultMap = result.data as? Map<String, Any>
                ?: return Result.failure(IllegalArgumentException("Unexpected result format"))

            Result.success(resultMap)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

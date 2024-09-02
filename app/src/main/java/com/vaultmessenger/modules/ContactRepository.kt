
package com.vaultmessenger.modules

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.vaultmessenger.model.Contact
import com.vaultmessenger.model.User
import com.vaultmessenger.viewModel.ErrorsViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModel
import com.vaultmessenger.viewModel.ReceiverUserViewModelFactory
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ContactRepository(private val errorsViewModel: ErrorsViewModel) {

    private val firestore = FirebaseService.firestore

    private fun getContactsCollection(userId: String): CollectionReference {
        return firestore.collection("Contacts").document(userId).collection("UserContacts")
    }

    // Function to get contacts as a Flow
    fun getContactsFlow(userId: String): Flow<List<Contact>> = callbackFlow {
        val contactsRef = getContactsCollection(userId)

        val listener = contactsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            snapshot?.let {
                trySend(it.toContactsList()).isSuccess
            }
        }

        awaitClose { listener.remove() }
    }

    // Extension function to map QuerySnapshot to a list of Contact objects
    private fun QuerySnapshot.toContactsList(): List<Contact> {
        return this.documents.mapNotNull { it.toObject(Contact::class.java) }
    }

    private suspend fun addContactFromUser(userId: String, contactHashUserId: String): Int {
        return try {
            // Step 1: Get all user documents
            val usersCollection = firestore.collection("users")
            val allUsersQuery = usersCollection.get().await()

            // Step 2: Find the document where hashUserId matches
            val userDoc = allUsersQuery.documents.firstOrNull { document ->
                document.getString("hashUserId") == contactHashUserId
            } ?: throw IllegalStateException("User not found for hashUserId: $contactHashUserId")

            // Retrieve the user details from the document
            val user = userDoc.toObject(User::class.java) ?: throw IllegalStateException("User data is null")

            // Step 3: Create a Contact object
            val contact = Contact(
                uid = user.userId,
                name = user.userName,
                number = "" // Assuming you don't have a phone number
            )

            // Step 4: Add the Contact to the contacts collection
            val contactsRef = firestore
                            .collection("contacts")
                            .document(userId)
                            .collection("user_contacts")

            contactsRef.add(contact).await()

            Log.d("ContactRepository", "Contact added successfully")
        } catch (e: Exception) {
            Log.e("ContactRepository", "Error adding contact: ${e.message}", e)
            throw e // Throw the exception to be handled by the caller
        }
    }

    private suspend fun getReceiverViewModel(
        viewModelStoreOwner: ViewModelStoreOwner?,
        receiverId: String
    ): ReceiverUserViewModel {
        val receiverUserRepository = ReceiverUserRepository(receiverId)
        val factory = ReceiverUserViewModelFactory(
           repository =  receiverUserRepository,
            errorsViewModel = errorsViewModel,
        )
        return ViewModelProvider(viewModelStoreOwner!!, factory)
            .get(ReceiverUserViewModel::class.java)
    }

    suspend fun lookUpAndAddContact(
        viewModelStoreOwner: ViewModelStoreOwner?,
        userId: String,
        receiverId: String
    ): Boolean {
        return try {
            val receiverViewModel = getReceiverViewModel(viewModelStoreOwner, receiverId)
            val receiverUser = receiverViewModel.receiverUser.value

            receiverUser?.let {

                addContactFromUser(
                    userId = userId,
                    contactHashUserId = receiverId
                ) // Assume addContact returns Unit or handles its own exceptions
                true // Return true to indicate success
            } ?: run {
                // User not found
                Log.e("ContactRepository", "User not found for receiver ID: $receiverId")
                false
            }
        } catch (e: Exception) {
            // Log error and return false
            Log.e("ContactRepository", "Error looking up and adding contact: ${e.message}", e)
            false
        }
    }

}



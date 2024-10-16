package com.vaultmessenger

import android.app.Application
import android.content.Context
import com.vaultmessenger.database.AppDatabase
import com.vaultmessenger.database.Converters
import com.vaultmessenger.local.LocalConversationRepository
import com.vaultmessenger.local.LocalMessageInsertRepository
import com.vaultmessenger.local.LocalMessageRepository
import com.vaultmessenger.local.LocalUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApp(context: Context) : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val converters = Converters()
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy {
        AppDatabase.getDatabase(context, applicationScope, converters) }

    val repositoryMessages by lazy {
        LocalMessageRepository(database.messageDao())}

    val repositoryUser by lazy {
        LocalUserRepository(database.userDao())
    }
    val repositoryConversation by lazy {
        LocalConversationRepository(database.conversationDao())
    }
    val repositoryInsertMessages by lazy {
        LocalMessageInsertRepository(database.MessageDaoInsert())
    }
    val repositoryDeleteAllMessages by lazy {
        LocalMessageRepository(database.messageDao())
    }
    val repositoryDeleteAllConversations by lazy {
        LocalConversationRepository(database.conversationDao())
    }
}


package com.vaultmessenger

import android.app.Application
import android.content.Context
import com.vaultmessenger.database.AppDatabase
import com.vaultmessenger.local.LocalMessageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApp(context: Context) : Application() {
    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase(context, applicationScope) }
    val repositoryMessages by lazy { LocalMessageRepository(database.messageDao())}
}


package com.vaultmessenger.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vaultmessenger.modules.ChatRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [LocalMessage::class, LocalUser::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
               // val chatRepository:ChatRepository = ChatRepository()
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_app"
                )
                    .addCallback(AppDatabaseCallback(scope)) // Callback is added here
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope,

    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // When the database is created, insert the sample messages
            INSTANCE?.let { database ->
                scope.launch {
                    val messageDao = database.messageDao()
                    messageDao.deleteAll()
                }
            }
        }
    }

}


package com.vaultmessenger.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.internal.wait

@Database(entities = [LocalMessage::class, LocalUser::class, LocalConversation::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

    abstract fun userDao(): UserDao

    abstract fun conversationDao():ConversationDao

    abstract fun MessageDaoInsert():MessageDaoInsert

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the new 'messageRead' column with a default value of 'false'
                db.execSQL("ALTER TABLE message ADD COLUMN messageRead INTEGER DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Step 1: Create the new `conversation` table with the correct schema
                db.execSQL("""
            CREATE TABLE conversation (
                userPhotos TEXT NOT NULL,
                conversationId TEXT NOT NULL PRIMARY KEY,
                userIds TEXT NOT NULL,
                lastMessage TEXT NOT NULL,
                userNames TEXT NOT NULL,
                isTyping TEXT NOT NULL,
                timestamp TEXT NOT NULL
            )
        """.trimIndent())
            }
        }

        fun getDatabase(
            context: Context,
            scope: CoroutineScope,
            converters: Converters,
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
               // val chatRepository:ChatRepository = ChatRepository()
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_app_2"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(AppDatabaseCallback(scope)) // Callback is added here
                    .addTypeConverter(converters)
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
                    database.clearAllTables()
                }
            }
        }
    }

}


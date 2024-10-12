package com.vaultmessenger.di

import com.vaultmessenger.modules.FirebaseUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    fun provideFirebaseUserRepository(): FirebaseUserRepository {
        return FirebaseUserRepository() // Add parameters if needed
    }
}

package com.vaultmessenger.di

import com.vaultmessenger.modules.FirebaseUserRepository
import com.vaultmessenger.viewModel.ErrorsViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseUserRepository(): FirebaseUserRepository {
        return FirebaseUserRepository()
    }

    @Provides
    @Singleton
    fun provideErrorsViewModel(): ErrorsViewModel {
        return ErrorsViewModel()
    }
}

package com.example.vetcarepro.data.di

import android.content.Context
import com.example.vetcarepro.data.local.LocalVetCareStore
import com.example.vetcarepro.data.repository.FirebaseVetCareRepository
import com.example.vetcarepro.domain.repository.VetCareRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    abstract fun bindVetCareRepository(impl: FirebaseVetCareRepository): VetCareRepository

    companion object {
        @Provides
        @Singleton
        fun provideLocalVetCareStore(@ApplicationContext context: Context): LocalVetCareStore = LocalVetCareStore(context)

        @Provides
        fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}


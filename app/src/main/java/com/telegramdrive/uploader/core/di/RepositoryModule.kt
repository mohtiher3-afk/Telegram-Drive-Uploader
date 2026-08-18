package com.telegramdrive.uploader.core.di

import com.telegramdrive.uploader.data.repository.UploadRepositoryImpl
import com.telegramdrive.uploader.data.telegram.client.TelegramClient
import com.telegramdrive.uploader.data.telegram.client.TelegramClientImpl
import com.telegramdrive.uploader.data.telegram.repository.TelegramRepositoryImpl
import com.telegramdrive.uploader.domain.repository.TelegramRepository
import com.telegramdrive.uploader.domain.repository.UploadRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUploadRepository(
        uploadRepositoryImpl: UploadRepositoryImpl
    ): UploadRepository

    @Binds
    @Singleton
    abstract fun bindTelegramClient(
        telegramClientImpl: TelegramClientImpl
    ): TelegramClient

    @Binds
    @Singleton
    abstract fun bindTelegramRepository(
        telegramRepositoryImpl: TelegramRepositoryImpl
    ): TelegramRepository
}


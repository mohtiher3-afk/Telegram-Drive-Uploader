package com.telegramdrive.uploader.core.di

import com.telegramdrive.uploader.data.upload.UploadManagerImpl
import com.telegramdrive.uploader.data.upload.TelegramUploadEngineImpl
import com.telegramdrive.uploader.domain.upload.UploadManager
import com.telegramdrive.uploader.domain.upload.TelegramUploadEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UploadModule {

    @Binds
    @Singleton
    abstract fun bindUploadManager(
        uploadManagerImpl: UploadManagerImpl
    ): UploadManager

    @Binds
    @Singleton
    abstract fun bindTelegramUploadEngine(
        telegramUploadEngineImpl: TelegramUploadEngineImpl
    ): TelegramUploadEngine
}

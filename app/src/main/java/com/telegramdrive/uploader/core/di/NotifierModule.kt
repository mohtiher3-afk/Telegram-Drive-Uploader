package com.telegramdrive.uploader.core.di

import com.telegramdrive.uploader.data.upload.notifications.UploadEventNotifier
import com.telegramdrive.uploader.notifications.AndroidUploadEventNotifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotifierModule {
    @Binds
    @Singleton
    abstract fun bindUploadEventNotifier(
        androidUploadEventNotifier: AndroidUploadEventNotifier
    ): UploadEventNotifier
}
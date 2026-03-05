package com.ciphersms.app.di

import android.content.Context
import androidx.room.Room
import com.ciphersms.app.data.local.dao.*
import com.ciphersms.app.data.local.database.CipherDatabase
import com.ciphersms.app.data.repository.MessageRepositoryImpl
import com.ciphersms.app.data.repository.ContactRepositoryImpl
import com.ciphersms.app.data.repository.SettingsRepositoryImpl
import com.ciphersms.app.domain.repository.ContactRepository
import com.ciphersms.app.domain.repository.MessageRepository
import com.ciphersms.app.domain.repository.SettingsRepository
import com.ciphersms.app.security.EncryptionManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CipherDatabase =
        Room.databaseBuilder(
            context,
            CipherDatabase::class.java,
            "cipher_sms_db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideMessageDao(db: CipherDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideConversationDao(db: CipherDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideScheduledMessageDao(db: CipherDatabase): ScheduledMessageDao = db.scheduledMessageDao()

    @Provides
    fun provideBlockedAddressDao(db: CipherDatabase): BlockedAddressDao = db.blockedAddressDao()

    @Provides
    fun provideDraftDao(db: CipherDatabase): DraftDao = db.draftDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    abstract fun bindContactRepository(impl: ContactRepositoryImpl): ContactRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides
    @Singleton
    fun provideEncryptionManager(@ApplicationContext context: Context): EncryptionManager =
        EncryptionManager(context)
}

package com.ciphersms.app.domain.repository

import androidx.paging.PagingData
import com.ciphersms.app.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

interface MessageRepository {
    fun getConversations(): Flow<List<Conversation>>
    fun getConversationsPaged(): Flow<PagingData<Conversation>>
    fun getConversation(threadId: Long): Flow<Conversation?>
    fun getMessages(threadId: Long): Flow<PagingData<Message>>
    fun getLatestMessages(threadId: Long, limit: Int = 20): Flow<List<Message>>
    suspend fun sendSms(address: String, body: String, simSlot: Int = 0): Result<Long>
    suspend fun sendMms(address: String, body: String, attachments: List<Attachment>): Result<Long>
    suspend fun sendRcs(address: String, body: String, attachments: List<Attachment>): Result<Long>
    suspend fun markAsRead(threadId: Long)
    suspend fun markMessageRead(messageId: Long)
    suspend fun deleteMessage(messageId: Long)
    suspend fun deleteConversation(threadId: Long)
    suspend fun archiveConversation(threadId: Long, archive: Boolean)
    suspend fun pinConversation(threadId: Long, pin: Boolean)
    suspend fun lockConversation(threadId: Long, lock: Boolean)
    suspend fun moveToVault(threadId: Long)
    suspend fun blockAddress(address: String)
    suspend fun unblockAddress(address: String)
    suspend fun getBlockedAddresses(): List<String>
    suspend fun searchMessages(query: String): List<Message>
    suspend fun scheduleMessage(message: ScheduledMessage): Long
    suspend fun cancelScheduledMessage(id: Long)
    suspend fun getScheduledMessages(): List<ScheduledMessage>
    suspend fun editMessage(messageId: Long, newBody: String): Result<Unit>
    suspend fun setMessageDisappear(messageId: Long, disappearsAt: Date)
    suspend fun translateMessage(messageId: Long, targetLanguage: String): Result<String>
    suspend fun analyzeSpam(message: Message): SpamAnalysis
    suspend fun summarizeConversation(threadId: Long): Result<String>
    suspend fun exportConversation(threadId: Long): Result<String>
}

interface ContactRepository {
    fun getContacts(): Flow<List<Contact>>
    suspend fun getContact(address: String): Contact?
    suspend fun searchContacts(query: String): List<Contact>
    fun observeRcsCapableContacts(): Flow<List<String>>
}

interface SettingsRepository {
    fun getSettings(): Flow<AppSettings>
    suspend fun updateSettings(settings: AppSettings)
    suspend fun setLockType(lockType: LockType)
    suspend fun setPinCode(pin: String)
    suspend fun verifyPin(pin: String): Boolean
    suspend fun isLocked(): Boolean
    suspend fun unlock()
    suspend fun lock()
}

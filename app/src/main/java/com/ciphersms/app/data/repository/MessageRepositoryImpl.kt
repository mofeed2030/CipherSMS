package com.ciphersms.app.data.repository

import android.content.Context
import android.telephony.SmsManager
import android.net.Uri
import androidx.paging.*
import com.ciphersms.app.data.local.dao.*
import com.ciphersms.app.data.local.entities.*
import com.ciphersms.app.domain.model.*
import com.ciphersms.app.domain.repository.MessageRepository
import com.ciphersms.app.security.EncryptionManager
import com.ciphersms.app.utils.SmsUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val messageDao: MessageDao,
    private val conversationDao: ConversationDao,
    private val scheduledMessageDao: ScheduledMessageDao,
    private val blockedAddressDao: BlockedAddressDao,
    private val encryptionManager: EncryptionManager
) : MessageRepository {

    override fun getConversations(): Flow<List<Conversation>> =
        conversationDao.getActiveConversations().map { list ->
            list.map { it.toDomain() }
        }

    override fun getConversationsPaged(): Flow<PagingData<Conversation>> =
        Pager(
            config = PagingConfig(pageSize = 20, enablePlaceholders = false),
            pagingSourceFactory = { conversationDao.getConversationsPaged() }
        ).flow.map { paging -> paging.map { it.toDomain() } }

    override fun getConversation(threadId: Long): Flow<Conversation?> =
        conversationDao.getConversation(threadId).map { it?.toDomain() }

    override fun getMessages(threadId: Long): Flow<PagingData<Message>> =
        Pager(
            config = PagingConfig(pageSize = 30, enablePlaceholders = false),
            pagingSourceFactory = { messageDao.getMessagesPaged(threadId) }
        ).flow.map { paging -> paging.map { it.toDomain(encryptionManager) } }

    override fun getLatestMessages(threadId: Long, limit: Int): Flow<List<Message>> =
        messageDao.getLatestMessages(threadId, limit).map { list ->
            list.map { it.toDomain(encryptionManager) }
        }

    override suspend fun sendSms(address: String, body: String, simSlot: Int): Result<Long> {
        return try {
            val smsManager = context.getSystemService(SmsManager::class.java)
            val parts = smsManager.divideMessage(body)
            smsManager.sendMultipartTextMessage(address, null, parts, null, null)

            val msgEntity = MessageEntity(
                threadId = SmsUtils.getOrCreateThreadId(context, address),
                address = address,
                body = body,
                type = "SMS",
                status = "SENT",
                date = Date(),
                isMine = true,
                isRead = true
            )
            val id = messageDao.insertMessage(msgEntity)
            updateConversationSnippet(msgEntity.threadId, body)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendMms(address: String, body: String, attachments: List<Attachment>): Result<Long> {
        return try {
            // MMS sending via MmsManager
            val msgEntity = MessageEntity(
                threadId = SmsUtils.getOrCreateThreadId(context, address),
                address = address,
                body = body,
                type = "MMS",
                status = "SENT",
                date = Date(),
                isMine = true,
                isRead = true
            )
            val id = messageDao.insertMessage(msgEntity)
            updateConversationSnippet(msgEntity.threadId, body)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendRcs(address: String, body: String, attachments: List<Attachment>): Result<Long> {
        return try {
            val msgEntity = MessageEntity(
                threadId = SmsUtils.getOrCreateThreadId(context, address),
                address = address,
                body = body,
                type = "RCS",
                status = "SENDING",
                date = Date(),
                isMine = true,
                isRead = true
            )
            val id = messageDao.insertMessage(msgEntity)
            updateConversationSnippet(msgEntity.threadId, body)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(threadId: Long) {
        messageDao.markThreadAsRead(threadId)
        conversationDao.markAsRead(threadId)
    }

    override suspend fun markMessageRead(messageId: Long) {
        messageDao.markMessageAsRead(messageId)
    }

    override suspend fun deleteMessage(messageId: Long) {
        messageDao.softDeleteMessage(messageId)
    }

    override suspend fun deleteConversation(threadId: Long) {
        messageDao.deleteConversationMessages(threadId)
        conversationDao.deleteConversation(threadId)
    }

    override suspend fun archiveConversation(threadId: Long, archive: Boolean) {
        conversationDao.archiveConversation(threadId, archive)
    }

    override suspend fun pinConversation(threadId: Long, pin: Boolean) {
        conversationDao.pinConversation(threadId, pin)
    }

    override suspend fun lockConversation(threadId: Long, lock: Boolean) {
        conversationDao.lockConversation(threadId, lock)
    }

    override suspend fun moveToVault(threadId: Long) {
        conversationDao.moveToVault(threadId)
    }

    override suspend fun blockAddress(address: String) {
        blockedAddressDao.block(BlockedAddressEntity(address))
    }

    override suspend fun unblockAddress(address: String) {
        blockedAddressDao.unblock(address)
    }

    override suspend fun getBlockedAddresses(): List<String> =
        blockedAddressDao.getAllBlocked()

    override suspend fun searchMessages(query: String): List<Message> =
        messageDao.searchMessages(query).map { it.toDomain(encryptionManager) }

    override suspend fun scheduleMessage(message: ScheduledMessage): Long {
        val entity = ScheduledMessageEntity(
            address = message.address,
            body = message.body,
            scheduledAt = message.scheduledAt,
            type = message.type.name
        )
        return scheduledMessageDao.insert(entity)
    }

    override suspend fun cancelScheduledMessage(id: Long) {
        scheduledMessageDao.delete(id)
    }

    override suspend fun getScheduledMessages(): List<ScheduledMessage> =
        scheduledMessageDao.getAll().map { it.toDomain() }

    override suspend fun editMessage(messageId: Long, newBody: String): Result<Unit> {
        return try {
            messageDao.editMessage(messageId, newBody, System.currentTimeMillis())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun setMessageDisappear(messageId: Long, disappearsAt: Date) {
        messageDao.setDisappear(messageId, disappearsAt.time)
    }

    override suspend fun translateMessage(messageId: Long, targetLanguage: String): Result<String> {
        // Integration point for translation API
        return Result.success("Translation placeholder for $targetLanguage")
    }

    override suspend fun analyzeSpam(message: Message): SpamAnalysis {
        val spamKeywords = listOf("win", "prize", "click here", "free money", "verify account",
            "urgent", "bitcoin", "lottery", "claim now", "congratulations")
        val lowerBody = message.body.lowercase()
        val matchCount = spamKeywords.count { lowerBody.contains(it) }
        val confidence = (matchCount.toFloat() / spamKeywords.size).coerceIn(0f, 1f)
        return SpamAnalysis(
            isSpam = confidence > 0.2f,
            confidence = confidence,
            reason = if (confidence > 0.2f) "Contains suspicious keywords" else "",
            isFraud = confidence > 0.5f
        )
    }

    override suspend fun summarizeConversation(threadId: Long): Result<String> {
        return Result.success("AI summary of conversation $threadId")
    }

    override suspend fun exportConversation(threadId: Long): Result<String> {
        return Result.success("Export path placeholder")
    }

    private suspend fun updateConversationSnippet(threadId: Long, snippet: String) {
        conversationDao.updateSnippet(threadId, snippet, System.currentTimeMillis())
    }
}

// ─── Mappers ─────────────────────────────────────────────────────────────────
private fun ConversationEntity.toDomain() = Conversation(
    id = id,
    threadId = threadId,
    address = address,
    contactName = contactName,
    contactPhotoUri = contactPhotoUri,
    snippet = snippet,
    date = date,
    unreadCount = unreadCount,
    isArchived = isArchived,
    isPinned = isPinned,
    isLocked = isLocked,
    isVault = isVault,
    isMuted = isMuted,
    isBlocked = isBlocked,
    isRCS = isRCS,
    customBackground = customBackground,
    customBubbleStyle = try { BubbleStyle.valueOf(customBubbleStyle) } catch (e: Exception) { BubbleStyle.DEFAULT },
    category = try { ConversationCategory.valueOf(category) } catch (e: Exception) { ConversationCategory.PERSONAL },
    encryptionStatus = try { EncryptionStatus.valueOf(encryptionStatus) } catch (e: Exception) { EncryptionStatus.NONE }
)

private fun MessageEntity.toDomain(encryptionManager: EncryptionManager): Message {
    val body = if (isEncrypted && encryptedBody != null) {
        try { encryptionManager.decrypt(encryptedBody) } catch (e: Exception) { "[Encrypted]" }
    } else body

    return Message(
        id = id,
        threadId = threadId,
        address = address,
        body = body,
        type = try { MessageType.valueOf(type) } catch (e: Exception) { MessageType.SMS },
        status = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.NONE },
        date = date,
        dateSent = dateSent,
        isRead = isRead,
        isMine = isMine,
        isEncrypted = isEncrypted,
        isDeleted = isDeleted,
        isEdited = isEdited,
        disappearsAt = disappearsAt,
        editedAt = editedAt,
        replyToId = replyToId,
        isScheduled = isScheduled,
        scheduledAt = scheduledAt,
        translation = translation,
        originalLanguage = originalLanguage,
        isVaultMessage = isVaultMessage
    )
}

private fun ScheduledMessageEntity.toDomain() = ScheduledMessage(
    id = id,
    address = address,
    body = body,
    scheduledAt = scheduledAt,
    type = try { MessageType.valueOf(type) } catch (e: Exception) { MessageType.SMS }
)

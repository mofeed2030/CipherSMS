package com.ciphersms.app.domain.usecase

import androidx.paging.PagingData
import com.ciphersms.app.domain.model.*
import com.ciphersms.app.domain.repository.MessageRepository
import com.ciphersms.app.domain.repository.ContactRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(): Flow<List<Conversation>> = repository.getConversations()
    fun paged(): Flow<PagingData<Conversation>> = repository.getConversationsPaged()
}

class GetMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    operator fun invoke(threadId: Long): Flow<PagingData<Message>> =
        repository.getMessages(threadId)

    fun latest(threadId: Long, limit: Int = 20) =
        repository.getLatestMessages(threadId, limit)
}

class SendMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend fun sendSms(address: String, body: String, simSlot: Int = 0): Result<Long> =
        repository.sendSms(address, body, simSlot)

    suspend fun sendMms(address: String, body: String, attachments: List<Attachment>): Result<Long> =
        repository.sendMms(address, body, attachments)

    suspend fun sendRcs(address: String, body: String, attachments: List<Attachment>): Result<Long> =
        repository.sendRcs(address, body, attachments)
}

class ScheduleMessageUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(message: ScheduledMessage): Long =
        repository.scheduleMessage(message)

    suspend fun cancel(id: Long) = repository.cancelScheduledMessage(id)

    suspend fun getAll(): List<ScheduledMessage> = repository.getScheduledMessages()
}

class SearchMessagesUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend operator fun invoke(query: String): List<Message> =
        repository.searchMessages(query)
}

class ManageConversationUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend fun archive(threadId: Long, archive: Boolean) =
        repository.archiveConversation(threadId, archive)

    suspend fun pin(threadId: Long, pin: Boolean) =
        repository.pinConversation(threadId, pin)

    suspend fun lock(threadId: Long, lock: Boolean) =
        repository.lockConversation(threadId, lock)

    suspend fun moveToVault(threadId: Long) =
        repository.moveToVault(threadId)

    suspend fun delete(threadId: Long) =
        repository.deleteConversation(threadId)

    suspend fun block(address: String) =
        repository.blockAddress(address)

    suspend fun markRead(threadId: Long) =
        repository.markAsRead(threadId)
}

class AiMessagingUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend fun analyzeSpam(message: Message): SpamAnalysis =
        repository.analyzeSpam(message)

    suspend fun translate(messageId: Long, targetLanguage: String): Result<String> =
        repository.translateMessage(messageId, targetLanguage)

    suspend fun summarize(threadId: Long): Result<String> =
        repository.summarizeConversation(threadId)
}

class SecurityUseCase @Inject constructor(
    private val repository: MessageRepository
) {
    suspend fun editMessage(messageId: Long, newBody: String): Result<Unit> =
        repository.editMessage(messageId, newBody)

    suspend fun setDisappear(messageId: Long, disappearsAt: Date) =
        repository.setMessageDisappear(messageId, disappearsAt)
}

class GetContactsUseCase @Inject constructor(
    private val repository: ContactRepository
) {
    operator fun invoke(): Flow<List<Contact>> = repository.getContacts()
    suspend fun getByAddress(address: String): Contact? = repository.getContact(address)
    suspend fun search(query: String): List<Contact> = repository.searchContacts(query)
}

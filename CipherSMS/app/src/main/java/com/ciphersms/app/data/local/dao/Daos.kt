package com.ciphersms.app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.ciphersms.app.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE threadId = :threadId AND isDeleted = 0 ORDER BY date DESC")
    fun getMessagesPaged(threadId: Long): PagingSource<Int, MessageEntity>

    @Query("SELECT * FROM messages WHERE threadId = :threadId AND isDeleted = 0 ORDER BY date DESC LIMIT :limit")
    fun getLatestMessages(threadId: Long, limit: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE id = :id")
    suspend fun getMessage(id: Long): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Update
    suspend fun updateMessage(message: MessageEntity)

    @Query("UPDATE messages SET isDeleted = 1 WHERE id = :id")
    suspend fun softDeleteMessage(id: Long)

    @Query("DELETE FROM messages WHERE threadId = :threadId")
    suspend fun deleteConversationMessages(threadId: Long)

    @Query("UPDATE messages SET isRead = 1 WHERE threadId = :threadId AND isMine = 0")
    suspend fun markThreadAsRead(threadId: Long)

    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    suspend fun markMessageAsRead(messageId: Long)

    @Query("UPDATE messages SET status = :status WHERE id = :messageId")
    suspend fun updateMessageStatus(messageId: Long, status: String)

    @Query("UPDATE messages SET body = :body, isEdited = 1, editedAt = :editedAt WHERE id = :messageId")
    suspend fun editMessage(messageId: Long, body: String, editedAt: Long)

    @Query("UPDATE messages SET disappearsAt = :disappearsAt WHERE id = :messageId")
    suspend fun setDisappear(messageId: Long, disappearsAt: Long)

    @Query("SELECT * FROM messages WHERE body LIKE '%' || :query || '%' AND isDeleted = 0 ORDER BY date DESC LIMIT 100")
    suspend fun searchMessages(query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE isVaultMessage = 1 AND isDeleted = 0 ORDER BY date DESC")
    fun getVaultMessages(): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE disappearsAt IS NOT NULL AND disappearsAt < :now")
    suspend fun deleteExpiredMessages(now: Long)

    @Query("SELECT COUNT(*) FROM messages WHERE threadId = :threadId AND isRead = 0 AND isMine = 0")
    fun getUnreadCount(threadId: Long): Flow<Int>
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE isVault = 0 ORDER BY isPinned DESC, date DESC")
    fun getAllConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isVault = 0 AND isArchived = 0 ORDER BY isPinned DESC, date DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isVault = 0 ORDER BY isPinned DESC, date DESC")
    fun getConversationsPaged(): PagingSource<Int, ConversationEntity>

    @Query("SELECT * FROM conversations WHERE threadId = :threadId")
    fun getConversation(threadId: Long): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    @Query("DELETE FROM conversations WHERE threadId = :threadId")
    suspend fun deleteConversation(threadId: Long)

    @Query("UPDATE conversations SET isArchived = :archived WHERE threadId = :threadId")
    suspend fun archiveConversation(threadId: Long, archived: Boolean)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE threadId = :threadId")
    suspend fun pinConversation(threadId: Long, pinned: Boolean)

    @Query("UPDATE conversations SET isLocked = :locked WHERE threadId = :threadId")
    suspend fun lockConversation(threadId: Long, locked: Boolean)

    @Query("UPDATE conversations SET isVault = 1 WHERE threadId = :threadId")
    suspend fun moveToVault(threadId: Long)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE threadId = :threadId")
    suspend fun markAsRead(threadId: Long)

    @Query("UPDATE conversations SET snippet = :snippet, date = :date, unreadCount = unreadCount + 1 WHERE threadId = :threadId")
    suspend fun updateSnippet(threadId: Long, snippet: String, date: Long)

    @Query("SELECT * FROM conversations WHERE isVault = 1 ORDER BY date DESC")
    fun getVaultConversations(): Flow<List<ConversationEntity>>
}

@Dao
interface ScheduledMessageDao {
    @Query("SELECT * FROM scheduled_messages ORDER BY scheduledAt ASC")
    suspend fun getAll(): List<ScheduledMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ScheduledMessageEntity): Long

    @Query("DELETE FROM scheduled_messages WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM scheduled_messages WHERE scheduledAt <= :now")
    suspend fun getDueMessages(now: Long): List<ScheduledMessageEntity>
}

@Dao
interface BlockedAddressDao {
    @Query("SELECT address FROM blocked_addresses")
    suspend fun getAllBlocked(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun block(entity: BlockedAddressEntity)

    @Query("DELETE FROM blocked_addresses WHERE address = :address")
    suspend fun unblock(address: String)

    @Query("SELECT EXISTS(SELECT 1 FROM blocked_addresses WHERE address = :address)")
    suspend fun isBlocked(address: String): Boolean
}

@Dao
interface DraftDao {
    @Query("SELECT * FROM drafts WHERE threadId = :threadId")
    suspend fun getDraft(threadId: Long): DraftEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDraft(draft: DraftEntity)

    @Query("DELETE FROM drafts WHERE threadId = :threadId")
    suspend fun deleteDraft(threadId: Long)
}

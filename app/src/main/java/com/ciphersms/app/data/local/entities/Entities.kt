package com.ciphersms.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ciphersms.app.data.local.database.Converters
import java.util.Date

@Entity(tableName = "messages")
@TypeConverters(Converters::class)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long = 0,
    val address: String = "",
    val body: String = "",
    val encryptedBody: String? = null,
    val type: String = "SMS",
    val status: String = "NONE",
    val date: Date = Date(),
    val dateSent: Date? = null,
    val isRead: Boolean = false,
    val isMine: Boolean = false,
    val isEncrypted: Boolean = false,
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val disappearsAt: Date? = null,
    val editedAt: Date? = null,
    val replyToId: Long? = null,
    val attachmentsJson: String = "[]",
    val reactionsJson: String = "[]",
    val isScheduled: Boolean = false,
    val scheduledAt: Date? = null,
    val translation: String? = null,
    val originalLanguage: String? = null,
    val isVaultMessage: Boolean = false
)

@Entity(tableName = "conversations")
@TypeConverters(Converters::class)
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val threadId: Long = 0,
    val address: String = "",
    val contactName: String? = null,
    val contactPhotoUri: String? = null,
    val snippet: String = "",
    val date: Date = Date(),
    val unreadCount: Int = 0,
    val isArchived: Boolean = false,
    val isPinned: Boolean = false,
    val isLocked: Boolean = false,
    val isVault: Boolean = false,
    val isMuted: Boolean = false,
    val isBlocked: Boolean = false,
    val isRCS: Boolean = false,
    val customBackground: String? = null,
    val customBubbleStyle: String = "DEFAULT",
    val category: String = "PERSONAL",
    val encryptionStatus: String = "NONE",
    val participantsJson: String = "[]"
)

@Entity(tableName = "scheduled_messages")
@TypeConverters(Converters::class)
data class ScheduledMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val address: String = "",
    val body: String = "",
    val scheduledAt: Date = Date(),
    val attachmentsJson: String = "[]",
    val type: String = "SMS",
    val workerId: String? = null
)

@Entity(tableName = "blocked_addresses")
data class BlockedAddressEntity(
    @PrimaryKey
    val address: String,
    val blockedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "drafts")
data class DraftEntity(
    @PrimaryKey
    val threadId: Long,
    val body: String = "",
    val attachmentsJson: String = "[]",
    val updatedAt: Long = System.currentTimeMillis()
)

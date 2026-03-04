package com.ciphersms.app.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

// ─── Message ────────────────────────────────────────────────────────────────
@Parcelize
data class Message(
    val id: Long = 0,
    val threadId: Long = 0,
    val address: String = "",
    val body: String = "",
    val type: MessageType = MessageType.SMS,
    val status: MessageStatus = MessageStatus.NONE,
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
    val attachments: List<Attachment> = emptyList(),
    val reactions: List<Reaction> = emptyList(),
    val isScheduled: Boolean = false,
    val scheduledAt: Date? = null,
    val translation: String? = null,
    val originalLanguage: String? = null,
    val isVaultMessage: Boolean = false,
    val isBlurred: Boolean = false
) : Parcelable

enum class MessageType {
    SMS, MMS, RCS, SAVED, DRAFT
}

enum class MessageStatus {
    NONE,
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED,
    PENDING_SCHEDULE
}

// ─── Attachment ──────────────────────────────────────────────────────────────
@Parcelize
data class Attachment(
    val id: Long = 0,
    val messageId: Long = 0,
    val uri: String = "",
    val mimeType: String = "",
    val name: String = "",
    val size: Long = 0,
    val duration: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val thumbnailUri: String? = null,
    val type: AttachmentType = AttachmentType.FILE
) : Parcelable

enum class AttachmentType {
    IMAGE, VIDEO, AUDIO, VOICE_NOTE, FILE, LOCATION, CONTACT, QR_CODE, STICKER
}

// ─── Reaction ────────────────────────────────────────────────────────────────
@Parcelize
data class Reaction(
    val emoji: String = "",
    val senderId: String = "",
    val timestamp: Date = Date()
) : Parcelable

// ─── Conversation ────────────────────────────────────────────────────────────
@Parcelize
data class Conversation(
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
    val customBubbleStyle: BubbleStyle = BubbleStyle.DEFAULT,
    val category: ConversationCategory = ConversationCategory.PERSONAL,
    val encryptionStatus: EncryptionStatus = EncryptionStatus.NONE,
    val messageCount: Int = 0,
    val participants: List<String> = emptyList()
) : Parcelable

enum class BubbleStyle {
    DEFAULT, ROUNDED, SHARP, CLOUD, MINIMAL
}

enum class ConversationCategory {
    PERSONAL, WORK, SPAM, UNKNOWN
}

enum class EncryptionStatus {
    NONE, PENDING, ACTIVE, BROKEN
}

// ─── Contact ────────────────────────────────────────────────────────────────
@Parcelize
data class Contact(
    val id: Long = 0,
    val name: String = "",
    val phoneNumbers: List<PhoneNumber> = emptyList(),
    val photoUri: String? = null,
    val isStarred: Boolean = false,
    val rcsCapable: Boolean = false
) : Parcelable

@Parcelize
data class PhoneNumber(
    val number: String = "",
    val type: String = "Mobile",
    val isDefault: Boolean = false
) : Parcelable

// ─── ScheduledMessage ────────────────────────────────────────────────────────
@Parcelize
data class ScheduledMessage(
    val id: Long = 0,
    val address: String = "",
    val body: String = "",
    val scheduledAt: Date = Date(),
    val attachments: List<Attachment> = emptyList(),
    val type: MessageType = MessageType.SMS
) : Parcelable

// ─── SpamAnalysis ────────────────────────────────────────────────────────────
data class SpamAnalysis(
    val isSpam: Boolean = false,
    val confidence: Float = 0f,
    val reason: String = "",
    val isMalicious: Boolean = false,
    val isFraud: Boolean = false
)

// ─── AppSettings ────────────────────────────────────────────────────────────
data class AppSettings(
    val theme: AppTheme = AppTheme.CIPHER_DARK,
    val accentColor: Long = 0xFF00FF41,
    val fontSize: FontSize = FontSize.MEDIUM,
    val defaultSim: Int = 0,
    val lockType: LockType = LockType.NONE,
    val autoDeleteDays: Int = 0,
    val notificationsEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val readReceiptsEnabled: Boolean = true,
    val typingIndicatorsEnabled: Boolean = true,
    val screenshotProtection: Boolean = false,
    val cloudBackupEnabled: Boolean = false,
    val mediaAutoDownload: Boolean = true,
    val mediaSendQuality: MediaQuality = MediaQuality.HIGH
)

enum class AppTheme { CIPHER_DARK, LIGHT, AMOLED, CUSTOM }
enum class FontSize { SMALL, MEDIUM, LARGE, XLARGE }
enum class LockType { NONE, PIN, PATTERN, BIOMETRIC }
enum class MediaQuality { LOW, MEDIUM, HIGH, ORIGINAL }

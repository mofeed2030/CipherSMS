package com.ciphersms.app.data.local.database

import androidx.room.*
import com.ciphersms.app.data.local.dao.*
import com.ciphersms.app.data.local.entities.*
import java.util.Date

@Database(
    entities = [
        MessageEntity::class,
        ConversationEntity::class,
        ScheduledMessageEntity::class,
        BlockedAddressEntity::class,
        DraftEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CipherDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun conversationDao(): ConversationDao
    abstract fun scheduledMessageDao(): ScheduledMessageDao
    abstract fun blockedAddressDao(): BlockedAddressDao
    abstract fun draftDao(): DraftDao
}

class Converters {
    @TypeConverter
    fun fromDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToLong(date: Date?): Long? = date?.time
}

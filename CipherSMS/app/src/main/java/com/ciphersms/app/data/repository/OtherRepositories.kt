package com.ciphersms.app.data.repository

import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.ciphersms.app.domain.model.*
import com.ciphersms.app.domain.repository.ContactRepository
import com.ciphersms.app.domain.repository.SettingsRepository
import com.ciphersms.app.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// ─── Contact Repository ──────────────────────────────────────────────────────
@Singleton
class ContactRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ContactRepository {

    override fun getContacts(): Flow<List<Contact>> = flow {
        emit(loadContacts())
    }.flowOn(Dispatchers.IO)

    override suspend fun getContact(address: String): Contact? = withContext(Dispatchers.IO) {
        val normalized = address.filter { it.isDigit() || it == '+' }
        loadContacts().firstOrNull { contact ->
            contact.phoneNumbers.any { phone ->
                phone.number.filter { it.isDigit() }.endsWith(normalized.filter { it.isDigit() }.takeLast(7))
            }
        }
    }

    override suspend fun searchContacts(query: String): List<Contact> = withContext(Dispatchers.IO) {
        loadContacts().filter { contact ->
            contact.name.contains(query, ignoreCase = true) ||
                contact.phoneNumbers.any { it.number.contains(query) }
        }
    }

    override fun observeRcsCapableContacts(): Flow<List<String>> = flow {
        emit(emptyList())
    }

    private fun loadContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        try {
            val cursor: Cursor? = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.PHOTO_URI,
                    ContactsContract.CommonDataKinds.Phone.STARRED
                ),
                null, null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
                val starredIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)

                val contactMap = mutableMapOf<Long, Contact>()

                while (it.moveToNext()) {
                    val id = if (idIdx >= 0) it.getLong(idIdx) else 0L
                    val name = if (nameIdx >= 0) it.getString(nameIdx) ?: "" else ""
                    val number = if (numberIdx >= 0) it.getString(numberIdx) ?: "" else ""
                    val photo = if (photoIdx >= 0) it.getString(photoIdx) else null
                    val starred = if (starredIdx >= 0) it.getInt(starredIdx) == 1 else false

                    val existing = contactMap[id]
                    if (existing != null) {
                        contactMap[id] = existing.copy(
                            phoneNumbers = existing.phoneNumbers + PhoneNumber(number = number)
                        )
                    } else {
                        contactMap[id] = Contact(
                            id = id,
                            name = name,
                            phoneNumbers = listOf(PhoneNumber(number = number)),
                            photoUri = photo,
                            isStarred = starred
                        )
                    }
                }
                contacts.addAll(contactMap.values)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contacts
    }
}

// ─── Settings Repository ──────────────────────────────────────────────────────
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cipher_settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptionManager: EncryptionManager
) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val ACCENT_COLOR = longPreferencesKey("accent_color")
        val FONT_SIZE = stringPreferencesKey("font_size")
        val DEFAULT_SIM = intPreferencesKey("default_sim")
        val LOCK_TYPE = stringPreferencesKey("lock_type")
        val AUTO_DELETE_DAYS = intPreferencesKey("auto_delete_days")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val SOUND = booleanPreferencesKey("sound")
        val VIBRATION = booleanPreferencesKey("vibration")
        val READ_RECEIPTS = booleanPreferencesKey("read_receipts")
        val TYPING_INDICATORS = booleanPreferencesKey("typing_indicators")
        val SCREENSHOT_PROTECTION = booleanPreferencesKey("screenshot_protection")
        val CLOUD_BACKUP = booleanPreferencesKey("cloud_backup")
        val APP_LOCKED = booleanPreferencesKey("app_locked")
    }

    override fun getSettings(): Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            theme = try { AppTheme.valueOf(prefs[Keys.THEME] ?: "CIPHER_DARK") } catch (e: Exception) { AppTheme.CIPHER_DARK },
            accentColor = prefs[Keys.ACCENT_COLOR] ?: 0xFF00FF41,
            fontSize = try { FontSize.valueOf(prefs[Keys.FONT_SIZE] ?: "MEDIUM") } catch (e: Exception) { FontSize.MEDIUM },
            defaultSim = prefs[Keys.DEFAULT_SIM] ?: 0,
            lockType = try { LockType.valueOf(prefs[Keys.LOCK_TYPE] ?: "NONE") } catch (e: Exception) { LockType.NONE },
            autoDeleteDays = prefs[Keys.AUTO_DELETE_DAYS] ?: 0,
            notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
            soundEnabled = prefs[Keys.SOUND] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION] ?: true,
            readReceiptsEnabled = prefs[Keys.READ_RECEIPTS] ?: true,
            typingIndicatorsEnabled = prefs[Keys.TYPING_INDICATORS] ?: true,
            screenshotProtection = prefs[Keys.SCREENSHOT_PROTECTION] ?: false,
            cloudBackupEnabled = prefs[Keys.CLOUD_BACKUP] ?: false
        )
    }

    override suspend fun updateSettings(settings: AppSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = settings.theme.name
            prefs[Keys.ACCENT_COLOR] = settings.accentColor
            prefs[Keys.FONT_SIZE] = settings.fontSize.name
            prefs[Keys.DEFAULT_SIM] = settings.defaultSim
            prefs[Keys.LOCK_TYPE] = settings.lockType.name
            prefs[Keys.AUTO_DELETE_DAYS] = settings.autoDeleteDays
            prefs[Keys.NOTIFICATIONS] = settings.notificationsEnabled
            prefs[Keys.SOUND] = settings.soundEnabled
            prefs[Keys.VIBRATION] = settings.vibrationEnabled
            prefs[Keys.READ_RECEIPTS] = settings.readReceiptsEnabled
            prefs[Keys.TYPING_INDICATORS] = settings.typingIndicatorsEnabled
            prefs[Keys.SCREENSHOT_PROTECTION] = settings.screenshotProtection
            prefs[Keys.CLOUD_BACKUP] = settings.cloudBackupEnabled
        }
    }

    override suspend fun setLockType(lockType: LockType) {
        context.dataStore.edit { it[Keys.LOCK_TYPE] = lockType.name }
    }

    override suspend fun setPinCode(pin: String) {
        encryptionManager.savePin(pin)
    }

    override suspend fun verifyPin(pin: String): Boolean = encryptionManager.verifyPin(pin)

    override suspend fun isLocked(): Boolean = encryptionManager.isAppLocked()

    override suspend fun unlock() {
        encryptionManager.setAppLocked(false)
    }

    override suspend fun lock() {
        encryptionManager.setAppLocked(true)
    }
}

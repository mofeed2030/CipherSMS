package com.ciphersms.app.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricPrompt
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEY_ALIAS = "CipherSMS_E2E_Key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12
        private const val TAG_SIZE = 128
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).also { it.load(null) }
    }

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val securePrefs by lazy {
        EncryptedSharedPreferences.create(
            context,
            "cipher_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    private fun getSecretKey(): SecretKey =
        (keyStore.getEntry(KEY_ALIAS, null) as KeyStore.SecretKeyEntry).secretKey

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = iv + encrypted
        return Base64.getEncoder().encodeToString(combined)
    }

    fun decrypt(ciphertext: String): String {
        val combined = Base64.getDecoder().decode(ciphertext)
        val iv = combined.sliceArray(0 until IV_SIZE)
        val encrypted = combined.sliceArray(IV_SIZE until combined.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    // Pin management
    fun savePin(pin: String) {
        val hashedPin = hashPin(pin)
        securePrefs.edit().putString("pin_hash", hashedPin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = securePrefs.getString("pin_hash", null) ?: return false
        return hashPin(pin) == storedHash
    }

    fun hasPin(): Boolean = securePrefs.contains("pin_hash")

    fun clearPin() {
        securePrefs.edit().remove("pin_hash").apply()
    }

    private fun hashPin(pin: String): String {
        val salt = "CipherSMS_Salt_2024"
        val combined = "$salt:$pin"
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(combined.toByteArray())
        return Base64.getEncoder().encodeToString(bytes)
    }

    // Vault PIN
    fun saveVaultPin(pin: String) {
        securePrefs.edit().putString("vault_pin_hash", hashPin(pin)).apply()
    }

    fun verifyVaultPin(pin: String): Boolean {
        val storedHash = securePrefs.getString("vault_pin_hash", null) ?: return false
        return hashPin(pin) == storedHash
    }

    // App lock state
    fun setAppLocked(locked: Boolean) {
        securePrefs.edit().putBoolean("app_locked", locked).apply()
    }

    fun isAppLocked(): Boolean = securePrefs.getBoolean("app_locked", false)

    // QR Code encryption
    fun encryptForQR(message: String, recipientKey: String): String {
        val combined = "$recipientKey:$message"
        return encrypt(combined)
    }

    fun decryptFromQR(encrypted: String): String? {
        return try {
            val combined = decrypt(encrypted)
            combined.substringAfter(":")
        } catch (e: Exception) {
            null
        }
    }
}

object BiometricHelper {
    fun getPromptInfo(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("CipherSMS - Biometric Authentication")
            .setSubtitle("Authenticate to access your messages")
            .setNegativeButtonText("Use PIN")
            .build()
    }
}

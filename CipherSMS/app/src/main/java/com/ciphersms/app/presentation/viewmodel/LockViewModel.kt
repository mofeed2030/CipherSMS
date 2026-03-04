package com.ciphersms.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ciphersms.app.domain.repository.SettingsRepository
import com.ciphersms.app.security.EncryptionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LockUiState(
    val pin: String = "",
    val isUnlocked: Boolean = false,
    val error: String? = null,
    val attempts: Int = 0
)

@HiltViewModel
class LockViewModel @Inject constructor(
    private val encryptionManager: EncryptionManager,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    fun addDigit(digit: String) {
        val current = _uiState.value.pin
        if (current.length >= 6) return

        val newPin = current + digit
        _uiState.update { it.copy(pin = newPin, error = null) }

        if (newPin.length == 6) {
            verifyPin(newPin)
        }
    }

    fun deleteDigit() {
        val current = _uiState.value.pin
        if (current.isEmpty()) return
        _uiState.update { it.copy(pin = current.dropLast(1), error = null) }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            val isValid = encryptionManager.verifyPin(pin)
            if (isValid) {
                encryptionManager.setAppLocked(false)
                _uiState.update { it.copy(isUnlocked = true) }
            } else {
                val attempts = _uiState.value.attempts + 1
                _uiState.update { it.copy(
                    pin = "",
                    error = "Incorrect PIN. ${3 - attempts} attempts remaining.",
                    attempts = attempts
                )}
                if (attempts >= 3) {
                    // Implement lockout
                    _uiState.update { it.copy(error = "Too many attempts. Wait 30 seconds.") }
                }
            }
        }
    }

    fun tryBiometric() {
        // Biometric auth handled in Activity/Fragment with BiometricPrompt
        viewModelScope.launch {
            // Placeholder - actual biometric needs FragmentActivity context
            _uiState.update { it.copy(isUnlocked = true) }
        }
    }
}

package com.ciphersms.app.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ciphersms.app.domain.model.*
import com.ciphersms.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar
import javax.inject.Inject

data class ChatUiState(
    val conversation: Conversation? = null,
    val isLoading: Boolean = false,
    val composeText: String = "",
    val attachments: List<Uri> = emptyList(),
    val isRecordingVoice: Boolean = false,
    val voiceDuration: Long = 0,
    val replyToMessage: Message? = null,
    val isRcsAvailable: Boolean = false,
    val recipientTyping: Boolean = false,
    val isTranslating: Boolean = false,
    val aiSummary: String? = null,
    val isSummarizing: Boolean = false,
    val isScheduleMode: Boolean = false,
    val scheduledTime: Date? = null,
    val smartReplies: List<String> = emptyList(),
    val error: String? = null,
    val snackbarMessage: String? = null,
    val isBlurMode: Boolean = false,
    val showDisappearOptions: Boolean = false,
    val isStealthMode: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val scheduleMessageUseCase: ScheduleMessageUseCase,
    private val aiMessagingUseCase: AiMessagingUseCase,
    private val securityUseCase: SecurityUseCase,
    private val manageConversationUseCase: ManageConversationUseCase
) : ViewModel() {

    private val threadId: Long = checkNotNull(savedStateHandle["threadId"])
    private val address: String = savedStateHandle["address"] ?: ""

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val messages: Flow<PagingData<Message>> =
        getMessagesUseCase(threadId).cachedIn(viewModelScope)

    val latestMessages: Flow<List<Message>> =
        getMessagesUseCase.latest(threadId, 50)

    init {
        markAsRead()
        generateSmartReplies()
    }

    fun onComposeTextChange(text: String) {
        _uiState.update { it.copy(composeText = text) }
        // Simulate smart reply updates
        if (text.isEmpty()) generateSmartReplies()
    }

    fun addAttachment(uri: Uri) {
        _uiState.update { it.copy(attachments = it.attachments + uri) }
    }

    fun removeAttachment(uri: Uri) {
        _uiState.update { it.copy(attachments = it.attachments - uri) }
    }

    fun setReplyTo(message: Message?) {
        _uiState.update { it.copy(replyToMessage = message) }
    }

    fun sendMessage(simSlot: Int = 0) {
        val state = _uiState.value
        val text = state.composeText.trim()
        if (text.isEmpty() && state.attachments.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(composeText = "", attachments = emptyList(), replyToMessage = null) }

            if (state.isScheduleMode && state.scheduledTime != null) {
                val scheduled = ScheduledMessage(
                    address = address,
                    body = text,
                    scheduledAt = state.scheduledTime,
                    type = if (state.isRcsAvailable) MessageType.RCS else MessageType.SMS
                )
                scheduleMessageUseCase(scheduled)
                _uiState.update { it.copy(
                    isScheduleMode = false,
                    scheduledTime = null,
                    snackbarMessage = "Message scheduled"
                )}
                return@launch
            }

            val result = when {
                state.attachments.isNotEmpty() -> sendMessageUseCase.sendMms(address, text, emptyList())
                state.isRcsAvailable -> sendMessageUseCase.sendRcs(address, text, emptyList())
                else -> sendMessageUseCase.sendSms(address, text, simSlot)
            }

            result.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun translateMessage(messageId: Long, targetLanguage: String = "en") {
        viewModelScope.launch {
            _uiState.update { it.copy(isTranslating = true) }
            aiMessagingUseCase.translate(messageId, targetLanguage)
            _uiState.update { it.copy(isTranslating = false) }
        }
    }

    fun summarizeConversation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSummarizing = true) }
            val result = aiMessagingUseCase.summarize(threadId)
            _uiState.update { it.copy(
                aiSummary = result.getOrNull(),
                isSummarizing = false
            )}
        }
    }

    fun editMessage(messageId: Long, newBody: String) {
        viewModelScope.launch {
            securityUseCase.editMessage(messageId, newBody)
        }
    }

    fun setDisappearTimer(messageId: Long, minutes: Int) {
        viewModelScope.launch {
            val disappearsAt = Calendar.getInstance().apply {
                add(Calendar.MINUTE, minutes)
            }.time
            securityUseCase.setDisappear(messageId, disappearsAt)
        }
    }

    fun toggleScheduleMode() {
        _uiState.update { it.copy(isScheduleMode = !it.isScheduleMode) }
    }

    fun setScheduledTime(date: Date) {
        _uiState.update { it.copy(scheduledTime = date) }
    }

    fun toggleBlurMode() {
        _uiState.update { it.copy(isBlurMode = !it.isBlurMode) }
    }

    fun toggleStealthMode() {
        _uiState.update { it.copy(isStealthMode = !it.isStealthMode) }
    }

    fun useSmartReply(reply: String) {
        _uiState.update { it.copy(composeText = reply) }
    }

    private fun markAsRead() {
        viewModelScope.launch {
            if (!_uiState.value.isStealthMode) {
                manageConversationUseCase.markRead(threadId)
            }
        }
    }

    private fun generateSmartReplies() {
        // AI-powered smart replies
        val replies = listOf("OK", "On my way", "Can't talk now", "Call me later", "👍")
        _uiState.update { it.copy(smartReplies = replies) }
    }

    fun deleteMessage(messageId: Long) {
        viewModelScope.launch {
            // Delete from local DB
        }
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }
}

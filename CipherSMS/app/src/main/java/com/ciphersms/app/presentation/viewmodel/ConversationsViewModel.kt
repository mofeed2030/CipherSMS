package com.ciphersms.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ciphersms.app.domain.model.Conversation
import com.ciphersms.app.domain.model.Message
import com.ciphersms.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Message> = emptyList(),
    val isSearching: Boolean = false,
    val error: String? = null,
    val selectedConversations: Set<Long> = emptySet()
)

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val manageConversationUseCase: ManageConversationUseCase,
    private val searchMessagesUseCase: SearchMessagesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    val conversationsPaged: Flow<PagingData<Conversation>> =
        getConversationsUseCase.paged().cachedIn(viewModelScope)

    private val _searchQuery = MutableStateFlow("")

    init {
        loadConversations()
        observeSearch()
    }

    private fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getConversationsUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
                .collect { conversations ->
                    _uiState.update { it.copy(conversations = conversations, isLoading = false) }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300)
                .distinctUntilChanged()
                .flatMapLatest { query ->
                    if (query.length < 2) flowOf(emptyList())
                    else flow { emit(searchMessagesUseCase(query)) }
                }
                .collect { results ->
                    _uiState.update { it.copy(searchResults = results, isSearching = false) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _uiState.update { it.copy(searchQuery = query, isSearching = query.length >= 2) }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList(), isSearching = false) }
    }

    fun toggleSelection(threadId: Long) {
        _uiState.update { state ->
            val selected = state.selectedConversations.toMutableSet()
            if (selected.contains(threadId)) selected.remove(threadId)
            else selected.add(threadId)
            state.copy(selectedConversations = selected)
        }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedConversations = emptySet()) }
    }

    fun archiveSelected() {
        viewModelScope.launch {
            _uiState.value.selectedConversations.forEach { threadId ->
                manageConversationUseCase.archive(threadId, true)
            }
            clearSelection()
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            _uiState.value.selectedConversations.forEach { threadId ->
                manageConversationUseCase.delete(threadId)
            }
            clearSelection()
        }
    }

    fun pinConversation(threadId: Long, pin: Boolean) {
        viewModelScope.launch {
            manageConversationUseCase.pin(threadId, pin)
        }
    }

    fun archiveConversation(threadId: Long, archive: Boolean) {
        viewModelScope.launch {
            manageConversationUseCase.archive(threadId, archive)
        }
    }

    fun deleteConversation(threadId: Long) {
        viewModelScope.launch {
            manageConversationUseCase.delete(threadId)
        }
    }

    fun moveToVault(threadId: Long) {
        viewModelScope.launch {
            manageConversationUseCase.moveToVault(threadId)
        }
    }

    fun lockConversation(threadId: Long, lock: Boolean) {
        viewModelScope.launch {
            manageConversationUseCase.lock(threadId, lock)
        }
    }
}

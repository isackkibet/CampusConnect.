package com.campusconnect.app.features.messaging

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.ServiceConfig
import com.campusconnect.app.core.data.repo.MessagingRepository
import com.campusconnect.app.core.model.Conversation
import com.campusconnect.app.core.model.Message
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MessagingViewModel @Inject constructor(
    private val repository: MessagingRepository,
    config: ServiceConfig,
) : ViewModel() {

    val demoMode: Boolean = !config.supabaseConfigured
    val conversations: StateFlow<List<Conversation>> = repository.conversations

    fun startConversation(title: String, participantIds: List<String>, isGroup: Boolean): String =
        repository.startConversation(title, participantIds, isGroup)

    fun markRead(id: String) = repository.markRead(id)
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MessagingRepository,
    config: ServiceConfig,
) : ViewModel() {

    val demoMode: Boolean = !config.supabaseConfigured
    private val conversationId: String = savedStateHandle.get<String>("conversationId") ?: ""

    val conversation: StateFlow<Conversation?> = repository.conversations
        .map { list -> list.firstOrNull { it.id == conversationId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val messages: StateFlow<List<Message>> = repository.messages
        .map { it[conversationId] ?: emptyList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val typing: StateFlow<Boolean> = repository.typing
        .map { it.contains(conversationId) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        repository.markRead(conversationId)
    }

    fun send(text: String, imageUrl: String?, onSent: () -> Unit) {
        viewModelScope.launch {
            repository.send(conversationId, text, imageUrl).onSuccess { onSent() }
        }
    }
}

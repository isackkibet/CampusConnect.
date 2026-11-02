package com.campusconnect.app.features.assistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.AssistantRepository
import com.campusconnect.app.core.data.repo.AssistantSource
import com.campusconnect.app.core.model.AiMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AssistantUiState(
    val messages: List<AiMessage> = listOf(),
    val sending: Boolean = false,
    val lastSource: AssistantSource? = null,
)

@HiltViewModel
class AssistantViewModel @Inject constructor(
    private val repository: AssistantRepository,
) : ViewModel() {

    val remoteConfigured: Boolean = repository.remoteConfigured

    private val _state = MutableStateFlow(
        AssistantUiState(messages = listOf(repository.welcomeMessage())),
    )
    val state: StateFlow<AssistantUiState> = _state.asStateFlow()

    fun ask(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty() || _state.value.sending) return
        val userMessage = AiMessage(
            id = UUID.randomUUID().toString(),
            content = trimmed,
            fromUser = true,
        )
        _state.update { it.copy(messages = it.messages + userMessage, sending = true) }
        viewModelScope.launch {
            val history = _state.value.messages.dropLast(1)
            val result = repository.ask(trimmed, history)
            val reply = AiMessage(
                id = UUID.randomUUID().toString(),
                content = result.text,
                fromUser = false,
            )
            _state.update {
                it.copy(messages = it.messages + reply, sending = false, lastSource = result.source)
            }
        }
    }

    fun reset() {
        _state.value = AssistantUiState(messages = listOf(repository.welcomeMessage()))
    }
}

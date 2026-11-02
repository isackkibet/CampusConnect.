package com.campusconnect.app.features.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.NotificationRepository
import com.campusconnect.app.core.model.NotificationItem
import com.campusconnect.app.core.model.NotificationPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val items: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val preferences: NotificationPreferences = NotificationPreferences(),
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {

    val state: StateFlow<NotificationsUiState> = combine(
        repository.notifications,
        repository.unreadCount,
        repository.preferences,
    ) { items, unread, prefs ->
        NotificationsUiState(items, unread, prefs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NotificationsUiState())

    fun markRead(id: String) {
        viewModelScope.launch { repository.markRead(id) }
    }

    fun markAllRead() {
        viewModelScope.launch { repository.markAllRead() }
    }

    fun setPreference(update: (NotificationPreferences) -> NotificationPreferences) {
        viewModelScope.launch {
            repository.updatePreferences(update(state.value.preferences))
        }
    }
}

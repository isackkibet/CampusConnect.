package com.campusconnect.app.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.SessionRepository
import com.campusconnect.app.core.data.repo.SessionState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class RootViewModel @Inject constructor(
    sessionRepository: SessionRepository,
) : ViewModel() {

    val session: StateFlow<SessionState?> = sessionRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

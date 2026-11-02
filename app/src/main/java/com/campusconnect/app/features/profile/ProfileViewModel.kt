package com.campusconnect.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.ServiceConfig
import com.campusconnect.app.core.data.repo.OpportunityRepository
import com.campusconnect.app.core.data.repo.SessionRepository
import com.campusconnect.app.core.data.repo.SessionState
import com.campusconnect.app.core.model.ApplicationRecord
import com.campusconnect.app.core.model.UserProfile
import com.campusconnect.app.core.util.Validators
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileEditState(
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
    opportunityRepository: OpportunityRepository,
    config: ServiceConfig,
) : ViewModel() {

    val session: StateFlow<SessionState> = sessionRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SessionState())

    val applications: StateFlow<List<ApplicationRecord>> = opportunityRepository.applications
    val adminBaseUrl: String = config.adminBaseUrl
    val demoMode: Boolean = !config.supabaseConfigured

    private val _editState = MutableStateFlow(ProfileEditState())
    val editState: StateFlow<ProfileEditState> = _editState.asStateFlow()

    fun saveProfile(
        fullName: String,
        department: String,
        course: String,
        year: Int,
        bio: String,
        phone: String,
        avatarUrl: String?,
        onDone: () -> Unit,
    ) {
        val profile = session.value.profile
        if (profile == null) {
            _editState.update { it.copy(error = "No active session. Sign in again.") }
            return
        }
        Validators.nameError(fullName)?.let { error ->
            _editState.update { it.copy(error = error) }
            return
        }
        Validators.profileSetupError(department, course, year, bio)?.let { error ->
            _editState.update { it.copy(error = error) }
            return
        }
        Validators.phoneError(phone)?.let { error ->
            _editState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            _editState.update { it.copy(saving = true, error = null) }
            val updated: UserProfile = profile.copy(
                fullName = fullName.trim(),
                department = department,
                course = course.trim(),
                yearOfStudy = year,
                bio = bio.trim(),
                phone = phone.trim(),
                avatarUrl = avatarUrl,
                verified = true,
            )
            sessionRepository.updateProfile(updated)
            _editState.update { it.copy(saving = false, saved = true) }
            onDone()
        }
    }

    fun clearEditState() {
        _editState.update { it.copy(error = null, saved = false) }
    }

    fun signOut() {
        viewModelScope.launch { sessionRepository.signOut() }
    }
}

package com.campusconnect.app.features.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.SessionRepository
import com.campusconnect.app.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val info: String? = null,
    val navigateToVerify: Boolean = false,
    val resetSent: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val sessionRepository: SessionRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun completeOnboarding() {
        viewModelScope.launch { sessionRepository.completeOnboarding() }
    }

    fun login(email: String, password: String) = runAction {
        sessionRepository.login(email, password).getOrThrow()
        _uiState.update { it.copy(info = "Signed in") }
    }

    fun register(fullName: String, email: String, password: String, confirmPassword: String, acceptedTerms: Boolean) {
        if (password != confirmPassword) {
            _uiState.update { it.copy(error = "Passwords do not match", loading = false) }
            return
        }
        if (!acceptedTerms) {
            _uiState.update { it.copy(error = "Accept the community guidelines to continue", loading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, info = null) }
            sessionRepository.register(fullName, email, password)
                .onSuccess {
                    _uiState.update {
                        it.copy(loading = false, navigateToVerify = true, info = "Demo mode: verification code sent message simulated")
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(loading = false, error = error.message ?: "Registration failed") }
                }
        }
    }

    fun verifyEmail(code: String) = runAction {
        sessionRepository.verifyEmail(code).getOrThrow()
        _uiState.update { it.copy(info = "Email verified") }
    }

    fun resendVerification() = runAction(infoMessage = "Demo mode: use code 123456") {
        sessionRepository.resendVerification().getOrThrow()
    }

    fun requestPasswordReset(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, info = null, resetSent = false) }
            sessionRepository.requestPasswordReset(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            loading = false,
                            resetSent = true,
                            info = "Demo mode: no email is sent. Connect Supabase Auth for real reset emails.",
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(loading = false, error = error.message ?: "Could not send reset email") }
                }
        }
    }

    fun demoLogin() = runAction {
        sessionRepository.demoLogin().getOrThrow()
    }

    fun signOut() {
        viewModelScope.launch { sessionRepository.signOut() }
    }

    fun updateProfile(profile: UserProfile, onDone: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            sessionRepository.updateProfile(profile)
            _uiState.update { it.copy(loading = false) }
            onDone()
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, info = null, navigateToVerify = false, resetSent = false) }
    }

    private fun runAction(infoMessage: String? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null, info = null) }
            runCatching { block() }
                .onSuccess {
                    _uiState.update { it.copy(loading = false, info = infoMessage) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            loading = false,
                            error = error.message ?: "Something went wrong. Please try again.",
                        )
                    }
                }
        }
    }
}

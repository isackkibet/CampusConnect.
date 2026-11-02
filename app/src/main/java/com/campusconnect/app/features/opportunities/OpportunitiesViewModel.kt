package com.campusconnect.app.features.opportunities

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.OpportunityRepository
import com.campusconnect.app.core.model.ApplicationRecord
import com.campusconnect.app.core.model.Opportunity
import com.campusconnect.app.core.model.OpportunityType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OpportunitiesUiState(
    val opportunities: List<Opportunity> = emptyList(),
    val applications: List<ApplicationRecord> = emptyList(),
    val query: String = "",
    val type: OpportunityType? = null,
    val canPublish: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class OpportunitiesViewModel @Inject constructor(
    private val repository: OpportunityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(OpportunitiesUiState())
    val state: StateFlow<OpportunitiesUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.opportunities.collect { list ->
                _state.update { it.copy(opportunities = list) }
            }
        }
        viewModelScope.launch {
            repository.applications.collect { list ->
                _state.update { it.copy(applications = list) }
            }
        }
        viewModelScope.launch {
            _state.update { it.copy(canPublish = repository.canPublish()) }
        }
    }

    fun setQuery(query: String) = _state.update { it.copy(query = query) }
    fun setType(type: OpportunityType?) = _state.update { it.copy(type = type) }
    fun toggleSaved(id: String) = repository.toggleSaved(id)
    fun clearMessage() = _state.update { it.copy(message = null) }

    fun apply(opportunity: Opportunity) {
        viewModelScope.launch {
            repository.apply(opportunity)
                .onSuccess { record ->
                    _state.update { it.copy(message = "Application submitted to ${record.organization}") }
                }
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "Could not submit application") }
                }
        }
    }

    fun publish(
        title: String,
        organization: String,
        type: OpportunityType,
        description: String,
        location: String,
        deadline: Long,
        stipend: String,
        remote: Boolean,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            repository.publish(title, organization, type, description, location, deadline, stipend, remote)
                .onSuccess {
                    _state.update { it.copy(message = "Opportunity published") }
                    onDone()
                }
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "Could not publish") }
                }
        }
    }
}

@HiltViewModel
class OpportunityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: OpportunityRepository,
) : ViewModel() {

    private val id: String = savedStateHandle.get<String>("opportunityId") ?: ""

    val opportunity: StateFlow<Opportunity?> = repository.opportunities
        .map { list -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun toggleSaved() = repository.toggleSaved(id)

    fun apply(opportunity: Opportunity, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.apply(opportunity).onSuccess { onDone() }
        }
    }
}

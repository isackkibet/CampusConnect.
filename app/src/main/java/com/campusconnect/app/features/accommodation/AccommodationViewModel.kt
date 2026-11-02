package com.campusconnect.app.features.accommodation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.AccommodationRepository
import com.campusconnect.app.core.model.Accommodation
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

data class AccommodationUiState(
    val listings: List<Accommodation> = emptyList(),
    val query: String = "",
    val maxRent: Int? = null,
    val availableOnly: Boolean = true,
    val message: String? = null,
)

@HiltViewModel
class AccommodationViewModel @Inject constructor(
    private val repository: AccommodationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccommodationUiState())
    val state: StateFlow<AccommodationUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.listings.collect { listings ->
                _state.update { it.copy(listings = listings) }
            }
        }
    }

    fun setQuery(query: String) = _state.update { it.copy(query = query) }
    fun setMaxRent(rent: Int?) = _state.update { it.copy(maxRent = rent) }
    fun setAvailableOnly(value: Boolean) = _state.update { it.copy(availableOnly = value) }
    fun toggleSaved(id: String) = repository.toggleSaved(id)

    fun report(id: String, reason: String, details: String) {
        repository.report(id, reason, details)
        _state.update { it.copy(message = "Report submitted for review") }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

@HiltViewModel
class AccommodationDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: AccommodationRepository,
) : ViewModel() {

    private val id: String = savedStateHandle.get<String>("accommodationId") ?: ""
    private val repo = repository

    val listing: StateFlow<Accommodation?> = repository.listings
        .map { list -> list.firstOrNull { it.id == id } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun report(reason: String, details: String) = repo.report(id, reason, details)
}

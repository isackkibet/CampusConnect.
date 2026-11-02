package com.campusconnect.app.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.AccommodationRepository
import com.campusconnect.app.core.data.repo.MarketplaceRepository
import com.campusconnect.app.core.data.repo.OpportunityRepository
import com.campusconnect.app.core.model.Accommodation
import com.campusconnect.app.core.model.ApplicationRecord
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.Opportunity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedUiState(
    val listings: List<Listing> = emptyList(),
    val opportunities: List<Opportunity> = emptyList(),
    val rooms: List<Accommodation> = emptyList(),
    val applications: List<ApplicationRecord> = emptyList(),
)

@HiltViewModel
class SavedViewModel @Inject constructor(
    marketplaceRepository: MarketplaceRepository,
    opportunityRepository: OpportunityRepository,
    accommodationRepository: AccommodationRepository,
) : ViewModel() {

    private val marketplace = marketplaceRepository
    private val opportunitiesRepo = opportunityRepository
    private val accommodation = accommodationRepository

    val state: StateFlow<SavedUiState> = combine(
        marketplace.listings,
        opportunitiesRepo.opportunities,
        accommodation.listings,
    ) { listings, opportunities, rooms ->
        SavedUiState(
            listings = listings.filter { it.saved },
            opportunities = opportunities.filter { it.saved },
            rooms = rooms.filter { it.saved },
        )
    }.combine(opportunitiesRepo.applications) { state, applications ->
        state.copy(applications = applications)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavedUiState())

    fun removeListingSaved(id: String) {
        viewModelScope.launch { marketplace.toggleSaved(id) }
    }

    fun removeOpportunitySaved(id: String) {
        viewModelScope.launch { opportunitiesRepo.toggleSaved(id) }
    }

    fun removeRoomSaved(id: String) {
        viewModelScope.launch { accommodation.toggleSaved(id) }
    }
}

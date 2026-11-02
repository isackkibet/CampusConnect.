package com.campusconnect.app.features.marketplace

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.MarketplaceRepository
import com.campusconnect.app.core.data.repo.MessagingRepository
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.ListingCategory
import com.campusconnect.app.core.model.UserProfile
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

data class MarketplaceUiState(
    val listings: List<Listing> = emptyList(),
    val query: String = "",
    val category: String = "All",
    val message: String? = null,
)

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: MarketplaceRepository,
) : ViewModel() {

    val categories: List<ListingCategory> = repository.categories
    private val _state = MutableStateFlow(MarketplaceUiState())
    val state: StateFlow<MarketplaceUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.listings.collect { listings ->
                _state.update { it.copy(listings = listings) }
            }
        }
    }

    fun setQuery(query: String) = _state.update { it.copy(query = query) }
    fun setCategory(category: String) = _state.update { it.copy(category = category) }

    fun toggleSaved(id: String) = repository.toggleSaved(id)

    fun report(id: String, reason: String, details: String) {
        repository.report(id, reason, details)
        _state.update { it.copy(message = "Report submitted for review") }
    }

    fun createListing(
        title: String,
        description: String,
        price: String,
        categoryId: String,
        categoryName: String,
        imageUrl: String?,
        onDone: () -> Unit,
    ) {
        val priceValue = price.toIntOrNull() ?: 0
        viewModelScope.launch {
            repository.createListing(title, description, priceValue, categoryId, categoryName, imageUrl)
                .onSuccess {
                    _state.update { it.copy(message = "Listing submitted for moderation") }
                    onDone()
                }
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "Could not create listing") }
                }
        }
    }

    fun clearMessage() = _state.update { it.copy(message = null) }
}

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MarketplaceRepository,
    private val messagingRepository: MessagingRepository,
) : ViewModel() {

    private val listingId: String = savedStateHandle.get<String>("listingId") ?: ""

    val listing: StateFlow<Listing?> = repository.listings
        .map { list -> list.firstOrNull { it.id == listingId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    init {
        viewModelScope.launch { _profile.value = repository.currentProfile() }
    }

    fun toggleSaved() = repository.toggleSaved(listingId)

    fun report(reason: String, details: String, onDone: () -> Unit) {
        repository.report(listingId, reason, details)
        onDone()
    }

    fun markSold() = repository.markSold(listingId)

    fun delete(onDone: () -> Unit) {
        repository.delete(listingId)
        onDone()
    }

    fun contactSeller(listing: Listing): String =
        messagingRepository.startConversation(listing.sellerName, listOf(listing.sellerId), false)
}

package com.campusconnect.app.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.ServiceConfig
import com.campusconnect.app.core.data.repo.CommunityRepository
import com.campusconnect.app.core.data.repo.EventRepository
import com.campusconnect.app.core.data.repo.MarketplaceRepository
import com.campusconnect.app.core.data.repo.NotificationRepository
import com.campusconnect.app.core.data.repo.OpportunityRepository
import com.campusconnect.app.core.data.repo.SessionRepository
import com.campusconnect.app.core.model.CampusEvent
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.NotificationItem
import com.campusconnect.app.core.model.Opportunity
import com.campusconnect.app.core.model.Post
import com.campusconnect.app.core.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val profile: UserProfile? = null,
    val posts: List<Post> = emptyList(),
    val events: List<CampusEvent> = emptyList(),
    val opportunities: List<Opportunity> = emptyList(),
    val listings: List<Listing> = emptyList(),
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    sessionRepository: SessionRepository,
    communityRepository: CommunityRepository,
    eventRepository: EventRepository,
    opportunityRepository: OpportunityRepository,
    marketplaceRepository: MarketplaceRepository,
    notificationRepository: NotificationRepository,
    config: ServiceConfig,
) : ViewModel() {

    val demoMode: Boolean = !config.supabaseConfigured

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            sessionRepository.session.collect { session ->
                _state.update { it.copy(profile = session.profile) }
            }
        }
        viewModelScope.launch {
            communityRepository.posts.collect { posts ->
                _state.update { it.copy(posts = posts.take(4)) }
            }
        }
        viewModelScope.launch {
            eventRepository.events.collect { events ->
                _state.update { it.copy(events = events.take(3)) }
            }
        }
        viewModelScope.launch {
            opportunityRepository.opportunities.collect { opportunities ->
                _state.update { it.copy(opportunities = opportunities.take(3)) }
            }
        }
        viewModelScope.launch {
            marketplaceRepository.listings.collect { listings ->
                _state.update { it.copy(listings = listings.filter { it.status == com.campusconnect.app.core.model.ListingStatus.ACTIVE }.take(4)) }
            }
        }
        viewModelScope.launch {
            notificationRepository.notifications.collect { items ->
                _state.update { it.copy(notifications = items.take(3)) }
            }
        }
        viewModelScope.launch {
            notificationRepository.unreadCount.collect { count ->
                _state.update { it.copy(unreadCount = count) }
            }
        }
    }
}

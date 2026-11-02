package com.campusconnect.app.core.data.repo

import com.campusconnect.app.core.data.DemoStore
import com.campusconnect.app.core.data.ServiceConfig
import com.campusconnect.app.core.model.Accommodation
import com.campusconnect.app.core.model.ApplicationRecord
import com.campusconnect.app.core.model.CampusEvent
import com.campusconnect.app.core.model.CampusPointOfInterest
import com.campusconnect.app.core.model.Conversation
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.ListingCategory
import com.campusconnect.app.core.model.Message
import com.campusconnect.app.core.model.Opportunity
import com.campusconnect.app.core.model.OpportunityType
import com.campusconnect.app.core.model.UserProfile
import com.campusconnect.app.core.model.UserRole
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class MarketplaceRepository @Inject constructor(
    private val store: DemoStore,
    private val sessionRepository: SessionRepository,
) {
    val listings: StateFlow<List<Listing>> = store.listings
    val categories: List<ListingCategory> = store.categories

    suspend fun createListing(
        title: String,
        description: String,
        priceKes: Int,
        categoryId: String,
        categoryName: String,
        imageUrl: String?,
    ): Result<Listing> {
        if (title.trim().length < 4) return Result.failure(IllegalArgumentException("Title must be at least 4 characters"))
        if (description.trim().length < 10) return Result.failure(IllegalArgumentException("Add a short description"))
        if (priceKes <= 0) return Result.failure(IllegalArgumentException("Enter a valid price"))
        val profile = sessionRepository.currentProfile()
            ?: return Result.failure(IllegalStateException("Sign in to create a listing."))
        val listing = store.newListingFrom(profile, title.trim(), description.trim(), priceKes, categoryId, categoryName, imageUrl)
        store.addListing(listing)
        return Result.success(listing)
    }

    fun toggleSaved(id: String) = store.toggleListingSaved(id)
    fun markSold(id: String) = store.markListingSold(id)
    fun delete(id: String) = store.deleteListing(id)
    fun report(id: String, reason: String, details: String) = store.reportListing(id, reason, details)
    suspend fun currentProfile(): UserProfile? = sessionRepository.currentProfile()
}

@Singleton
class OpportunityRepository @Inject constructor(
    private val store: DemoStore,
    private val sessionRepository: SessionRepository,
) {
    val opportunities: StateFlow<List<Opportunity>> = store.opportunities
    val applications: StateFlow<List<ApplicationRecord>> = store.applications

    fun toggleSaved(id: String) = store.toggleOpportunitySaved(id)

    suspend fun apply(opportunity: Opportunity): Result<ApplicationRecord> {
        if (opportunity.applied) return Result.failure(IllegalStateException("You already applied to this opportunity."))
        val profile = sessionRepository.currentProfile()
            ?: return Result.failure(IllegalStateException("Sign in to apply."))
        if (profile.role == UserRole.STUDENT) {
            // Students can always apply; publishing is the role-gated action.
        }
        return Result.success(store.applyToOpportunity(opportunity))
    }

    suspend fun canPublish(): Boolean {
        val profile = sessionRepository.currentProfile() ?: return false
        return profile.role == UserRole.ADMIN || profile.role == UserRole.MODERATOR
    }

    suspend fun publish(
        title: String,
        organization: String,
        type: OpportunityType,
        description: String,
        location: String,
        deadline: Long,
        stipend: String,
        remote: Boolean,
    ): Result<Opportunity> {
        if (!canPublish()) return Result.failure(SecurityException("Only verified staff can publish opportunities."))
        if (title.trim().length < 4) return Result.failure(IllegalArgumentException("Enter a title"))
        if (description.trim().length < 20) return Result.failure(IllegalArgumentException("Add a fuller description"))
        val opportunity = store.newOpportunityFrom(
            title.trim(), organization.trim(), type, description.trim(), location.trim(), deadline, stipend, remote,
        )
        store.addOpportunity(opportunity)
        return Result.success(opportunity)
    }
}

@Singleton
class AccommodationRepository @Inject constructor(
    private val store: DemoStore,
) {
    val listings: StateFlow<List<Accommodation>> = store.accommodation
    fun toggleSaved(id: String) = store.toggleAccommodationSaved(id)
    fun report(id: String, reason: String, details: String) = store.reportAccommodation(id, reason, details)
}

@Singleton
class EventRepository @Inject constructor(
    private val store: DemoStore,
) {
    val events: StateFlow<List<CampusEvent>> = store.events
    fun toggleRsvp(id: String) = store.toggleRsvp(id)
    fun toggleReminder(id: String) = store.toggleReminder(id)
}

@Singleton
class MessagingRepository @Inject constructor(
    private val store: DemoStore,
    private val sessionRepository: SessionRepository,
) {
    val conversations: StateFlow<List<Conversation>> = store.conversations
    val messages: StateFlow<Map<String, List<Message>>> = store.messages
    val typing: StateFlow<Set<String>> = store.typing

    fun messagesFor(conversationId: String): Flow<List<Message>> =
        kotlinx.coroutines.flow.flow {
            store.messages.collect { emit(it[conversationId] ?: emptyList()) }
        }

    fun conversation(id: String): Conversation? = store.conversations.value.firstOrNull { it.id == id }

    fun markRead(id: String) = store.markConversationRead(id)

    suspend fun send(conversationId: String, content: String, imageUrl: String? = null): Result<Unit> {
        if (content.isBlank() && imageUrl == null) {
            return Result.failure(IllegalArgumentException("Write a message or attach a photo"))
        }
        val profile = sessionRepository.currentProfile()
            ?: return Result.failure(IllegalStateException("Sign in to send messages."))
        store.sendMessage(conversationId, content.trim(), profile.fullName, imageUrl)
        return Result.success(Unit)
    }

    fun startConversation(title: String, participantIds: List<String>, isGroup: Boolean): String =
        store.createConversation(title, participantIds, isGroup)
}

@Singleton
class MapRepository @Inject constructor(
    private val config: ServiceConfig,
) {
    val pointsOfInterest: List<CampusPointOfInterest> = com.campusconnect.app.core.data.DemoData.pointsOfInterest()
    val mapsConfigured: Boolean = config.mapsConfigured
    val approximateCenter = -1.2921 to 36.8219
}

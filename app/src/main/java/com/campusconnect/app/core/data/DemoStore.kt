package com.campusconnect.app.core.data

import com.campusconnect.app.core.di.ApplicationScope
import com.campusconnect.app.core.model.Accommodation
import com.campusconnect.app.core.model.ApplicationRecord
import com.campusconnect.app.core.model.CampusEvent
import com.campusconnect.app.core.model.CommunityGroup
import com.campusconnect.app.core.model.Conversation
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.ListingStatus
import com.campusconnect.app.core.model.Message
import com.campusconnect.app.core.model.Opportunity
import com.campusconnect.app.core.model.Report
import com.campusconnect.app.core.model.StudentSummary
import com.campusconnect.app.core.model.UserProfile
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Local-first demo store. All mutations are real within the running app but are
 * not synced to a backend. It is replaced by Supabase-backed repositories when
 * credentials are configured (see docs/ARCHITECTURE.md).
 */
@Singleton
class DemoStore @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
) {
    val categories = DemoData.categories()

    private val _listings = MutableStateFlow(DemoData.listings())
    val listings: StateFlow<List<Listing>> = _listings.asStateFlow()

    private val _opportunities = MutableStateFlow(DemoData.opportunities())
    val opportunities: StateFlow<List<Opportunity>> = _opportunities.asStateFlow()

    private val _applications = MutableStateFlow<List<ApplicationRecord>>(emptyList())
    val applications: StateFlow<List<ApplicationRecord>> = _applications.asStateFlow()

    private val _accommodation = MutableStateFlow(DemoData.accommodation())
    val accommodation: StateFlow<List<Accommodation>> = _accommodation.asStateFlow()

    private val _events = MutableStateFlow(DemoData.events())
    val events: StateFlow<List<CampusEvent>> = _events.asStateFlow()

    private val _groups = MutableStateFlow(DemoData.groups())
    val groups: StateFlow<List<CommunityGroup>> = _groups.asStateFlow()

    private val _students = MutableStateFlow(DemoData.students())
    val students: StateFlow<List<StudentSummary>> = _students.asStateFlow()

    private val _conversations = MutableStateFlow(DemoData.conversations())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow(DemoData.messages())
    val messages: StateFlow<Map<String, List<Message>>> = _messages.asStateFlow()

    private val _typing = MutableStateFlow<Set<String>>(emptySet())
    val typing: StateFlow<Set<String>> = _typing.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    val pointsOfInterest = DemoData.pointsOfInterest()

    // ---- Marketplace ----

    fun addListing(listing: Listing) {
        _listings.update { listOf(listing.copy(status = ListingStatus.PENDING_REVIEW)) + it }
        appScope.launch {
            delay(4_000)
            approveListing(listing.id)
        }
    }

    fun approveListing(id: String) {
        _listings.update { list -> list.map { if (it.id == id) it.copy(status = ListingStatus.ACTIVE) else it } }
    }

    fun toggleListingSaved(id: String) {
        _listings.update { list -> list.map { if (it.id == id) it.copy(saved = !it.saved) else it } }
    }

    fun markListingSold(id: String) {
        _listings.update { list -> list.map { if (it.id == id) it.copy(status = ListingStatus.SOLD) else it } }
    }

    fun deleteListing(id: String) {
        _listings.update { list -> list.filterNot { it.id == id } }
    }

    fun reportListing(id: String, reason: String, details: String) = addReport("listing", id, reason, details)

    // ---- Opportunities ----

    fun toggleOpportunitySaved(id: String) {
        _opportunities.update { list -> list.map { if (it.id == id) it.copy(saved = !it.saved) else it } }
    }

    fun applyToOpportunity(opportunity: Opportunity): ApplicationRecord {
        val record = ApplicationRecord(
            id = "app-" + UUID.randomUUID(),
            opportunityId = opportunity.id,
            opportunityTitle = opportunity.title,
            organization = opportunity.organization,
            appliedAt = System.currentTimeMillis(),
        )
        _applications.update { listOf(record) + it }
        _opportunities.update { list ->
            list.map { if (it.id == opportunity.id) it.copy(applied = true, applicationStatus = "Submitted") else it }
        }
        return record
    }

    fun addOpportunity(opportunity: Opportunity) {
        _opportunities.update { listOf(opportunity) + it }
    }

    // ---- Accommodation ----

    fun toggleAccommodationSaved(id: String) {
        _accommodation.update { list -> list.map { if (it.id == id) it.copy(saved = !it.saved) else it } }
    }

    fun reportAccommodation(id: String, reason: String, details: String) =
        addReport("accommodation", id, reason, details)

    // ---- Events ----

    fun toggleRsvp(id: String) {
        _events.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(
                        rsvped = !it.rsvped,
                        rsvpCount = (it.rsvpCount + if (it.rsvped) -1 else 1).coerceAtLeast(0),
                    )
                } else it
            }
        }
    }

    fun toggleReminder(id: String) {
        _events.update { list -> list.map { if (it.id == id) it.copy(reminder = !it.reminder) else it } }
    }

    // ---- Community ----

    fun toggleGroupJoined(id: String) {
        _groups.update { list ->
            list.map {
                if (it.id == id) {
                    it.copy(joined = !it.joined, memberCount = (it.memberCount + if (it.joined) -1 else 1).coerceAtLeast(0))
                } else it
            }
        }
    }

    fun toggleFollow(id: String) {
        _students.update { list -> list.map { if (it.id == id) it.copy(followed = !it.followed) else it } }
    }

    fun reportPost(id: String, reason: String, details: String) = addReport("post", id, reason, details)

    // ---- Messaging ----

    fun markConversationRead(id: String) {
        _conversations.update { list -> list.map { if (it.id == id) it.copy(unread = 0) else it } }
    }

    fun createConversation(
        title: String,
        participantIds: List<String> = emptyList(),
        isGroup: Boolean = false,
        existingId: String? = null,
    ): String {
        if (existingId != null) return existingId
        val existing = _conversations.value.firstOrNull {
            !isGroup && it.title.equals(title, ignoreCase = true)
        }
        if (existing != null) return existing.id
        val id = "conv-" + UUID.randomUUID()
        val conversation = Conversation(
            id = id,
            title = title,
            participantIds = participantIds,
            lastMessage = "Conversation started",
            lastMessageAt = System.currentTimeMillis(),
            isGroup = isGroup,
        )
        _conversations.update { listOf(conversation) + it }
        _messages.update { it + (id to emptyList()) }
        return id
    }

    fun sendMessage(
        conversationId: String,
        content: String,
        senderName: String,
        imageUrl: String? = null,
    ) {
        val message = Message(
            id = "msg-" + UUID.randomUUID(),
            conversationId = conversationId,
            senderId = "u-me",
            senderName = senderName,
            content = content,
            imageUrl = imageUrl,
            sentAt = System.currentTimeMillis(),
            isMine = true,
        )
        _messages.update { map ->
            map + (conversationId to ((map[conversationId] ?: emptyList()) + message))
        }
        updateConversationPreview(conversationId, if (content.isBlank()) "Photo" else content, incrementUnread = false)

        val conversation = _conversations.value.firstOrNull { it.id == conversationId }
        if (conversation != null && !conversation.isGroup) {
            _typing.update { it + conversationId }
            appScope.launch {
                delay(1_600)
                val reply = Message(
                    id = "msg-" + UUID.randomUUID(),
                    conversationId = conversationId,
                    senderId = conversation.participantIds.firstOrNull() ?: "u-amina",
                    senderName = conversation.title,
                    content = autoReply(content),
                    sentAt = System.currentTimeMillis(),
                )
                _messages.update { map ->
                    map + (conversationId to ((map[conversationId] ?: emptyList()) + reply))
                }
                _typing.update { it - conversationId }
                updateConversationPreview(conversationId, reply.content, incrementUnread = true)
            }
        }
    }

    private fun updateConversationPreview(conversationId: String, preview: String, incrementUnread: Boolean) {
        _conversations.update { list ->
            list.map {
                if (it.id == conversationId) {
                    it.copy(
                        lastMessage = preview,
                        lastMessageAt = System.currentTimeMillis(),
                        unread = if (incrementUnread) it.unread + 1 else it.unread,
                    )
                } else it
            }
        }
    }

    private fun autoReply(userMessage: String): String = when {
        userMessage.contains("library", ignoreCase = true) -> "Room 4 is free after 8am. See you there!"
        userMessage.contains("deadline", ignoreCase = true) -> "The sample deadline is still open. I will confirm with the office."
        userMessage.contains("price", ignoreCase = true) || userMessage.contains("cost", ignoreCase = true) ->
            "The price on the listing is what the seller shared. Confirm before paying anything."
        userMessage.contains("hello", ignoreCase = true) || userMessage.contains("hi", ignoreCase = true) ->
            "Hey! How can I help on campus today?"
        else -> "Sounds good. This is an automated demo reply - real conversations use Stream Chat."
    }

    // ---- Reports ----

    fun addReport(contentType: String, contentId: String, reason: String, details: String) {
        _reports.update {
            listOf(
                Report(
                    id = "rep-" + UUID.randomUUID(),
                    contentType = contentType,
                    contentId = contentId,
                    reason = reason,
                    details = details,
                ),
            ) + it
        }
    }

    fun newListingFrom(
        profile: UserProfile,
        title: String,
        description: String,
        priceKes: Int,
        categoryId: String,
        categoryName: String,
        imageUrl: String?,
    ): Listing = Listing(
        id = "lst-" + UUID.randomUUID(),
        title = title,
        description = description,
        priceKes = priceKes,
        categoryId = categoryId,
        categoryName = categoryName,
        sellerId = profile.id,
        sellerName = profile.fullName,
        imageUrl = imageUrl,
        location = "Campus",
        createdAt = System.currentTimeMillis(),
        isOwn = true,
    )

    fun newOpportunityFrom(
        title: String,
        organization: String,
        type: com.campusconnect.app.core.model.OpportunityType,
        description: String,
        location: String,
        deadline: Long,
        stipend: String,
        remote: Boolean,
    ): Opportunity = Opportunity(
        id = "opp-" + UUID.randomUUID(),
        title = title,
        organization = organization,
        type = type,
        description = description,
        location = location,
        deadline = deadline,
        stipend = stipend,
        postedAt = System.currentTimeMillis(),
        remote = remote,
        isOwn = true,
    )
}

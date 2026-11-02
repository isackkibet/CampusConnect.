package com.campusconnect.app.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole { STUDENT, MODERATOR, ADMIN }

@Serializable
data class Institution(
    val id: String,
    val name: String,
    val shortName: String,
    val city: String,
)

@Serializable
data class Department(
    val id: String,
    val institutionId: String,
    val name: String,
)

@Serializable
data class Course(
    val id: String,
    val departmentId: String,
    val name: String,
)

@Serializable
data class UserProfile(
    val id: String = "u-me",
    val fullName: String = "",
    val email: String = "",
    val role: UserRole = UserRole.STUDENT,
    val institutionId: String = "nnp",
    val institutionName: String = "Nairobi National Polytechnic",
    val department: String = "",
    val course: String = "",
    val yearOfStudy: Int = 1,
    val bio: String = "",
    val avatarUrl: String? = null,
    val phone: String = "",
    val verified: Boolean = false,
    val followers: Int = 0,
    val following: Int = 0,
) {
    val initials: String
        get() = fullName.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifBlank { "CC" }
}

@Serializable
data class Post(
    val id: String,
    val authorId: String,
    val authorName: String,
    val authorDepartment: String,
    val institutionId: String = "nnp",
    val content: String,
    val imageUrl: String? = null,
    val createdAt: Long,
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedByMe: Boolean = false,
    val communityName: String? = null,
    val isOwn: Boolean = false,
)

@Serializable
data class Comment(
    val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val createdAt: Long,
)

@Serializable
enum class CommunityType { DEPARTMENT, CLASS, CLUB, GENERAL }

@Serializable
data class CommunityGroup(
    val id: String,
    val name: String,
    val type: CommunityType,
    val description: String,
    val memberCount: Int,
    val institutionId: String = "nnp",
    val joined: Boolean = false,
)

@Serializable
data class StudentSummary(
    val id: String,
    val name: String,
    val department: String,
    val course: String,
    val year: Int,
    val bio: String,
    val followed: Boolean = false,
)

@Serializable
data class Conversation(
    val id: String,
    val title: String,
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageAt: Long = 0L,
    val unread: Int = 0,
    val isGroup: Boolean = false,
    val online: Boolean = false,
) {
    val initials: String
        get() = title.trim().split(" ").filter { it.isNotBlank() }
            .take(2).joinToString("") { it.first().uppercase() }
            .ifBlank { "CC" }
}

@Serializable
data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val sentAt: Long,
    val isMine: Boolean = false,
    val imageUrl: String? = null,
    val read: Boolean = true,
)

@Serializable
data class ListingCategory(val id: String, val name: String)

@Serializable
enum class ListingStatus { ACTIVE, SOLD, PENDING_REVIEW, REJECTED }

@Serializable
data class Listing(
    val id: String,
    val title: String,
    val description: String,
    val priceKes: Int,
    val categoryId: String,
    val categoryName: String,
    val sellerId: String,
    val sellerName: String,
    val sellerRating: Double = 4.5,
    val imageUrl: String? = null,
    val location: String = "Campus",
    val createdAt: Long,
    val status: ListingStatus = ListingStatus.ACTIVE,
    val saved: Boolean = false,
    val isOwn: Boolean = false,
)

@Serializable
enum class OpportunityType { JOB, INTERNSHIP, ATTACHMENT, GIG }

@Serializable
data class Opportunity(
    val id: String,
    val title: String,
    val organization: String,
    val type: OpportunityType,
    val description: String,
    val location: String,
    val deadline: Long,
    val stipend: String,
    val postedAt: Long,
    val remote: Boolean = false,
    val eligibility: String = "Open to students",
    val saved: Boolean = false,
    val applied: Boolean = false,
    val applicationStatus: String? = null,
    val isOwn: Boolean = false,
)

@Serializable
data class ApplicationRecord(
    val id: String,
    val opportunityId: String,
    val opportunityTitle: String,
    val organization: String,
    val appliedAt: Long,
    val status: String = "Submitted",
)

@Serializable
data class Accommodation(
    val id: String,
    val title: String,
    val description: String,
    val rentKes: Int,
    val location: String,
    val distanceFromCampus: String,
    val bedrooms: Int,
    val imageUrl: String? = null,
    val landlordName: String,
    val landlordPhone: String,
    val available: Boolean = true,
    val verified: Boolean = false,
    val saved: Boolean = false,
    val isOwn: Boolean = false,
)

@Serializable
data class CampusEvent(
    val id: String,
    val title: String,
    val description: String,
    val startAt: Long,
    val venue: String,
    val organizer: String,
    val imageUrl: String? = null,
    val rsvped: Boolean = false,
    val reminder: Boolean = false,
    val rsvpCount: Int = 0,
)

@Serializable
data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val type: NotificationType,
    val createdAt: Long,
    val read: Boolean = false,
    val targetId: String? = null,
)

@Serializable
enum class NotificationType { ANNOUNCEMENT, MESSAGE, EVENT, OPPORTUNITY, MARKETPLACE, SYSTEM }

@Serializable
data class NotificationPreferences(
    val announcements: Boolean = true,
    val messages: Boolean = true,
    val events: Boolean = true,
    val opportunities: Boolean = true,
    val marketing: Boolean = false,
)

@Serializable
data class CampusPointOfInterest(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
)

@Serializable
data class AiMessage(
    val id: String,
    val content: String,
    val fromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
)

@Serializable
data class Report(
    val id: String,
    val contentType: String,
    val contentId: String,
    val reason: String,
    val details: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "OPEN",
)

@Serializable
data class PaymentTransaction(
    val id: String,
    val reference: String,
    val amountKes: Int,
    val purpose: String,
    val status: String,
    val createdAt: Long,
)

data class ReportOption(val value: String, val label: String)

val ReportOptions = listOf(
    ReportOption("spam", "Spam or scam"),
    ReportOption("harassment", "Harassment or bullying"),
    ReportOption("hate", "Hate speech"),
    ReportOption("inappropriate", "Inappropriate content"),
    ReportOption("fake", "Fake listing or profile"),
    ReportOption("other", "Other"),
)

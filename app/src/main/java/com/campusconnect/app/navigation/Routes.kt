package com.campusconnect.app.navigation

object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot-password"
    const val VERIFY = "verify"

    const val SETUP = "setup"
    const val EDIT_PROFILE = "edit-profile"

    const val HOME = "home"
    const val COMMUNITY = "community"
    const val MARKETPLACE = "marketplace"
    const val OPPORTUNITIES = "opportunities"
    const val PROFILE = "profile"

    const val CREATE_POST = "community/create"
    const val POST_DETAIL = "post/{postId}"
    const val MESSAGES = "messages"
    const val CHAT = "chat/{conversationId}"
    const val NOTIFICATIONS = "notifications"
    const val NOTIFICATION_PREFS = "notification-preferences"
    const val SAVED = "saved"
    const val APPLICATIONS = "applications"

    const val LISTING_DETAIL = "listing/{listingId}"
    const val CREATE_LISTING = "listing/create"
    const val OPPORTUNITY_DETAIL = "opportunity/{opportunityId}"
    const val CREATE_OPPORTUNITY = "opportunity/create"
    const val ACCOMMODATION = "accommodation"
    const val ACCOMMODATION_DETAIL = "accommodation/{accommodationId}"
    const val EVENTS = "events"
    const val CAMPUS_MAP = "campus-map"
    const val ASSISTANT = "assistant"

    fun post(postId: String) = "post/$postId"
    fun chat(conversationId: String) = "chat/$conversationId"
    fun listing(listingId: String) = "listing/$listingId"
    fun opportunity(opportunityId: String) = "opportunity/$opportunityId"
    fun accommodation(accommodationId: String) = "accommodation/$accommodationId"

    val mainTabs = setOf(HOME, COMMUNITY, MARKETPLACE, OPPORTUNITIES, PROFILE)
    val authRoutes = setOf(SPLASH, ONBOARDING, LOGIN, REGISTER, FORGOT_PASSWORD, VERIFY, SETUP)
}

data class MainTab(
    val route: String,
    val label: String,
)

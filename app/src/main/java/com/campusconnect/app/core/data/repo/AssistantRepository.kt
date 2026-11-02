package com.campusconnect.app.core.data.repo

import com.campusconnect.app.core.data.BackendApi
import com.campusconnect.app.core.model.AiMessage
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

enum class AssistantSource { REMOTE, OFFLINE }

data class AssistantResult(
    val text: String,
    val source: AssistantSource,
)

@Singleton
class AssistantRepository @Inject constructor(
    private val backendApi: BackendApi,
) {
    val remoteConfigured: Boolean = backendApi.assistantAvailable

    suspend fun ask(prompt: String, history: List<AiMessage>): AssistantResult {
        if (remoteConfigured) {
            return runCatching {
                val wireHistory = history.takeLast(12).map {
                    (if (it.fromUser) "user" else "assistant") to it.content
                }
                AssistantResult(
                    text = backendApi.askAssistant(wireHistory, prompt, "nnp"),
                    source = AssistantSource.REMOTE,
                )
            }.getOrElse { error ->
                AssistantResult(
                    text = "The secure AI backend could not be reached (${error.message ?: "unknown error"}). " +
                        "Here is an offline guide instead.\n\n" + offlineAnswer(prompt),
                    source = AssistantSource.OFFLINE,
                )
            }
        }
        return AssistantResult(offlineAnswer(prompt), AssistantSource.OFFLINE)
    }

    fun welcomeMessage(): AiMessage = AiMessage(
        id = UUID.randomUUID().toString(),
        content = "Hi, I am the CampusConnect guide. I can help you navigate the app, find " +
            "opportunities, marketplace listings and events. In offline mode I only use " +
            "information shown inside the app, not official campus records.",
        fromUser = false,
    )

    /**
     * Deterministic offline help. It never claims official campus information.
     */
    fun offlineAnswer(prompt: String): String {
        val text = prompt.lowercase()
        val prefix = "Offline guide (not official campus information):\n\n"
        return prefix + when {
            text.contains("attachment") || text.contains("internship") || text.contains("job") ->
                "Open Opportunities from the bottom bar and use the type filter. You can save an " +
                    "opportunity or open it to apply. Application tracking is available under " +
                    "Profile > My applications."
            text.contains("marketplace") || text.contains("sell") || text.contains("buy") ->
                "Open Marketplace, search by category, and tap a listing for seller details. " +
                    "To sell something, use the + button. New listings are marked Pending review " +
                    "in this demo before becoming active. Never send money before verifying an item."
            text.contains("accommodation") || text.contains("hostel") || text.contains("rent") ->
                "Accommodation is available from Home quick actions and the Profile services list. " +
                    "You can filter by rent and availability, then contact the listed landlord. " +
                    "Sample listings are clearly labelled; always inspect a room before paying."
            text.contains("event") || text.contains("club") ->
                "Open Events from Home to see upcoming sample events. Tap RSVP to record attendance " +
                    "and the reminder bell to mark an event for reminders."
            text.contains("map") || text.contains("library") || text.contains("lecture") ->
                "Open the Campus Map. Without a Google Maps API key the app shows an accessible " +
                    "point-of-interest list. Map locations in this build are approximate sample data."
            text.contains("message") || text.contains("chat") ->
                "Open Messages and choose a conversation. This demo simulates replies locally. " +
                    "Real-time messaging and calls require Stream credentials and a token backend."
            text.contains("password") || text.contains("login") || text.contains("account") ->
                "You can reset your password from the login screen. In demo mode no email is sent " +
                    "and verification uses code 123456. Connect Supabase Auth for real emails."
            text.contains("mpesa") || text.contains("payment") || text.contains("pay") ->
                "M-PESA payments are routed through a secure backend and are not enabled until " +
                    "Daraja credentials are configured server-side. The app never asks for payment " +
                    "secrets and never marks a payment successful on its own."
            text.contains("post") || text.contains("community") || text.contains("follow") ->
                "Use Community to create a post, like, comment, share or report. You can edit and " +
                    "delete your own posts from the post menu. Switch to Groups or Students tabs " +
                    "to join communities and follow classmates."
            else ->
                "I can help with community posts, messaging, marketplace, opportunities, " +
                    "accommodation, events, maps and account questions. Ask about one of those, " +
                    "or connect the secure backend for richer AI answers."
        }
    }
}

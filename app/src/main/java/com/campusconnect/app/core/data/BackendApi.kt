package com.campusconnect.app.core.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.Serializable

@Serializable
data class AssistantRequest(
    val messages: List<AssistantMessageDto>,
    val institutionId: String? = null,
)

@Serializable
data class AssistantMessageDto(val role: String, val content: String)

@Serializable
data class AssistantResponse(
    val reply: String? = null,
    val error: String? = null,
)

@Serializable
data class PaymentRequest(
    val amountKes: Int,
    val phone: String,
    val purpose: String,
    val reference: String,
)

@Serializable
data class PaymentResponse(
    val checkoutRequestId: String? = null,
    val message: String? = null,
    val error: String? = null,
)

@Singleton
class BackendApi @Inject constructor(
    private val client: HttpClient,
    private val config: ServiceConfig,
) {
    val assistantAvailable: Boolean = config.backendConfigured

    suspend fun askAssistant(
        history: List<Pair<String, String>>,
        prompt: String,
        institutionId: String?,
    ): String {
        check(config.backendConfigured) {
            "Secure backend is not configured. Add BACKEND_BASE_URL to local.properties."
        }
        val messages = history.map { AssistantMessageDto(role = it.first, content = it.second) } +
            AssistantMessageDto(role = "user", content = prompt)
        val response = client.post(config.assistantEndpoint()) {
            contentType(ContentType.Application.Json)
            setBody(AssistantRequest(messages = messages, institutionId = institutionId))
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Assistant backend returned ${response.status.value}")
        }
        val body = response.body<AssistantResponse>()
        return body.reply?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException(body.error ?: "Assistant returned an empty response")
    }

    suspend fun initiatePayment(
        amountKes: Int,
        phone: String,
        purpose: String,
        reference: String,
    ): PaymentResponse {
        check(config.backendConfigured) {
            "Secure payment backend is not configured. Add BACKEND_BASE_URL to local.properties."
        }
        val response = client.post(config.paymentsEndpoint()) {
            contentType(ContentType.Application.Json)
            setBody(PaymentRequest(amountKes, phone, purpose, reference))
        }
        if (!response.status.isSuccess()) {
            throw IllegalStateException("Payment backend returned ${response.status.value}")
        }
        return response.body()
    }
}

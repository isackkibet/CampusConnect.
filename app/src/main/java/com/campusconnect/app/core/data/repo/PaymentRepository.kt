package com.campusconnect.app.core.data.repo

import com.campusconnect.app.core.data.BackendApi
import com.campusconnect.app.core.data.ServiceConfig
import com.campusconnect.app.core.model.PaymentTransaction
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Payment orchestration only. The Android client never holds M-PESA secrets and
 * never marks a transaction as paid. Status must come from the backend callback.
 */
@Singleton
class PaymentRepository @Inject constructor(
    private val backendApi: BackendApi,
    private val config: ServiceConfig,
) {
    val configured: Boolean = config.backendConfigured

    suspend fun initiateStkPush(
        amountKes: Int,
        phone: String,
        purpose: String,
    ): Result<PaymentTransaction> {
        if (!config.backendConfigured) {
            return Result.failure(
                IllegalStateException(
                    "Secure payment backend is not configured. Add BACKEND_BASE_URL and deploy the " +
                        "mpesa-stk-push function. Payments are disabled in demo mode.",
                ),
            )
        }
        val digits = phone.filter { it.isDigit() }
        if (digits.length !in 9..13) return Result.failure(IllegalArgumentException("Enter a valid M-PESA phone number"))
        if (amountKes <= 0) return Result.failure(IllegalArgumentException("Enter a valid amount"))
        val reference = "CC-" + UUID.randomUUID().toString().take(8).uppercase()
        return runCatching {
            val response = backendApi.initiatePayment(amountKes, phone, purpose, reference)
            if (response.checkoutRequestId == null) {
                throw IllegalStateException(response.error ?: "Payment request was not accepted")
            }
            PaymentTransaction(
                id = reference,
                reference = response.checkoutRequestId,
                amountKes = amountKes,
                purpose = purpose,
                status = "PENDING",
                createdAt = System.currentTimeMillis(),
            )
        }
    }
}

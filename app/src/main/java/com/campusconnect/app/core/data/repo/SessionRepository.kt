package com.campusconnect.app.core.data.repo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.campusconnect.app.core.data.DemoData
import com.campusconnect.app.core.model.UserProfile
import com.campusconnect.app.core.model.UserRole
import com.campusconnect.app.core.util.Validators
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

data class SessionState(
    val onboardingComplete: Boolean = false,
    val signedIn: Boolean = false,
    val emailVerified: Boolean = false,
    val pendingVerificationEmail: String = "",
    val profile: UserProfile? = null,
) {
    val profileComplete: Boolean
        get() = profile != null && profile.department.isNotBlank() && profile.course.isNotBlank()
}

@Singleton
class SessionRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    private object Keys {
        val onboarding = booleanPreferencesKey("onboarding_complete")
        val signedIn = booleanPreferencesKey("signed_in")
        val verified = booleanPreferencesKey("email_verified")
        val pendingEmail = stringPreferencesKey("pending_email")
        val pendingName = stringPreferencesKey("pending_name")
        val profile = stringPreferencesKey("profile_json")
        val notifyAnnouncements = booleanPreferencesKey("notify_announcements")
        val notifyMessages = booleanPreferencesKey("notify_messages")
        val notifyEvents = booleanPreferencesKey("notify_events")
        val notifyOpportunities = booleanPreferencesKey("notify_opportunities")
        val notifyMarketing = booleanPreferencesKey("notify_marketing")
    }

    val session: Flow<SessionState> = dataStore.data.map { prefs ->
        SessionState(
            onboardingComplete = prefs[Keys.onboarding] ?: false,
            signedIn = prefs[Keys.signedIn] ?: false,
            emailVerified = prefs[Keys.verified] ?: false,
            pendingVerificationEmail = prefs[Keys.pendingEmail] ?: "",
            profile = prefs[Keys.profile]?.let { runCatching { json.decodeFromString<UserProfile>(it) }.getOrNull() },
        )
    }

    val notificationPreferences: Flow<com.campusconnect.app.core.model.NotificationPreferences> =
        dataStore.data.map { prefs ->
            com.campusconnect.app.core.model.NotificationPreferences(
                announcements = prefs[Keys.notifyAnnouncements] ?: true,
                messages = prefs[Keys.notifyMessages] ?: true,
                events = prefs[Keys.notifyEvents] ?: true,
                opportunities = prefs[Keys.notifyOpportunities] ?: true,
                marketing = prefs[Keys.notifyMarketing] ?: false,
            )
        }

    suspend fun currentProfile(): UserProfile? = session.first().profile

    suspend fun currentSession(): SessionState = session.first()

    suspend fun completeOnboarding() {
        dataStore.edit { it[Keys.onboarding] = true }
    }

    suspend fun register(fullName: String, email: String, password: String): Result<Unit> {
        Validators.nameError(fullName)?.let { return Result.failure(IllegalArgumentException(it)) }
        Validators.emailError(email)?.let { return Result.failure(IllegalArgumentException(it)) }
        Validators.passwordError(password)?.let { return Result.failure(IllegalArgumentException(it)) }
        val cleanEmail = email.trim().lowercase()
        val cleanName = fullName.trim()
        dataStore.edit { prefs ->
            prefs[Keys.pendingEmail] = cleanEmail
            prefs[Keys.pendingName] = cleanName
            prefs[Keys.verified] = false
            prefs[Keys.signedIn] = false
            val existing = prefs[Keys.profile]?.let { runCatching { json.decodeFromString<UserProfile>(it) }.getOrNull() }
            if (existing == null || existing.email.lowercase() != cleanEmail) {
                prefs[Keys.profile] = json.encodeToString(
                    UserProfile.serializer(),
                    demoProfile(name = cleanName, email = cleanEmail),
                )
            }
        }
        return Result.success(Unit)
    }

    suspend fun verifyEmail(code: String): Result<Unit> {
        if (code.trim() != DEMO_VERIFICATION_CODE) {
            return Result.failure(IllegalArgumentException("That code is not valid. In demo mode use $DEMO_VERIFICATION_CODE."))
        }
        dataStore.edit { prefs ->
            prefs[Keys.verified] = true
            prefs[Keys.signedIn] = true
            prefs[Keys.pendingEmail] = ""
        }
        return Result.success(Unit)
    }

    suspend fun resendVerification(): Result<Unit> {
        val pending = session.first().pendingVerificationEmail
        if (pending.isBlank()) return Result.failure(IllegalStateException("No pending verification found."))
        return Result.success(Unit)
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        Validators.emailError(email)?.let { return Result.failure(IllegalArgumentException(it)) }
        if (password.length < 8) return Result.failure(IllegalArgumentException("Password must be at least 8 characters"))
        val state = session.first()
        if (state.pendingVerificationEmail.isNotBlank() && !state.emailVerified) {
            return Result.failure(IllegalStateException("Verify your email first. In demo mode use $DEMO_VERIFICATION_CODE."))
        }
        val cleanEmail = email.trim().lowercase()
        val existing = state.profile
        dataStore.edit { prefs ->
            prefs[Keys.signedIn] = true
            prefs[Keys.verified] = true
            if (existing == null || (existing.email.isNotBlank() && existing.email.lowercase() != cleanEmail)) {
                prefs[Keys.profile] = json.encodeToString(
                    UserProfile.serializer(),
                    demoProfile(name = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() }, email = cleanEmail),
                )
            }
        }
        return Result.success(Unit)
    }

    suspend fun demoLogin(): Result<Unit> {
        dataStore.edit { prefs ->
            prefs[Keys.signedIn] = true
            prefs[Keys.verified] = true
            prefs[Keys.onboarding] = true
            val existing = prefs[Keys.profile]?.let { runCatching { json.decodeFromString<UserProfile>(it) }.getOrNull() }
            val profile = if (existing != null && existing.department.isNotBlank()) {
                existing
            } else {
                fullySetUpDemoProfile()
            }
            prefs[Keys.profile] = json.encodeToString(UserProfile.serializer(), profile)
        }
        return Result.success(Unit)
    }

    suspend fun requestPasswordReset(email: String): Result<Unit> {
        Validators.emailError(email)?.let { return Result.failure(IllegalArgumentException(it)) }
        return Result.success(Unit)
    }

    suspend fun updateProfile(profile: UserProfile) {
        dataStore.edit { prefs ->
            prefs[Keys.profile] = json.encodeToString(UserProfile.serializer(), profile)
        }
    }

    suspend fun updateNotificationPreferences(prefs: com.campusconnect.app.core.model.NotificationPreferences) {
        dataStore.edit {
            it[Keys.notifyAnnouncements] = prefs.announcements
            it[Keys.notifyMessages] = prefs.messages
            it[Keys.notifyEvents] = prefs.events
            it[Keys.notifyOpportunities] = prefs.opportunities
            it[Keys.notifyMarketing] = prefs.marketing
        }
    }

    suspend fun signOut() {
        dataStore.edit { prefs ->
            prefs[Keys.signedIn] = false
            prefs[Keys.verified] = false
            prefs[Keys.pendingEmail] = ""
        }
    }

    private fun demoProfile(name: String, email: String): UserProfile = UserProfile(
        id = "u-me",
        fullName = name.ifBlank { "Campus Student" },
        email = email.ifBlank { "student@demo.campusconnect.app" },
        role = UserRole.STUDENT,
        institutionId = DemoData.DEMO_INSTITUTION_ID,
        institutionName = DemoData.institutions.first().name,
        verified = true,
    )

    private fun fullySetUpDemoProfile(): UserProfile = demoProfile(
        name = "Mary Achieng (demo)",
        email = "student@demo.campusconnect.app",
    ).copy(
        department = DemoData.departments.first().name,
        course = DemoData.courses.first().name,
        yearOfStudy = 2,
        bio = "Demo student account exploring CampusConnect. Replace this with your own profile.",
        followers = 24,
        following = 18,
    )

    companion object {
        const val DEMO_VERIFICATION_CODE = "123456"
    }
}

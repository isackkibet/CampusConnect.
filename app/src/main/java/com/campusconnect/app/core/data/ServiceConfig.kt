package com.campusconnect.app.core.data

import com.campusconnect.app.BuildConfig

data class ServiceConfig(
    val supabaseUrl: String,
    val supabaseAnonKey: String,
    val mapsApiKey: String,
    val streamApiKey: String,
    val backendBaseUrl: String,
    val adminBaseUrl: String,
) {
    val supabaseConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabaseAnonKey.isNotBlank()

    val mapsConfigured: Boolean
        get() = mapsApiKey.isNotBlank()

    val streamConfigured: Boolean
        get() = streamApiKey.isNotBlank()

    val backendConfigured: Boolean
        get() = backendBaseUrl.isNotBlank() && backendBaseUrl.startsWith("http")

    fun assistantEndpoint(): String =
        backendBaseUrl.trimEnd('/') + "/functions/v1/groq-assistant"

    fun paymentsEndpoint(): String =
        backendBaseUrl.trimEnd('/') + "/functions/v1/mpesa-stk-push"

    companion object {
        fun fromBuildConfig(): ServiceConfig = ServiceConfig(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseAnonKey = BuildConfig.SUPABASE_ANON_KEY,
            mapsApiKey = BuildConfig.MAPS_API_KEY,
            streamApiKey = BuildConfig.STREAM_API_KEY,
            backendBaseUrl = BuildConfig.BACKEND_BASE_URL,
            adminBaseUrl = BuildConfig.ADMIN_BASE_URL,
        )
    }
}

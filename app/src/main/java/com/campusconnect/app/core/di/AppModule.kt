package com.campusconnect.app.core.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.campusconnect.app.core.data.ServiceConfig
import com.campusconnect.app.core.data.local.CampusDatabase
import com.campusconnect.app.core.data.local.CommentDao
import com.campusconnect.app.core.data.local.NotificationDao
import com.campusconnect.app.core.data.local.PostDao
import com.campusconnect.app.core.data.local.SavedItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            produceFile = { context.preferencesDataStoreFile("campus_session") },
        )

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CampusDatabase =
        Room.databaseBuilder(context, CampusDatabase::class.java, "campusconnect.db")
            .fallbackToDestructiveMigration(true)
            .build()

    @Provides
    fun providePostDao(database: CampusDatabase): PostDao = database.postDao()

    @Provides
    fun provideCommentDao(database: CampusDatabase): CommentDao = database.commentDao()

    @Provides
    fun provideNotificationDao(database: CampusDatabase): NotificationDao = database.notificationDao()

    @Provides
    fun provideSavedItemDao(database: CampusDatabase): SavedItemDao = database.savedItemDao()

    @Provides
    @Singleton
    fun provideServiceConfig(): ServiceConfig = ServiceConfig.fromBuildConfig()

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 20_000
            socketTimeoutMillis = 30_000
        }
        install(Logging) {
            level = LogLevel.NONE
        }
    }
}

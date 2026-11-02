package com.campusconnect.app.core.data.repo

import com.campusconnect.app.core.data.DemoData
import com.campusconnect.app.core.data.local.NotificationDao
import com.campusconnect.app.core.data.local.NotificationEntity
import com.campusconnect.app.core.di.ApplicationScope
import com.campusconnect.app.core.model.NotificationItem
import com.campusconnect.app.core.model.NotificationPreferences
import com.campusconnect.app.core.model.NotificationType
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val sessionRepository: SessionRepository,
    @ApplicationScope scope: CoroutineScope,
) {
    val notifications: Flow<List<NotificationItem>> = notificationDao.observeAll().map { list ->
        list.map {
            NotificationItem(
                id = it.id,
                title = it.title,
                body = it.body,
                type = runCatching { NotificationType.valueOf(it.type) }.getOrDefault(NotificationType.SYSTEM),
                createdAt = it.createdAt,
                read = it.read,
                targetId = it.targetId,
            )
        }
    }

    val unreadCount: Flow<Int> = notificationDao.observeUnreadCount()
    val preferences: Flow<NotificationPreferences> = sessionRepository.notificationPreferences

    init {
        scope.launch {
            if (notificationDao.count() == 0) {
                notificationDao.insertAll(
                    DemoData.notifications().map {
                        NotificationEntity(
                            id = it.id,
                            title = it.title,
                            body = it.body,
                            type = it.type.name,
                            createdAt = it.createdAt,
                            read = it.read,
                            targetId = it.targetId,
                        )
                    },
                )
            }
        }
    }

    suspend fun markRead(id: String) = notificationDao.markRead(id)

    suspend fun markAllRead() = notificationDao.markAllRead()

    suspend fun updatePreferences(preferences: NotificationPreferences) =
        sessionRepository.updateNotificationPreferences(preferences)

    suspend fun pushLocal(title: String, body: String, type: NotificationType, targetId: String? = null) {
        notificationDao.insert(
            NotificationEntity(
                id = "not-" + java.util.UUID.randomUUID(),
                title = title,
                body = body,
                type = type.name,
                createdAt = System.currentTimeMillis(),
                read = false,
                targetId = targetId,
            ),
        )
    }
}

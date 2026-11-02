package com.campusconnect.app.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PostEntity::class,
        CommentEntity::class,
        NotificationEntity::class,
        SavedItemEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class CampusDatabase : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun commentDao(): CommentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun savedItemDao(): SavedItemDao
}

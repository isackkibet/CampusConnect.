package com.campusconnect.app.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorDepartment: String,
    val institutionId: String,
    val content: String,
    val imageUrl: String?,
    val createdAt: Long,
    val likeCount: Int,
    val commentCount: Int,
    val likedByMe: Boolean,
    val communityName: String?,
    val isOwn: Boolean,
    val hidden: Boolean = false,
)

@Entity(tableName = "cached_comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorId: String,
    val authorName: String,
    val content: String,
    val createdAt: Long,
)

@Entity(tableName = "cached_notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val body: String,
    val type: String,
    val createdAt: Long,
    val read: Boolean,
    val targetId: String?,
)

@Entity(tableName = "cached_saved_items")
data class SavedItemEntity(
    @PrimaryKey val id: String,
    val type: String,
    val targetId: String,
    val title: String,
    val savedAt: Long,
)

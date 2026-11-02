package com.campusconnect.app.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM cached_posts WHERE hidden = 0 ORDER BY createdAt DESC")
    fun observePosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM cached_posts WHERE id = :id")
    suspend fun getById(id: String): PostEntity?

    @Query("SELECT COUNT(*) FROM cached_posts")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(posts: List<PostEntity>)

    @Query("UPDATE cached_posts SET likedByMe = :liked, likeCount = MAX(0, likeCount + :delta) WHERE id = :id")
    suspend fun setLiked(id: String, liked: Boolean, delta: Int)

    @Query("UPDATE cached_posts SET commentCount = MAX(0, commentCount + :delta) WHERE id = :id")
    suspend fun adjustCommentCount(id: String, delta: Int)

    @Query("UPDATE cached_posts SET content = :content WHERE id = :id AND isOwn = 1")
    suspend fun updateOwnPost(id: String, content: String)

    @Query("UPDATE cached_posts SET hidden = 1 WHERE id = :id")
    suspend fun hide(id: String)

    @Query("DELETE FROM cached_posts WHERE id = :id AND isOwn = 1")
    suspend fun deleteOwn(id: String)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM cached_comments WHERE postId = :postId ORDER BY createdAt ASC")
    fun observeForPost(postId: String): Flow<List<CommentEntity>>

    @Query("SELECT COUNT(*) FROM cached_comments WHERE postId = :postId")
    suspend fun countForPost(postId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: CommentEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM cached_notifications ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM cached_notifications WHERE read = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM cached_notifications")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<NotificationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NotificationEntity)

    @Query("UPDATE cached_notifications SET read = 1 WHERE id = :id")
    suspend fun markRead(id: String)

    @Query("UPDATE cached_notifications SET read = 1")
    suspend fun markAllRead()
}

@Dao
interface SavedItemDao {
    @Query("SELECT * FROM cached_saved_items ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedItemEntity>>

    @Query("SELECT COUNT(*) FROM cached_saved_items WHERE type = :type AND targetId = :targetId")
    suspend fun exists(type: String, targetId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SavedItemEntity)

    @Query("DELETE FROM cached_saved_items WHERE type = :type AND targetId = :targetId")
    suspend fun delete(type: String, targetId: String)
}

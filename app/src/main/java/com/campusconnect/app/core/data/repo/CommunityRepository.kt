package com.campusconnect.app.core.data.repo

import com.campusconnect.app.core.data.DemoData
import com.campusconnect.app.core.data.DemoStore
import com.campusconnect.app.core.data.local.CommentEntity
import com.campusconnect.app.core.data.local.CommentDao
import com.campusconnect.app.core.data.local.PostDao
import com.campusconnect.app.core.data.local.PostEntity
import com.campusconnect.app.core.di.ApplicationScope
import com.campusconnect.app.core.model.Comment
import com.campusconnect.app.core.model.Post
import com.campusconnect.app.core.util.Validators
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
class CommunityRepository @Inject constructor(
    private val postDao: PostDao,
    private val commentDao: CommentDao,
    private val sessionRepository: SessionRepository,
    private val store: DemoStore,
    @ApplicationScope scope: CoroutineScope,
) {
    val posts: Flow<List<Post>> = postDao.observePosts().map { list -> list.map { it.toModel() } }
    val groups = store.groups
    val students = store.students

    init {
        scope.launch {
            if (postDao.count() == 0) {
                seed()
            }
        }
    }

    private suspend fun seed() {
        val now = DemoData.now()
        postDao.upsertAll(
            DemoData.posts(now).map { it.toEntity(isOwn = it.authorId == "u-me") },
        )
        DemoData.comments(now).forEach { (postId, comments) ->
            comments.forEach { comment ->
                commentDao.insert(
                    CommentEntity(
                        id = comment.id,
                        postId = postId,
                        authorId = comment.authorId,
                        authorName = comment.authorName,
                        content = comment.content,
                        createdAt = comment.createdAt,
                    ),
                )
            }
        }
    }

    fun comments(postId: String): Flow<List<Comment>> =
        commentDao.observeForPost(postId).map { list ->
            list.map {
                Comment(it.id, it.postId, it.authorId, it.authorName, it.content, it.createdAt)
            }
        }

    suspend fun postById(id: String): Post? = postDao.getById(id)?.toModel()

    suspend fun createPost(content: String, imageUrl: String?, communityName: String?): Result<Post> {
        Validators.postError(content)?.let { return Result.failure(IllegalArgumentException(it)) }
        val profile = sessionRepository.currentProfile()
            ?: return Result.failure(IllegalStateException("Sign in to post."))
        val post = Post(
            id = "post-" + UUID.randomUUID(),
            authorId = profile.id,
            authorName = profile.fullName,
            authorDepartment = profile.department.ifBlank { profile.institutionName },
            institutionId = profile.institutionId,
            content = content.trim(),
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis(),
            communityName = communityName,
            isOwn = true,
        )
        postDao.upsert(post.toEntity(isOwn = true))
        return Result.success(post)
    }

    suspend fun editPost(id: String, content: String): Result<Unit> {
        Validators.postError(content)?.let { return Result.failure(IllegalArgumentException(it)) }
        val entity = postDao.getById(id) ?: return Result.failure(IllegalStateException("Post not found"))
        if (!entity.isOwn) return Result.failure(SecurityException("You can only edit your own posts."))
        postDao.updateOwnPost(id, content.trim())
        return Result.success(Unit)
    }

    suspend fun deletePost(id: String): Result<Unit> {
        val entity = postDao.getById(id) ?: return Result.failure(IllegalStateException("Post not found"))
        if (!entity.isOwn) return Result.failure(SecurityException("You can only delete your own posts."))
        postDao.deleteOwn(id)
        return Result.success(Unit)
    }

    suspend fun toggleLike(id: String) {
        val entity = postDao.getById(id) ?: return
        postDao.setLiked(id, !entity.likedByMe, if (entity.likedByMe) -1 else 1)
    }

    suspend fun addComment(postId: String, content: String): Result<Comment> {
        if (content.isBlank()) return Result.failure(IllegalArgumentException("Write a comment first"))
        if (content.length > 500) return Result.failure(IllegalArgumentException("Comments are limited to 500 characters"))
        val profile = sessionRepository.currentProfile()
            ?: return Result.failure(IllegalStateException("Sign in to comment."))
        val comment = Comment(
            id = "cmt-" + UUID.randomUUID(),
            postId = postId,
            authorId = profile.id,
            authorName = profile.fullName,
            content = content.trim(),
            createdAt = System.currentTimeMillis(),
        )
        commentDao.insert(
            CommentEntity(comment.id, comment.postId, comment.authorId, comment.authorName, comment.content, comment.createdAt),
        )
        postDao.adjustCommentCount(postId, 1)
        return Result.success(comment)
    }

    suspend fun reportPost(id: String, reason: String, details: String) {
        store.reportPost(id, reason, details)
    }

    suspend fun hidePost(id: String) {
        postDao.hide(id)
    }

    fun toggleGroupJoined(id: String) = store.toggleGroupJoined(id)

    fun toggleFollow(id: String) = store.toggleFollow(id)

    private fun PostEntity.toModel() = Post(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorDepartment = authorDepartment,
        institutionId = institutionId,
        content = content,
        imageUrl = imageUrl,
        createdAt = createdAt,
        likeCount = likeCount,
        commentCount = commentCount,
        likedByMe = likedByMe,
        communityName = communityName,
        isOwn = isOwn,
    )

    private fun Post.toEntity(isOwn: Boolean) = PostEntity(
        id = id,
        authorId = authorId,
        authorName = authorName,
        authorDepartment = authorDepartment,
        institutionId = institutionId,
        content = content,
        imageUrl = imageUrl,
        createdAt = createdAt,
        likeCount = likeCount,
        commentCount = commentCount,
        likedByMe = likedByMe,
        communityName = communityName,
        isOwn = isOwn,
    )
}

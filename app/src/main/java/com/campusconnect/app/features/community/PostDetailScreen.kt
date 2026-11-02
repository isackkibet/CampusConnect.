package com.campusconnect.app.features.community

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.CommunityRepository
import com.campusconnect.app.core.model.Comment
import com.campusconnect.app.core.model.Post
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.LoadingState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CommunityRepository,
) : ViewModel() {

    private val postId: String = savedStateHandle.get<String>("postId") ?: ""

    val post: StateFlow<Post?> = repository.posts
        .map { list -> list.firstOrNull { it.id == postId } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val comments: StateFlow<List<Comment>> = repository.comments(postId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleLike() {
        viewModelScope.launch { repository.toggleLike(postId) }
    }

    fun addComment(content: String, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.addComment(postId, content).onSuccess { onDone() }
        }
    }

    fun editPost(content: String) {
        viewModelScope.launch { repository.editPost(postId, content) }
    }

    fun deletePost(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.deletePost(postId).onSuccess { onDone() }
        }
    }

    fun reportPost(reason: String, details: String) {
        viewModelScope.launch { repository.reportPost(postId, reason, details) }
    }

    fun hidePost() {
        viewModelScope.launch { repository.hidePost(postId) }
    }

    fun sharePost() = Unit
}

@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val post by viewModel.post.collectAsStateWithLifecycle()
    val comments by viewModel.comments.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        CampusTopBar(title = "Post", subtitle = "Conversation", onBack = onBack)
        if (post == null) {
            LoadingState("Loading post…")
        } else {
            val current = post ?: return@Column
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    CommunityPostCard(
                        post = current,
                        onOpen = {},
                        onLike = viewModel::toggleLike,
                        onComment = {},
                        onShare = {},
                        onEdit = viewModel::editPost,
                        onDelete = { viewModel.deletePost(onBack) },
                        onReport = viewModel::reportPost,
                        onHide = { viewModel.hidePost(); onBack() },
                    )
                }
                item {
                    Text(
                        "Comments (${comments.size})",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                if (comments.isEmpty()) {
                    item {
                        EmptyState(
                            Icons.AutoMirrored.Filled.Send,
                            "No comments yet",
                            "Start the conversation with a respectful reply.",
                        )
                    }
                } else {
                    items(comments, key = { it.id }) { comment ->
                        CommentRow(comment)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it.take(500) },
                    placeholder = { Text("Add a comment…") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 3,
                )
                Spacer(Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(commentText) { commentText = "" }
                        }
                    },
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send comment")
                }
            }
        }
    }
}

@Composable
private fun CommentRow(comment: Comment) {
    CampusCard {
        Row(modifier = Modifier.padding(12.dp)) {
            Avatar(comment.authorName, size = 34.dp)
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(comment.authorName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        TimeFormat.relative(comment.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.padding(top = 2.dp))
                Text(comment.content, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

package com.campusconnect.app.features.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.CommunityRepository
import com.campusconnect.app.core.model.CommunityGroup
import com.campusconnect.app.core.model.Post
import com.campusconnect.app.core.model.StudentSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityUiState(
    val posts: List<Post> = emptyList(),
    val groups: List<CommunityGroup> = emptyList(),
    val students: List<StudentSummary> = emptyList(),
    val query: String = "",
    val message: String? = null,
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: CommunityRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.posts.collect { posts -> _state.update { it.copy(posts = posts) } }
        }
        viewModelScope.launch {
            repository.groups.collect { groups -> _state.update { it.copy(groups = groups) } }
        }
        viewModelScope.launch {
            repository.students.collect { students -> _state.update { it.copy(students = students) } }
        }
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
    }

    fun toggleLike(post: Post) {
        viewModelScope.launch { repository.toggleLike(post.id) }
    }

    fun createPost(content: String, imageUrl: String?, communityName: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            repository.createPost(content, imageUrl, communityName)
                .onSuccess {
                    _state.update { it.copy(message = "Post published") }
                    onDone()
                }
                .onFailure { error ->
                    _state.update { it.copy(message = error.message ?: "Could not publish post") }
                }
        }
    }

    fun editPost(id: String, content: String) {
        viewModelScope.launch {
            repository.editPost(id, content)
                .onSuccess { _state.update { it.copy(message = "Post updated") } }
                .onFailure { _state.update { it.copy(message = it.message ?: "Could not update post") } }
        }
    }

    fun deletePost(id: String) {
        viewModelScope.launch {
            repository.deletePost(id)
                .onSuccess { _state.update { it.copy(message = "Post deleted") } }
                .onFailure { _state.update { it.copy(message = it.message ?: "Could not delete post") } }
        }
    }

    fun reportPost(id: String, reason: String, details: String) {
        viewModelScope.launch {
            repository.reportPost(id, reason, details)
            _state.update { it.copy(message = "Report submitted for review") }
        }
    }

    fun hidePost(id: String) {
        viewModelScope.launch {
            repository.hidePost(id)
            _state.update { it.copy(message = "Post hidden from your feed") }
        }
    }

    fun toggleFollow(id: String) = repository.toggleFollow(id)

    fun toggleGroup(id: String) = repository.toggleGroupJoined(id)

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}

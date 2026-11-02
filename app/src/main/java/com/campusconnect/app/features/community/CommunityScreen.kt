package com.campusconnect.app.features.community

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.model.CommunityType
import com.campusconnect.app.core.model.Post
import com.campusconnect.app.core.model.StudentSummary
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.SearchField

@Composable
fun CommunityScreen(
    onCreatePost: () -> Unit,
    onOpenPost: (String) -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Feed", "Groups", "Students")

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(title = "Community", subtitle = "Connect with your campus")
        TabRow(selectedTabIndex = tab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = tab == index,
                    onClick = { tab = index },
                    text = { Text(title) },
                )
            }
        }

        when (tab) {
            0 -> FeedTab(
                posts = state.posts,
                query = state.query,
                onQuery = viewModel::setQuery,
                onOpen = onOpenPost,
                onLike = viewModel::toggleLike,
                onComment = onOpenPost,
                onEdit = viewModel::editPost,
                onDelete = viewModel::deletePost,
                onReport = viewModel::reportPost,
                onHide = viewModel::hidePost,
                onCreatePost = onCreatePost,
            )
            1 -> GroupsTab(
                groups = state.groups,
                onToggle = viewModel::toggleGroup,
            )
            else -> StudentsTab(
                students = state.students,
                query = state.query,
                onQuery = viewModel::setQuery,
                onToggleFollow = viewModel::toggleFollow,
            )
        }
    }
}

@Composable
private fun FeedTab(
    posts: List<Post>,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (String) -> Unit,
    onLike: (Post) -> Unit,
    onComment: (String) -> Unit,
    onEdit: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onReport: (String, String, String) -> Unit,
    onHide: (String) -> Unit,
    onCreatePost: () -> Unit,
) {
    val filtered = remember(posts, query) {
        if (query.isBlank()) posts
        else posts.filter {
            it.content.contains(query, ignoreCase = true) ||
                it.authorName.contains(query, ignoreCase = true) ||
                (it.communityName?.contains(query, ignoreCase = true) == true)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (posts.isEmpty()) {
            EmptyState(
                Icons.Filled.Groups,
                "Your feed is warming up",
                "Be the first to share something with campus.",
                actionLabel = "Create a post",
                onAction = onCreatePost,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    SearchField(
                        value = query,
                        onValueChange = onQuery,
                        placeholder = "Search posts, people and groups",
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        EmptyState(
                            Icons.Filled.Search,
                            "No matches",
                            "Try a different search term.",
                        )
                    }
                }
                items(filtered, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        onOpen = { onOpen(post.id) },
                        onLike = { onLike(post) },
                        onComment = { onComment(post.id) },
                        onShare = {},
                        onEdit = { content -> onEdit(post.id, content) },
                        onDelete = { onDelete(post.id) },
                        onReport = { reason, details -> onReport(post.id, reason, details) },
                        onHide = { onHide(post.id) },
                    )
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onCreatePost,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Post") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }
}

@Composable
private fun GroupsTab(
    groups: List<com.campusconnect.app.core.model.CommunityGroup>,
    onToggle: (String) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                "Department communities, class groups and clubs. Membership is stored locally in demo mode.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(groups, key = { it.id }) { group ->
            CampusCard {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(group.name, size = 46.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(group.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            group.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Pill(group.type.name.lowercase().replaceFirstChar { it.uppercase() })
                            Text(
                                "${group.memberCount} members",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { onToggle(group.id) },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(if (group.joined) "Joined" else "Join")
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentsTab(
    students: List<StudentSummary>,
    query: String,
    onQuery: (String) -> Unit,
    onToggleFollow: (String) -> Unit,
) {
    val filtered = remember(students, query) {
        if (query.isBlank()) students
        else students.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.department.contains(query, ignoreCase = true) ||
                it.course.contains(query, ignoreCase = true)
        }
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            SearchField(query, onQuery, "Discover students by name, course or department")
        }
        items(filtered, key = { it.id }) { student ->
            CampusCard {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Avatar(student.name, size = 46.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(student.name, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${student.course} • Year ${student.year}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (student.bio.isNotBlank()) {
                            Text(
                                student.bio,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = { onToggleFollow(student.id) },
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Icon(
                            if (student.followed) Icons.Filled.PersonAdd else Icons.Filled.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (student.followed) "Following" else "Follow")
                    }
                }
            }
        }
    }
}

package com.campusconnect.app.features.community

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.campusconnect.app.core.model.Post
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.RemoteImage
import com.campusconnect.app.ui.components.ReportDialog

@Composable
fun CommunityPostCard(
    post: Post,
    onOpen: () -> Unit,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onShare: () -> Unit,
    onEdit: (String) -> Unit,
    onDelete: () -> Unit,
    onReport: (String, String) -> Unit,
    onHide: () -> Unit,
) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf(false) }
    var reporting by remember { mutableStateOf(false) }
    var editText by remember { mutableStateOf(post.content) }

    CampusCard(onClick = onOpen) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(post.authorName, size = 42.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorName, style = MaterialTheme.typography.titleSmall)
                        if (post.isOwn) {
                            Spacer(Modifier.width(6.dp))
                            Pill("You")
                        }
                    }
                    Text(
                        "${post.authorDepartment} • ${TimeFormat.relative(post.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                androidx.compose.foundation.layout.Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Post options")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        if (post.isOwn) {
                            DropdownMenuItem(
                                text = { Text("Edit post") },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    editText = post.content
                                    editing = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete post") },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    onDelete()
                                },
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text("Report post") },
                                leadingIcon = { Icon(Icons.Filled.Flag, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    reporting = true
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Hide from feed") },
                                leadingIcon = { Icon(Icons.Filled.VisibilityOff, contentDescription = null) },
                                onClick = {
                                    menuOpen = false
                                    onHide()
                                },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Share") },
                            leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                            onClick = {
                                menuOpen = false
                                onShare()
                            },
                        )
                    }
                }
            }

            if (post.communityName != null) {
                Spacer(Modifier.height(10.dp))
                Pill(post.communityName)
            }
            Spacer(Modifier.height(10.dp))
            Text(post.content, style = MaterialTheme.typography.bodyMedium)

            if (post.imageUrl != null) {
                Spacer(Modifier.height(12.dp))
                RemoteImage(
                    url = post.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onLike) {
                    Icon(
                        if (post.likedByMe) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = if (post.likedByMe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(post.likeCount.toString())
                }
                TextButton(onClick = onComment) {
                    Icon(
                        Icons.Filled.ChatBubbleOutline,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(post.commentCount.toString())
                }
                TextButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "${post.authorName} on CampusConnect: ${post.content}")
                    }
                    runCatching { context.startActivity(Intent.createChooser(shareIntent, "Share post")) }
                }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Share")
                }
            }
        }
    }

    if (editing) {
        AlertDialog(
            onDismissRequest = { editing = false },
            title = { Text("Edit post") },
            text = {
                OutlinedTextField(
                    value = editText,
                    onValueChange = { editText = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 8,
                )
            },
            confirmButton = {
                Button(onClick = {
                    onEdit(editText)
                    editing = false
                }, shape = RoundedCornerShape(12.dp)) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { editing = false }) { Text("Cancel") } },
        )
    }

    if (reporting) {
        ReportDialog(
            title = "Report post",
            onDismiss = { reporting = false },
            onSubmit = { reason, details -> onReport(reason, details) },
        )
    }
}

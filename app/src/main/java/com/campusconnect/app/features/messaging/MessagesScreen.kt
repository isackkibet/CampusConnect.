package com.campusconnect.app.features.messaging

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.data.DemoData
import com.campusconnect.app.core.model.Conversation
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.EmptyState

@Composable
fun MessagesScreen(
    onBack: () -> Unit,
    onOpenConversation: (String) -> Unit,
    viewModel: MessagingViewModel = hiltViewModel(),
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    var newChatOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CampusTopBar(
                title = "Messages",
                subtitle = "${conversations.size} conversations",
                onBack = onBack,
            )
            if (conversations.isEmpty()) {
                EmptyState(
                    Icons.Filled.ChatBubbleOutline,
                    "No conversations yet",
                    "Start a chat with a classmate or create a group.",
                    actionLabel = "New conversation",
                    onAction = { newChatOpen = true },
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (viewModel.demoMode) {
                        item {
                            DemoModeBanner(
                                "Demo messaging runs locally with simulated replies. Real-time chat, read receipts and calls require Stream credentials.",
                            )
                        }
                    }
                    items(conversations, key = { it.id }) { conversation ->
                        ConversationRow(conversation) {
                            viewModel.markRead(conversation.id)
                            onOpenConversation(conversation.id)
                        }
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = { newChatOpen = true },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("New chat") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    if (newChatOpen) {
        NewConversationDialog(
            onDismiss = { newChatOpen = false },
            onCreate = { title, memberIds, isGroup ->
                val id = viewModel.startConversation(title, memberIds, isGroup)
                newChatOpen = false
                onOpenConversation(id)
            },
        )
    }
}

@Composable
private fun ConversationRow(conversation: Conversation, onClick: () -> Unit) {
    CampusCard(onClick = onClick) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box {
                Avatar(conversation.title, size = 50.dp)
                if (conversation.online) {
                    Surface(
                        shape = CircleShape,
                        color = com.campusconnect.app.core.design.Green600,
                        modifier = Modifier
                            .size(13.dp)
                            .align(Alignment.BottomEnd),
                    ) {}
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (conversation.isGroup) {
                        Icon(Icons.Filled.Groups, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                    }
                    Text(
                        conversation.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (conversation.unread > 0) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        TimeFormat.relative(conversation.lastMessageAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.padding(top = 3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        conversation.lastMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (conversation.unread > 0) {
                        Spacer(Modifier.width(8.dp))
                        Badge { Text(conversation.unread.toString()) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewConversationDialog(
    onDismiss: () -> Unit,
    onCreate: (String, List<String>, Boolean) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var group by remember { mutableStateOf(false) }
    val selected = remember { mutableSetOf<String>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New conversation") },
        text = {
            Column {
                if (group) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Group name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                } else {
                    Text(
                        "Choose a student to message",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { group = !group }
                        .padding(vertical = 6.dp),
                ) {
                    Checkbox(checked = group, onCheckedChange = { group = it })
                    Text("Create a group")
                }
                LazyColumn(modifier = Modifier.padding(top = 4.dp)) {
                    items(DemoData.students(), key = { it.id }) { student ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!group) {
                                        title = student.name
                                        selected.clear()
                                        selected.add(student.id)
                                    } else {
                                        if (!selected.add(student.id)) selected.remove(student.id)
                                        val names = DemoData.students().filter { selected.contains(it.id) }.joinToString(", ") { it.name.substringBefore(" ") }
                                        if (names.isNotBlank()) title = names + " group"
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Avatar(student.name, size = 34.dp)
                            Spacer(Modifier.width(10.dp))
                            Text(student.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            if (group && selected.contains(student.id)) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank() && (group || selected.isNotEmpty())) {
                        onCreate(title, selected.toList(), group)
                    }
                },
                enabled = title.isNotBlank(),
            ) { Text("Start") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

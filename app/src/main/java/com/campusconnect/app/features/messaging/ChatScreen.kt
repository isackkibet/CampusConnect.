package com.campusconnect.app.features.messaging

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.campusconnect.app.core.model.Message
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.RemoteImage

@Composable
fun ChatScreen(
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val conversation by viewModel.conversation.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val typing by viewModel.typing.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var text by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var callInfo by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUrl = uri.toString()
    }

    LaunchedEffect(messages.size, typing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding(),
    ) {
        CampusTopBar(
            title = conversation?.title ?: "Conversation",
            subtitle = when {
                typing -> "typing…"
                conversation?.online == true -> "Online"
                conversation?.isGroup == true -> "Group conversation"
                else -> "Demo conversation"
            },
            onBack = onBack,
            actions = {
                IconButton(onClick = { callInfo = true }) {
                    Icon(Icons.Filled.Call, contentDescription = "Voice call")
                }
                IconButton(onClick = { callInfo = true }) {
                    Icon(Icons.Filled.Videocam, contentDescription = "Video call")
                }
            },
        )

        if (messages.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    "No messages yet. Say hello!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (viewModel.demoMode) {
                    item {
                        DemoModeBanner("Demo conversation: replies are simulated on this device. Stream Chat is not connected.")
                    }
                }
                items(messages, key = { it.id }) { message ->
                    MessageBubble(message, showSender = conversation?.isGroup == true)
                }
                if (typing) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Text(
                                "typing…",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }

        if (imageUrl != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Attachment preview",
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(8.dp))
                Text("Photo attached", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = { imageUrl = null }) {
                    Icon(Icons.Filled.Close, contentDescription = "Remove attachment")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { picker.launch("image/*") }) {
                Icon(Icons.Filled.AttachFile, contentDescription = "Attach photo")
            }
            OutlinedTextField(
                value = text,
                onValueChange = { text = it.take(2000) },
                placeholder = { Text("Message…") },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                shape = RoundedCornerShape(18.dp),
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = {
                    if (text.isNotBlank() || imageUrl != null) {
                        viewModel.send(text, imageUrl) {
                            text = ""
                            imageUrl = null
                        }
                    }
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
            }
        }
    }

    if (callInfo) {
        AlertDialog(
            onDismissRequest = { callInfo = false },
            icon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
            title = { Text("Voice and video calls") },
            text = {
                Text(
                    "Calls are implemented through Stream Video behind a service interface, but they " +
                        "are not enabled in this build because Stream credentials and a server-side " +
                        "token endpoint are not configured. Add STREAM_API_KEY and deploy the token " +
                        "backend to enable them. See docs/DEPLOYMENT.md.",
                )
            },
            confirmButton = { TextButton(onClick = { callInfo = false }) { Text("Got it") } },
        )
    }
}

@Composable
private fun MessageBubble(message: Message, showSender: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
    ) {
        if (showSender && !message.isMine) {
            Text(
                message.senderName,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp),
            )
        }
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isMine) 16.dp else 4.dp,
                bottomEnd = if (message.isMine) 4.dp else 16.dp,
            ),
            color = if (message.isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(0.82f),
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                if (message.imageUrl != null) {
                    RemoteImage(
                        url = message.imageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    if (message.content.isNotBlank()) Spacer(Modifier.height(6.dp))
                }
                if (message.content.isNotBlank()) {
                    Text(
                        message.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (message.isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    TimeFormat.time(message.sentAt) + if (message.isMine && message.read) " ✓✓" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (message.isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.End),
                )
            }
        }
    }
}

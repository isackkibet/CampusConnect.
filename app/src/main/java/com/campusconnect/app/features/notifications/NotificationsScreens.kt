package com.campusconnect.app.features.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.model.NotificationItem
import com.campusconnect.app.core.model.NotificationPreferences
import com.campusconnect.app.core.model.NotificationType
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.ChipRow
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.DemoModeBanner

@Composable
fun NotificationsScreen(
    onBack: () -> Unit,
    onOpen: (NotificationType, String?) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(
            title = "Notifications",
            subtitle = if (state.unreadCount > 0) "${state.unreadCount} unread" else "You are all caught up",
            onBack = onBack,
            actions = {
                IconButton(onClick = { viewModel.markAllRead() }) {
                    Icon(Icons.Filled.DoneAll, contentDescription = "Mark all read")
                }
            },
        )
        if (state.items.isEmpty()) {
            EmptyState(
                Icons.Filled.NotificationsNone,
                "No notifications yet",
                "Announcements, messages and campus activity will appear here.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    DemoModeBanner("In-app notifications are stored locally. Push notifications require Firebase configuration.")
                }
                items(state.items, key = { it.id }) { item ->
                    NotificationRow(item) {
                        viewModel.markRead(item.id)
                        onOpen(item.type, item.targetId)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(item: NotificationItem, onClick: () -> Unit) {
    CampusCard(onClick = onClick) {
        Row(modifier = Modifier.padding(14.dp)) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    iconFor(item.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(10.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (item.read) FontWeight.Normal else FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    if (!item.read) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(8.dp),
                        ) {}
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(item.body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(TimeFormat.relative(item.createdAt), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun iconFor(type: NotificationType): ImageVector = when (type) {
    NotificationType.ANNOUNCEMENT -> Icons.Filled.Campaign
    NotificationType.MESSAGE -> Icons.Filled.ChatBubble
    NotificationType.EVENT -> Icons.Filled.Event
    NotificationType.OPPORTUNITY -> Icons.Filled.Work
    NotificationType.MARKETPLACE -> Icons.Filled.Storefront
    NotificationType.SYSTEM -> Icons.Filled.NotificationsNone
}

@Composable
fun NotificationPreferencesScreen(
    onBack: () -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val prefs = state.preferences

    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(title = "Notification preferences", onBack = onBack)
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DemoModeBanner(
                    "Preferences are saved on this device. Push delivery only works after Firebase is configured and the device token is registered.",
                )
            }
            item {
                PreferenceCard(
                    title = "Announcements",
                    subtitle = "Campus announcements and admin updates",
                    checked = prefs.announcements,
                ) { viewModel.setPreference { p -> p.copy(announcements = it) } }
            }
            item {
                PreferenceCard(
                    title = "Messages",
                    subtitle = "New chats and group mentions",
                    checked = prefs.messages,
                ) { viewModel.setPreference { p -> p.copy(messages = it) } }
            }
            item {
                PreferenceCard(
                    title = "Events",
                    subtitle = "RSVP reminders and club activity",
                    checked = prefs.events,
                ) { viewModel.setPreference { p -> p.copy(events = it) } }
            }
            item {
                PreferenceCard(
                    title = "Opportunities",
                    subtitle = "Jobs, internships and attachments",
                    checked = prefs.opportunities,
                ) { viewModel.setPreference { p -> p.copy(opportunities = it) } }
            }
            item {
                PreferenceCard(
                    title = "Marketplace tips",
                    subtitle = "Occasional product and marketplace updates",
                    checked = prefs.marketing,
                ) { viewModel.setPreference { p -> p.copy(marketing = it) } }
            }
        }
    }
}

@Composable
private fun PreferenceCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    CampusCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCheckedChange(!checked) }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

package com.campusconnect.app.features.home

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.model.CampusEvent
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.Opportunity
import com.campusconnect.app.core.model.Post
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.core.util.Validators
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.LoadingState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.QuickActionTile
import com.campusconnect.app.ui.components.RemoteImage
import com.campusconnect.app.ui.components.SectionHeader

@Composable
fun HomeScreen(
    onMessages: () -> Unit,
    onNotifications: () -> Unit,
    onEvents: () -> Unit,
    onMap: () -> Unit,
    onAccommodation: () -> Unit,
    onAssistant: () -> Unit,
    onPost: (String) -> Unit,
    onOpportunity: (String) -> Unit,
    onListing: (String) -> Unit,
    onAllCommunity: () -> Unit,
    onAllOpportunities: () -> Unit,
    onAllMarketplace: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val profile = state.profile

    if (profile == null) {
        LoadingState("Loading your dashboard…")
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), Color.Transparent),
                        ),
                    )
                    .padding(start = 20.dp, end = 12.dp, top = 18.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Avatar(profile.fullName, size = 46.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        TimeFormat.greeting().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        profile.fullName.substringBefore(" ("),
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "${profile.institutionName} • Year ${profile.yearOfStudy}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onMessages) {
                    Icon(Icons.Filled.ChatBubble, contentDescription = "Messages")
                }
                IconButton(onClick = onNotifications) {
                    BadgedBox(
                        badge = {
                            if (state.unreadCount > 0) {
                                Badge { Text(state.unreadCount.toString()) }
                            }
                        },
                    ) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (viewModel.demoMode) {
                    DemoModeBanner("Demo data mode. Connect Supabase and the secure backend to sync real campus content.")
                    Spacer(Modifier.height(14.dp))
                }
                CampusCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.size(42.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val announcement = state.notifications.firstOrNull {
                                    it.type == com.campusconnect.app.core.model.NotificationType.ANNOUNCEMENT
                                }
                                Text(
                                    announcement?.title ?: "Welcome to CampusConnect",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Text(
                                    announcement?.body ?: "Explore your campus dashboard, communities and opportunities.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            TextButton(onClick = onNotifications) { Text("View") }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickActionTile(Icons.Filled.ChatBubble, "Messages", onMessages, Modifier.weight(1f))
                QuickActionTile(Icons.Filled.Event, "Events", onEvents, Modifier.weight(1f))
                QuickActionTile(Icons.Filled.Map, "Map", onMap, Modifier.weight(1f))
                QuickActionTile(Icons.Filled.Apartment, "Housing", onAccommodation, Modifier.weight(1f))
                QuickActionTile(Icons.Filled.SmartToy, "AI guide", onAssistant, Modifier.weight(1f))
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SectionHeader("Recent community", actionLabel = "See all", onAction = onAllCommunity)
            }
        }
        if (state.posts.isEmpty()) {
            item { LoadingState("Loading community posts…") }
        } else {
            items(state.posts, key = { it.id }) { post ->
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    HomePostCard(post, onClick = { onPost(post.id) })
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionHeader("Upcoming events", actionLabel = "View all", onAction = onEvents)
            }
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.events, key = { it.id }) { event ->
                    EventMiniCard(event, onClick = onEvents)
                }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionHeader("Opportunities for you", actionLabel = "Browse", onAction = onAllOpportunities)
            }
        }
        items(state.opportunities, key = { it.id }) { opportunity ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                OpportunityMiniCard(opportunity, onClick = { onOpportunity(opportunity.id) })
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                SectionHeader("Marketplace highlights", actionLabel = "Shop", onAction = onAllMarketplace)
            }
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.listings, key = { it.id }) { listing ->
                    ListingMiniCard(listing, onClick = { onListing(listing.id) })
                }
            }
        }
    }
}

@Composable
private fun HomePostCard(post: Post, onClick: () -> Unit) {
    CampusCard(onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(post.authorName, size = 38.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(post.authorName, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${TimeFormat.relative(post.createdAt)} • ${post.communityName ?: "Campus feed"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                post.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
            if (post.imageUrl != null) {
                Spacer(Modifier.height(10.dp))
                RemoteImage(
                    url = post.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(14.dp)),
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("${post.likeCount} likes", style = MaterialTheme.typography.labelSmall)
                Text("${post.commentCount} comments", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun EventMiniCard(event: CampusEvent, onClick: () -> Unit) {
    CampusCard(modifier = Modifier.width(240.dp), onClick = onClick) {
        Column(modifier = Modifier.padding(14.dp)) {
            Pill(TimeFormat.dateTime(event.startAt))
            Spacer(Modifier.height(8.dp))
            Text(event.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(
                event.venue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Text("${event.rsvpCount} going", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun OpportunityMiniCard(opportunity: Opportunity, onClick: () -> Unit) {
    CampusCard(onClick = onClick) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(46.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.Work,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(opportunity.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${opportunity.organization} • ${opportunity.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(opportunity.type.name.lowercase().replaceFirstChar { it.uppercase() })
                    Pill(TimeFormat.countdown(opportunity.deadline))
                }
            }
        }
    }
}

@Composable
private fun ListingMiniCard(listing: Listing, onClick: () -> Unit) {
    CampusCard(modifier = Modifier.width(180.dp), onClick = onClick) {
        Column {
            RemoteImage(
                url = listing.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                icon = Icons.Filled.Work,
            )
            Column(modifier = Modifier.padding(12.dp)) {
                Text(listing.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(
                    Validators.formatKes(listing.priceKes),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    listing.sellerName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

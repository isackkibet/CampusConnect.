package com.campusconnect.app.features.marketplace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.ListingStatus
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.core.util.Validators
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.InfoRow
import com.campusconnect.app.ui.components.LoadingState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.RemoteImage
import com.campusconnect.app.ui.components.ReportDialog
import com.campusconnect.app.ui.components.SecondaryButton

@Composable
fun ListingDetailScreen(
    onBack: () -> Unit,
    onChat: (String) -> Unit,
    viewModel: ListingDetailViewModel = hiltViewModel(),
) {
    val listing by viewModel.listing.collectAsStateWithLifecycle()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    var reporting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        if (listing == null) {
            LoadingState("Loading listing…")
        } else {
            val item = listing ?: return@Column
            val own = item.sellerId == profile?.id
            CampusTopBar(
                title = "Listing",
                subtitle = item.categoryName,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleSaved() }) {
                        Icon(
                            if (item.saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Save listing",
                        )
                    }
                    if (!own) {
                        IconButton(onClick = { reporting = true }) {
                            Icon(Icons.Filled.Flag, contentDescription = "Report listing")
                        }
                    }
                },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                RemoteImage(
                    url = item.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f),
                    )
                    if (item.status != ListingStatus.ACTIVE) {
                        Pill(
                            when (item.status) {
                                ListingStatus.SOLD -> "Sold"
                                ListingStatus.PENDING_REVIEW -> "In review"
                                ListingStatus.REJECTED -> "Rejected"
                                else -> item.status.name
                            },
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    Validators.formatKes(item.priceKes),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(item.categoryName)
                    Pill(item.location)
                    Pill(TimeFormat.relative(item.createdAt))
                }
                Spacer(Modifier.height(16.dp))
                Text("Description", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                CampusCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Seller", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(item.sellerName, size = 46.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(item.sellerName, style = MaterialTheme.typography.titleSmall)
                                    if (item.sellerRating >= 4.5) {
                                        Spacer(Modifier.width(4.dp))
                                        Icon(
                                            Icons.Filled.Verified,
                                            contentDescription = "Trusted seller",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = com.campusconnect.app.core.design.Amber500,
                                        modifier = Modifier.size(15.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${item.sellerRating} seller rating",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        InfoRow("Status", if (item.status == ListingStatus.ACTIVE) "Available" else item.status.name.lowercase())
                        InfoRow("Safety", "Meet in a public campus area")
                    }
                }
                Spacer(Modifier.height(12.dp))
                DemoModeBanner("Payments are not processed in the app. Never send money before inspecting an item. M-PESA integration is server-side and disabled until credentials are configured.")
                Spacer(Modifier.height(20.dp))
                if (own) {
                    SecondaryButton(
                        text = "Mark as sold",
                        icon = Icons.Filled.Sell,
                        enabled = item.status != ListingStatus.SOLD,
                        onClick = { viewModel.markSold() },
                    )
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(
                        text = "Delete listing",
                        icon = Icons.Filled.Delete,
                        onClick = { viewModel.delete(onBack) },
                    )
                } else {
                    PrimaryButton(
                        text = "Contact seller",
                        icon = Icons.Filled.ChatBubble,
                        onClick = { onChat(viewModel.contactSeller(item)) },
                    )
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(
                        text = if (item.saved) "Remove from saved" else "Save listing",
                        icon = if (item.saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        onClick = { viewModel.toggleSaved() },
                    )
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (reporting) {
        ReportDialog(
            title = "Report listing",
            onDismiss = { reporting = false },
            onSubmit = { reason, details -> viewModel.report(reason, details) {} },
        )
    }
}

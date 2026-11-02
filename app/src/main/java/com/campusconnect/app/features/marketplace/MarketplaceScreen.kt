package com.campusconnect.app.features.marketplace

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.model.Listing
import com.campusconnect.app.core.model.ListingStatus
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.core.util.Validators
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.RemoteImage
import com.campusconnect.app.ui.components.ReportDialog
import com.campusconnect.app.ui.components.SearchField

@Composable
fun MarketplaceScreen(
    onOpenListing: (String) -> Unit,
    onCreateListing: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var reportTarget by remember { androidx.compose.runtime.mutableStateOf<Listing?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val filtered = remember(state.listings, state.query, state.category) {
        state.listings.filter { listing ->
            (state.category == "All" || listing.categoryName == state.category) &&
                (state.query.isBlank() ||
                    listing.title.contains(state.query, ignoreCase = true) ||
                    listing.description.contains(state.query, ignoreCase = true) ||
                    listing.sellerName.contains(state.query, ignoreCase = true))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CampusTopBar(title = "Marketplace", subtitle = "Buy and sell within campus")
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SearchField(state.query, viewModel::setQuery, "Search listings, categories and sellers")
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.category == "All",
                            onClick = { viewModel.setCategory("All") },
                            label = { Text("All") },
                        )
                    }
                    items(viewModel.categories, key = { it.id }) { category ->
                        FilterChip(
                            selected = state.category == category.name,
                            onClick = { viewModel.setCategory(category.name) },
                            label = { Text(category.name) },
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                EmptyState(
                    Icons.Filled.Storefront,
                    "Nothing here yet",
                    "Try another category or be the first to list an item.",
                    actionLabel = "Create listing",
                    onAction = onCreateListing,
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { listing ->
                        ListingRow(
                            listing = listing,
                            onOpen = { onOpenListing(listing.id) },
                            onToggleSaved = { viewModel.toggleSaved(listing.id) },
                            onReport = { reportTarget = listing },
                        )
                    }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onCreateListing,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            text = { Text("Sell") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        )
    }

    reportTarget?.let { listing ->
        ReportDialog(
            title = "Report listing",
            onDismiss = { reportTarget = null },
            onSubmit = { reason, details -> viewModel.report(listing.id, reason, details) },
        )
    }
}

@Composable
fun ListingRow(
    listing: Listing,
    onOpen: () -> Unit,
    onToggleSaved: () -> Unit,
    onReport: () -> Unit,
) {
    CampusCard(onClick = onOpen) {
        Row(modifier = Modifier.padding(12.dp)) {
            RemoteImage(
                url = listing.imageUrl,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(14.dp)),
                icon = Icons.Filled.Image,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        listing.title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onToggleSaved, modifier = Modifier.size(32.dp)) {
                        Icon(
                            if (listing.saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = if (listing.saved) "Remove saved listing" else "Save listing",
                            modifier = Modifier.size(19.dp),
                        )
                    }
                }
                Text(
                    Validators.formatKes(listing.priceKes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    "${listing.categoryName} • ${listing.location}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        listing.sellerName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    when (listing.status) {
                        ListingStatus.SOLD -> Pill("Sold")
                        ListingStatus.PENDING_REVIEW -> Pill("In review")
                        ListingStatus.REJECTED -> Pill("Rejected")
                        ListingStatus.ACTIVE -> Text(
                            TimeFormat.relative(listing.createdAt),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

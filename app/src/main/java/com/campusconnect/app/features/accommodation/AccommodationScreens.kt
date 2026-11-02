package com.campusconnect.app.features.accommodation

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import com.campusconnect.app.core.model.Accommodation
import com.campusconnect.app.core.util.Validators
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.InfoRow
import com.campusconnect.app.ui.components.LoadingState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.RemoteImage
import com.campusconnect.app.ui.components.ReportDialog
import com.campusconnect.app.ui.components.SearchField

@Composable
fun AccommodationScreen(
    onOpenListing: (String) -> Unit,
    viewModel: AccommodationViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var reportTarget by remember { mutableStateOf<Accommodation?>(null) }

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    val filtered = remember(state.listings, state.query, state.maxRent, state.availableOnly) {
        val availableOnly = state.availableOnly
        val maxRent = state.maxRent
        state.listings.filter { item ->
            (!availableOnly || item.available) &&
                (maxRent == null || item.rentKes <= maxRent) &&
                (state.query.isBlank() ||
                    item.title.contains(state.query, ignoreCase = true) ||
                    item.location.contains(state.query, ignoreCase = true))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(title = "Accommodation", subtitle = "Student housing near campus")
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            SearchField(state.query, viewModel::setQuery, "Search hostels and apartments")
            Spacer(Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val budgets = listOf(null to "Any rent", 8000 to "≤ 8k", 12_000 to "≤ 12k", 20_000 to "≤ 20k")
                items(budgets, key = { it.second }) { budget ->
                    FilterChip(
                        selected = state.maxRent == budget.first,
                        onClick = { viewModel.setMaxRent(budget.first) },
                        label = { Text(budget.second) },
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Available only", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                Switch(checked = state.availableOnly, onCheckedChange = viewModel::setAvailableOnly)
            }
        }
        if (filtered.isEmpty()) {
            EmptyState(
                Icons.Filled.HomeWork,
                "No housing matches",
                "Try a higher budget or clear your search.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.id }) { listing ->
                    AccommodationCard(
                        listing = listing,
                        onOpen = { onOpenListing(listing.id) },
                        onToggleSaved = { viewModel.toggleSaved(listing.id) },
                        onReport = { reportTarget = listing },
                    )
                }
            }
        }
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
private fun AccommodationCard(
    listing: Accommodation,
    onOpen: () -> Unit,
    onToggleSaved: () -> Unit,
    onReport: () -> Unit,
) {
    CampusCard(onClick = onOpen) {
        Column(modifier = Modifier.padding(12.dp)) {
            RemoteImage(
                url = listing.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp)),
            )
            Spacer(Modifier.height(10.dp))
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
                        contentDescription = "Save accommodation",
                        modifier = Modifier.size(19.dp),
                    )
                }
            }
            Text(
                "${Validators.formatKes(listing.rentKes)} / month",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "${listing.location} • ${listing.distanceFromCampus}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "${listing.bedrooms} bed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (listing.verified) Pill("Verified", color = MaterialTheme.colorScheme.secondaryContainer)
                if (!listing.available) Pill("Taken") else if (listing.verified) Pill("Available")
            }
        }
    }
}

@Composable
fun AccommodationDetailScreen(
    onBack: () -> Unit,
    viewModel: AccommodationDetailViewModel = hiltViewModel(),
) {
    val listing by viewModel.listing.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var reporting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        if (listing == null) {
            LoadingState("Loading accommodation…")
        } else {
            val item = listing ?: return@Column
            CampusTopBar(
                title = "Accommodation",
                subtitle = item.location,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { reporting = true }) {
                        Icon(Icons.Filled.Flag, contentDescription = "Report listing")
                    }
                },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
            ) {
                RemoteImage(
                    url = item.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(230.dp)
                        .clip(RoundedCornerShape(20.dp)),
                )
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                    if (item.verified) {
                        Icon(
                            Icons.Filled.Verified,
                            contentDescription = "Verified listing",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "${Validators.formatKes(item.rentKes)} / month",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(10.dp))
                CampusCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        InfoRow("Distance", item.distanceFromCampus)
                        InfoRow("Bedrooms", item.bedrooms.toString())
                        InfoRow("Availability", if (item.available) "Available now" else "Currently taken")
                        InfoRow("Landlord", item.landlordName)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("About this place", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(18.dp))
                PrimaryButton(
                    text = "Call landlord",
                    icon = Icons.Filled.Phone,
                    enabled = item.available,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.landlordPhone}"))
                        context.startActivity(intent)
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Always inspect the property in person and never pay a deposit before signing an agreement.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }

    if (reporting) {
        ReportDialog(
            title = "Report accommodation",
            onDismiss = { reporting = false },
            onSubmit = { reason, details ->
                viewModel.report(reason, details)
                Toast.makeText(context, "Report submitted for review", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

package com.campusconnect.app.features.profile

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.core.util.Validators
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.ChipRow
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.Pill

@Composable
fun SavedScreen(
    onBack: () -> Unit,
    onOpenListing: (String) -> Unit,
    onOpenOpportunity: (String) -> Unit,
    onOpenAccommodation: (String) -> Unit,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf("Listings") }
    val tabs = listOf("Listings", "Opportunities", "Rooms")

    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(title = "Saved items", onBack = onBack)
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            ChipRow(tabs, tab, { tab = it })
        }
        if (tab == "Listings" && state.listings.isEmpty()) {
            EmptyState(Icons.Filled.Bookmark, "No saved listings", "Tap the bookmark on a marketplace listing to save it here.")
        } else if (tab == "Opportunities" && state.opportunities.isEmpty()) {
            EmptyState(Icons.Filled.Work, "No saved opportunities", "Save jobs, internships and attachments to review them later.")
        } else if (tab == "Rooms" && state.rooms.isEmpty()) {
            EmptyState(Icons.Filled.Bookmark, "No saved rooms", "Save accommodation listings to compare them later.")
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                when (tab) {
                    "Listings" -> items(state.listings, key = { it.id }) { listing ->
                        CampusCard(onClick = { onOpenListing(listing.id) }) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(listing.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${Validators.formatKes(listing.priceKes)} • ${listing.categoryName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(onClick = { viewModel.removeListingSaved(listing.id) }) { Text("Remove") }
                            }
                        }
                    }
                    "Opportunities" -> items(state.opportunities, key = { it.id }) { opportunity ->
                        CampusCard(onClick = { onOpenOpportunity(opportunity.id) }) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(opportunity.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${opportunity.organization} • ${TimeFormat.countdown(opportunity.deadline)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(onClick = { viewModel.removeOpportunitySaved(opportunity.id) }) { Text("Remove") }
                            }
                        }
                    }
                    else -> items(state.rooms, key = { it.id }) { room ->
                        CampusCard(onClick = { onOpenAccommodation(room.id) }) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(room.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        "${Validators.formatKes(room.rentKes)} / month • ${room.location}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                TextButton(onClick = { viewModel.removeRoomSaved(room.id) }) { Text("Remove") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ApplicationsScreen(
    onBack: () -> Unit,
    viewModel: SavedViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(title = "My applications", subtitle = "${state.applications.size} submitted", onBack = onBack)
        if (state.applications.isEmpty()) {
            EmptyState(
                Icons.Filled.Description,
                "No applications yet",
                "When you apply to an opportunity it is tracked here.",
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(state.applications, key = { it.id }) { application ->
                    CampusCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    application.opportunityTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f),
                                )
                                Pill(application.status)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                application.organization,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Applied ${TimeFormat.date(application.appliedAt)}",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

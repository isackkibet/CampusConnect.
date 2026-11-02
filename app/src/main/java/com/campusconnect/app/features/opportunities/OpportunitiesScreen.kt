package com.campusconnect.app.features.opportunities

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.model.Opportunity
import com.campusconnect.app.core.model.OpportunityType
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.SearchField

@Composable
fun OpportunitiesScreen(
    onOpenOpportunity: (String) -> Unit,
    onCreateOpportunity: () -> Unit,
    viewModel: OpportunitiesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        state.message?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    val filtered = remember(state.opportunities, state.query, state.type) {
        state.opportunities.filter { opportunity ->
            (state.type == null || opportunity.type == state.type) &&
                (state.query.isBlank() ||
                    opportunity.title.contains(state.query, ignoreCase = true) ||
                    opportunity.organization.contains(state.query, ignoreCase = true) ||
                    opportunity.location.contains(state.query, ignoreCase = true))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            CampusTopBar(title = "Opportunities", subtitle = "Jobs, internships, attachments and gigs")
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SearchField(state.query, viewModel::setQuery, "Search roles and organizations")
                Spacer(Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = state.type == null,
                            onClick = { viewModel.setType(null) },
                            label = { Text("All") },
                        )
                    }
                    items(OpportunityType.entries.toList(), key = { it.name }) { type ->
                        FilterChip(
                            selected = state.type == type,
                            onClick = { viewModel.setType(type) },
                            label = { Text(type.label()) },
                        )
                    }
                }
            }
            if (filtered.isEmpty()) {
                EmptyState(
                    Icons.Filled.Work,
                    "No opportunities match",
                    "Adjust your filters or check back soon.",
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { opportunity ->
                        OpportunityCard(
                            opportunity = opportunity,
                            onOpen = { onOpenOpportunity(opportunity.id) },
                            onToggleSaved = { viewModel.toggleSaved(opportunity.id) },
                            onApply = { viewModel.apply(opportunity) },
                        )
                    }
                }
            }
        }
        if (state.canPublish) {
            ExtendedFloatingActionButton(
                onClick = onCreateOpportunity,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Publish") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
            )
        }
    }
}

@Composable
private fun OpportunityCard(
    opportunity: Opportunity,
    onOpen: () -> Unit,
    onToggleSaved: () -> Unit,
    onApply: () -> Unit,
) {
    CampusCard(onClick = onOpen) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        opportunity.title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        opportunity.organization,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onToggleSaved) {
                    Icon(
                        if (opportunity.saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = "Save opportunity",
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Pill(opportunity.type.label())
                if (opportunity.remote) Pill("Remote")
                if (opportunity.applied) Pill("Applied", color = MaterialTheme.colorScheme.secondaryContainer)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.width(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    opportunity.location,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.width(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "Closes ${TimeFormat.relative(opportunity.deadline)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    opportunity.stipend,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                if (opportunity.applied) {
                    Text(
                        opportunity.applicationStatus ?: "Submitted",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                } else {
                    androidx.compose.material3.TextButton(onClick = onApply) { Text("Quick apply") }
                }
            }
        }
    }
}

fun OpportunityType.label(): String = when (this) {
    OpportunityType.JOB -> "Job"
    OpportunityType.INTERNSHIP -> "Internship"
    OpportunityType.ATTACHMENT -> "Attachment"
    OpportunityType.GIG -> "Gig"
}

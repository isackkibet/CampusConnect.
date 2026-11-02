package com.campusconnect.app.features.opportunities

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.InfoRow
import com.campusconnect.app.ui.components.LoadingState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.SecondaryButton

@Composable
fun OpportunityDetailScreen(
    onBack: () -> Unit,
    viewModel: OpportunityDetailViewModel = hiltViewModel(),
) {
    val opportunity by viewModel.opportunity.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        if (opportunity == null) {
            LoadingState("Loading opportunity…")
        } else {
            val item = opportunity ?: return@Column
            CampusTopBar(
                title = "Opportunity",
                subtitle = item.organization,
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.toggleSaved() }) {
                        Icon(
                            if (item.saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Save opportunity",
                        )
                    }
                },
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Pill(item.type.label())
                    if (item.remote) Pill("Remote")
                    if (item.applied) Pill("Applied", color = MaterialTheme.colorScheme.secondaryContainer)
                }
                Spacer(Modifier.height(10.dp))
                Text(item.title, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Verified,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.size(4.dp))
                    Text(item.organization, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.weight(1f))
                    Text(
                        item.stipend,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(14.dp))
                CampusCard {
                    Column(modifier = Modifier.padding(14.dp)) {
                        InfoRow("Location", item.location)
                        InfoRow("Deadline", TimeFormat.dateTime(item.deadline))
                        InfoRow("Closes", TimeFormat.countdown(item.deadline))
                        InfoRow("Eligibility", item.eligibility)
                        InfoRow("Posted", TimeFormat.relative(item.postedAt))
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("About this opportunity", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(item.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                DemoModeBanner("Applications are stored locally in this demo build. When Supabase is configured, applications sync to your account and the employer receives them via the backend.")
                Spacer(Modifier.height(20.dp))
                if (item.applied) {
                    SecondaryButton(
                        text = "Applied · ${item.applicationStatus ?: "Submitted"}",
                        icon = Icons.Filled.CheckCircle,
                        enabled = false,
                        onClick = {},
                    )
                } else {
                    PrimaryButton(
                        text = "Apply now",
                        onClick = { viewModel.apply(item) {} },
                    )
                }
                Spacer(Modifier.height(8.dp))
                SecondaryButton(
                    text = if (item.saved) "Remove from saved" else "Save opportunity",
                    icon = if (item.saved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    onClick = { viewModel.toggleSaved() },
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

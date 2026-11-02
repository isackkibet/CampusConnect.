package com.campusconnect.app.features.events

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.campusconnect.app.core.data.repo.EventRepository
import com.campusconnect.app.core.model.CampusEvent
import com.campusconnect.app.core.util.TimeFormat
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.EmptyState
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.RemoteImage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EventsUiState(
    val events: List<CampusEvent> = emptyList(),
    val showPast: Boolean = false,
)

@HiltViewModel
class EventsViewModel @Inject constructor(
    private val repository: EventRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(EventsUiState())
    val state: StateFlow<EventsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.events.collect { events ->
                _state.update { it.copy(events = events) }
            }
        }
    }

    fun setShowPast(showPast: Boolean) = _state.update { it.copy(showPast = showPast) }
    fun toggleRsvp(id: String) = repository.toggleRsvp(id)
    fun toggleReminder(id: String) = repository.toggleReminder(id)
}

@Composable
fun EventsScreen(
    onBack: () -> Unit,
    viewModel: EventsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val now = remember { System.currentTimeMillis() }
    val filtered = state.events
        .filter { if (state.showPast) it.startAt < now else it.startAt >= now }
        .sortedBy { it.startAt }

    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(title = "Events", subtitle = "What's happening on campus", onBack = onBack)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = !state.showPast,
                    onClick = { viewModel.setShowPast(false) },
                    label = { Text("Upcoming") },
                )
            }
            item {
                FilterChip(
                    selected = state.showPast,
                    onClick = { viewModel.setShowPast(true) },
                    label = { Text("Past") },
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (filtered.isEmpty()) {
            EmptyState(
                Icons.Filled.Event,
                if (state.showPast) "No past events" else "No upcoming events",
                "Check back later for campus activities.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filtered, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        past = state.showPast,
                        onToggleRsvp = { viewModel.toggleRsvp(event.id) },
                        onToggleReminder = { viewModel.toggleReminder(event.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCard(
    event: CampusEvent,
    past: Boolean,
    onToggleRsvp: () -> Unit,
    onToggleReminder: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    CampusCard(onClick = { expanded = !expanded }) {
        Column(modifier = Modifier.padding(12.dp)) {
            RemoteImage(
                url = event.imageUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(14.dp)),
                icon = Icons.Filled.Event,
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (event.rsvped) Pill("Going", color = MaterialTheme.colorScheme.secondaryContainer)
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    TimeFormat.dateTime(event.startAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (!past) {
                    Text(
                        TimeFormat.countdown(event.startAt),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "${event.venue} • ${event.organizer}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                Text(event.description, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${event.rsvpCount} students going",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (!past) {
                    PrimaryButton(
                        text = if (event.rsvped) "Cancel RSVP" else "RSVP",
                        icon = if (event.rsvped) Icons.Filled.Check else Icons.Filled.Event,
                        onClick = onToggleRsvp,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onToggleReminder) {
                        Icon(
                            if (event.reminder) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsNone,
                            contentDescription = if (event.reminder) "Disable reminder" else "Enable reminder",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

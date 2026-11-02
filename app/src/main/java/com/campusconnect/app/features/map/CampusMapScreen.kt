package com.campusconnect.app.features.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.data.repo.MapRepository
import com.campusconnect.app.core.model.CampusPointOfInterest
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.Pill
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MapUiState(
    val category: String? = null,
    val selected: CampusPointOfInterest? = null,
)

@HiltViewModel
class CampusMapViewModel @Inject constructor(
    private val repository: MapRepository,
) : ViewModel() {

    val pointsOfInterest: List<CampusPointOfInterest> = repository.pointsOfInterest
    val mapsConfigured: Boolean = repository.mapsConfigured
    val center: Pair<Double, Double> = repository.approximateCenter
    val categories: List<String> = pointsOfInterest.map { it.category }.distinct().sorted()

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    fun setCategory(category: String?) = _state.update { it.copy(category = category) }
    fun select(poi: CampusPointOfInterest?) = _state.update { it.copy(selected = poi) }
}

@Composable
fun CampusMapScreen(
    onBack: () -> Unit,
    viewModel: CampusMapViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val points = remember(state.category, viewModel.pointsOfInterest) {
        viewModel.pointsOfInterest.filter { state.category == null || it.category == state.category }
    }
    val minLat = remember(viewModel.pointsOfInterest) { viewModel.pointsOfInterest.minOf { it.latitude } }
    val maxLat = remember(viewModel.pointsOfInterest) { viewModel.pointsOfInterest.maxOf { it.latitude } }
    val minLon = remember(viewModel.pointsOfInterest) { viewModel.pointsOfInterest.minOf { it.longitude } }
    val maxLon = remember(viewModel.pointsOfInterest) { viewModel.pointsOfInterest.maxOf { it.longitude } }

    fun openExternally(poi: CampusPointOfInterest) {
        val uri = Uri.parse("geo:${poi.latitude},${poi.longitude}?q=${Uri.encode(poi.name)}")
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item {
            CampusTopBar(title = "Campus map", subtitle = "Find facilities and services", onBack = onBack)
        }
        if (!viewModel.mapsConfigured) {
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    DemoModeBanner(
                        "Showing a schematic view based on sample coordinates. Add MAPS_API_KEY to enable live Google Maps tiles.",
                    )
                }
            }
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = state.category == null,
                        onClick = { viewModel.setCategory(null) },
                        label = { Text("All") },
                    )
                }
                items(viewModel.categories, key = { it }) { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { viewModel.setCategory(category) },
                        label = { Text(category) },
                    )
                }
            }
        }
        item {
            CampusCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                ) {
                    val width = maxWidth
                    val height = maxHeight
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val steps = 6
                        for (i in 1 until steps) {
                            val x = size.width * i / steps
                            val y = size.height * i / steps
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.25f),
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 2f,
                            )
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.25f),
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 2f,
                            )
                        }
                        drawCircle(
                            color = Color(0xFF0F766E).copy(alpha = 0.10f),
                            radius = size.minDimension * 0.42f,
                            center = Offset(size.width * 0.5f, size.height * 0.5f),
                        )
                    }
                    points.forEach { poi ->
                        val fx = if (maxLon == minLon) 0.5 else (poi.longitude - minLon) / (maxLon - minLon)
                        val fy = if (maxLat == minLat) 0.5 else (maxLat - poi.latitude) / (maxLat - minLat)
                        val markerSize = 34.dp
                        val horizontal = (width - markerSize) * fx.toFloat()
                        val vertical = (height - markerSize) * fy.toFloat()
                        val selected = state.selected?.id == poi.id
                        Surface(
                            modifier = Modifier
                                .offset(x = horizontal, y = vertical)
                                .size(markerSize)
                                .clickable { viewModel.select(poi) },
                            shape = CircleShape,
                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            shadowElevation = 3.dp,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary,
                            ),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = poi.name,
                                    modifier = Modifier.size(19.dp),
                                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(10.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Approximate campus area", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
        state.selected?.let { poi ->
            item {
                CampusCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(poi.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Pill(poi.category)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(poi.description, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { openExternally(poi) },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Directions in Google Maps", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
        item {
            Text(
                "Points of interest",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 4.dp),
            )
        }
        items(points, key = { it.id }) { poi ->
            CampusCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                onClick = { viewModel.select(poi) },
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Filled.Map,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(19.dp),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(poi.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            poi.description,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Pill(poi.category)
                }
            }
        }
    }
}

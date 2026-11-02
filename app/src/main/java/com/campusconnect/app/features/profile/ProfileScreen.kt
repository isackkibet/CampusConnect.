package com.campusconnect.app.features.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusCard
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.Pill
import com.campusconnect.app.ui.components.SecondaryButton

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onMessages: () -> Unit,
    onNotifications: () -> Unit,
    onNotificationPreferences: () -> Unit,
    onApplications: () -> Unit,
    onSaved: () -> Unit,
    onEvents: () -> Unit,
    onMap: () -> Unit,
    onAccommodation: () -> Unit,
    onAssistant: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val applications by viewModel.applications.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showAbout by remember { mutableStateOf(false) }
    val profile = session.profile

    Column(modifier = Modifier.fillMaxSize()) {
        CampusTopBar(title = "Profile", subtitle = "Your campus identity")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                CampusCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Avatar(profile?.fullName ?: "Campus Student", size = 72.dp)
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        profile?.fullName ?: "Campus Student",
                                        style = MaterialTheme.typography.titleLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (profile?.verified == true) {
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            Icons.Filled.Verified,
                                            contentDescription = "Verified",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    profile?.let { "${it.department} • Year ${it.yearOfStudy}" } ?: "Profile not set up",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Pill(profile?.role?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Student")
                                    Pill(profile?.institutionName ?: "NNP")
                                }
                            }
                        }
                        if (!profile?.bio.isNullOrBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Text(profile?.bio ?: "", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            StatColumn(profile?.followers ?: 0, "Followers")
                            StatColumn(profile?.following ?: 0, "Following")
                            StatColumn(applications.size, "Applications")
                        }
                        Spacer(Modifier.height(16.dp))
                        SecondaryButton(
                            text = "Edit profile",
                            icon = Icons.Filled.Edit,
                            onClick = onEditProfile,
                        )
                    }
                }
            }

            if (viewModel.demoMode) {
                item {
                    DemoModeBanner("Demo mode: profile changes are stored on this device only. Connect Supabase for cross-device sync.")
                }
            }

            item {
                SectionTitle("My activity")
                MenuCard {
                    MenuRow(Icons.Filled.Description, "My applications", "${applications.size} application(s)", onApplications)
                    MenuRow(Icons.Filled.Bookmark, "Saved items", "Listings, opportunities and rooms", onSaved)
                    MenuRow(Icons.Filled.ChatBubble, "Messages", "Conversations and groups", onMessages)
                    MenuRow(Icons.Filled.Notifications, "Notifications", "Activity and alerts", onNotifications)
                    MenuRow(Icons.Filled.Settings, "Notification preferences", "Choose what you receive", onNotificationPreferences)
                }
            }

            item {
                SectionTitle("Campus services")
                MenuCard {
                    MenuRow(Icons.Filled.Event, "Events", "Clubs, fairs and activities", onEvents)
                    MenuRow(Icons.Filled.Map, "Campus map", "Find halls, library and services", onMap)
                    MenuRow(Icons.Filled.Apartment, "Accommodation", "Hostels and rooms", onAccommodation)
                    MenuRow(Icons.Filled.SmartToy, "AI campus guide", "Ask questions in the app", onAssistant)
                }
            }

            item {
                SectionTitle("Support and administration")
                MenuCard {
                    MenuRow(
                        Icons.Filled.OpenInNew,
                        "Admin dashboard",
                        "Web portal for staff",
                        onClick = {
                            val url = viewModel.adminBaseUrl
                            if (url.startsWith("http") && !url.contains("example.invalid")) {
                                runCatching {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                }.onFailure {
                                    Toast.makeText(context, "Could not open the admin dashboard", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Set ADMIN_BASE_URL in local.properties after deploying the admin app.",
                                    Toast.LENGTH_LONG,
                                ).show()
                            }
                        },
                    )
                    MenuRow(Icons.Filled.Info, "About CampusConnect", "Version and build info", onClick = { showAbout = true })
                    MenuRow(
                        Icons.Filled.Logout,
                        "Sign out",
                        null,
                        isDestructive = true,
                        onClick = { viewModel.signOut() },
                    )
                }
            }
        }
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            title = { Text("CampusConnect") },
            text = {
                Text(
                    "Student super-app for Nairobi National Polytechnic.\n\n" +
                        "Version 1.0.0\n" +
                        "Build: ${if (viewModel.demoMode) "Local demo" else "Connected"}\n\n" +
                        "External integrations (Supabase, Stream, Google Maps, Groq, M-PESA, Firebase) " +
                        "are only active when credentials are configured by the operator. " +
                        "Sample data is clearly labelled and is not official institute information.",
                )
            },
            confirmButton = {
                Button(onClick = { showAbout = false }, shape = RoundedCornerShape(12.dp)) { Text("Close") }
            },
        )
    }
}

@Composable
private fun StatColumn(value: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
    )
}

@Composable
private fun MenuCard(content: @Composable () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column { content() }
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDestructive) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(38.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (!isDestructive) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

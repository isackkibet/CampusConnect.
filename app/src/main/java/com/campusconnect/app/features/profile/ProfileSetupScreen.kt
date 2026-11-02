package com.campusconnect.app.features.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.campusconnect.app.core.data.DemoData
import com.campusconnect.app.ui.components.Avatar
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.TextFieldSupportError

@Composable
fun ProfileSetupScreen(
    isEdit: Boolean,
    onDone: () -> Unit,
    onBack: (() -> Unit)?,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val profile = session.profile

    var fullName by remember { mutableStateOf(profile?.fullName ?: "") }
    var department by remember { mutableStateOf(profile?.department ?: "") }
    var course by remember { mutableStateOf(profile?.course ?: "") }
    var year by remember { mutableIntStateOf(profile?.yearOfStudy ?: 1) }
    var bio by remember { mutableStateOf(profile?.bio ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var avatarUrl by remember { mutableStateOf(profile?.avatarUrl) }

    LaunchedEffect(profile?.id, profile?.department) {
        if (profile != null && department.isBlank()) {
            fullName = profile.fullName
            department = profile.department
            course = profile.course
            year = profile.yearOfStudy
            bio = profile.bio
            phone = profile.phone
            avatarUrl = profile.avatarUrl
        }
    }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) avatarUrl = uri.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        CampusTopBar(
            title = if (isEdit) "Edit profile" else "Set up your profile",
            subtitle = if (isEdit) "Keep your details current" else "Tell classmates who you are",
            onBack = if (isEdit) onBack else null,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            if (!isEdit) {
                DemoModeBanner("Institution preview: Nairobi National Polytechnic. Departments, courses and years below are sample options, not official records.")
                Spacer(Modifier.height(16.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    if (avatarUrl != null) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(48.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Avatar(profile?.fullName ?: fullName.ifBlank { "Campus Student" }, size = 96.dp)
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(34.dp),
                    ) {
                        IconButton(onClick = { photoPicker.launch("image/*") }) {
                            Icon(
                                Icons.Filled.AddAPhoto,
                                contentDescription = "Add profile photo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { photoPicker.launch("image/*") }) { Text("Choose photo (optional)") }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; viewModel.clearEditState() },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.School, contentDescription = null)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Institution", style = MaterialTheme.typography.labelSmall)
                        Text(DemoData.institutions.first().name, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            SelectionField(
                label = "Department",
                value = department,
                options = DemoData.departments.map { it.name },
                onSelect = {
                    department = it
                    viewModel.clearEditState()
                },
            )
            Spacer(Modifier.height(12.dp))
            SelectionField(
                label = "Course",
                value = course,
                options = DemoData.courses.map { it.name },
                onSelect = {
                    course = it
                    viewModel.clearEditState()
                },
            )

            Spacer(Modifier.height(16.dp))
            Text("Year of study", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..4).forEach { option ->
                    FilterChip(
                        selected = year == option,
                        onClick = { year = option; viewModel.clearEditState() },
                        label = { Text("Year $option") },
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it.take(280); viewModel.clearEditState() },
                label = { Text("Bio") },
                placeholder = { Text("A short introduction for your profile") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                supportingText = { Text("${bio.length}/280") },
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it.take(15); viewModel.clearEditState() },
                label = { Text("Phone (optional, private)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = RoundedCornerShape(14.dp),
            )
            TextFieldSupportError(editState.error)
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = if (isEdit) "Save changes" else "Finish setup",
                loading = editState.saving,
                icon = Icons.Filled.Check,
                onClick = {
                    viewModel.saveProfile(fullName, department, course, year, bio, phone, avatarUrl, onDone)
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SelectionField(
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { open = true },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    value.ifBlank { "Select $label" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                )
            }
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
    }
    if (open) {
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("Select $label") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(option)
                                    open = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(option, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            if (option == value) {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { open = false }) { Text("Close") } },
        )
    }
}

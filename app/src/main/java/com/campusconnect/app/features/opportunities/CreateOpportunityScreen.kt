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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.campusconnect.app.core.model.OpportunityType
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.TextFieldSupportError

@Composable
fun CreateOpportunityScreen(
    onBack: () -> Unit,
    onPublished: () -> Unit,
    viewModel: OpportunitiesViewModel = hiltViewModel(),
) {
    var title by remember { mutableStateOf("") }
    var organization by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(OpportunityType.JOB) }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var stipend by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("14") }
    var remote by remember { mutableStateOf(false) }
    var pickType by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        CampusTopBar(title = "Publish opportunity", subtitle = "Staff only", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            DemoModeBanner("Publishing requires a staff account (admin or moderator). In demo mode this is enforced locally; Supabase RLS enforces it in production.")
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(120); error = null },
                label = { Text("Role title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = organization,
                onValueChange = { organization = it.take(80); error = null },
                label = { Text("Organization") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            AssistChip(onClick = { pickType = true }, label = { Text("Type: ${type.label()}") })
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(3000); error = null },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it.take(80); error = null },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = stipend,
                onValueChange = { stipend = it.take(60); error = null },
                label = { Text("Stipend / salary") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = days,
                onValueChange = { days = it.filter(Char::isDigit).take(3); error = null },
                label = { Text("Deadline (days from today)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = remote, onCheckedChange = { remote = it })
                Text("Remote friendly", style = MaterialTheme.typography.bodyMedium)
            }
            TextFieldSupportError(error)
            Spacer(Modifier.height(16.dp))
            PrimaryButton(
                text = "Publish",
                icon = Icons.Filled.Publish,
                onClick = {
                    val dayCount = days.toIntOrNull()
                    if (title.trim().length < 4) error = "Enter a role title"
                    else if (organization.trim().length < 2) error = "Enter the organization"
                    else if (description.trim().length < 20) error = "Add a fuller description"
                    else if (location.trim().length < 2) error = "Enter a location"
                    else if (stipend.trim().isEmpty()) error = "Enter the stipend or salary"
                    else if (dayCount == null || dayCount !in 1..180) error = "Deadline must be 1–180 days away"
                    else {
                        viewModel.publish(
                            title = title,
                            organization = organization,
                            type = type,
                            description = description,
                            location = location,
                            deadline = System.currentTimeMillis() + dayCount * 86_400_000L,
                            stipend = stipend,
                            remote = remote,
                        ) { onPublished() }
                    }
                },
            )
            Spacer(Modifier.height(28.dp))
        }
    }

    if (pickType) {
        AlertDialog(
            onDismissRequest = { pickType = false },
            title = { Text("Opportunity type") },
            text = {
                Column {
                    OpportunityType.entries.forEach { option ->
                        TextButton(
                            onClick = {
                                type = option
                                pickType = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                Text(option.label())
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickType = false }) { Text("Cancel") } },
        )
    }
}

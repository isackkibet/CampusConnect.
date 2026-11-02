package com.campusconnect.app.features.marketplace

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.TextFieldSupportError

@Composable
fun CreateListingScreen(
    onBack: () -> Unit,
    onCreated: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(viewModel.categories.first()) }
    var pickCategory by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUrl = uri.toString()
    }

    LaunchedEffect(state.message) {
        if (state.message == "Listing submitted for moderation") {
            viewModel.clearMessage()
            onCreated()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        CampusTopBar(title = "Create a listing", subtitle = "Sell to your campus", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            DemoModeBanner("New listings enter a local moderation queue and become active after a few seconds. No payment is collected in the app.")
            Spacer(Modifier.height(12.dp))
            if (imageUrl != null) {
                Box {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Listing photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    IconButton(onClick = { imageUrl = null }) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove photo")
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            OutlinedButton(onClick = { picker.launch("image/*") }, shape = RoundedCornerShape(14.dp)) {
                Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(if (imageUrl == null) "Add product photo" else "Change photo")
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(120); localError = null },
                label = { Text("Listing title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it.take(1500); localError = null },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter(Char::isDigit).take(8); localError = null },
                label = { Text("Price (KES)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
            )
            Spacer(Modifier.height(12.dp))
            AssistChip(
                onClick = { pickCategory = true },
                label = { Text("Category: ${category.name}") },
            )
            Spacer(Modifier.height(8.dp))
            if (state.message != null && state.message != "Listing submitted for moderation") {
                TextFieldSupportError(state.message)
            }
            TextFieldSupportError(localError)
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Submit listing",
                icon = Icons.Filled.Check,
                onClick = {
                    if (title.trim().length < 4) {
                        localError = "Title must be at least 4 characters"
                    } else if (description.trim().length < 10) {
                        localError = "Add a short description"
                    } else if (price.toIntOrNull() == null || price.toInt() <= 0) {
                        localError = "Enter a valid price in KES"
                    } else {
                        viewModel.createListing(
                            title, description, price, category.id, category.name, imageUrl,
                        ) { onCreated() }
                    }
                },
            )
            Spacer(Modifier.height(28.dp))
        }
    }

    if (pickCategory) {
        AlertDialog(
            onDismissRequest = { pickCategory = false },
            title = { Text("Category") },
            text = {
                Column {
                    viewModel.categories.forEach { option ->
                        TextButton(
                            onClick = {
                                category = option
                                pickCategory = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                                Text(option.name)
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pickCategory = false }) { Text("Cancel") } },
        )
    }
}

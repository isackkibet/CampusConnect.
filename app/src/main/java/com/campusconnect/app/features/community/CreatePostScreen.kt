package com.campusconnect.app.features.community

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.campusconnect.app.ui.components.CampusTopBar
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.TextFieldSupportError

@Composable
fun CreatePostScreen(
    onBack: () -> Unit,
    onPosted: () -> Unit,
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var content by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var community by remember { mutableStateOf<String?>(null) }
    var pickCommunity by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUrl = uri.toString()
    }

    LaunchedEffect(state.message) {
        if (state.message == "Post published") {
            viewModel.clearMessage()
            onPosted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        CampusTopBar(title = "Create post", subtitle = "Share with campus", onBack = onBack)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it.take(2000)
                    localError = null
                    viewModel.clearMessage()
                },
                placeholder = { Text("What is happening on campus?") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(16.dp),
                supportingText = { Text("${content.length}/2000") },
            )

            if (imageUrl != null) {
                Spacer(Modifier.height(12.dp))
                Box {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = "Attached image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                    )
                    IconButton(
                        onClick = { imageUrl = null },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove image")
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { picker.launch("image/*") },
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (imageUrl == null) "Add photo" else "Change photo")
                }
                AssistChip(
                    onClick = { pickCommunity = true },
                    label = { Text(community ?: "Campus feed") },
                )
            }

            if (state.message != null && state.message != "Post published") {
                TextFieldSupportError(state.message)
            }
            TextFieldSupportError(localError)
            Spacer(Modifier.height(20.dp))
            PrimaryButton(
                text = "Publish post",
                icon = Icons.Filled.Check,
                onClick = {
                    if (content.isBlank()) {
                        localError = "Write something before posting"
                    } else {
                        viewModel.createPost(content, imageUrl, community) { onPosted() }
                    }
                },
            )
            Spacer(Modifier.height(24.dp))
        }
    }

    if (pickCommunity) {
        val options = listOf("Campus feed", "ICT Students Hub", "Year 1 Class Group", "Robotics & Innovation Club", "Campus Entrepreneurs")
        AlertDialog(
            onDismissRequest = { pickCommunity = false },
            title = { Text("Post to") },
            text = {
                Column {
                    options.forEach { option ->
                        TextButton(
                            onClick = {
                                community = if (option == "Campus feed") null else option
                                pickCommunity = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(option, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pickCommunity = false }) { Text("Cancel") }
            },
        )
    }
}

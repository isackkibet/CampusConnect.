package com.campusconnect.app.features.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.core.design.Teal400
import com.campusconnect.app.core.design.Teal500
import com.campusconnect.app.features.auth.AuthViewModel
import com.campusconnect.app.ui.components.PrimaryButton
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String,
)

private val pages = listOf(
    OnboardingPage(
        Icons.Filled.Groups,
        "Your campus, connected",
        "Follow classmates, join department communities and keep up with everything happening on campus.",
    ),
    OnboardingPage(
        Icons.Filled.Work,
        "Opportunities that fit",
        "Find jobs, internships, attachments and gigs - then track every application in one place.",
    ),
    OnboardingPage(
        Icons.Filled.School,
        "Everything student life",
        "Marketplace, accommodation, events, maps, messaging and an AI campus guide. Built for Kenyan campuses.",
    ),
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                ),
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = {
                    viewModel.completeOnboarding()
                    onFinished()
                }) { Text("Skip") }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
            ) { index ->
                val page = pages[index]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(140.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                page.icon,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                    Spacer(Modifier.height(36.dp))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        page.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (pagerState.currentPage == index) 22.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index) Teal500
                                else MaterialTheme.colorScheme.outlineVariant,
                            ),
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                PrimaryButton(
                    text = if (pagerState.currentPage == pages.lastIndex) "Get started" else "Continue",
                    onClick = {
                        if (pagerState.currentPage == pages.lastIndex) {
                            viewModel.completeOnboarding()
                            onFinished()
                        } else {
                            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }
                    },
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Nairobi National Polytechnic • More institutions coming soon",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

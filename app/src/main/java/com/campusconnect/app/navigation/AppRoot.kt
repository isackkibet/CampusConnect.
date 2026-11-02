package com.campusconnect.app.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.campusconnect.app.core.model.NotificationType
import com.campusconnect.app.features.accommodation.AccommodationDetailScreen
import com.campusconnect.app.features.accommodation.AccommodationScreen
import com.campusconnect.app.features.assistant.AssistantScreen
import com.campusconnect.app.features.auth.ForgotPasswordScreen
import com.campusconnect.app.features.auth.LoginScreen
import com.campusconnect.app.features.auth.RegisterScreen
import com.campusconnect.app.features.auth.VerifyEmailScreen
import com.campusconnect.app.features.community.CommunityScreen
import com.campusconnect.app.features.community.CreatePostScreen
import com.campusconnect.app.features.community.PostDetailScreen
import com.campusconnect.app.features.events.EventsScreen
import com.campusconnect.app.features.home.HomeScreen
import com.campusconnect.app.features.map.CampusMapScreen
import com.campusconnect.app.features.marketplace.CreateListingScreen
import com.campusconnect.app.features.marketplace.ListingDetailScreen
import com.campusconnect.app.features.marketplace.MarketplaceScreen
import com.campusconnect.app.features.messaging.ChatScreen
import com.campusconnect.app.features.messaging.MessagesScreen
import com.campusconnect.app.features.notifications.NotificationPreferencesScreen
import com.campusconnect.app.features.notifications.NotificationsScreen
import com.campusconnect.app.features.onboarding.OnboardingScreen
import com.campusconnect.app.features.opportunities.CreateOpportunityScreen
import com.campusconnect.app.features.opportunities.OpportunitiesScreen
import com.campusconnect.app.features.opportunities.OpportunityDetailScreen
import com.campusconnect.app.features.profile.ApplicationsScreen
import com.campusconnect.app.features.profile.ProfileScreen
import com.campusconnect.app.features.profile.ProfileSetupScreen
import com.campusconnect.app.features.profile.SavedScreen

@Composable
fun AppRoot(viewModel: RootViewModel = hiltViewModel()) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val tabRoutes = Routes.mainTabs
    val showBottomBar = currentRoute in tabRoutes

    LaunchedEffect(session?.onboardingComplete, session?.signedIn, session?.profileComplete) {
        val state = session ?: return@LaunchedEffect
        val target = when {
            !state.onboardingComplete -> Routes.ONBOARDING
            !state.signedIn -> Routes.LOGIN
            !state.profileComplete -> Routes.SETUP
            else -> Routes.HOME
        }
        val current = currentRoute
        val shouldNavigate = when (target) {
            Routes.ONBOARDING -> current != Routes.ONBOARDING
            Routes.LOGIN -> current !in setOf(
                Routes.LOGIN, Routes.REGISTER, Routes.FORGOT_PASSWORD, Routes.VERIFY,
            )
            Routes.SETUP -> current != Routes.SETUP && current != Routes.EDIT_PROFILE
            else -> current in Routes.authRoutes
        }
        if (shouldNavigate) {
            navController.navigate(target) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    onSelect = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            composable(Routes.SPLASH) { SplashScreen() }

            composable(Routes.ONBOARDING) {
                OnboardingScreen(onFinished = {})
            }
            composable(Routes.LOGIN) {
                LoginScreen(
                    onRegister = { navController.navigate(Routes.REGISTER) },
                    onForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) },
                )
            }
            composable(Routes.REGISTER) {
                RegisterScreen(
                    onLogin = { navController.popBackStack(Routes.LOGIN, inclusive = false) },
                    onVerify = { navController.navigate(Routes.VERIFY) },
                )
            }
            composable(Routes.FORGOT_PASSWORD) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.VERIFY) {
                VerifyEmailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SETUP) {
                ProfileSetupScreen(
                    isEdit = false,
                    onDone = {},
                    onBack = null,
                )
            }
            composable(Routes.EDIT_PROFILE) {
                ProfileSetupScreen(
                    isEdit = true,
                    onDone = { navController.popBackStack() },
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.HOME) {
                HomeScreen(
                    onMessages = { navController.navigate(Routes.MESSAGES) },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onEvents = { navController.navigate(Routes.EVENTS) },
                    onMap = { navController.navigate(Routes.CAMPUS_MAP) },
                    onAccommodation = { navController.navigate(Routes.ACCOMMODATION) },
                    onAssistant = { navController.navigate(Routes.ASSISTANT) },
                    onPost = { navController.navigate(Routes.post(it)) },
                    onOpportunity = { navController.navigate(Routes.opportunity(it)) },
                    onListing = { navController.navigate(Routes.listing(it)) },
                    onAllCommunity = { navController.navigate(Routes.COMMUNITY) },
                    onAllOpportunities = { navController.navigate(Routes.OPPORTUNITIES) },
                    onAllMarketplace = { navController.navigate(Routes.MARKETPLACE) },
                )
            }
            composable(Routes.COMMUNITY) {
                CommunityScreen(
                    onCreatePost = { navController.navigate(Routes.CREATE_POST) },
                    onOpenPost = { navController.navigate(Routes.post(it)) },
                )
            }
            composable(Routes.CREATE_POST) {
                CreatePostScreen(
                    onBack = { navController.popBackStack() },
                    onPosted = { navController.popBackStack() },
                )
            }
            composable(Routes.POST_DETAIL) {
                PostDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.MARKETPLACE) {
                MarketplaceScreen(
                    onOpenListing = { navController.navigate(Routes.listing(it)) },
                    onCreateListing = { navController.navigate(Routes.CREATE_LISTING) },
                )
            }
            composable(Routes.LISTING_DETAIL) {
                ListingDetailScreen(
                    onBack = { navController.popBackStack() },
                    onChat = { navController.navigate(Routes.chat(it)) },
                )
            }
            composable(Routes.CREATE_LISTING) {
                CreateListingScreen(
                    onBack = { navController.popBackStack() },
                    onCreated = { navController.popBackStack() },
                )
            }
            composable(Routes.OPPORTUNITIES) {
                OpportunitiesScreen(
                    onOpenOpportunity = { navController.navigate(Routes.opportunity(it)) },
                    onCreateOpportunity = { navController.navigate(Routes.CREATE_OPPORTUNITY) },
                )
            }
            composable(Routes.OPPORTUNITY_DETAIL) {
                OpportunityDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CREATE_OPPORTUNITY) {
                CreateOpportunityScreen(
                    onBack = { navController.popBackStack() },
                    onPublished = { navController.popBackStack() },
                )
            }

            composable(Routes.ACCOMMODATION) {
                AccommodationScreen(
                    onOpenListing = { navController.navigate(Routes.accommodation(it)) },
                )
            }
            composable(Routes.ACCOMMODATION_DETAIL) {
                AccommodationDetailScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.EVENTS) {
                EventsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CAMPUS_MAP) {
                CampusMapScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.ASSISTANT) {
                AssistantScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.MESSAGES) {
                MessagesScreen(
                    onBack = { navController.popBackStack() },
                    onOpenConversation = { navController.navigate(Routes.chat(it)) },
                )
            }
            composable(Routes.CHAT) {
                ChatScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.NOTIFICATIONS) {
                NotificationsScreen(
                    onBack = { navController.popBackStack() },
                    onOpen = { type, targetId ->
                        when (type) {
                            NotificationType.MESSAGE -> targetId?.let { navController.navigate(Routes.chat(it)) }
                            NotificationType.MARKETPLACE -> targetId?.let { navController.navigate(Routes.listing(it)) }
                            NotificationType.OPPORTUNITY -> targetId?.let { navController.navigate(Routes.opportunity(it)) }
                            NotificationType.EVENT -> navController.navigate(Routes.EVENTS)
                            else -> Unit
                        }
                    },
                )
            }
            composable(Routes.NOTIFICATION_PREFS) {
                NotificationPreferencesScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.SAVED) {
                SavedScreen(
                    onBack = { navController.popBackStack() },
                    onOpenListing = { navController.navigate(Routes.listing(it)) },
                    onOpenOpportunity = { navController.navigate(Routes.opportunity(it)) },
                    onOpenAccommodation = { navController.navigate(Routes.accommodation(it)) },
                )
            }
            composable(Routes.APPLICATIONS) {
                ApplicationsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.PROFILE) {
                ProfileScreen(
                    onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onMessages = { navController.navigate(Routes.MESSAGES) },
                    onNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                    onNotificationPreferences = { navController.navigate(Routes.NOTIFICATION_PREFS) },
                    onApplications = { navController.navigate(Routes.APPLICATIONS) },
                    onSaved = { navController.navigate(Routes.SAVED) },
                    onEvents = { navController.navigate(Routes.EVENTS) },
                    onMap = { navController.navigate(Routes.CAMPUS_MAP) },
                    onAccommodation = { navController.navigate(Routes.ACCOMMODATION) },
                    onAssistant = { navController.navigate(Routes.ASSISTANT) },
                )
            }
        }
    }
}

@Composable
private fun MainBottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    val tabs = listOf(
        Triple(Routes.HOME, "Home", Icons.Filled.Home),
        Triple(Routes.COMMUNITY, "Community", Icons.Filled.Forum),
        Triple(Routes.MARKETPLACE, "Market", Icons.Filled.Storefront),
        Triple(Routes.OPPORTUNITIES, "Opps", Icons.Filled.Work),
        Triple(Routes.PROFILE, "Profile", Icons.Filled.Person),
    )
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        tabs.forEach { (route, label, icon) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick = { if (currentRoute != route) onSelect(route) },
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
            )
        }
    }
}

@Composable
private fun SplashScreen() {
    Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                "CampusConnect",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(18.dp))
            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        }
    }
}

package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CwcViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContainer(viewModel: CwcViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isAdminRole by viewModel.isAdminRole.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val isLoggedIn = userProfile?.isLoggedIn == true

    if (!isLoggedIn) {
        // Enforce landing screen for authentication & welcome public features first
        Scaffold(
            contentWindowInsets = WindowInsets.systemBars
        ) { innerPadding ->
            LandingScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
        return
    }

    // --- LOGGED IN RESPONSIVE LAYOUT FRAMEWORK ---
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = if (currentTab == "admin") "UCN Admin Portal" else "UCN Rider Dashboard",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Uganda Cycling Network",
                                fontSize = 10.sp,
                                color = CwcGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    actions = {
                        // Secure toggler button to alternate between Cyclist Rider dashboard and Admin moderation panel
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { viewModel.toggleAdminRole() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("admin_switcher_toggle")
                        ) {
                            Icon(
                                imageVector = if (isAdminRole) Icons.Filled.SupervisorAccount else Icons.Outlined.Security,
                                contentDescription = "Admin Role Switcher",
                                tint = if (isAdminRole) CwcGreen else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAdminRole) "ADMIN ACTIVE" else "ADMIN PORTAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAdminRole) CwcGreen else Color.LightGray
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
                )
            },
            bottomBar = {
                // Renders bottom navigation ONLY on Compact mobile layouts (< 600dp) for mobile ergonomics
                if (!isWideScreen && currentTab != "admin") {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                    ) {
                        listOfTabs().forEach { item ->
                            NavigationBarItem(
                                selected = currentTab == item.route,
                                onClick = { viewModel.navigateTo(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == item.route) item.selectedIcon else item.outlinedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CwcGreen,
                                    indicatorColor = CwcGreen,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("tab_nav_${item.route}")
                            )
                        }
                    }
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Renders vertical side navigation rail ONLY on Expanded wide tablet layouts (>= 600dp)
                if (isWideScreen && currentTab != "admin") {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        header = {
                            Icon(
                                imageVector = Icons.Filled.DirectionsBike,
                                contentDescription = null,
                                tint = CwcGreen,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        }
                    ) {
                        listOfTabs().forEach { item ->
                            NavigationRailItem(
                                selected = currentTab == item.route,
                                onClick = { viewModel.navigateTo(item.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (currentTab == item.route) item.selectedIcon else item.outlinedIcon,
                                        contentDescription = item.label
                                    )
                                },
                                label = { Text(item.label, fontSize = 9.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Color.Black,
                                    selectedTextColor = CwcGreen,
                                    indicatorColor = CwcGreen,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray
                                ),
                                modifier = Modifier.testTag("rail_nav_${item.route}")
                            )
                        }
                    }
                }

                // Core content routers mapping
                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "ScreenTransition"
                    ) { target ->
                        when (target) {
                            "social" -> FeedScreen(viewModel = viewModel)
                            "tracking" -> TrackScreen(viewModel = viewModel)
                            "routes" -> RoutesScreen(viewModel = viewModel)
                            "community" -> ClubsEventsScreen(viewModel = viewModel)
                            "chat" -> ChatScreen(viewModel = viewModel)
                            "marketplace" -> MarketplaceScreen(viewModel = viewModel)
                            "profile" -> ProfileScreen(viewModel = viewModel)
                            "admin" -> AdminScreen(viewModel = viewModel)
                            else -> FeedScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// Data holder representing M3 navigation segments
data class NavigationTab(
    val route: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val outlinedIcon: androidx.compose.ui.graphics.vector.ImageVector
)

private fun listOfTabs() = listOf(
    NavigationTab("social", "Feed", Icons.Filled.DynamicFeed, Icons.Outlined.DynamicFeed),
    NavigationTab("tracking", "GPS Track", Icons.Filled.DirectionsRun, Icons.Outlined.DirectionsRun),
    NavigationTab("routes", "Routes", Icons.Filled.AltRoute, Icons.Outlined.AltRoute),
    NavigationTab("community", "Clubs", Icons.Filled.GroupWork, Icons.Outlined.GroupWork),
    NavigationTab("chat", "Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    NavigationTab("marketplace", "Bikes", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    NavigationTab("profile", "Profile", Icons.Filled.AccountCircle, Icons.Outlined.AccountCircle)
)

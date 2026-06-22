package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CwcViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAppMockup(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A)) // Slate cyber dark background
    ) {
        // Draw elegant decorative cycling circuit curves in the wide browser/computer background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Subtle premium Uganda route trails in background
            drawLine(CwcGreen.copy(alpha = 0.05f), Offset(w * 0.1f, 0f), Offset(w * 0.4f, h), strokeWidth = 3f)
            drawLine(CwcGreen.copy(alpha = 0.03f), Offset(0f, h * 0.7f), Offset(w, h * 0.9f), strokeWidth = 5f)
            drawCircle(CwcGreen.copy(alpha = 0.02f), radius = w * 0.18f, center = Offset(w * 0.8f, h * 0.25f))
        }

        // Center a premium simulated smartphone chassis
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .width(400.dp)
                .fillMaxHeight(0.96f)
                .padding(vertical = 10.dp)
                .clip(RoundedCornerShape(40.dp))
                .border(8.dp, Color(0xFF282E3D), RoundedCornerShape(40.dp)) // outer phone ring bezel
                .border(10.dp, Color(0xFF161A23), RoundedCornerShape(40.dp)) // inner gloss border
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Simulated top camera notch / island
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .width(115.dp)
                    .height(20.dp)
                    .background(Color(0xFF0C0E14), RoundedCornerShape(10.dp)),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker grille
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(3.dp)
                        .background(Color(0xFF323A49), RoundedCornerShape(1.5.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Camera lens circle
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(Color(0xFF1E3A8A), CircleShape)
                )
            }

            // Real-time styled status bar inside the phone frame
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "13:07",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Wifi, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(11.dp))
                    Text("5G", fontSize = 9.sp, fontWeight = FontWeight.Black, color = CwcGreen)
                    Icon(Icons.Filled.Battery5Bar, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(11.dp))
                }
            }

            // The encapsulated phone screen viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
            ) {
                content()
            }

            // Virtual Home Swipe bottom bar
            Box(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .width(130.dp)
                    .height(4.dp)
                    .background(Color.White.copy(alpha = 0.35f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderMobileAppContent(
    viewModel: CwcViewModel,
    currentTab: String,
    isAdminRole: Boolean,
    isWideScreen: Boolean
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "UCN Rider Dashboard",
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Uganda Cycling Network",
                            fontSize = 9.sp,
                            color = CwcGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    // Switch to Admin Portal Securely
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "ADMIN PORTAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            // Mobile app bottom menu bar navigation
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
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
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
                    else -> FeedScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContainer(viewModel: CwcViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isAdminRole by viewModel.isAdminRole.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val isLoggedIn = userProfile?.isLoggedIn == true

    if (!isLoggedIn) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp
            if (isWideScreen) {
                PhoneAppMockup {
                    LandingScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Scaffold(
                    contentWindowInsets = WindowInsets.systemBars
                ) { innerPadding ->
                    LandingScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        return
    }

    // --- LOGGED IN RESPONSIVE LAYOUT FRAMEWORK ---
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (currentTab == "admin") {
            // ADMIN PANEL AS A WIDE WEB-UI APPLICATION
            AdminScreen(viewModel = viewModel)
        } else {
            // CYCLIST SIDE AS A COMMITTED MOBILE PHONE APPLICATION
            if (isWideScreen) {
                PhoneAppMockup {
                    RiderMobileAppContent(
                        viewModel = viewModel,
                        currentTab = currentTab,
                        isAdminRole = isAdminRole,
                        isWideScreen = false // Constrain to phone size internally
                    )
                }
            } else {
                RiderMobileAppContent(
                    viewModel = viewModel,
                    currentTab = currentTab,
                    isAdminRole = isAdminRole,
                    isWideScreen = false
                )
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

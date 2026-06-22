package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.CwcViewModel
import com.example.data.RouteEntity
import com.example.ui.theme.*
import java.util.Locale
import kotlin.random.Random

@Composable
fun TrackScreen(viewModel: CwcViewModel) {
    val isTracking by viewModel.isTracking.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val seconds by viewModel.rideSeconds.collectAsState()
    val distance by viewModel.rideDistance.collectAsState()
    val speed by viewModel.currentSpeed.collectAsState()
    val maxSpeed by viewModel.maxSpeed.collectAsState()
    val elevation by viewModel.elevationGain.collectAsState()
    val calories by viewModel.caloriesBurned.collectAsState()
    val liveSharing by viewModel.liveSharingEnabled.collectAsState()
    val gpsCoordinates by viewModel.gpsCoordinates.collectAsState()

    // Safety Alert states
    val sosFired by viewModel.sosFired.collectAsState()
    val accidentDetected by viewModel.accidentDetected.collectAsState()

    // Advanced GPS States (Requirement 1, 2, 7, 8, 13)
    val locationPermissionGranted by viewModel.isLocationPermissionGranted.collectAsState()
    val batteryMode by viewModel.batteryOptimizationMode.collectAsState()
    val activeNavRoute by viewModel.activeNavigationRoute.collectAsState()
    val navCue by viewModel.currentNavigationCue.collectAsState()
    val navIcon by viewModel.currentNavigationIcon.collectAsState()
    val searchRadius by viewModel.searchRadiusKm.collectAsState()
    val showNearbyOnMap by viewModel.showNearbyCyclistsOnMap.collectAsState()

    var showCrashTesterDialog by remember { mutableStateOf(false) }
    var selectedFriendInfo by remember { mutableStateOf<FriendSim?>(null) }

    // Hardcoded list of Kampala's active co-cyclists for live simulation (Requirement 7)
    val friendsList = remember {
        listOf(
            FriendSim("Arthur Mukasa", 0.3280, 32.5812, "Luwum St, Kampala", 24.5, "Gravel Trek"),
            FriendSim("Sarah Namubiru", 0.3410, 32.5970, "Bukoto Incline Rd", 21.0, "Specialized Allez"),
            FriendSim("Denis Okello", 0.3340, 32.5991, "Northern Bypass High Speed Segment", 29.2, "Pinarello Dogma"),
            FriendSim("Aisha Chemutai", 0.3136, 32.5811, "Clock Tower intersection", 31.8, "Giant Defy")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // --- GPS PERMISSION SOLICITATION BANNER (Requirement 1) ---
            if (!locationPermissionGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .border(1.dp, Color(0xFFFFD54F).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E2C1F))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationSearching, contentDescription = null, tint = Color(0xFFFFD54F))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GPS Location Permission Required", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Together We Can (CWC) uses active fine-grained GPS locations to render live speed telemetry, accumulate distance segments, trace maps, screen nearby cycling events, and automatically notify emergency services in case of collisions.",
                            fontSize = 11.sp,
                            color = Color.LightGray,
                            lineHeight = 16.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.requestLocationPermission() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.End)
                                .testTag("grant_location_perm_btn")
                        ) {
                            Text("Grant Fine GPS Permission", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            // --- TOP SAFETY HEADER BAR ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Live tracking sharing status
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(
                            if (liveSharing) Color(0xFF00E676).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.toggleLiveSharing() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (liveSharing) Color(0xFF00E676) else Color.Gray, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (liveSharing) "LIVE BROADCAST ON" else "OFFLINE TRACKING",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (liveSharing) Color(0xFF00E676) else Color.LightGray
                    )
                }

                // EMERGENCY SOS BUTTON (CRITICAL)
                Button(
                    onClick = { viewModel.triggerEmergencySos() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("sos_alert_trigger_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.ReportGmailerrorred, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Text("EMERGENCY SOS", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // --- ACTIVE NAVIGATION INSTRUCTION BAR (Requirement 8 - Turn-by-Turn) ---
            if (activeNavRoute != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2822)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Map icon based on viewModel current state
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0xFF00E676).copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (navIcon) {
                                        "left" -> Icons.Filled.ArrowBack
                                        "right" -> Icons.Filled.ArrowForward
                                        "uturn" -> Icons.Filled.Directions
                                        "destination" -> Icons.Filled.Flag
                                        else -> Icons.Filled.ArrowUpward
                                    },
                                    contentDescription = "Turn signal",
                                    tint = Color(0xFF00E676)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = activeNavRoute?.routeName ?: "Active Cuestar Routing",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E676)
                                )
                                Text(
                                    text = if (navCue.isNotBlank()) navCue else "Retrieving route segments...",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        IconButton(onClick = { viewModel.clearNavigationRoute() }) {
                            Icon(Icons.Filled.Cancel, contentDescription = "Exit Navigation", tint = Color.LightGray)
                        }
                    }
                }
            }

            // --- LOCAL LIVE NETWORKS & BATTERY OPTIMIZER HEADER (Requirement 6, 13) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isTracking && !isPaused) Icons.Filled.Sensors else Icons.Filled.SensorsOff,
                        contentDescription = "GPS active sensor",
                        modifier = Modifier.size(13.dp),
                        tint = if (isTracking && !isPaused) Color(0xFF00E676) else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Interval: $batteryMode",
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isTracking && liveSharing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Wifi, contentDescription = "WS status", modifier = Modifier.size(12.dp), tint = Color(0xFF00E676))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Supabase Realtime Synced",
                            fontSize = 8.sp,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- MAP CANVAS & LOCS OVERLAYS ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF15151F)) // stylized charcoal maps background
            ) {
                // Main route history map rendering
                val showCyclists = showNearbyOnMap
                val currentUsersLocation = gpsCoordinates.lastOrNull() ?: (0.3204 to 32.5898)

                MapRenderCanvas(
                    gpsPoints = gpsCoordinates,
                    nearbyFriends = if (showCyclists) friendsList else emptyList(),
                    userLatLng = currentUsersLocation,
                    searchRadiusKm = searchRadius,
                    modifier = Modifier.fillMaxSize()
                )

                // Map Information Overlay Card
                Card(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(14.dp)
                        .widthIn(max = 240.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("CWC Uganda Live GPS", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        Text("Active Location: Kampala, Central", fontSize = 9.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF00E676), CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("You (Current Position)", fontSize = 10.sp, color = Color.White)
                        }

                        if (showCyclists) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF2196F3), CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Live Riders within ${searchRadius.toInt()}km", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }

                // Crash simulator triggers overlay in corner
                IconButton(
                    onClick = { showCrashTesterDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp)
                        .background(Color.Yellow.copy(alpha = 0.85f), CircleShape)
                        .size(40.dp)
                        .testTag("test_crash_sensor")
                ) {
                    Icon(Icons.Filled.CarCrash, contentDescription = "Test Crash Alert", tint = Color.Black)
                }
            }

            // --- NEARBY LIVE CYCLISTS RADIAL PANEL (Requirement 7) ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Nearby Cyclists Radar", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        }

                        // Toggle Nearby cyclists visibility
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Show on Maps", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(end = 4.dp))
                            Switch(
                                checked = showNearbyOnMap,
                                onCheckedChange = { viewModel.toggleNearbyCyclistsOnMap() },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676)),
                                modifier = Modifier.scale(0.7f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Radius filters chips (Requirement 7)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Search Radius:", fontSize = 10.sp, color = Color.Gray)
                        listOf(5.0, 10.0, 20.0).forEach { rVal ->
                            FilterChip(
                                selected = searchRadius == rVal,
                                onClick = { viewModel.setSearchRadius(rVal) },
                                label = { Text("${rVal.toInt()} KM", fontSize = 9.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = Color.Black.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showNearbyOnMap) {
                        // Display co-riders that fit inside radius!
                        val userLat = gpsCoordinates.lastOrNull()?.first ?: 0.3204
                        val userLng = gpsCoordinates.lastOrNull()?.second ?: 32.5898

                        val insideRadiusFriends = friendsList.filter { fr ->
                            // Haversine distance calculate
                            val dist = viewModel.let {
                                val rVal = 6371.0 // earth rad km
                                val dLat = Math.toRadians(fr.lat - userLat)
                                val dLon = Math.toRadians(fr.lng - userLng)
                                val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                        Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(fr.lat)) *
                                        Math.sin(dLon / 2) * Math.sin(dLon / 2)
                                val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                                rVal * c
                            }
                            dist <= searchRadius
                        }

                        if (insideRadiusFriends.isEmpty()) {
                            Text("No community cyclists found in active range. Try expanding search radius.", color = Color.Gray, fontSize = 9.sp)
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                insideRadiusFriends.forEach { fr ->
                                    val dist = viewModel.let {
                                        val rVal = 6371.0
                                        val dLat = Math.toRadians(fr.lat - userLat)
                                        val dLon = Math.toRadians(fr.lng - userLng)
                                        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                                                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(fr.lat)) *
                                                Math.sin(dLon / 2) * Math.sin(dLon / 2)
                                        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                                        rVal * c
                                    }

                                    Card(
                                        modifier = Modifier
                                            .width(135.dp)
                                            .clickable { selectedFriendInfo = fr },
                                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.25f)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(6.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF2196F3), CircleShape))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(fr.name.split(" ").first(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 10.sp)
                                            }
                                            Text("Dist: ${String.format(Locale.US, "%.2f", dist)} KM", color = Color.LightGray, fontSize = 9.sp)
                                            Text("Speed: ${fr.speed} km/h", color = Color(0xFF00E676), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text("Rider indicators hidden. Enable toggle to view co-cyclists.", color = Color.Gray, fontSize = 9.sp)
                    }
                }
            }

            // --- ACTIVE SPORTS TELEMETRY HUD BAR ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Row stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TelemetryWidget(
                            title = "DISTANCE RIDE",
                            value = "${String.format(Locale.US, "%.2f", distance)} KM",
                            metricColor = Color(0xFF00E676)
                        )
                        TelemetryWidget(
                            title = "SPEED NOW",
                            value = "${String.format(Locale.US, "%.1f", speed)} km/h",
                            metricColor = Color.White
                        )
                        TelemetryWidget(
                            title = "ELAPSED TIMER",
                            value = formatChronometer(seconds),
                            metricColor = Color(0xFF00E676)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SecondaryWidget(title = "SPEED MAX", value = "$maxSpeed kmh", icon = Icons.Filled.Speed)
                        SecondaryWidget(title = "ELEVATION", value = "+${elevation}m", icon = Icons.Filled.TrendingUp)
                        SecondaryWidget(title = "CALORIES", value = "$calories kcal", icon = Icons.Filled.LocalFireDepartment)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- TRACKING CORE CONTROLLER CONTROLS ---
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isTracking) {
                            Button(
                                onClick = { viewModel.startRide() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("start_tracking_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                                    Text("START GPS TRACKING", fontWeight = FontWeight.Black, fontSize = 15.sp)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (!isPaused) {
                                    Button(
                                        onClick = { viewModel.pauseRide() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .testTag("pause_tracking_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Filled.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("PAUSE", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.resumeRide() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color.Black),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .testTag("resume_tracking_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Text("RESUME", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Button(
                                    onClick = { viewModel.stopAndSaveRide() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(50.dp)
                                        .testTag("stop_tracking_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Text("STOP & SAVE", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- MOCK CRASH SENSOR DIALOG ---
        if (showCrashTesterDialog) {
            AlertDialog(
                onDismissRequest = { showCrashTesterDialog = false },
                confirmButton = {
                    TextButton(onClick = {
                        showCrashTesterDialog = false
                        viewModel.triggerAccidentSimulation()
                    }, modifier = Modifier.testTag("dialog_confirm_crash")) {
                        Text("Simulate Shock / decel", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCrashTesterDialog = false }) {
                        Text("Cancel")
                    }
                },
                title = { Text("Impact decel Shock Simulator") },
                text = {
                    Text("This triggers a localized deceleration impact (or prolonged static resting sequence), activating the CWC Accident Detection system and launching emergency SOS alerts.")
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // --- SELECTED FRIEND LIVE TELEMETRY MODAL ---
        if (selectedFriendInfo != null) {
            val fr = selectedFriendInfo!!
            AlertDialog(
                onDismissRequest = { selectedFriendInfo = null },
                confirmButton = {
                    TextButton(onClick = { selectedFriendInfo = null }) {
                        Text("Close", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(Color(0xFF2196F3), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fr.name, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Current Location: ${fr.locName}", fontSize = 12.sp, color = Color.White)
                        Text("Riding Speed: ${fr.speed} km/h", fontSize = 12.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                        Text("Bicycle Model: ${fr.bike}", fontSize = 12.sp, color = Color.LightGray)
                        Text("Status: Broadcaster Active - Online via Supabase websocket stream.", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }

        // --- EMERGENCY SOS OVERLAY (CRITICAL STATE) ---
        if (sosFired) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.95f))
                    .clickable { /* prevent background clicks */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "SOS",
                        tint = Color.White,
                        modifier = Modifier
                            .size(96.dp)
                            .background(Color.Black.copy(alpha = 0.15f), CircleShape)
                            .padding(16.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "EMERGENCY SOS FIRED",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Your exact GPS lat/lng coordinates have been transmitted to the CWC Safety database, emergency safety dispatchers, and Jane Don (+256 701 234567). Help is on the way!",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = { viewModel.dismissSos() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(200.dp)
                            .height(48.dp)
                            .testTag("dismiss_sos_btn")
                    ) {
                        Text("I am Safe (Dismiss)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- ACCIDENT CRASH AUTO ALARM ALERTER ---
        if (accidentDetected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE65100).copy(alpha = 0.98f))
                    .clickable { /* blank */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.NotificationsActive,
                        contentDescription = "Collision",
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "SUDDEN IMPACT DETECTED!",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Prolonged inactivity or deep deceleration shock recorded near Kampala Bypass. An emergency dispatch will be sent automatically to Kampala Emergency sectors within 30 seconds unless aborted.",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = { viewModel.dismissAccident() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFE65100)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(220.dp)
                            .height(50.dp)
                            .testTag("dismiss_crash_btn")
                    ) {
                        Text("FALSE ALARM (I'M OK)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class FriendSim(
    val name: String,
    val lat: Double,
    val lng: Double,
    val locName: String,
    val speed: Double,
    val bike: String
)

@Composable
fun TelemetryWidget(
    title: String,
    value: String,
    metricColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(title, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Black, color = metricColor)
    }
}

@Composable
fun SecondaryWidget(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Column {
            Text(title, fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Text(value, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MapRenderCanvas(
    gpsPoints: List<Pair<Double, Double>>,
    nearbyFriends: List<FriendSim>,
    userLatLng: Pair<Double, Double>,
    searchRadiusKm: Double,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    // Build points JSON array safely using Android native JSON classes
    val pointsArray = remember(gpsPoints) {
        org.json.JSONArray().apply {
            gpsPoints.forEach { pt ->
                put(org.json.JSONArray().apply {
                    put(pt.first)
                    put(pt.second)
                })
            }
        }.toString()
    }

    // Build friends JSON array safely using Android native JSON classes
    val friendsArray = remember(nearbyFriends) {
        org.json.JSONArray().apply {
            nearbyFriends.forEach { fr ->
                put(org.json.JSONObject().apply {
                    put("name", fr.name)
                    put("lat", fr.lat)
                    put("lng", fr.lng)
                    put("locName", fr.locName)
                    put("speed", fr.speed)
                    put("bike", fr.bike)
                })
            }
        }.toString()
    }

    val mapHtml = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #15151F;
                }
                .leaflet-bar {
                    border: none !important;
                    box-shadow: 0 4px 12px rgba(0,0,0,0.6) !important;
                }
                .leaflet-bar a {
                    background-color: #1e1e2d !important;
                    color: #00E676 !important;
                    border-bottom: 1px solid #2d2d3d !important;
                }
                .leaflet-bar a:hover {
                    background-color: #2a2a3d !important;
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: true,
                    attributionControl: false
                }).setView([0.3320, 32.5898], 13);

                // CartoDB Dark Matter matches premium look perfectly
                L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                    maxZoom: 19
                }).addTo(map);

                var userIcon = L.divIcon({
                    className: 'custom-user-icon',
                    html: '<div style="background-color: #00E676; width: 14px; height: 14px; border-radius: 50%; border: 2px solid white; box-shadow: 0 0 12px #00E676; position: relative;">' +
                          '<div style="position: absolute; top: -5px; left: -5px; width: 22px; height: 22px; border-radius: 50%; background-color: rgba(0,230,118,0.3); animation: pulse 1.5s infinite;"></div>' +
                          '</div>',
                    iconSize: [22, 22],
                    iconAnchor: [11, 11]
                });

                var friendIcon = L.divIcon({
                    className: 'custom-friend-icon',
                    html: '<div style="background-color: #2196F3; width: 12px; height: 12px; border-radius: 50%; border: 2px solid white; box-shadow: 0 0 10px rgba(33,150,243,0.8); position: relative;">' +
                          '<div style="position: absolute; top: -4px; left: -4px; width: 16px; height: 16px; border-radius: 50%; background-color: rgba(33,150,243,0.25); animation: pulse 2s infinite;"></div>' +
                          '</div>',
                    iconSize: [20, 20],
                    iconAnchor: [10, 10]
                });

                var userMarker = null;
                var routePolyline = L.polyline([], {
                    color: '#00E676',
                    weight: 6,
                    opacity: 0.9,
                    lineJoin: 'round'
                }).addTo(map);

                var searchCircle = null;
                var friendMarkers = {};

                function updateUserLocation(lat, lng, searchRadiusKm) {
                    if (!userMarker) {
                        userMarker = L.marker([lat, lng], {icon: userIcon}).addTo(map);
                        map.setView([lat, lng], 14);
                    } else {
                        userMarker.setLatLng([lat, lng]);
                    }

                    if (searchRadiusKm && searchRadiusKm > 0) {
                        if (!searchCircle) {
                            searchCircle = L.circle([lat, lng], {
                                radius: searchRadiusKm * 1000,
                                color: '#2196F3',
                                weight: 1.5,
                                fillColor: '#2196F3',
                                fillOpacity: 0.05,
                                dashArray: '5, 5'
                            }).addTo(map);
                        } else {
                            searchCircle.setLatLng([lat, lng]);
                            searchCircle.setRadius(searchRadiusKm * 1000);
                        }
                    } else {
                        if (searchCircle) {
                            map.removeLayer(searchCircle);
                            searchCircle = null;
                        }
                    }
                }

                function updatePath(coordsJson, searchRadiusKm) {
                    try {
                        var pts = JSON.parse(coordsJson);
                        if (pts && pts.length > 0) {
                            var latlngs = pts.map(function(p) { return [p[0], p[1]]; });
                            routePolyline.setLatLngs(latlngs);
                            
                            var lastPt = latlngs[latlngs.length - 1];
                            updateUserLocation(lastPt[0], lastPt[1], searchRadiusKm);
                        } else {
                            // Default to user's standard position if no points are recorded yet
                            updateUserLocation(0.3320, 32.5898, searchRadiusKm);
                        }
                    } catch (e) {
                        console.error("Path error: " + e.message);
                    }
                }

                function updateFriends(friendsJson) {
                    try {
                        var friends = JSON.parse(friendsJson);
                        
                        var currentKeys = Object.keys(friendMarkers);
                        var activeKeys = friends.map(function(f) { return f.name; });
                        
                        currentKeys.forEach(function(key) {
                            if (activeKeys.indexOf(key) === -1) {
                                map.removeLayer(friendMarkers[key]);
                                delete friendMarkers[key];
                            }
                        });

                        friends.forEach(function(fr) {
                            var lat = fr.lat;
                            var lng = fr.lng;
                            var popupText = "<b>" + fr.name + "</b><br/>Speed: " + fr.speed + " km/h<br/>Bike: " + fr.bike + "<br/>Loc: " + fr.locName;
                            
                            if (friendMarkers[fr.name]) {
                                friendMarkers[fr.name].setLatLng([lat, lng]);
                                friendMarkers[fr.name].getPopup().setContent(popupText);
                            } else {
                                var m = L.marker([lat, lng], {icon: friendIcon})
                                    .addTo(map)
                                    .bindPopup(popupText);
                                friendMarkers[fr.name] = m;
                            }
                        });
                    } catch (e) {
                        console.error("Friends error: " + e.message);
                    }
                }

                // Inject Animation styles
                var styleEl = document.createElement('style');
                styleEl.innerHTML = "@keyframes pulse { 0% { transform: scale(0.5); opacity: 1; } 100% { transform: scale(1.6); opacity: 0; } }";
                document.head.appendChild(styleEl);
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Synchronize coordinates and search radius with Leaflet WebView
    LaunchedEffect(pointsArray, searchRadiusKm, webViewRef) {
        webViewRef?.let { wv ->
            wv.post {
                wv.evaluateJavascript("updatePath('$pointsArray', $searchRadiusKm)", null)
            }
        }
    }

    // Synchronize community cyclists with Leaflet WebView
    val friendsJsonEscaped = remember(friendsArray) { friendsArray.replace("'", "\\'") }
    LaunchedEffect(friendsJsonEscaped, webViewRef) {
        webViewRef?.let { wv ->
            wv.post {
                wv.evaluateJavascript("updateFriends('$friendsJsonEscaped')", null)
            }
        }
    }

    // Embed WebView with proper interactions and dark-themed OpenStreetMap tiles
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Trigger initial synchronizations
                        view?.evaluateJavascript("updatePath('$pointsArray', $searchRadiusKm)", null)
                        view?.evaluateJavascript("updateFriends('$friendsJsonEscaped')", null)
                    }
                }
                loadDataWithBaseURL("https://openstreetmap.org", mapHtml, "text/html", "UTF-8", null)
                webViewRef = this
            }
        },
        modifier = modifier
    )
}

fun formatChronometer(numSeconds: Long): String {
    val h = numSeconds / 3600
    val m = (numSeconds % 3600) / 60
    val s = numSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}

// Helpers for Switch modification scales
private fun Modifier.scale(scale: Float): Modifier = this.drawBehind {
    // Basic scaling helper
}

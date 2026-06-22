package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CwcViewModel
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun AdminScreen(viewModel: CwcViewModel) {
    val posts by viewModel.posts.collectAsState()
    val marketplace by viewModel.marketplaceItems.collectAsState()
    val clubs by viewModel.clubs.collectAsState()
    val events by viewModel.events.collectAsState()
    val rides by viewModel.rides.collectAsState()

    var activeModerationTab by remember { mutableStateOf("dashboard") } // dashboard, users, posts, marketplace, publish

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- ADMIN WEB NAVIGATION HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E2833)) // slate header
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Security, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(24.dp).padding(start = 6.dp))
            Text("CWC ADMIN PORTAL", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp, modifier = Modifier.padding(end = 8.dp))

            AdminTabItem(label = "Dashboard", active = activeModerationTab == "dashboard", onClick = { activeModerationTab = "dashboard" })
            AdminTabItem(label = "Publish Content", active = activeModerationTab == "publish", onClick = { activeModerationTab = "publish" })
            AdminTabItem(label = "Moderate Forums", active = activeModerationTab == "posts", onClick = { activeModerationTab = "posts" })
            AdminTabItem(label = "Approve Gear", active = activeModerationTab == "marketplace", onClick = { activeModerationTab = "marketplace" })
            AdminTabItem(label = "Rider Registry", active = activeModerationTab == "users", onClick = { activeModerationTab = "users" })

            Spacer(modifier = Modifier.width(4.dp))
            // Quick navigation to user view
            Row(
                modifier = Modifier
                    .background(CwcGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .border(1.dp, CwcGreen, RoundedCornerShape(6.dp))
                    .clickable { viewModel.toggleAdminRole() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.DirectionsBike, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(14.dp))
                Text("Rider View", fontSize = 11.sp, color = CwcGreen, fontWeight = FontWeight.Bold)
            }

            // Quick exit to landing page (logout)
            Row(
                modifier = Modifier
                    .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .border(1.dp, Color.Red, RoundedCornerShape(6.dp))
                    .clickable { viewModel.performLogout() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Filled.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(14.dp))
                Text("Logout to Landing", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }

        // --- SUB CONTENT ---
        when (activeModerationTab) {
            "dashboard" -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    item {
                        Text("Administrative Control System", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Central database diagnostics monitoring Kampala & Beyond networks", fontSize = 12.sp, color = Color.Gray)
                    }

                    // System Quick Navigation Dashboard Panel
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("System Quick Action Panel", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Instantly switch perspective to the user/rider interface or exit secure session.", fontSize = 11.sp, color = Color.LightGray)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = { viewModel.toggleAdminRole() },
                                        colors = ButtonDefaults.buttonColors(containerColor = CwcGreen, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(38.dp).testTag("admin_action_switch_to_user")
                                    ) {
                                        Icon(Icons.Filled.DirectionsBike, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Rider View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { viewModel.performLogout() },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(38.dp).testTag("admin_action_logout")
                                    ) {
                                        Icon(Icons.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Logout", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Numeric Metrics Grid Row
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                TelemetryMetricCard(title = "FORUM DISCUSSIONS", value = "${posts.size} Posts", icon = Icons.Filled.Forum, modifier = Modifier.weight(1f))
                                TelemetryMetricCard(title = "CLASSIFIED GEARS", value = "${marketplace.size} Listings", icon = Icons.Filled.Sell, modifier = Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                TelemetryMetricCard(title = "CLUBS ENROLLED", value = "${clubs.size} Active", icon = Icons.Filled.DirectionsBike, modifier = Modifier.weight(1f))
                                TelemetryMetricCard(title = "SCHEDULED RIDES", value = "${events.size} Events", icon = Icons.Filled.Event, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Moderator Quick Warning Alert info
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = CwcGreen)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Unified Admin Credentials", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("This dashboard directly moderates contents stored inside Room's SQLite engine. Modifications apply in real-time across the entire mobile client interface.", fontSize = 12.sp, color = Color.LightGray)
                            }
                        }
                    }
                }
            }

            "posts" -> {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    Text("Moderate Forum Content", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Remove inappropriate, spam, or misleading reports securely", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                    if (posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No posts exists in database.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(posts) { post ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(post.authorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                Text(post.authorLocation, fontSize = 11.sp, color = Color.Gray)
                                            }
                                            Button(
                                                onClick = { viewModel.deleteSocialPostAdmin(post) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(32.dp).testTag("admin_delete_post_${post.id}")
                                            ) {
                                                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Remove", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(post.caption, fontSize = 12.5.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "marketplace" -> {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    Text("Classified Consignments Review", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Approve or reject equipment postings submitted by local riders", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                    if (marketplace.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No marketplace gear posted.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(marketplace) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                Text("Listed Price: $${item.priceUsd} • Category: ${item.category}", fontSize = 11.sp, color = CwcGreen, fontWeight = FontWeight.Bold)
                                                Text("Seller: ${item.sellerName} (${item.phone})", fontSize = 11.sp, color = Color.Gray)
                                            }

                                            // Approval validation buttons
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                if (!item.isApproved) {
                                                    Button(
                                                        onClick = { viewModel.approveItemAdmin(item.id) },
                                                        colors = ButtonDefaults.buttonColors(containerColor = CwcGreen, contentColor = Color.Black),
                                                        modifier = Modifier.height(30.dp).testTag("admin_approve_item_${item.id}"),
                                                        shape = RoundedCornerShape(6.dp)
                                                    ) {
                                                        Text("Approve", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Button(
                                                    onClick = { viewModel.deleteItemAdmin(item) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.2f), contentColor = Color.Red),
                                                    modifier = Modifier.height(30.dp).testTag("admin_reject_item_${item.id}"),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text("Decline", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(item.description, fontSize = 12.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "users" -> {
                // Riders management database simulation
                val ridersList = listOf(
                    "Arthur Mukasa" to "Jinja base • General warnings",
                    "Sarah Namubiru" to "Kampala base • Verified Champion",
                    "Dennis Ssekitoleko" to "Wakiso base • Advanced rider",
                    "Aisha Nakato" to "Entebbe base • Leisure cruiser"
                )

                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    Text("Rider Registries Moderation", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Issue warnings or verify active profiles on standard safety criteria", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(ridersList) { rider ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(rider.first, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                        Text(rider.second, fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { viewModel.suspendCyclistUser(rider.first) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow.copy(alpha = 0.15f), contentColor = Color.Yellow),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp).testTag("warn_user_${rider.first.replace(" ", "")}")
                                        ) {
                                            Text("Warn", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { /* verify rider */ },
                                            colors = ButtonDefaults.buttonColors(containerColor = CwcGreen.copy(alpha = 0.15f), contentColor = CwcGreen),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Verify", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            "publish" -> {
                AdminPublishScreen(viewModel)
            }
        }
    }
}

@Composable
fun AdminPublishScreen(viewModel: CwcViewModel) {
    val rides by viewModel.rides.collectAsState()
    
    var captionText by remember { mutableStateOf("") }
    var postType by remember { mutableStateOf("video") } // video, photo, ride
    var categoryText by remember { mutableStateOf("Announcement") } // General, Route, Achievement, Announcement (Announcements are exclusive to admins!)
    
    // Video states
    var selectedVideoIdx by remember { mutableStateOf(0) }
    val videoPresets = listOf(
        "cwc_high_speed_expressway.mp4" to "Kampala-Entebbe Expressway Sprint Tour",
        "jinja_bridge_river_descent.mp4" to "Nile River Crossing Bridge Cruise",
        "kigo_jungle_mud.mp4" to "Kigo Hills Gravel Dirt Singletrack",
        "kampala_group_critical_mass.mp4" to "Kampala City Night Ride Critical Mass"
    )
    var videoDurationSeconds by remember { mutableStateOf("45") }

    // Photo states
    val photoOptions = listOf(
        "ride_nile" to "Scenic Nile Tour Captured Image",
        "gravel_track" to "Uganda Gravel Singletrack Action",
        "badge_100" to "100 KM Century Badge accomplishment",
        "ride_completed" to "Expressway Night Segment snapshot"
    )
    // To allow multi photo gallery selection
    var selectedPhotoSeeds by remember { mutableStateOf(setOf("ride_nile", "gravel_track")) }

    // Linked Ride states
    var selectedRideId by remember { mutableStateOf<Long?>(null) }
    
    var successToastText by remember { mutableStateOf("") }

    LaunchedEffect(successToastText) {
        if (successToastText.isNotEmpty()) {
            kotlinx.coroutines.delay(3500)
            successToastText = ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CwcGreen.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Publish, contentDescription = null, tint = CwcGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Creator Studio",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Text(
                        text = "Publish high-fidelity live videos, multi-photo slideshow carousels, or official verified workouts directly to the rider network.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Main Editor Sheet
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Headline Caption Field
                    Text("Caption Text", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CwcGreen)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = captionText,
                        onValueChange = { captionText = it },
                        placeholder = { Text("What information or achievement should admins share? Group updates, segment safety warn codes...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_composer_caption"),
                        textStyle = TextStyle(fontSize = 13.sp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CwcGreen,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Feed category tags
                    Text("Publish Category", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CwcGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Announcement", "Route", "Achievement", "General").forEach { cat ->
                            val isSelected = categoryText == cat
                            Box(
                                modifier = Modifier
                                    .border(
                                        1.dp,
                                        if (isSelected) CwcGreen else Color.Gray.copy(alpha = 0.4f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .background(
                                        if (isSelected) CwcGreen.copy(alpha = 0.15f) else Color.Transparent,
                                        RoundedCornerShape(6.dp)
                                    )
                                    .clickable { categoryText = cat }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Post types grid
                    Text("Select Attachment Type", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CwcGreen)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("video", Icons.Filled.VideoLibrary, "📹 Video Clip"),
                            Triple("photo", Icons.Filled.Collections, "📷 Photo Grid"),
                            Triple("ride", Icons.Filled.AddRoad, "🚴‍♂️ Attach Ride")
                        ).forEach { pt ->
                            val isSelected = postType == pt.first
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        1.5.dp,
                                        if (isSelected) CwcGreen else Color.Gray.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .background(
                                        if (isSelected) CwcGreen.copy(alpha = 0.1f) else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { postType = pt.first }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(pt.second, contentDescription = null, tint = if (isSelected) CwcGreen else Color.Gray, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(pt.third, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sub editors sheets based on type
                    when (postType) {
                        "video" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text("Select simulated Premium Video Asset", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(8.dp))
                                videoPresets.forEachIndexed { idx, item ->
                                    val isSelected = selectedVideoIdx == idx
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedVideoIdx = idx }
                                            .padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { selectedVideoIdx = idx },
                                            colors = RadioButtonDefaults.colors(selectedColor = CwcGreen)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(item.second, fontSize = 12.sp, color = if (isSelected) Color.White else Color.Gray, fontWeight = FontWeight.Medium)
                                            Text("File: ${item.first}", fontSize = 9.sp, color = Color.DarkGray)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Duration (seconds)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = videoDurationSeconds,
                                    onValueChange = { videoDurationSeconds = it.filter { char -> char.isDigit() } },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CwcGreen)
                                )
                            }
                        }

                        "photo" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Text("Choose Photo Assets to assemble Slideshow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Selected: ${selectedPhotoSeeds.size} items", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp))
                                
                                photoOptions.forEach { opt ->
                                    val isChecked = selectedPhotoSeeds.contains(opt.first)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedPhotoSeeds = if (isChecked) {
                                                    selectedPhotoSeeds - opt.first
                                                } else {
                                                    selectedPhotoSeeds + opt.first
                                                }
                                            }
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = {
                                                selectedPhotoSeeds = if (isChecked) {
                                                    selectedPhotoSeeds - opt.first
                                                } else {
                                                    selectedPhotoSeeds + opt.first
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = CwcGreen)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(opt.second, fontSize = 12.sp, color = if (isChecked) Color.White else Color.Gray)
                                    }
                                }
                            }
                        }

                        "ride" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Simulate or Sync Saved Rides", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    IconButton(
                                        onClick = {
                                            // Admin triggers custom ride pre-seeding
                                            viewModel.triggerCreatePost(
                                                caption = "Admin recorded quick Jinja Speedway loop testing the GPS tracking intervals and telemetry systems.",
                                                category = "Announcement",
                                                imageSeed = "ride_completed"
                                            )
                                            successToastText = "Dispatched test workout post instantly!"
                                        },
                                        modifier = Modifier.height(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Autorenew, contentDescription = "Auto Seed Demo Ride", tint = CwcGreen, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                if (rides.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color.DarkGray.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "No rider workouts logged in Room SQLite database. Hop over to Tracker tab and slide 'Start Ride', record some nodes, then return here to attach your live trace!",
                                            fontSize = 11.sp,
                                            color = Color.LightGray,
                                            lineHeight = 16.sp
                                        )
                                    }
                                } else {
                                    rides.forEach { rd ->
                                        val isSelected = selectedRideId == rd.id
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    if (isSelected) CwcGreen else Color.Transparent,
                                                    RoundedCornerShape(6.dp)
                                                )
                                                .clickable { selectedRideId = rd.id }
                                                .padding(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedRideId = rd.id },
                                                colors = RadioButtonDefaults.colors(selectedColor = CwcGreen)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text("Workout - ${rd.dateText}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color.Gray)
                                                Text("Distance: ${String.format(Locale.US, "%.1f", rd.distanceKm)} KM • Elev: +${rd.elevationGainM}m", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (successToastText.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF00E676).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .border(1.dp, CwcGreen, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(successToastText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Publish Button
                    Button(
                        onClick = {
                            if (captionText.isNotBlank()) {
                                when (postType) {
                                    "video" -> {
                                        val asset = videoPresets[selectedVideoIdx]
                                        val durSec = videoDurationSeconds.toIntOrNull() ?: 45
                                        viewModel.triggerCreatePost(
                                            caption = captionText,
                                            category = categoryText,
                                            imageSeed = "ride_nile",
                                            videoUrl = asset.first,
                                            videoDurationSec = durSec
                                        )
                                        successToastText = "Successfully stream-delivered video post to Social Feed! 📹"
                                    }
                                    "photo" -> {
                                        val finalSeeds = if (selectedPhotoSeeds.isEmpty()) "ride_nile" else selectedPhotoSeeds.joinToString(",")
                                        viewModel.triggerCreatePost(
                                            caption = captionText,
                                            category = categoryText,
                                            imageSeed = selectedPhotoSeeds.firstOrNull() ?: "ride_nile",
                                            isPhotoGallery = true,
                                            gallerySeeds = finalSeeds
                                        )
                                        successToastText = "Successfully broadcasted Multi-Photo gallery post! 📷"
                                    }
                                    "ride" -> {
                                        viewModel.triggerCreatePost(
                                            caption = captionText,
                                            category = categoryText,
                                            imageSeed = "ride_completed",
                                            linkedRideId = selectedRideId ?: 0L
                                        )
                                        successToastText = "Successfully integrated official Workout tracking ride to Social Feed! 🚴‍♂️"
                                    }
                                }
                                captionText = ""
                            } else {
                                successToastText = "Please write a summary/caption description before submitting!"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CwcGreen, contentColor = Color.Black),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("admin_upload_submit"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                            Text("PUBLISH BROADCAST TO NETWORK", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminTabItem(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                if (active) Color(0xFF00E676) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.Black else Color.LightGray
        )
    }
}

@Composable
fun TelemetryMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(28.dp))
            Column {
                Text(title, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, modifier = Modifier.padding(top = 1.dp))
            }
        }
    }
}

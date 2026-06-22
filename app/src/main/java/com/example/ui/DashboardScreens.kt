package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CwcViewModel
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale

// ==========================================
// 1. SOCIAL FEEDS DIRECTIVE
// ==========================================
@Composable
fun FeedScreen(viewModel: CwcViewModel) {
    val posts by viewModel.posts.collectAsState()
    val focusedPostId by viewModel.focusedPostId.collectAsState()
    val comments by viewModel.activePostComments.collectAsState()

    var postInputText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("General") }
    var selectedImageSeed by remember { mutableStateOf("ride_nile") }

    var commentInputText by remember { mutableStateOf("") }

    val imageOptions = listOf(
        "ride_nile" to "Nile Cruise",
        "gravel_track" to "Gravel Dirt",
        "badge_100" to "100K Century Badge",
        "ride_completed" to "Expressway Speed segment",
        "item_generic_bike" to "Gravel Rig"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        if (focusedPostId == null) {
            // --- Normal Feed Screen ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
            ) {
                // Post Composer Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF00E676).copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Post Something to riders", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = postInputText,
                                onValueChange = { postInputText = it },
                                placeholder = { Text("What's on your cycling mind? Share routes, safety status, group paces...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("feed_composer_input"),
                                textStyle = TextStyle(fontSize = 13.sp),
                                maxLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Category selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Type:", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                listOf("General", "Route", "Achievement").forEach { cat ->
                                    FilterChip(
                                        selected = selectedCategory == cat,
                                        onClick = { selectedCategory = cat },
                                        label = { Text(cat, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }

                            // Banner Image Selection Roll
                            Text("Attach visual layout:", color = Color.Gray, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                items(imageOptions) { opt ->
                                    val isSelected = selectedImageSeed == opt.first
                                    Box(
                                        modifier = Modifier
                                            .border(
                                                1.5.dp,
                                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFF00E676).copy(alpha = 0.15f) else Color.Transparent)
                                            .clickable { selectedImageSeed = opt.first }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(opt.second, fontSize = 11.sp, color = if (isSelected) Color.White else Color.Gray)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (postInputText.isNotBlank()) {
                                        viewModel.triggerCreatePost(postInputText, selectedCategory, selectedImageSeed)
                                        postInputText = ""
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .testTag("feed_submit_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Post Feed", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Feed List
                items(posts) { post ->
                    FeedCard(
                        post = post,
                        viewModel = viewModel,
                        onLike = { viewModel.likePost(post.id) },
                        onSave = { viewModel.savePost(post.id) },
                        onCommentClick = { viewModel.viewPostComments(post.id) }
                    )
                }
            }
        } else {
            // --- Comments Detail Panel ---
            val activePost = posts.find { it.id == focusedPostId }
            if (activePost != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                ) {
                    // Back header bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.clearFocusedPost() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                        Text("Comments & Intersections", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Original Post Core
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            FeedCard(
                                post = activePost,
                                viewModel = viewModel,
                                onLike = { viewModel.likePost(activePost.id) },
                                onSave = { viewModel.savePost(activePost.id) },
                                onCommentClick = {}
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                            Text("Rider Comments", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp, modifier = Modifier.padding(top = 10.dp, bottom = 4.dp))
                        }

                        if (comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No comments yet. Start the chain!", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        } else {
                            items(comments) { comment ->
                                CommentCard(comment = comment)
                            }
                        }
                    }

                    // Bottom Comment Input Block
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = commentInputText,
                                onValueChange = { commentInputText = it },
                                placeholder = { Text("Write comments...") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("comment_input_field"),
                                textStyle = TextStyle(fontSize = 12.sp),
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f)
                                )
                            )
                            IconButton(
                                onClick = {
                                    viewModel.postComment(activePost.id, commentInputText)
                                    commentInputText = ""
                                },
                                modifier = Modifier.testTag("comment_send_btn")
                            ) {
                                Icon(Icons.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeedCard(
    post: PostEntity,
    viewModel: CwcViewModel,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onCommentClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Procedural avatar placeholder
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(colors = listOf(CwcGreen, CwcDarkGreen)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        post.authorName.take(2).uppercase(),
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(post.authorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        if (post.category != "General") {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (post.category == "Announcement") Color(0xFFFFD54F).copy(alpha = 0.2f)
                                        else Color(0xFF00E676).copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = post.category,
                                    fontSize = 9.sp,
                                    color = if (post.category == "Announcement") Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Text("${post.authorLocation} • Just now", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Caption Text
            Text(
                post.caption,
                fontSize = 13.5.sp,
                color = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp
            )

            // --- 1. RENDER VIDEO PLAYER COMPONENT (IF LINKED / SPECIFIED) ---
            if (post.videoUrl.isNotEmpty()) {
                var isPlaying by remember { mutableStateOf(false) }
                var playProgress by remember { mutableFloatStateOf(0.0f) }
                var isMuted by remember { mutableStateOf(false) }

                LaunchedEffect(isPlaying) {
                    if (isPlaying) {
                        while (isPlaying) {
                            kotlinx.coroutines.delay(100)
                            playProgress += (0.1f / (if (post.videoDurationSec > 0) post.videoDurationSec.toFloat() else 30.0f))
                            if (playProgress >= 1.0f) {
                                playProgress = 0.0f
                                isPlaying = false
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Video Screen
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFF263238), Color(0xFF0F171A))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.PauseCircleFilled else Icons.Filled.PlayCircleFilled,
                                    contentDescription = "Play Video",
                                    tint = if (isPlaying) CwcGreen else Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clickable { isPlaying = !isPlaying }
                                        .testTag("play_video_btn_${post.id}")
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isPlaying) "Streaming: ${post.videoUrl}" else "Press Play to Stream Video Stream",
                                    fontSize = 12.sp,
                                    color = Color.LightGray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "CWC Stream • 1080p Ultra HD Simulation",
                                    fontSize = 9.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Video Player controls bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.82f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { isPlaying = !isPlaying }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = "Play state toggle",
                                    tint = CwcGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Slider(
                                value = playProgress,
                                onValueChange = { playProgress = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(20.dp),
                                colors = SliderDefaults.colors(
                                    thumbColor = CwcGreen,
                                    activeTrackColor = CwcGreen,
                                    inactiveTrackColor = Color.Gray
                                )
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            val totalSec = if (post.videoDurationSec > 0) post.videoDurationSec else 30
                            val currSec = (playProgress * totalSec).toInt()
                            Text(
                                text = String.format("00:%02d / 00:%02d", currSec, totalSec),
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )

                            IconButton(onClick = { isMuted = !isMuted }, modifier = Modifier.size(28.dp)) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Filled.VolumeOff else Icons.Filled.VolumeUp,
                                    contentDescription = "Volume toggle",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- 2. RENDER MULTI-PHOTO GALLERY / SLIDESHOW COMPONENT ---
            else if (post.isPhotoGallery) {
                Spacer(modifier = Modifier.height(12.dp))
                val photoSeeds = if (post.gallerySeeds.isNotEmpty()) {
                    post.gallerySeeds.split(",")
                } else {
                    listOf("ride_nile", "gravel_track", "badge_100")
                }
                var activeIdx by remember { mutableStateOf(0) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = when (photoSeeds[activeIdx]) {
                                        "ride_nile" -> listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
                                        "gravel_track" -> listOf(Color(0xFFF45C43), Color(0xFFEB3349))
                                        "badge_100" -> listOf(Color(0xFFF12711), Color(0xFFF5AF19))
                                        "ride_completed" -> listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
                                        else -> listOf(Color(0xFF1F4037), Color(0xFF99F2C8))
                                    }
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        // Previous Photo Button
                        IconButton(
                            onClick = {
                                activeIdx = if (activeIdx > 0) activeIdx - 1 else photoSeeds.size - 1
                            },
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 6.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .size(28.dp)
                        ) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        // Center Content
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (photoSeeds[activeIdx]) {
                                    "badge_100" -> Icons.Filled.MilitaryTech
                                    "gravel_track" -> Icons.Filled.Landscape
                                    "ride_nile" -> Icons.Filled.PhotoCamera
                                    else -> Icons.Filled.DirectionsBike
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = when (photoSeeds[activeIdx]) {
                                    "ride_nile" -> "📷 Scenic Nile Tour Photo"
                                    "gravel_track" -> "📷 Uganda Gravel Ride Action"
                                    "badge_100" -> "📷 Century Milestone Accomplished"
                                    "ride_completed" -> "📷 Night Ride Group Snapshot"
                                    else -> "📷 CWC Live Community Capture"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Next Photo Button
                        IconButton(
                            onClick = {
                                activeIdx = (activeIdx + 1) % photoSeeds.size
                            },
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 6.dp)
                                .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                                .size(28.dp)
                        ) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = "Next Photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }

                        // Bottom Page counter overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${activeIdx + 1}/${photoSeeds.size} Photos",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Circle dots indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        photoSeeds.forEachIndexed { i, _ ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (i == activeIdx) 8.dp else 5.dp)
                                    .background(
                                        if (i == activeIdx) CwcGreen else Color.Gray.copy(alpha = 0.4f),
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            // --- 3. RENDER ATTACHED WORKOUT RIDE COMPONENT ---
            else if (post.linkedRideId > 0L) {
                val ridesList by viewModel.rides.collectAsState()
                val matchedRide = ridesList.find { it.id == post.linkedRideId }

                if (matchedRide != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CwcGreen.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141F1A)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.Polyline, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("RIDER WORKOUT ATTACHED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CwcGreen, letterSpacing = 1.sp)
                                }
                                Text(matchedRide.dateText, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("DISTANCE", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("${String.format(Locale.US, "%.2f", matchedRide.distanceKm)} KM", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                Column {
                                    Text("DURATION", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    val durationHours = matchedRide.durationSeconds / 3600
                                    val durationMins = (matchedRide.durationSeconds % 3600) / 60
                                    Text("${String.format("%02d:%02dm", durationHours, durationMins)}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                Column {
                                    Text("SPEED MAX", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("${String.format(Locale.US, "%.1f", matchedRide.maxSpeedKmh)} kmh", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                                }
                                Column {
                                    Text("CLIMBED", fontSize = 8.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                    Text("+${matchedRide.elevationGainM}m", fontSize = 13.sp, fontWeight = FontWeight.Black, color = CwcGreen)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Draw mini-map track trace inside the social capsule
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(65.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0C1310))
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val coords = matchedRide.routeHistoryText.split(";")
                                        .filter { it.contains(",") }
                                        .mapNotNull {
                                            val parts = it.split(",")
                                            val lt = parts.getOrNull(0)?.toDoubleOrNull()
                                            val lg = parts.getOrNull(1)?.toDoubleOrNull()
                                            if (lt != null && lg != null) lt to lg else null
                                        }

                                    if (coords.isNotEmpty()) {
                                        val tracePath = Path()
                                        val minLt = coords.minOf { it.first }
                                        val maxLt = coords.maxOf { it.first }
                                        val minLg = coords.minOf { it.second }
                                        val maxLg = coords.maxOf { it.second }

                                        val px = size.width * 0.15f
                                        val py = size.height * 0.15f
                                        val mW = size.width - px * 2
                                        val mH = size.height - py * 2

                                        coords.forEachIndexed { i, pt ->
                                            val x = px + if (maxLg != minLg) (((pt.second - minLg) / (maxLg - minLg)).toFloat() * mW) else mW / 2
                                            val y = py + if (maxLt != minLt) ((1.0f - ((pt.first - minLt) / (maxLt - minLt)).toFloat()) * mH) else mH / 2

                                            if (i == 0) tracePath.moveTo(x, y) else tracePath.lineTo(x, y)
                                        }

                                        drawPath(
                                            path = tracePath,
                                            color = CwcGreen,
                                            style = Stroke(width = 4f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                                        )

                                        // Pinpoints
                                        val sX = px + if (maxLg != minLg) (((coords.first().second - minLg) / (maxLg - minLg)).toFloat() * mW) else mW / 2
                                        val sY = py + if (maxLt != minLt) ((1.0f - ((coords.first().first - minLt) / (maxLt - minLt)).toFloat()) * mH) else mH / 2
                                        drawCircle(color = Color.White, radius = 5f, center = Offset(sX, sY))

                                        val eX = px + if (maxLg != minLg) (((coords.last().second - minLg) / (maxLg - minLg)).toFloat() * mW) else mW / 2
                                        val eY = py + if (maxLt != minLt) ((1.0f - ((coords.last().first - minLt) / (maxLt - minLt)).toFloat()) * mH) else mH / 2
                                        drawCircle(color = CwcGreen, radius = 6f, center = Offset(eX, eY))
                                    } else {
                                        // procedural ripple
                                        val p = Path().apply {
                                            moveTo(0f, size.height * 0.5f)
                                            cubicTo(size.width * 0.25f, size.height * 0.8f, size.width * 0.75f, size.height * 0.2f, size.width, size.height * 0.5f)
                                        }
                                        drawPath(p, color = CwcGreen.copy(alpha = 0.4f), style = Stroke(width = 3f))
                                    }
                                }
                                Text(
                                    text = "Simulated GPS Track Vector",
                                    modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp),
                                    fontSize = 8.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // --- 4. RENDER STANDARD SINGLE STATIC IMAGE BANNER ---
            else if (post.imageUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = when (post.imageUrl) {
                                    "ride_nile" -> listOf(Color(0xFF00C9FF), Color(0xFF92FE9D))
                                    "gravel_track" -> listOf(Color(0xFFF45C43), Color(0xFFEB3349))
                                    "badge_100" -> listOf(Color(0xFFF12711), Color(0xFFF5AF19))
                                    else -> listOf(Color(0xFF1F4037), Color(0xFF99F2C8))
                                }
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = when (post.imageUrl) {
                                "badge_100" -> Icons.Filled.MilitaryTech
                                "gravel_track" -> Icons.Filled.Landscape
                                else -> Icons.Filled.DirectionsBike
                            },
                            contentDescription = "Cycling Graphic",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = when (post.imageUrl) {
                                "ride_nile" -> "Scenic Nile River Ride Track"
                                "gravel_track" -> "Kigo Forest Gravel Run"
                                "badge_100" -> "Century 100 KM Unlocked"
                                else -> "CWC Registered GPS Workout"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButtonText(
                    onClick = onLike,
                    icon = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    label = "${post.likesCount}",
                    active = post.isLiked,
                    activeColor = Color.Red,
                    tag = "like_btn_${post.id}"
                )

                IconButtonText(
                    onClick = onCommentClick,
                    icon = Icons.Outlined.Comment,
                    label = "${post.commentsCount}",
                    tag = "comment_btn_${post.id}"
                )

                IconButton(
                    onClick = onSave,
                    modifier = Modifier.testTag("save_btn_${post.id}")
                ) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Post",
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
fun IconButtonText(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean = false,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    tag: String = ""
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (active) activeColor else Color.LightGray,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) activeColor else Color.LightGray
        )
    }
}

@Composable
fun CommentCard(comment: CommentEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color.Gray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(comment.authorName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(comment.authorName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                Text(comment.text, fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// ==========================================
// 2. DISCOVER & COMPILATION ROUTES
// ==========================================
@Composable
fun RoutesScreen(viewModel: CwcViewModel) {
    val routes by viewModel.routes.collectAsState()

    var showCreateForm by remember { mutableStateOf(false) }
    var inputRouteName by remember { mutableStateOf("") }
    var inputDesc by remember { mutableStateOf("") }
    var inputDistance by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableStateOf("Moderate") }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Discover Safe Routes", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Button(
                onClick = { showCreateForm = !showCreateForm },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("create_route_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Filled.AddLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Add Route", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Expanded route drafting form
        AnimatedVisibility(visible = showCreateForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Draft Custom Cycling Path", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputRouteName,
                        onValueChange = { inputRouteName = it },
                        label = { Text("Route Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = inputDesc,
                        onValueChange = { inputDesc = it },
                        label = { Text("Description & Warning signs") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = inputDistance,
                        onValueChange = { inputDistance = it },
                        label = { Text("Distance (KM)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("route_dist_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Difficulty selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Difficulty:", color = Color.Gray, fontSize = 12.sp)
                        listOf("Easy", "Moderate", "Hard").forEach { diff ->
                            FilterChip(
                                selected = selectedDifficulty == diff,
                                onClick = { selectedDifficulty = diff },
                                label = { Text(diff, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val distDouble = inputDistance.toDoubleOrNull() ?: 5.0
                            if (inputRouteName.isNotBlank()) {
                                viewModel.createRoute(inputRouteName, inputDesc, distDouble, selectedDifficulty)
                                inputRouteName = ""
                                inputDesc = ""
                                inputDistance = ""
                                showCreateForm = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_route_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Text("Publish Location Path", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Routes list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(routes) { route ->
                RouteCard(
                    route = route,
                    onFavoriteToggle = { viewModel.toggleFavoriteRoute(route.id) },
                    onNavigateClick = { viewModel.selectNavigationRoute(route) }
                )
            }
        }
    }
}

@Composable
fun RouteCard(route: RouteEntity, onFavoriteToggle: () -> Unit, onNavigateClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(route.routeName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text("Created by: ${route.creatorName}", fontSize = 11.sp, color = Color.Gray)
                }
                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (route.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (route.isFavorite) GoldBadge else Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(route.description, fontSize = 13.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(12.dp))

            // Stats grid tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricPill(label = "${route.distanceKm} KM", icon = Icons.Filled.Timeline, containerColor = Color(0xFF00E676).copy(alpha = 0.15f))
                MetricPill(label = route.difficulty, icon = Icons.Filled.TrendingUp, containerColor = when (route.difficulty) {
                    "Easy" -> Color.Green.copy(alpha = 0.15f)
                    "Hard" -> Color.Red.copy(alpha = 0.15f)
                    else -> Color.Yellow.copy(alpha = 0.15f)
                })
                MetricPill(label = "~${route.estimatedTimeMinutes} min", icon = Icons.Filled.LockClock, containerColor = Color.Blue.copy(alpha = 0.12f))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Procedural Elevation Canvas graph
            Text("Elevation Profile Dynamics", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            ) {
                val elevationPoints = route.elevationProfileString.split(",").mapNotNull { it.toFloatOrNull() }
                if (elevationPoints.size > 1) {
                    val maxValue = elevationPoints.maxOrNull() ?: 1.0f
                    val minValue = elevationPoints.minOrNull() ?: 0.0f
                    val delta = if (maxValue - minValue == 0f) 1f else maxValue - minValue

                    val widthFactor = size.width / (elevationPoints.size - 1)
                    val points = elevationPoints.mapIndexed { index, value ->
                        val x = index * widthFactor
                        // Inverse scale so higher value climbs up the canvas
                        val y = size.height - ((value - minValue) / delta) * (size.height * 0.7f) - (size.height * 0.15f)
                        Offset(x, y)
                    }

                    // Stroke line
                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = CwcGreen,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 5f
                        )
                    }
                    // Glowing nodes
                    points.forEach { pt ->
                        drawCircle(color = Color.White, radius = 5f, center = pt)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onNavigateClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CwcGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("navigate_route_btn_${route.id}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("START TURN-BY-TURN NAVIGATION", fontWeight = FontWeight.Black, fontSize = 11.sp, letterSpacing = 0.5.sp)
                }
            }
        }
    }
}

@Composable
fun MetricPill(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color) {
    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ==========================================
// 3. CLUBS & EVENT RUNS SEGMENTS
// ==========================================
@Composable
fun ClubsEventsScreen(viewModel: CwcViewModel) {
    val clubs by viewModel.clubs.collectAsState()
    val events by viewModel.events.collectAsState()

    var activeSubTab by remember { mutableStateOf("clubs") } // clubs, events

    var showCreateForm by remember { mutableStateOf(false) }

    // State bindings for creating clubs/events
    var cName by remember { mutableStateOf("") }
    var cDesc by remember { mutableStateOf("") }
    var cLoc by remember { mutableStateOf("Kampala") }

    var eTitle by remember { mutableStateOf("") }
    var eDesc by remember { mutableStateOf("") }
    var eDate by remember { mutableStateOf("July 12, 2026") }
    var eTime by remember { mutableStateOf("07:00 AM") }
    var ePlace by remember { mutableStateOf("") }
    var eLimit by remember { mutableStateOf("100") }

    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { activeSubTab = "clubs"; showCreateForm = false },
                modifier = Modifier
                    .weight(1f)
                    .testTag("clubs_tab_btn")
            ) {
                Text(
                    "Cycling Clubs (${clubs.size})",
                    color = if (activeSubTab == "clubs") MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (activeSubTab == "clubs") FontWeight.ExtraBold else FontWeight.Medium
                )
            }
            TextButton(
                onClick = { activeSubTab = "events"; showCreateForm = false },
                modifier = Modifier
                    .weight(1f)
                    .testTag("events_tab_btn")
            ) {
                Text(
                    "Upcoming Events (${events.size})",
                    color = if (activeSubTab == "events") MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (activeSubTab == "events") FontWeight.ExtraBold else FontWeight.Medium
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (activeSubTab == "clubs") "Connect with Clubs" else "Register for Rides",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Button(
                onClick = { showCreateForm = !showCreateForm },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("create_community_element_btn")
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (activeSubTab == "clubs") "Create Club" else "Schedule Event", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Interactive Composer Form
        AnimatedVisibility(visible = showCreateForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .border(1.dp, CwcGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (activeSubTab == "clubs") "Inaugurate New Cycling Club" else "Schedule Structured Group Ride",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (activeSubTab == "clubs") {
                        OutlinedTextField(
                            value = cName,
                            onValueChange = { cName = it },
                            label = { Text("Club Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("club_name_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = cDesc,
                            onValueChange = { cDesc = it },
                            label = { Text("Base Description & Goals") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("club_desc_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = cLoc,
                            onValueChange = { cLoc = it },
                            label = { Text("Location City") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("club_loc_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                    } else {
                        OutlinedTextField(
                            value = eTitle,
                            onValueChange = { eTitle = it },
                            label = { Text("Event Title") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_title_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = eDesc,
                            onValueChange = { eDesc = it },
                            label = { Text("Event Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_desc_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = eDate,
                                onValueChange = { eDate = it },
                                label = { Text("Date") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("event_date_field"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                            )
                            OutlinedTextField(
                                value = eTime,
                                onValueChange = { eTime = it },
                                label = { Text("Time") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("event_time_field"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = ePlace,
                            onValueChange = { ePlace = it },
                            label = { Text("Assembling spot / Location") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("event_loc_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (activeSubTab == "clubs") {
                                if (cName.isNotBlank()) {
                                    viewModel.createClub(cName, cDesc, cLoc)
                                    cName = ""
                                    cDesc = ""
                                    showCreateForm = false
                                }
                            } else {
                                if (eTitle.isNotBlank()) {
                                    val lim = eLimit.toIntOrNull() ?: 50
                                    viewModel.createNewEvent(eTitle, eDesc, eDate, eTime, ePlace, lim)
                                    eTitle = ""
                                    eDesc = ""
                                    ePlace = ""
                                    showCreateForm = false
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Text("Publish Community Node", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Core dynamic lists
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            if (activeSubTab == "clubs") {
                items(clubs) { club ->
                    ClubCard(club = club, onToggleJoin = { viewModel.joinOrLeaveClub(club.id) })
                }
            } else {
                items(events) { event ->
                    EventCard(event = event, onRegisterToggle = { viewModel.registerEvent(event.id) })
                }
            }
        }
    }
}

@Composable
fun ClubCard(club: ClubEntity, onToggleJoin: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(club.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        if (club.id == 1L) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.Verified, contentDescription = "Verified Club", tint = CwcGreen, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text(club.location, fontSize = 11.sp, color = Color.Gray)
                }

                Button(
                    onClick = onToggleJoin,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (club.isJoined) Color.Gray else MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("club_join_btn_${club.id}")
                ) {
                    Text(if (club.isJoined) "Joined ✓" else "Join Club", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(club.description, fontSize = 13.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.People, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${club.membersCount} cyclists associated", fontSize = 12.sp, color = Color.LightGray, fontWeight = FontWeight.Bold)
            }

            // Recent announcement banner
            if (club.recentAnnouncement.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CwcGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Campaign, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Club Announcement", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CwcGreen)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(club.recentAnnouncement, fontSize = 11.5.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun EventCard(event: EventEntity, onRegisterToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(event.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                    Text("Scheduled by: ${event.organizer}", fontSize = 11.sp, color = Color.Gray)
                }

                if (!event.isCancelled) {
                    Button(
                        onClick = onRegisterToggle,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (event.isRegistered) Color.Gray.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primary,
                            contentColor = if (event.isRegistered) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("event_reg_btn_${event.id}")
                    ) {
                        Text(if (event.isRegistered) "Attending ✓" else "Register", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color.Red.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Cancelled", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(event.description, fontSize = 13.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Time and spot stats
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${event.dateText} at ${event.timeText}", fontSize = 12.sp, color = Color.White)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Place, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(event.location, fontSize = 12.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("RSVPs: ${event.registeredCount} / ${event.registrationLimit}", fontSize = 12.sp, color = Color.LightGray)
                }
            }
        }
    }
}

// ==========================================
// 4. CYCLING MARKETPLACE CLASSIFIEDS
// ==========================================
@Composable
fun MarketplaceScreen(viewModel: CwcViewModel) {
    val items by viewModel.marketplaceItems.collectAsState()

    var activeCategory by remember { mutableStateOf("All") }
    var showSellForm by remember { mutableStateOf(false) }

    var itemTitle by remember { mutableStateOf("") }
    var itemCat by remember { mutableStateOf("Road Bikes") }
    var itemPrice by remember { mutableStateOf("") }
    var itemPhone by remember { mutableStateOf("") }
    var itemDesc by remember { mutableStateOf("") }

    val categories = listOf("All", "Road Bikes", "Mountain Bikes", "Helmets", "Cycling Gear", "Accessories")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Cyclists' Marketplace", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Button(
                onClick = { showSellForm = !showSellForm },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("list_marketplace_item_btn")
            ) {
                Icon(Icons.Filled.Sell, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Post Listing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Categories filters row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = activeCategory == cat
                Box(
                    modifier = Modifier
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { activeCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        cat,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.Black else Color.White
                    )
                }
            }
        }

        // Sell Listings Composer
        AnimatedVisibility(visible = showSellForm) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .border(1.dp, CwcGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("List New Gear for Sale", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = itemTitle,
                        onValueChange = { itemTitle = it },
                        label = { Text("Product Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_title_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = itemPrice,
                        onValueChange = { itemPrice = it },
                        label = { Text("Price (USD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = itemPhone,
                        onValueChange = { itemPhone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = itemDesc,
                        onValueChange = { itemDesc = it },
                        label = { Text("Description (Condition, Size, Speeds...)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_desc_input"),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val pr = itemPrice.toDoubleOrNull() ?: 10.0
                            if (itemTitle.isNotBlank()) {
                                viewModel.sellItem(itemTitle, itemCat, pr, itemDesc, itemPhone)
                                itemTitle = ""
                                itemPrice = ""
                                itemPhone = ""
                                itemDesc = ""
                                showSellForm = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("marketplace_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Text("List Equipment (Ready for Admin Review)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        val filteredItems = if (activeCategory == "All") items else items.filter { it.category == activeCategory }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No gears listed in this category.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredItems) { item ->
                    MarketItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun MarketItemCard(item: MarketplaceItemEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                    Text("Listed by: ${item.sellerName} in ${item.location}", fontSize = 11.sp, color = Color.Gray)
                }

                // Price Badge
                Box(
                    modifier = Modifier
                        .background(CwcGreen.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        "$${String.format(Locale.US, "%.0f", item.priceUsd)}",
                        fontWeight = FontWeight.Black,
                        color = CwcGreen,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(item.description, fontSize = 13.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(12.dp))

            // Procedural Status or verification indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(item.category, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (!item.isApproved) {
                    Box(
                        modifier = Modifier
                            .background(Color.Yellow.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Review Pending", color = Color.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .background(CwcGreen, RoundedCornerShape(8.dp))
                            .clickable { /* action calling simulation */ }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Text("Call Seller", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. PERSONAL PROFILE & CYCLING HISTORY
// ==========================================
@Composable
fun ProfileScreen(viewModel: CwcViewModel) {
    val profile by viewModel.userProfile.collectAsState()
    val rides by viewModel.rides.collectAsState()

    var showEditProfile by remember { mutableStateOf(false) }

    // local editing states
    var pName by remember { mutableStateOf("") }
    var pBio by remember { mutableStateOf("") }
    var pLevel by remember { mutableStateOf("") }
    var pBike by remember { mutableStateOf("") }
    var pEName by remember { mutableStateOf("") }
    var pEPhone by remember { mutableStateOf("") }

    if (profile == null) return

    val uProfile = profile!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp)
    ) {
        // --- COVER GRAPHIC ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Brush.linearGradient(colors = listOf(CwcDarkGreen, CwcBlack)))
                .drawBehind {
                    // Procedural road graphic details
                    val h = size.height
                    val w = size.width
                    drawLine(Color.White.copy(alpha = 0.15f), Offset(0f, h * 0.7f), Offset(w, h * 0.7f), strokeWidth = 4f)
                }
        )

        // --- AVATAR & METRICS HEADER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-40).dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Profile circular thumbnail
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(Color.Black, CircleShape)
                    .border(3.dp, CwcGreen, CircleShape)
                    .background(CwcGreen.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.DirectionsBike, contentDescription = null, tint = CwcGreen, modifier = Modifier.size(44.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.padding(bottom = 4.dp)) {
                Text(uProfile.fullName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("@${uProfile.username}", fontSize = 12.sp, color = CwcGreen, fontWeight = FontWeight.Bold)
                Text(uProfile.location, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
            }
        }

        // --- PROFILE CONTROL / EDIT SHEET TRIGGER ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .offset(y = (-20).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    pName = uProfile.fullName
                    pBio = uProfile.bio
                    pLevel = uProfile.cyclingLevel
                    pBike = uProfile.bikeType
                    pEName = uProfile.emergencyContactName
                    pEPhone = uProfile.emergencyContactPhone
                    showEditProfile = !showEditProfile
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("edit_profile_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Edit Profile Details", fontSize = 12.sp, color = Color.White)
            }

            Button(
                onClick = { viewModel.performLogout() },
                modifier = Modifier.testTag("logout_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Log Out", fontSize = 12.sp, color = Color.White)
            }
        }

        // Edit Profile Composer form
        AnimatedVisibility(visible = showEditProfile, modifier = Modifier.padding(horizontal = 24.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CwcGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Modify Cyclist Profile", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Full Name") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = pBio, onValueChange = { pBio = it }, label = { Text("Profile Bio") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = pLevel, onValueChange = { pLevel = it }, label = { Text("Riding Level") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = pBike, onValueChange = { pBike = it }, label = { Text("Riding Bike") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = pEName, onValueChange = { pEName = it }, label = { Text("SOS Safety Contact Name") })
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = pEPhone, onValueChange = { pEPhone = it }, label = { Text("SOS Contact Phone") })

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(pName, pBio, pLevel, pBike, pEName, pEPhone)
                            showEditProfile = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_profile_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black)
                    ) {
                        Text("Save Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- STATS OVERVIEW & SUMMARY ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Bio & Hardware Specs", fontWeight = FontWeight.Bold, color = CwcGreen, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(uProfile.bio, fontSize = 13.sp, color = Color.White)

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("CYCLING LEVEL", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(uProfile.cyclingLevel, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("HARDWARE RIG", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(uProfile.bikeType, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("EMERGENCY CONTACT", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(uProfile.emergencyContactName, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("CONTACT PHONE", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                        Text(uProfile.emergencyContactPhone, fontSize = 13.sp, color = CwcGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- DISTANCE ACHIEVEMENTS BADGES ---
        Text(
            text = "Elegance Achievements Mapped",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val unlocked = uProfile.achievementsString.split(",")
            val allBadges = listOf(
                "First Ride" to Icons.Filled.MilitaryTech,
                "50 KM" to Icons.Filled.AddRoad,
                "100 KM" to Icons.Filled.Speed,
                "Club Champion" to Icons.Filled.EmojiEvents
            )

            allBadges.forEach { badge ->
                val isUnlocked = unlocked.contains(badge.first)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUnlocked) Color(0xFF1B5E20) else Color.Gray.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = badge.second,
                            contentDescription = null,
                            tint = if (isUnlocked) GoldBadge else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = badge.first,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) Color.White else Color.Gray
                        )
                    }
                }
            }
        }

        // --- WORKOUTS CHRONICALLY LOGGED ---
        Text(
            text = "Your Logged GPS Rides (${rides.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 8.dp)
        )

        if (rides.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No rides logged yet. Power up the GPS tracker!", color = Color.Gray, fontSize = 13.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rides.forEach { ride ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(ride.dateText, fontWeight = FontWeight.Bold, color = CwcGreen, fontSize = 13.sp)
                                IconButton(
                                    onClick = { viewModel.deleteRide(ride.id) },
                                    modifier = Modifier.size(24.dp).testTag("delete_ride_${ride.id}")
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatSnippet(title = "DISTANCE", value = "${String.format(Locale.US, "%.1f", ride.distanceKm)} KM")
                                StatSnippet(title = "DURATION", value = "${ride.durationSeconds / 60}m")
                                StatSnippet(title = "SPEED AVG", value = "${String.format(Locale.US, "%.1f", ride.averageSpeedKmh)} kmh")
                                StatSnippet(title = "CLIMB", value = "${ride.elevationGainM}m")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatSnippet(title: String, value: String) {
    Column {
        Text(title, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
        Text(value, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 1.dp))
    }
}

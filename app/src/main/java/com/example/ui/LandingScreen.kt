package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CwcViewModel

@Composable
fun LandingScreen(
    viewModel: CwcViewModel,
    modifier: Modifier = Modifier
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var inputName by remember { mutableStateOf("") }
    var inputEmailPhone by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 50.dp)
        ) {
            // --- HERO BANNER & MISSION ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .drawBehind {
                        // Drawing subtle cycling grid trails prosed for atmosphere
                        val width = size.width
                        val height = size.height
                        drawRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF00E676).copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                        drawLine(
                            color = Color(0xFF00E676).copy(alpha = 0.3f),
                            start = Offset(0f, height * 0.9f),
                            end = Offset(width, height * 0.9f),
                            strokeWidth = 3f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(24.dp)
                ) {
                    // Logo Banner Icon
                    Icon(
                        imageVector = Icons.Filled.DirectionsBike,
                        contentDescription = "CWC Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(76.dp)
                            .background(Color(0xFF00E676).copy(alpha = 0.15f), RoundedCornerShape(24.dp))
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "UGANDA CYCLING NETWORK",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "TOGETHER WE CAN",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 3.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Connect. Ride. Track. Explore.",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // --- HERO CALL TO ACTION SEGMENTS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        isLoginMode = false
                        inputEmailPhone = ""
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("join_community_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Join Community", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        isLoginMode = true
                        authError = "Please register or login first to access the app and explore Ugandan routes."
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("explore_routes_btn"),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Explore Routes", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // --- AUTHENTICATION DIALOG CARD ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .border(1.dp, Color(0xFF00E676).copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = if (isLoginMode) "Secure Rider Login" else "Create Cycling Account",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = if (isLoginMode) "Enter credentials to access social feeds & GPS tools" else "Register to trace records and follow Ugandan clubs",
                        fontSize = 13.sp,
                        color = Color.LightGray.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    if (authError.isNotEmpty()) {
                        Text(
                            text = authError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    if (!isLoginMode) {
                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_field"),
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                focusedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = inputEmailPhone,
                        onValueChange = { inputEmailPhone = it },
                        label = { Text("Email or Phone Number") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_id_field"),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
                        placeholder = { Text("+256700000000 or cyclist@cwc.com") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = { inputPassword = it },
                        label = { Text("Password") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_field"),
                        shape = RoundedCornerShape(12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (inputEmailPhone.isBlank() || inputPassword.length < 4) {
                                authError = "Please enter valid credentials (min 4 character password)"
                            } else {
                                authError = ""
                                if (isLoginMode) {
                                    viewModel.performLogin(inputEmailPhone, inputPassword) { success, msg ->
                                        if (!success) {
                                            authError = msg
                                        }
                                    }
                                } else {
                                    val finalName = if (inputName.isBlank()) "New Cyclist" else inputName
                                    viewModel.performRegister(finalName, inputEmailPhone, inputPassword) { success, msg ->
                                        if (!success) {
                                            authError = msg
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isLoginMode) "Log In Securely" else "Register Now",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isLoginMode) "New to CWC?" else "Already have an account?",
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )
                        TextButton(
                            onClick = {
                                isLoginMode = !isLoginMode
                                authError = ""
                            }
                        ) {
                            Text(
                                text = if (isLoginMode) "Create Account" else "Log In",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- COMMUNITY LIVE STATS COOPERATION ---
            Text(
                text = "Community Telemetry",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Distance",
                    value = "24,530 KM",
                    icon = Icons.Filled.Timeline,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Active Members",
                    value = "1,240+",
                    icon = Icons.Filled.People,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Clubs Registered",
                    value = "38",
                    icon = Icons.Filled.DirectionsBike,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Annual Events",
                    value = "64 Runs",
                    icon = Icons.Filled.CalendarMonth,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- ABOUT US & MISSION STATEMENT ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Our Mission & Safety Focus", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To unite cyclists, improve rider safety, promote healthy cycling activities, and build the largest community platform in Uganda and beyond.",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Active tracking leverages on-device GPS capabilities paired with a sudden accident watchdog system, giving cyclists secure SOS alarms to warn emergency contacts of any danger.",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- NEWS & ANNOUNCEMENTS SECTION ---
            Text(
                text = "Network Announcements",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
            )

            NewsCard(
                title = "Uganda National Car-Free Sunday Slated",
                desc = "We are partnering with Kampala Capital City Authority (KCCA) to clear core urban segments. Safe routes and water points provided.",
                date = "June 20, 2026"
            )

            NewsCard(
                title = "New Gravel Speed Tracking Trails Added",
                desc = "Five premium off-highway loops mapped around Kajjansi and Mukono forest sectors for off-road riders. Safe, secure, and rated by community champions.",
                date = "June 14, 2026"
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            Text(text = title, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun NewsCard(
    title: String,
    desc: String,
    date: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, fontSize = 12.sp, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(text = date, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

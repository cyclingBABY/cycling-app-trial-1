package com.example.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.CwcViewModel
import com.example.data.MessageEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatScreen(viewModel: CwcViewModel) {
    val messages by viewModel.messages.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- CHAT CHANNEL PANEL TOP SUMMARY ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel group icon representation
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF00E676).copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = CwcGreen)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text("Uganda Cycling Network Feed", fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(CwcGreen, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("24 Cyclists Online now • Active read receipts", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }

        // --- MESSAGES HISTORY FEED ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            items(messages) { msg ->
                val isMyMessage = profile?.fullName == msg.senderName
                MessageBubbleCell(message = msg, isMe = isMyMessage)
            }
        }

        // --- SUBMIT ATTACHMENTS FOOTER ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                // Attachments selectors row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.sendChatMessage("📍 SHARED CYCLIST LOCATION (Kampala, Central segment)", isLocation = true) },
                        modifier = Modifier.testTag("chat_share_location_btn_quick")
                    ) {
                        Icon(Icons.Filled.MyLocation, contentDescription = "Share Location", tint = CwcGreen, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { viewModel.sendChatMessage("📷 Sent a photo of central Kampala traffic segment") }) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = "Share Image", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { viewModel.sendChatMessage("🎤 [Audio Voice Note: 0:12]") }) {
                        Icon(Icons.Filled.KeyboardVoice, contentDescription = "Share Audio", tint = Color.LightGray, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Core edit text bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatTextInput,
                        onValueChange = { chatTextInput = it },
                        placeholder = { Text("Compile messages...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        textStyle = TextStyle(fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    IconButton(
                        onClick = {
                            if (chatTextInput.isNotBlank()) {
                                viewModel.sendChatMessage(chatTextInput)
                                chatTextInput = ""
                            }
                        },
                        modifier = Modifier.testTag("chat_message_submit_btn")
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = CwcGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubbleCell(message: MessageEntity, isMe: Boolean) {
    val sdf = SimpleDateFormat("HH:mm", Locale.US)
    val timeText = sdf.format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        // Author tagline
        if (!isMe) {
            Text(
                text = message.senderName,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isMe) {
                Text(timeText, fontSize = 9.sp, color = Color.Gray)
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isMe) Color(0xFF00E676) else Color(0xFF1F2833)
                ),
                shape = RoundedCornerShape(
                    topStart = 14.dp,
                    topEnd = 14.dp,
                    bottomStart = if (isMe) 14.dp else 2.dp,
                    bottomEnd = if (isMe) 2.dp else 14.dp
                ),
                modifier = Modifier.widthIn(max = 260.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(
                        text = message.text,
                        fontSize = 13.sp,
                        color = if (isMe) Color.Black else Color.White
                    )

                    // Draw location map card attachment simulation if location segment
                    if (message.isLocationShare) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.4f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Filled.Place, contentDescription = null, tint = if (isMe) Color.Black else CwcGreen)
                                Text("Map node share", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isMe) Color.Black else Color.White)
                                Text("Kampala (0.32, 32.58)", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            }

            if (!isMe) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(timeText, fontSize = 9.sp, color = Color.Gray)
                    Icon(Icons.Filled.DoneAll, contentDescription = "Seen", tint = CwcGreen, modifier = Modifier.size(10.dp))
                }
            } else {
                Icon(Icons.Filled.DoneAll, contentDescription = "Read", tint = Color.Black, modifier = Modifier.size(10.dp))
            }
        }
    }
}

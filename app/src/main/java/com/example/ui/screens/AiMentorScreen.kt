package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MentorMessage
import com.example.ui.components.CyberBadge
import com.example.ui.theme.*
import com.example.viewmodel.SocViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AiMentorScreen(viewModel: SocViewModel) {
    val messages by viewModel.mentorMessages.collectAsState()
    val isThinking by viewModel.isMentorThinking.collectAsState()
    val activeIncident by viewModel.activeIncident.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    val quickPrompts = listOf(
        "Give me a progressive hint",
        "Explain active incident logs",
        "Challenge my investigative hypothesis",
        "How do I detect IAM PrivEsc?",
        "Explain Sysmon Event ID 10 (ProcessAccess)"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // Mentor Persona Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .border(1.dp, CyberBorder)
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberPurple.copy(alpha = 0.2f))
                        .border(1.5.dp, CyberPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Mentor",
                        tint = CyberPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Alex Vance",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = CyberTextPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        CyberBadge(text = "LEAD MENTOR", color = CyberGreen)
                    }
                    Text(
                        text = "Principal Detection Engineer | Active Case: ${activeIncident?.id ?: "General"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = CyberTextMuted
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {
            items(messages) { msg ->
                MentorMessageBubble(message = msg)
            }

            if (isThinking) {
                item {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardBg)
                            .border(1.dp, CyberPurple.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyberPurple,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Alex is analyzing forensic telemetry...",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = CyberTextMuted
                        )
                    }
                }
            }
        }

        // Quick Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberPurple.copy(alpha = 0.15f))
                        .border(1.dp, CyberPurple.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .clickable {
                            viewModel.sendMentorMessage(prompt)
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = CyberPurple
                    )
                }
            }
        }

        // Input Field Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberCardBg)
                .border(1.dp, CyberBorder)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask Alex a question, request a hint, or test a hypothesis...", fontSize = 12.sp, color = CyberTextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_mentor_input_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberPurple,
                        unfocusedBorderColor = CyberBorder,
                        focusedContainerColor = CyberTerminalBg,
                        unfocusedContainerColor = CyberTerminalBg,
                        focusedTextColor = CyberTextPrimary,
                        unfocusedTextColor = CyberTextPrimary
                    ),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendMentorMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberPurple)
                        .testTag("ai_mentor_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = CyberBg,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MentorMessageBubble(message: MentorMessage) {
    val isUser = message.sender == "USER"
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            Text(
                text = if (isUser) "You (Analyst)" else "Alex Vance (Senior Lead)",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = if (isUser) CyberCyan else CyberPurple
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeStr,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = CyberTextMuted
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isUser) CyberCyan.copy(alpha = 0.12f) else CyberSurface)
                .border(
                    1.dp,
                    if (isUser) CyberCyan.copy(alpha = 0.4f) else CyberPurple.copy(alpha = 0.4f),
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = if (!isUser && (message.text.contains("•") || message.text.contains("["))) FontFamily.Monospace else FontFamily.SansSerif,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                color = CyberTextPrimary
            )
        }
    }
}

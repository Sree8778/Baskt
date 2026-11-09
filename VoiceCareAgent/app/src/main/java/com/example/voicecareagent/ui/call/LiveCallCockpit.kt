package com.example.voicecareagent.ui.call

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicecareagent.model.*
import com.example.voicecareagent.telephony.ActiveCallState

@Composable
fun LiveCallCockpit(
    state: ActiveCallState,
    onWhisper: (String) -> Unit,
    onToggleTakeover: () -> Unit,
    onToggleMute: () -> Unit,
    onEndCall: () -> Unit,
    onOpenDialer: () -> Unit = {},
    onCalleeSpeech: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var whisperInput by remember { mutableStateOf("") }

    LaunchedEffect(state.transcript.size) {
        if (state.transcript.isNotEmpty()) {
            listState.animateScrollToItem(state.transcript.size - 1)
        }
    }

    val mission = state.mission ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A10))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Call Header: Target Entity & Call Timer
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF141624)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(22.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val avatarColor = Color(mission.friendContact?.avatarColorHex ?: (mission.target?.brandColorHex ?: 0xFF0A84FF))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(avatarColor)
                        ) {
                            Icon(
                                imageVector = if (mission.callMode == CallMode.FRIENDS_AND_CONTACTS) Icons.Default.Person else Icons.Default.Call,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = mission.calleeDisplayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = mission.calleePhoneNumber,
                                fontSize = 12.sp,
                                color = Color(0xFF8E8E93)
                            )
                        }
                    }

                    // Cellular Line & Call Duration Timer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1F2236))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                .clickable { onOpenDialer() }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneForwarded,
                                contentDescription = "Open Dial Line",
                                tint = Color(0xFF30D158),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1F2236))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = state.formattedDuration,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF64D2FF)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Active Stage & Dynamic Turn Badge
                val stageColor = when {
                    state.isListeningForCallee -> Color(0xFF30D158)
                    state.isTtsSpeaking -> Color(0xFF0A84FF)
                    state.isRepSpeaking -> Color(0xFFFF9F0A)
                    state.stage == CallStage.DIALING -> Color(0xFFFF9F0A)
                    state.stage == CallStage.NAVIGATING_IVR -> Color(0xFF64D2FF)
                    state.stage == CallStage.ON_HOLD -> Color(0xFFBF5AF2)
                    state.stage == CallStage.CONNECTED -> Color(0xFF30D158)
                    state.stage == CallStage.CONVERSING -> Color(0xFF0A84FF)
                    state.stage == CallStage.CONFIRMING_RESOLUTION -> Color(0xFF30D158)
                    state.stage == CallStage.COMPLETED -> Color(0xFF30D158)
                    state.stage == CallStage.FAILED -> Color(0xFFFF453A)
                    else -> Color(0xFF8E8E93)
                }

                val statusLabel = when {
                    state.isListeningForCallee -> "🎙️ Listening to ${mission.calleeDisplayName}... (Speak anytime)"
                    state.isTtsSpeaking -> "🗣️ AI Agent Speaking..."
                    state.isRepSpeaking -> "👂 ${mission.calleeDisplayName} Speaking..."
                    else -> state.stage.displayTitle
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(stageColor.copy(alpha = 0.15f))
                        .border(1.dp, stageColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(stageColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = stageColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chromatic Waveform Audio Visualizer
        WaveformVisualizer(
            isTtsSpeaking = state.isTtsSpeaking,
            isRepSpeaking = state.isRepSpeaking,
            stage = state.stage
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Live Transcript Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "LIVE CALL TRANSCRIPT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF8E8E93),
                letterSpacing = 0.8.sp
            )
            if (state.isUserTakeover) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF453A).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFFFF453A), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "YOU ARE SPEAKING DIRECTLY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF453A)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Transcript Entries Feed
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            items(state.transcript, key = { it.id }) { entry ->
                TranscriptBubble(entry = entry)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Turn Input & Quick Chips for Friends mode
        if (mission.callMode == CallMode.FRIENDS_AND_CONTACTS) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 3.dp)
            ) {
                listOf(
                    "Yeah, what's up?",
                    "Saturday 7 PM works!",
                    "Who is this again?",
                    "I'm busy, call me later",
                    "Sounds awesome!"
                ).forEach { phrase ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E2338))
                            .border(1.dp, Color(0xFF30D158).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .clickable { onCalleeSpeech(phrase) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "🗣️ \"$phrase\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF30D158)
                        )
                    }
                }
            }
        }

        // Quick Whisper Guidance Chips
        val quickWhispers = listOf(
            "Insist on card refund",
            "Ask for supervisor",
            "Ask for \$10 courtesy credit",
            "Request email confirmation"
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            quickWhispers.take(3).forEach { whisper ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141624))
                        .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onWhisper(whisper) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = whisper,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64D2FF),
                        maxLines = 1
                    )
                }
            }
        }

        // Whisper Input Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            OutlinedTextField(
                value = whisperInput,
                onValueChange = { whisperInput = it },
                placeholder = { Text("🤫 Whisper guidance to AI agent...", fontSize = 12.sp, color = Color(0xFF636366)) },
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF141624),
                    unfocusedContainerColor = Color(0xFF141624),
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedBorderColor = Color(0x33FFFFFF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (whisperInput.isNotBlank()) {
                                onWhisper(whisperInput)
                                whisperInput = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Whisper", tint = Color(0xFF0A84FF))
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Call Action Control Bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Take Over Call Toggle
            Button(
                onClick = onToggleTakeover,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isUserTakeover) Color(0xFFFF9F0A) else Color(0xFF1C1D2A)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x33FFFFFF)),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (state.isUserTakeover) Icons.Default.Mic else Icons.Default.Hearing,
                    contentDescription = null,
                    tint = if (state.isUserTakeover) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state.isUserTakeover) "AI Resume" else "Take Over",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (state.isUserTakeover) Color.Black else Color.White
                )
            }

            // Mute Button
            IconButton(
                onClick = onToggleMute,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (state.isMuted) Color(0xFFFF453A).copy(alpha = 0.2f) else Color(0xFF1C1D2A))
                    .border(1.dp, if (state.isMuted) Color(0xFFFF453A) else Color(0x33FFFFFF), CircleShape)
            ) {
                Icon(
                    imageVector = if (state.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute",
                    tint = if (state.isMuted) Color(0xFFFF453A) else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // End Call Button
            Button(
                onClick = onEndCall,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF453A)),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("End Call", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun TranscriptBubble(entry: TranscriptEntry) {
    val (bubbleBg, borderColor, titleColor) = when (entry.speaker) {
        TranscriptSpeaker.AI_AGENT -> Triple(Color(0xFF0D2545), Color(0xFF0A84FF).copy(alpha = 0.5f), Color(0xFF64D2FF))
        TranscriptSpeaker.CONTACT_OR_REP -> Triple(Color(0xFF281E14), Color(0xFFFF9F0A).copy(alpha = 0.5f), Color(0xFFFF9F0A))
        TranscriptSpeaker.IVR_AUTOMATION -> Triple(Color(0xFF141620), Color(0x22FFFFFF), Color(0xFF8E8E93))
        TranscriptSpeaker.USER_WHISPER -> Triple(Color(0xFF0D2818), Color(0xFF30D158), Color(0xFF30D158))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bubbleBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = entry.speakerLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor
                )
                Text(
                    text = entry.timestamp,
                    fontSize = 9.sp,
                    color = Color(0xFF636366)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = entry.text,
                fontSize = 13.sp,
                color = Color.White,
                lineHeight = 18.sp
            )
        }
    }
}

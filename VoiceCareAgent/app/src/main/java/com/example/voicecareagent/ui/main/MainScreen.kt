package com.example.voicecareagent.ui.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.voicecareagent.model.*
import com.example.voicecareagent.ui.call.CallSummaryDialog
import com.example.voicecareagent.ui.call.LiveCallCockpit
import java.util.Locale

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val callState by viewModel.callState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        val callPhoneGranted = permissions[Manifest.permission.CALL_PHONE] ?: false
        val readContactsGranted = permissions[Manifest.permission.READ_CONTACTS] ?: false

        if (readContactsGranted) {
            viewModel.loadDeviceContacts()
        }
        if (recordAudioGranted && callPhoneGranted) {
            viewModel.initiateCall()
        }
    }

    val onStartCallClick: () -> Unit = {
        val hasAudio = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasCall = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

        if (hasAudio && hasCall) {
            viewModel.initiateCall()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.CALL_PHONE, Manifest.permission.READ_CONTACTS)
            )
        }
    }

    // Active In-Call Cockpit View
    if (callState.isCallActive) {
        LiveCallCockpit(
            state = callState,
            onWhisper = { viewModel.whisperToAgent(it) },
            onToggleTakeover = { viewModel.toggleTakeover() },
            onToggleMute = { viewModel.toggleMute() },
            onEndCall = { viewModel.endCall() },
            onOpenDialer = { viewModel.openSystemDialer() },
            onCalleeSpeech = { viewModel.sendCalleeSpeech(it) },
            modifier = modifier
        )
    } else {
        // Pre-Call Setup & Mode Selector Dashboard
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(Color(0xFF090A10))
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Header: App Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF30D158))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "VOICECARE AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64D2FF),
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Autonomous Voice Agent",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF141624))
                        .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Live Duplex Ready",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF30D158)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mode Selector: Call Friends vs Customer Care
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141624))
                    .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                val isFriendsMode = state.callMode == CallMode.FRIENDS_AND_CONTACTS
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFriendsMode) Color(0xFF0A84FF) else Color.Transparent)
                        .clickable { viewModel.switchCallMode(CallMode.FRIENDS_AND_CONTACTS) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = if (isFriendsMode) Color.White else Color(0xFF8E8E93),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Friends & Contacts",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFriendsMode) Color.White else Color(0xFF8E8E93)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (!isFriendsMode) Color(0xFF0A84FF) else Color.Transparent)
                        .clickable { viewModel.switchCallMode(CallMode.CUSTOMER_CARE) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.HeadsetMic,
                            contentDescription = null,
                            tint = if (!isFriendsMode) Color.White else Color(0xFF8E8E93),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Customer Care",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isFriendsMode) Color.White else Color(0xFF8E8E93)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MODE 1: CALL FRIENDS & CONTACTS
            if (state.callMode == CallMode.FRIENDS_AND_CONTACTS) {
                // Section Header: Select Friend or Contact (Safe Allowlist)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "AUTHORIZED TEST CONTACTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF8E8E93),
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🛡️ Safe Mode: Only selected contacts can be called",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF30D158)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0A84FF).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .clickable { viewModel.openContactPicker() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "+ Select Contacts",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64D2FF)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1E2338))
                                .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .clickable { viewModel.openCustomContactDialog() }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "+ Custom",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Contacts in Safe Allowlist
                OutlinedTextField(
                    value = state.contactSearchQuery,
                    onValueChange = { viewModel.setContactSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Filter authorized test contacts...",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Contacts",
                            tint = Color(0xFF64D2FF),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (state.contactSearchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.clearContactSearch() }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Search",
                                    tint = Color(0xFF8E8E93),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF141624),
                        unfocusedContainerColor = Color(0xFF141624),
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedBorderColor = Color(0x33FFFFFF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                val displayedContacts = state.filteredContacts

                if (displayedContacts.isEmpty()) {
                    // Empty search state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF141624))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No authorized contacts match \"${state.contactSearchQuery}\"",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF8E8E93)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Tap '+ Select Contacts' above to add from your phonebook",
                                fontSize = 11.sp,
                                color = Color(0xFF64D2FF)
                            )
                        }
                    }
                } else {
                    // Contacts Carousel (filtered by search query in real-time)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        displayedContacts.forEach { contact ->
                            val isSelected = contact.id == state.selectedContact.id
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0xFF1E2338) else Color(0xFF141624)
                                ),
                                modifier = Modifier
                                    .width(185.dp)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF0A84FF) else Color(0x22FFFFFF),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { viewModel.selectContact(contact) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(contact.avatarColorHex))
                                            ) {
                                                Text(
                                                    text = contact.name.take(2).uppercase(),
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = contact.name,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = contact.relation,
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF8E8E93),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // Allow removing contact from safe test list
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove contact from testing list",
                                            tint = Color(0xFF8E8E93),
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.removeContactFromAllowlist(contact) }
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = contact.phoneNumber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF64D2FF),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Selected Contact Confirmation Strip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A84FF).copy(alpha = 0.12f))
                        .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF30D158))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Selected: ${state.selectedContact.name} • ${state.selectedContact.phoneNumber}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    if (state.contactSearchQuery.isNotBlank()) {
                        Text(
                            text = "${displayedContacts.size} found",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64D2FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Topic Selector
                Text(
                    text = "CONVERSATION TOPIC / INSTRUCTION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preset Topic Quick Chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    state.presetTopics.forEach { topic ->
                        val isSelected = state.personalTopic == topic
                        val chipLabel = when {
                            topic.contains("dinner") -> "🍕 Dinner Plans"
                            topic.contains("tuition") -> "📚 Tuition Schedule"
                            topic.contains("Birthday") -> "🎂 Birthday Wish"
                            topic.contains("resume") -> "📄 Resume Follow-up"
                            topic.contains("landlord") -> "🔧 AC Maintenance"
                            else -> topic.take(16) + "..."
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFF0A84FF) else Color(0xFF141624))
                                .border(1.dp, if (isSelected) Color(0xFF0A84FF) else Color(0x22FFFFFF), RoundedCornerShape(14.dp))
                                .clickable { viewModel.setPersonalTopic(topic) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = chipLabel,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF8E8E93)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Editable Topic Prompt Card
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141624)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "What should the AI say & discuss with ${state.selectedContact.name}?",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.personalTopic,
                            onValueChange = { viewModel.setPersonalTopic(it) },
                            placeholder = { Text("E.g. Call my friend to confirm dinner this weekend at 7 PM...", fontSize = 12.sp, color = Color(0xFF636366)) },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF090A10),
                                unfocusedContainerColor = Color(0xFF090A10),
                                focusedBorderColor = Color(0xFF0A84FF),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Tone Selector
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Agent Tone:", fontSize = 12.sp, color = Color(0xFF8E8E93))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Warm & Casual", "Polite & Brief", "Excited").forEach { tone ->
                                    val isToneSelected = state.relationshipTone == tone
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isToneSelected) Color(0xFF30D158).copy(alpha = 0.2f) else Color(0xFF1F2236))
                                            .border(1.dp, if (isToneSelected) Color(0xFF30D158) else Color(0x22FFFFFF), RoundedCornerShape(10.dp))
                                            .clickable { viewModel.setRelationshipTone(tone) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = tone,
                                            fontSize = 10.sp,
                                            fontWeight = if (isToneSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToneSelected) Color(0xFF30D158) else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button for Friends Call
                Button(
                    onClick = onStartCallClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Call ${state.selectedContact.name} on this Topic",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

            } else {
                // MODE 2: CUSTOMER CARE & BUSINESSES
                // Section 1: Customer Care Target Lines
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SELECT SUPPORT LINE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF8E8E93),
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "+ Custom Line",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A84FF),
                        modifier = Modifier.clickable { viewModel.openCustomTargetDialog() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Targets Carousel
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    state.targets.forEach { target ->
                        val isSelected = target.id == state.selectedTarget.id
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E2338) else Color(0xFF141624)
                            ),
                            modifier = Modifier
                                .width(180.dp)
                                .border(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF0A84FF) else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { viewModel.selectTarget(target) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(Color(target.brandColorHex))
                                    )
                                    Text(
                                        text = target.defaultHoldTime,
                                        fontSize = 10.sp,
                                        color = Color(0xFF8E8E93)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = target.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    text = target.phoneNumber,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64D2FF)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Mission Details Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141624)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Call Evidence & Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = state.orderNumber,
                            onValueChange = { viewModel.updateOrderNumber(it) },
                            label = { Text("Order / Account #") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.itemDetails,
                            onValueChange = { viewModel.updateItemDetails(it) },
                            label = { Text("Items Damaged / Issue Description") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = String.format(Locale.US, "%.2f", state.requestedAmount),
                            onValueChange = {
                                val amt = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateRequestedAmount(amt)
                            },
                            label = { Text("Target Refund ($)") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Button for Customer Care
                Button(
                    onClick = onStartCallClick,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Call ${state.selectedTarget.name} with AI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Call Activity & Resolution History
            Text(
                text = "RECENT CALL RESOLUTIONS & CHATS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF8E8E93),
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            state.pastCallRecords.forEach { record ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF141624)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (record.callMode == CallMode.FRIENDS_AND_CONTACTS) Color(0xFF0A84FF) else Color(0xFF30D158))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (record.callMode == CallMode.FRIENDS_AND_CONTACTS) "FRIEND CALL" else "SUPPORT CALL",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = record.targetName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            if (record.refundSecured > 0.0) {
                                Text(
                                    text = "+\$${String.format(Locale.US, "%.2f", record.refundSecured)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF30D158)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Topic: ${record.topicOrMission}",
                            fontSize = 12.sp,
                            color = Color(0xFF64D2FF)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = record.outcomeSummary,
                            fontSize = 11.sp,
                            color = Color(0xFF8E8E93)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = record.dateFormatted,
                            fontSize = 10.sp,
                            color = Color(0xFF636366)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Call Summary Dialog
    if (state.showCallSummaryDialog) {
        CallSummaryDialog(
            state = callState,
            onDismiss = { viewModel.dismissSummary() }
        )
    }

    // Custom Contact Dialog
    if (state.showCustomContactDialog) {
        var friendName by remember { mutableStateOf("") }
        var friendPhone by remember { mutableStateOf("") }
        var friendRelation by remember { mutableStateOf("Friend") }

        AlertDialog(
            onDismissRequest = { viewModel.closeCustomContactDialog() },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(22.dp),
            title = {
                Text("Add Contact to Call", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = friendName,
                        onValueChange = { friendName = it },
                        label = { Text("Contact Name (e.g. Alex)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = friendPhone,
                        onValueChange = { friendPhone = it },
                        label = { Text("Phone Number (e.g. +1 561-555-0192)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = friendRelation,
                        onValueChange = { friendRelation = it },
                        label = { Text("Relation (e.g. Friend, Sister, Colleague)") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addCustomContact(friendName, friendPhone, friendRelation) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                ) {
                    Text("Save & Select", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeCustomContactDialog() }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // Custom Target Dial Dialog
    if (state.showCustomTargetDialog) {
        var customName by remember { mutableStateOf("") }
        var customPhone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.closeCustomTargetDialog() },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(22.dp),
            title = {
                Text("Add Custom Support Line", fontWeight = FontWeight.Bold, color = Color.White)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Company or Service Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customPhone,
                        onValueChange = { customPhone = it },
                        label = { Text("Support Phone Number") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addCustomTarget(customName, customPhone) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                ) {
                    Text("Add & Select", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeCustomTargetDialog() }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // Explicit Device Contact Picker Dialog (Authorizes only user-selected contacts for testing)
    if (state.showContactPickerDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeContactPicker() },
            containerColor = Color(0xFF141624),
            shape = RoundedCornerShape(22.dp),
            title = {
                Column {
                    Text(
                        text = "Authorize Contacts for Testing",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select only the specific contacts you wish to test with. All other contacts remain protected.",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    OutlinedTextField(
                        value = state.pickerSearchQuery,
                        onValueChange = { viewModel.setPickerSearchQuery(it) },
                        placeholder = { Text("Filter phonebook contacts...", fontSize = 12.sp, color = Color(0xFF8E8E93)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64D2FF),
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            if (state.pickerSearchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setPickerSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color(0xFF8E8E93),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF090A10),
                            unfocusedContainerColor = Color(0xFF090A10),
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (state.isPickerLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFF0A84FF),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    } else {
                        val filtered = state.filteredPickerContacts
                        if (filtered.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No matching contacts found in phonebook",
                                    fontSize = 12.sp,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                items(filtered, key = { it.id }) { contact ->
                                    val isChecked = state.pendingPickerSelection.contains(contact.id)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isChecked) Color(0xFF1E2338) else Color(0xFF090A10))
                                            .border(
                                                width = 1.dp,
                                                color = if (isChecked) Color(0xFF0A84FF) else Color(0x22FFFFFF),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable { viewModel.togglePickerContactSelection(contact.id) }
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { viewModel.togglePickerContactSelection(contact.id) },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFF0A84FF),
                                                checkmarkColor = Color.White
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(Color(contact.avatarColorHex))
                                        ) {
                                            Text(
                                                text = contact.name.take(2).uppercase(),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = contact.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color.White,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = contact.phoneNumber,
                                                fontSize = 11.sp,
                                                color = Color(0xFF64D2FF),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                val count = state.pendingPickerSelection.size
                Button(
                    onClick = { viewModel.confirmPickerSelection() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                    enabled = count > 0
                ) {
                    Text(
                        text = if (count > 0) "Authorize ($count Selected)" else "Select Contacts",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeContactPicker() }) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }
}

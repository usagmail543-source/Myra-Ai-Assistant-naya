package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.MayraRepository
import com.example.model.ActionProposal
import com.example.model.ActionType
import com.example.model.ChatMessage
import com.example.model.CompanionMode
import com.example.model.Sender
import com.example.network.GeminiClient
import com.example.service.AllowanceManager
import com.example.service.DeviceActionDispatcher
import com.example.service.SpeechAssistant
import com.example.ui.components.ActionProposalCard
import com.example.ui.components.AllowanceBanner
import com.example.ui.components.MayraOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.MayraAmberGold
import com.example.ui.theme.MayraCyanAccent
import com.example.ui.theme.MayraCyanGlow
import com.example.ui.theme.MayraErrorRed
import com.example.ui.theme.MayraNavyBorder
import com.example.ui.theme.MayraNavyCard
import com.example.ui.theme.MayraNavyDark
import com.example.ui.theme.MayraNavyElevated
import com.example.ui.theme.MayraPinkAccent
import com.example.ui.theme.MayraPurpleGlow
import com.example.ui.theme.MayraPurplePrimary
import com.example.ui.theme.MayraSuccessGreen
import com.example.ui.theme.MayraTextMuted
import com.example.ui.theme.MayraTextPrimary
import com.example.ui.theme.MayraTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CompanionScreen(
    repository: MayraRepository,
    speechAssistant: SpeechAssistant,
    actionDispatcher: DeviceActionDispatcher,
    allowanceManager: AllowanceManager,
    onNavigateToBuyPro: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages by repository.chatMessages.collectAsState()
    val activeMode by repository.activeMode.collectAsState()
    val activeLang by repository.activeLanguage.collectAsState()
    val memories by repository.memories.collectAsState()
    val isAssistantActive by repository.isAssistantActive.collectAsState()
    val autoExecuteEnabled by repository.autoExecuteEnabled.collectAsState()
    val isPro = repository.isProUser()

    val remainingSeconds by allowanceManager.remainingSeconds.collectAsState()
    val isAllowanceLocked by allowanceManager.isAllowanceLocked.collectAsState()
    val isSpeaking by speechAssistant.isSpeaking.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }
    var isListeningAudio by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchingHistory by remember { mutableStateOf(false) }

    // Derive Orb State
    val orbState = when {
        !isAssistantActive -> OrbState.IDLE
        isListeningAudio -> OrbState.LISTENING
        isThinking -> OrbState.THINKING
        isSpeaking -> OrbState.SPEAKING
        else -> OrbState.IDLE
    }

    // Scroll to bottom on new messages
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Speech-to-text launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListeningAudio = false
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                textInput = spoken
                sendMessage(
                    text = spoken,
                    isVoice = true,
                    repository = repository,
                    speechAssistant = speechAssistant,
                    allowanceManager = allowanceManager,
                    actionDispatcher = actionDispatcher,
                    isPro = isPro,
                    onThinking = { isThinking = it }
                )
                textInput = ""
            }
        }
    }

    // Audio Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchVoiceRecognizer(context, speechLauncher, activeLang.code)
            isListeningAudio = true
        } else {
            Toast.makeText(context, "Microphone permission is required for voice commands.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MayraNavyDark)
            .imePadding()
    ) {
        // Prominent Assistant Activation & Auto Execute Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MayraNavyCard
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Assistant Status Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        val newState = !isAssistantActive
                        repository.isAssistantActive.value = newState
                        if (!newState) {
                            speechAssistant.stop()
                            Toast.makeText(context, "Mayra Assistant Deactivated (Standby)", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Mayra Assistant Activated! 🟢", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAssistantActive) MayraSuccessGreen else MayraTextMuted,
                        modifier = Modifier.size(10.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isAssistantActive) "MAYRA ACTIVE" else "MAYRA STANDBY",
                        color = if (isAssistantActive) MayraSuccessGreen else MayraTextMuted,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                // Auto Execute Indicator & History Search Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Auto Execute Chip
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (autoExecuteEnabled) MayraCyanAccent.copy(alpha = 0.2f) else MayraNavyElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (autoExecuteEnabled) MayraCyanAccent.copy(alpha = 0.5f) else MayraNavyBorder
                        ),
                        modifier = Modifier.clickable {
                            val next = !autoExecuteEnabled
                            repository.autoExecuteEnabled.value = next
                            Toast.makeText(context, if (next) "Auto Execute: ON (Actions run directly)" else "Auto Execute: OFF (Confirmations shown)", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = if (autoExecuteEnabled) MayraCyanAccent else MayraTextMuted, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (autoExecuteEnabled) "Auto Run: ON" else "Auto Run: OFF",
                                color = if (autoExecuteEnabled) MayraCyanAccent else MayraTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Search history toggle
                    IconButton(
                        onClick = { isSearchingHistory = !isSearchingHistory },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchingHistory) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search Chat",
                            tint = if (isSearchingHistory) MayraAmberGold else MayraTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Search Bar in History
        AnimatedVisibility(visible = isSearchingHistory) {
            Surface(color = MayraNavyElevated, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search messages...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f).height(46.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MayraTextPrimary,
                            unfocusedTextColor = MayraTextPrimary,
                            focusedBorderColor = MayraCyanAccent,
                            unfocusedBorderColor = MayraNavyBorder,
                            focusedContainerColor = MayraNavyDark,
                            unfocusedContainerColor = MayraNavyDark
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        // Daily Allowance Tracker Bar
        AllowanceBanner(
            remainingSeconds = remainingSeconds,
            isLocked = isAllowanceLocked && !isPro,
            isPro = isPro,
            nextResetString = allowanceManager.getNextResetTimeString(),
            onUpgradeClick = onNavigateToBuyPro
        )

        // 7 Personality Modes Horizontal Switcher
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CompanionMode.values()) { mode ->
                val isSelected = activeMode == mode
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) MayraPurplePrimary else MayraNavyCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MayraPurplePrimary else MayraNavyBorder
                    ),
                    modifier = Modifier.clickable { repository.activeMode.value = mode }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(mode.emoji, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.hindiTitle,
                            color = if (isSelected) MayraNavyDark else MayraTextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }

        // Orb & Voice Control Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Animated Mayra AI Orb
            MayraOrb(
                state = orbState,
                onClick = {
                    if (!isAssistantActive) {
                        repository.isAssistantActive.value = true
                        Toast.makeText(context, "Mayra Assistant Activated! 🟢", Toast.LENGTH_SHORT).show()
                        return@MayraOrb
                    }
                    if (isSpeaking) {
                        speechAssistant.stop()
                    } else {
                        checkAndStartVoice(context, permissionLauncher, speechLauncher, activeLang.code)
                    }
                }
            )

            // Voice State & Controls
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = when {
                        !isAssistantActive -> "Mayra is in Standby mode. Tap orb to activate."
                        orbState == OrbState.IDLE -> "Mayra is active • Tap to speak"
                        orbState == OrbState.LISTENING -> "Listening to your voice..."
                        orbState == OrbState.THINKING -> "Thinking with Gemini AI..."
                        orbState == OrbState.SPEAKING -> "Mayra is speaking..."
                        else -> "Ready"
                    },
                    color = when {
                        !isAssistantActive -> MayraTextMuted
                        orbState == OrbState.SPEAKING -> MayraCyanAccent
                        orbState == OrbState.LISTENING -> MayraPinkAccent
                        orbState == OrbState.THINKING -> MayraAmberGold
                        else -> MayraTextSecondary
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (isSpeaking) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MayraErrorRed.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { speechAssistant.stop() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.AutoMirrored.Filled.VolumeOff, contentDescription = "Stop", tint = MayraErrorRed, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Stop", color = MayraErrorRed, fontSize = 11.sp)
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MayraNavyElevated,
                        modifier = Modifier.clickable { speechAssistant.repeat() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Replay, contentDescription = "Repeat", tint = MayraCyanAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Repeat", color = MayraCyanAccent, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        val filteredMessages = if (searchQuery.isBlank()) {
            messages
        } else {
            messages.filter { it.text.contains(searchQuery, ignoreCase = true) }
        }

        // Conversation Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredMessages) { message ->
                MessageBubble(
                    message = message,
                    onExecuteAction = { proposal ->
                        actionDispatcher.executeAction(proposal) { resultText ->
                            Toast.makeText(context, resultText, Toast.LENGTH_SHORT).show()
                            repository.addChatMessage(
                                ChatMessage(
                                    sender = Sender.SYSTEM,
                                    text = "✓ $resultText"
                                )
                            )
                        }
                    },
                    onSpeakAgain = {
                        speechAssistant.speak(message.text, activeLang)
                    },
                    onDelete = {
                        repository.deleteChatMessage(message.id)
                    }
                )
            }
        }

        // Quick Suggestion Chips (Includes Screen OCR & Direct Actions)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val suggestions = listOf(
                "Torch On karo",
                "YouTube par Tu Hi Ho",
                "Screen par kya likha hai?",
                "10 minute timer lagao",
                "Camera kholo",
                "Scroll down karo",
                "Physics study help"
            )
            items(suggestions) { sugg ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MayraNavyElevated,
                    modifier = Modifier.clickable {
                        textInput = sugg
                    }
                ) {
                    Text(
                        text = "💬 $sugg",
                        color = MayraTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Bottom Input Bar
        Surface(
            color = MayraNavyCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Push-to-Talk Button
                IconButton(
                    onClick = {
                        if (!isAssistantActive) {
                            repository.isAssistantActive.value = true
                        }
                        checkAndStartVoice(context, permissionLauncher, speechLauncher, activeLang.code)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isListeningAudio) MayraPinkAccent else MayraPurplePrimary.copy(alpha = 0.2f))
                        .testTag("companion_voice_mic")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input",
                        tint = if (isListeningAudio) Color.White else MayraPurplePrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Text field
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = if (isAllowanceLocked && !isPro) "Free time ended. Upgrade to Pro." else if (!isAssistantActive) "Assistant standby. Type to chat..." else "Baat karo Mayra se...",
                            color = MayraTextMuted,
                            fontSize = 13.sp
                        )
                    },
                    enabled = !isAllowanceLocked || isPro,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("companion_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MayraCyanAccent,
                        unfocusedBorderColor = MayraNavyBorder,
                        focusedTextColor = MayraTextPrimary,
                        unfocusedTextColor = MayraTextPrimary,
                        focusedContainerColor = MayraNavyDark,
                        unfocusedContainerColor = MayraNavyDark
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            val text = textInput
                            textInput = ""
                            sendMessage(
                                text = text,
                                isVoice = false,
                                repository = repository,
                                speechAssistant = speechAssistant,
                                allowanceManager = allowanceManager,
                                actionDispatcher = actionDispatcher,
                                isPro = isPro,
                                onThinking = { isThinking = it }
                            )
                        }
                    },
                    enabled = textInput.isNotBlank() && (!isAllowanceLocked || isPro),
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (textInput.isNotBlank()) MayraCyanAccent else MayraNavyElevated)
                        .testTag("companion_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (textInput.isNotBlank()) MayraNavyDark else MayraTextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun checkAndStartVoice(
    context: android.content.Context,
    permissionLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    speechLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    langCode: String
) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
        launchVoiceRecognizer(context, speechLauncher, langCode)
    } else {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
}

private fun launchVoiceRecognizer(
    context: android.content.Context,
    speechLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    langCode: String
) {
    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (langCode == "hi") "hi-IN" else "en-IN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Mayra AI in Hindi or English...")
        }
        speechLauncher.launch(intent)
    } catch (_: Exception) {
        Toast.makeText(context, "Voice recognizer not available on this device.", Toast.LENGTH_SHORT).show()
    }
}

private fun sendMessage(
    text: String,
    isVoice: Boolean,
    repository: MayraRepository,
    speechAssistant: SpeechAssistant,
    allowanceManager: AllowanceManager,
    actionDispatcher: DeviceActionDispatcher,
    isPro: Boolean,
    onThinking: (Boolean) -> Unit
) {
    // Add user message
    val userMsg = ChatMessage(sender = Sender.USER, text = text, isVoice = isVoice)
    repository.addChatMessage(userMsg)

    // Consume 15 seconds from daily allowance per conversation interaction
    allowanceManager.consumeAllowance(15, isPro)

    // Parse Intent for Android Smart Action
    val lower = text.lowercase()
    val proposedAction: ActionProposal? = when {
        lower.contains("torch") || lower.contains("flashlight") -> {
            ActionProposal(
                actionType = ActionType.TOGGLE_FLASHLIGHT,
                title = "Toggle Flashlight",
                description = "Switch device torch light on or off",
                canAutoExecute = true
            )
        }
        lower.contains("camera") || lower.contains("photo") -> {
            ActionProposal(
                actionType = ActionType.OPEN_CAMERA,
                title = "Launch Camera",
                description = "Open device camera app to capture moment",
                canAutoExecute = true
            )
        }
        lower.contains("youtube") || lower.contains("gana") || lower.contains("song") || lower.contains("video") -> {
            val q = text.replace(Regex("youtube|chalao|play|search", RegexOption.IGNORE_CASE), "").trim().ifBlank { "Tu Hi Ho" }
            ActionProposal(
                actionType = ActionType.SEARCH_YOUTUBE,
                title = "Search YouTube",
                description = "Find '$q' on YouTube",
                payload = q,
                canAutoExecute = true
            )
        }
        lower.contains("settings") -> {
            ActionProposal(
                actionType = ActionType.OPEN_SETTINGS,
                title = "Android Settings",
                description = "Open system preferences",
                canAutoExecute = true
            )
        }
        lower.contains("timer") -> {
            ActionProposal(
                actionType = ActionType.SET_TIMER,
                title = "10-Minute Focus Timer",
                description = "Set 10-minute timer in clock",
                payload = "600",
                canAutoExecute = true
            )
        }
        lower.contains("alarm") -> {
            ActionProposal(
                actionType = ActionType.SET_ALARM,
                title = "Set 6:00 AM Alarm",
                description = "Set morning alarm in clock",
                canAutoExecute = true
            )
        }
        lower.contains("scroll down") || lower.contains("neeche scroll") -> {
            ActionProposal(
                actionType = ActionType.SCROLL_DOWN,
                title = "Scroll Down",
                description = "Scroll downward on active screen",
                canAutoExecute = true
            )
        }
        lower.contains("scroll up") || lower.contains("upar scroll") -> {
            ActionProposal(
                actionType = ActionType.SCROLL_UP,
                title = "Scroll Up",
                description = "Scroll upward on active screen",
                canAutoExecute = true
            )
        }
        lower.contains("home screen") || lower.contains("home jao") -> {
            ActionProposal(
                actionType = ActionType.NAVIGATE_HOME,
                title = "Go to Home Screen",
                description = "Minimize and navigate to Android launcher",
                canAutoExecute = true
            )
        }
        lower.contains("screen par kya") || lower.contains("ocr") || lower.contains("read screen") -> {
            ActionProposal(
                actionType = ActionType.SCREEN_AWARENESS_OCR,
                title = "Analyze Screen Content",
                description = "Inspect visible headings, error messages, and questions",
                canAutoExecute = true
            )
        }
        else -> null
    }

    // Auto Execute Engine: if autoExecuteEnabled is true and action canAutoExecute, execute immediately!
    var immediateResultNotice = ""
    if (proposedAction != null && repository.autoExecuteEnabled.value && proposedAction.canAutoExecute && !proposedAction.isSensitive) {
        actionDispatcher.executeAction(proposedAction) { res ->
            immediateResultNotice = "✓ Auto-Executed: $res"
            repository.addChatMessage(
                ChatMessage(sender = Sender.SYSTEM, text = immediateResultNotice)
            )
        }
    }

    onThinking(true)
    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
        val historyPairs = repository.chatMessages.value.takeLast(6).map {
            (if (it.sender == Sender.USER) "user" else "model") to it.text
        }
        val memoryStrings = if (repository.isMemoryEnabled.value) {
            repository.memories.value.map { "${it.key}: ${it.value}" }
        } else emptyList()

        val reply = GeminiClient.askMayra(
            prompt = text,
            mode = repository.activeMode.value,
            language = repository.activeLanguage.value,
            history = historyPairs,
            userCustomKey = repository.userCustomApiKey.value,
            userMemories = memoryStrings
        )

        // If auto-executed, mention it smoothly
        val finalReply = if (immediateResultNotice.isNotBlank()) {
            "$reply\n\n*(Maine yeh action turant execute kar diya! ⚡)*"
        } else reply

        val mayraMsg = ChatMessage(
            sender = Sender.MAYRA,
            text = finalReply,
            // Only show manual run card if it wasn't auto-executed
            actionProposal = if (repository.autoExecuteEnabled.value && proposedAction?.canAutoExecute == true) null else proposedAction,
            isVoice = isVoice
        )

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
            repository.addChatMessage(mayraMsg)
            onThinking(false)
            speechAssistant.speak(finalReply, repository.activeLanguage.value)
        }
    }
}

@Composable
private fun MessageBubble(
    message: ChatMessage,
    onExecuteAction: (ActionProposal) -> Unit,
    onSpeakAgain: () -> Unit,
    onDelete: () -> Unit
) {
    val isUser = message.sender == Sender.USER
    val isSystem = message.sender == Sender.SYSTEM

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isSystem) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MayraNavyElevated,
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = message.text,
                    color = MayraCyanAccent,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            return
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = MayraPurplePrimary.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("M", color = MayraPurplePrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MayraPurplePrimary else MayraNavyCard
                ),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder),
                modifier = Modifier.widthIn(max = 290.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else MayraTextPrimary,
                        fontSize = 13.5.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("hh:mm a", Locale.US).format(Date(message.timestamp)),
                            color = if (isUser) Color.White.copy(alpha = 0.7f) else MayraTextMuted,
                            fontSize = 9.5.sp
                        )

                        Row {
                            if (!isUser) {
                                IconButton(
                                    onClick = onSpeakAgain,
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Listen",
                                        tint = MayraCyanAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MayraTextMuted,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Inline Action Proposal Card if not auto-executed
        message.actionProposal?.let { proposal ->
            ActionProposalCard(
                proposal = proposal,
                onExecute = { onExecuteAction(proposal) },
                onDismiss = { /* dismissed */ },
                modifier = Modifier.widthIn(max = 300.dp).padding(start = 34.dp, top = 4.dp)
            )
        }
    }
}

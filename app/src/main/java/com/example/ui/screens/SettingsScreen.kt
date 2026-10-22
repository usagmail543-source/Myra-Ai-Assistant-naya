package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.MayraRepository
import com.example.model.LanguagePreference
import com.example.network.GeminiClient
import com.example.service.SpeechAssistant
import com.example.ui.theme.MayraAmberGold
import com.example.ui.theme.MayraCyanAccent
import com.example.ui.theme.MayraCyanGlow
import com.example.ui.theme.MayraErrorRed
import com.example.ui.theme.MayraNavyBorder
import com.example.ui.theme.MayraNavyCard
import com.example.ui.theme.MayraNavyDark
import com.example.ui.theme.MayraNavyElevated
import com.example.ui.theme.MayraPurplePrimary
import com.example.ui.theme.MayraSuccessGreen
import com.example.ui.theme.MayraTextMuted
import com.example.ui.theme.MayraTextPrimary
import com.example.ui.theme.MayraTextSecondary
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: MayraRepository,
    speechAssistant: SpeechAssistant,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentLang by repository.activeLanguage.collectAsState()
    val isAssistantActive by repository.isAssistantActive.collectAsState()
    val autoExecuteEnabled by repository.autoExecuteEnabled.collectAsState()
    val isMemoryEnabled by repository.isMemoryEnabled.collectAsState()

    var customApiKey by remember { mutableStateOf(repository.userCustomApiKey.value) }
    var selectedModel by remember { mutableStateOf("gemini-3.5-flash") }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }

    var speechRate by remember { mutableFloatStateOf(speechAssistant.speechRate) }
    var speechPitch by remember { mutableFloatStateOf(speechAssistant.speechPitch) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MayraNavyDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = MayraCyanAccent)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Mayra AI Settings", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Assistant Activation • Auto Execute • Gemini Brain • Privacy", color = MayraTextSecondary, fontSize = 11.5.sp)
                }
            }
        }

        // Assistant Activation & Auto Execute Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MayraCyanAccent.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Assistant Operational State", color = MayraCyanGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Active Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mayra Active State", color = MayraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Enables voice listening, proactive alerts and instant processing.", color = MayraTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isAssistantActive,
                            onCheckedChange = {
                                repository.isAssistantActive.value = it
                                if (!it) speechAssistant.stop()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = MayraSuccessGreen, checkedTrackColor = MayraSuccessGreen.copy(alpha = 0.5f))
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto Execute Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto Execute Actions", color = MayraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Directly execute allowlisted Android actions (Torch, YouTube, Timer, Scroll) without manual confirmation buttons.", color = MayraTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = autoExecuteEnabled,
                            onCheckedChange = { repository.autoExecuteEnabled.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MayraCyanAccent, checkedTrackColor = MayraPurplePrimary)
                        )
                    }
                }
            }
        }

        // Gemini AI Configuration Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MayraPurplePrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Google Gemini AI Engine", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Build Key Status: ${if (BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY") "Configured via Secrets ✓" else "Active (AI Studio Injected / Fallback)"}",
                        color = MayraCyanGlow,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = customApiKey,
                        onValueChange = {
                            customApiKey = it
                            repository.userCustomApiKey.value = it
                        },
                        label = { Text("Custom Gemini API Key (Optional)") },
                        placeholder = { Text("Paste AI Studio API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MayraCyanAccent,
                            unfocusedBorderColor = MayraNavyBorder,
                            focusedTextColor = MayraTextPrimary,
                            unfocusedTextColor = MayraTextPrimary,
                            focusedContainerColor = MayraNavyElevated,
                            unfocusedContainerColor = MayraNavyElevated
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Select Active Model:", color = MayraTextSecondary, fontSize = 11.5.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val models = listOf("gemini-3.5-flash", "gemini-3.1-pro-preview")
                        models.forEach { m ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedModel == m) MayraPurplePrimary else MayraNavyElevated,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedModel = m }
                            ) {
                                Text(
                                    text = if (m.contains("pro")) "Gemini Pro\n(Reasoning/Code)" else "Gemini Flash\n(Companion/Fast)",
                                    color = if (selectedModel == m) MayraNavyDark else MayraTextPrimary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isTestingConnection = true
                            coroutineScope.launch {
                                try {
                                    val reply = GeminiClient.askMayra(
                                        prompt = "Say hello in one short sentence as Mayra AI companion.",
                                        mode = repository.activeMode.value,
                                        language = repository.activeLanguage.value,
                                        userCustomKey = customApiKey,
                                        modelOverride = selectedModel
                                    )
                                    testResult = "✓ Connected! Mayra: $reply"
                                } catch (e: Exception) {
                                    testResult = "Notice: ${e.message}"
                                } finally {
                                    isTestingConnection = false
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MayraCyanAccent),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MayraNavyDark)
                        } else {
                            Text("Test Gemini AI Connection", color = MayraNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    testResult?.let { res ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(res, color = if (res.startsWith("✓")) MayraSuccessGreen else MayraAmberGold, fontSize = 11.5.sp)
                    }
                }
            }
        }

        // Language Preferences
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Language, contentDescription = null, tint = MayraCyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Companion Language", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    LanguagePreference.values().forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { repository.activeLanguage.value = lang }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (currentLang == lang) Icons.Default.CheckCircle else Icons.Default.Language,
                                contentDescription = null,
                                tint = if (currentLang == lang) MayraPurplePrimary else MayraTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = lang.label,
                                color = if (currentLang == lang) MayraTextPrimary else MayraTextSecondary,
                                fontWeight = if (currentLang == lang) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // User Privacy & Memory Management
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Conversational Memory", color = MayraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Allows Mayra to remember user preferences and study goals.", color = MayraTextSecondary, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isMemoryEnabled,
                            onCheckedChange = { repository.isMemoryEnabled.value = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = MayraPurplePrimary)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            repository.clearAllMemories()
                            Toast.makeText(context, "All conversational memories cleared!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MayraErrorRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Saved Memories", fontSize = 11.sp)
                    }
                }
            }
        }

        // Speech & Voice Tuning
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null, tint = MayraAmberGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Voice Assistant & TTS Tuning", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Speech Rate: ${String.format("%.2f", speechRate)}x", color = MayraTextSecondary, fontSize = 11.5.sp)
                    Slider(
                        value = speechRate,
                        onValueChange = {
                            speechRate = it
                            speechAssistant.speechRate = it
                        },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(thumbColor = MayraAmberGold, activeTrackColor = MayraAmberGold)
                    )

                    Text("Voice Pitch / Tone: ${String.format("%.2f", speechPitch)}", color = MayraTextSecondary, fontSize = 11.5.sp)
                    Slider(
                        value = speechPitch,
                        onValueChange = {
                            speechPitch = it
                            speechAssistant.speechPitch = it
                        },
                        valueRange = 0.8f..1.3f,
                        colors = SliderDefaults.colors(thumbColor = MayraPurplePrimary, activeTrackColor = MayraPurplePrimary)
                    )

                    Button(
                        onClick = {
                            speechAssistant.speak("Namaste! Main Mayra hoon, aapki AI companion aur assistant.", repository.activeLanguage.value)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MayraNavyElevated),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🔊 Test Mayra Voice Output", color = MayraTextPrimary, fontSize = 12.sp)
                    }
                }
            }
        }

        // Appearance & Dark Mode Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Brightness4, contentDescription = null, tint = MayraCyanAccent)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Cyber Dark Theme", color = MayraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                            Text("Futuristic deep navy & neon accents", color = MayraTextSecondary, fontSize = 11.sp)
                        }
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = { onToggleDarkTheme(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MayraCyanAccent,
                            checkedTrackColor = MayraPurplePrimary
                        )
                    )
                }
            }
        }
    }
}

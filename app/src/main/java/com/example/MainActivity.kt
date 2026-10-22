package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MayraRepository
import com.example.service.AllowanceManager
import com.example.service.DeviceActionDispatcher
import com.example.service.SpeechAssistant
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.CodingScreen
import com.example.ui.screens.CompanionScreen
import com.example.ui.screens.PortalScreen
import com.example.ui.screens.ProductivityScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WhiteboardScreen
import com.example.ui.theme.MayraAmberGold
import com.example.ui.theme.MayraCyanAccent
import com.example.ui.theme.MayraCyanGlow
import com.example.ui.theme.MayraNavyBorder
import com.example.ui.theme.MayraNavyCard
import com.example.ui.theme.MayraNavyDark
import com.example.ui.theme.MayraNavyElevated
import com.example.ui.theme.MayraPurplePrimary
import com.example.ui.theme.MayraSuccessGreen
import com.example.ui.theme.MayraTextMuted
import com.example.ui.theme.MayraTextPrimary
import com.example.ui.theme.MayraTextSecondary
import com.example.ui.theme.MayraTheme

enum class MainDestination(val label: String, val icon: ImageVector, val tag: String) {
    COMPANION("Mayra", Icons.Default.AutoAwesome, "nav_companion"),
    WHITEBOARD("Study", Icons.Default.Draw, "nav_whiteboard"),
    CODING("Coding", Icons.Default.Code, "nav_coding"),
    PRODUCTIVITY("Actions", Icons.Default.TaskAlt, "nav_productivity"),
    PORTAL("Portal", Icons.Default.Language, "nav_portal")
}

class MainActivity : ComponentActivity() {
    private lateinit var speechAssistant: SpeechAssistant
    private lateinit var repository: MayraRepository
    private lateinit var actionDispatcher: DeviceActionDispatcher
    private lateinit var allowanceManager: AllowanceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        speechAssistant = SpeechAssistant(this)
        repository = MayraRepository(this)
        actionDispatcher = DeviceActionDispatcher(this)
        allowanceManager = AllowanceManager(this)

        setContent {
            var isDarkTheme by remember { mutableStateOf(true) }
            var currentDestination by remember { mutableStateOf(MainDestination.COMPANION) }
            var showAdminScreen by remember { mutableStateOf(false) }
            var showSettingsScreen by remember { mutableStateOf(false) }

            val activeLicense by repository.activeLicense.collectAsState()
            val isAdminMode by repository.isAdminMode.collectAsState()
            val isAssistantActive by repository.isAssistantActive.collectAsState()
            val autoExecuteEnabled by repository.autoExecuteEnabled.collectAsState()

            // Handle system back navigation
            BackHandler(enabled = showAdminScreen || showSettingsScreen || currentDestination != MainDestination.COMPANION) {
                when {
                    showAdminScreen -> showAdminScreen = false
                    showSettingsScreen -> showSettingsScreen = false
                    else -> currentDestination = MainDestination.COMPANION
                }
            }

            MayraTheme(darkTheme = isDarkTheme) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        MayraTopAppBar(
                            isPro = activeLicense != null,
                            isAdmin = isAdminMode,
                            isAssistantActive = isAssistantActive,
                            autoExecuteEnabled = autoExecuteEnabled,
                            onToggleAssistant = {
                                val next = !isAssistantActive
                                repository.isAssistantActive.value = next
                                if (!next) speechAssistant.stop()
                            },
                            onToggleAutoExecute = {
                                repository.autoExecuteEnabled.value = !autoExecuteEnabled
                            },
                            onAdminClick = {
                                showAdminScreen = true
                                showSettingsScreen = false
                            },
                            onSettingsClick = {
                                showSettingsScreen = true
                                showAdminScreen = false
                            }
                        )
                    },
                    bottomBar = {
                        if (!showAdminScreen && !showSettingsScreen) {
                            MayraBottomNavigation(
                                currentDestination = currentDestination,
                                onSelectDestination = { currentDestination = it }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when {
                            showAdminScreen -> {
                                AdminScreen(
                                    repository = repository,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            showSettingsScreen -> {
                                SettingsScreen(
                                    repository = repository,
                                    speechAssistant = speechAssistant,
                                    isDarkTheme = isDarkTheme,
                                    onToggleDarkTheme = { isDarkTheme = it },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Crossfade(
                                    targetState = currentDestination,
                                    label = "screen_crossfade"
                                ) { destination ->
                                    when (destination) {
                                        MainDestination.COMPANION -> {
                                            CompanionScreen(
                                                repository = repository,
                                                speechAssistant = speechAssistant,
                                                actionDispatcher = actionDispatcher,
                                                allowanceManager = allowanceManager,
                                                onNavigateToBuyPro = {
                                                    currentDestination = MainDestination.PORTAL
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        MainDestination.WHITEBOARD -> {
                                            WhiteboardScreen(
                                                repository = repository,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        MainDestination.CODING -> {
                                            CodingScreen(
                                                repository = repository,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        MainDestination.PRODUCTIVITY -> {
                                            ProductivityScreen(
                                                repository = repository,
                                                actionDispatcher = actionDispatcher,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        MainDestination.PORTAL -> {
                                            PortalScreen(
                                                repository = repository,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechAssistant.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MayraTopAppBar(
    isPro: Boolean,
    isAdmin: Boolean,
    isAssistantActive: Boolean,
    autoExecuteEnabled: Boolean,
    onToggleAssistant: () -> Unit,
    onToggleAutoExecute: () -> Unit,
    onAdminClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current

    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MayraPurplePrimary.copy(alpha = 0.25f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "M",
                            color = MayraCyanAccent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mayra AI",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        // Assistant Active Dot
                        Surface(
                            shape = CircleShape,
                            color = if (isAssistantActive) MayraSuccessGreen else MayraTextMuted,
                            modifier = Modifier
                                .size(8.dp)
                                .clickable {
                                    onToggleAssistant()
                                    Toast.makeText(context, if (!isAssistantActive) "Mayra Assistant Activated! 🟢" else "Mayra Standby", Toast.LENGTH_SHORT).show()
                                }
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        // Pro / Free Badge
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (isPro) MayraAmberGold.copy(alpha = 0.2f) else MayraNavyElevated
                        ) {
                            Text(
                                text = if (isPro) "PRO" else "FREE",
                                color = if (isPro) MayraAmberGold else MayraTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (isAssistantActive) "AI Companion • Active" else "AI Companion • Standby",
                        color = if (isAssistantActive) MayraCyanGlow else MayraTextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        },
        actions = {
            // Auto Execute quick indicator chip
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (autoExecuteEnabled) MayraCyanAccent.copy(alpha = 0.15f) else MayraNavyElevated,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clickable {
                        onToggleAutoExecute()
                        Toast.makeText(context, if (!autoExecuteEnabled) "Auto Execute ON" else "Auto Execute OFF", Toast.LENGTH_SHORT).show()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Bolt, contentDescription = null, tint = if (autoExecuteEnabled) MayraCyanAccent else MayraTextMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = if (autoExecuteEnabled) "Auto" else "Manual",
                        color = if (autoExecuteEnabled) MayraCyanAccent else MayraTextMuted,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Admin button
            IconButton(
                onClick = onAdminClick,
                modifier = Modifier.testTag("topbar_admin_button")
            ) {
                BadgedBox(
                    badge = {
                        if (isAdmin) {
                            Badge(containerColor = MayraSuccessGreen)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Console",
                        tint = if (isAdmin) MayraAmberGold else MayraTextSecondary
                    )
                }
            }

            // Settings button
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.testTag("topbar_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "AI Settings",
                    tint = MayraTextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MayraNavyCard,
            titleContentColor = MayraTextPrimary
        )
    )
}

@Composable
fun MayraBottomNavigation(
    currentDestination: MainDestination,
    onSelectDestination: (MainDestination) -> Unit
) {
    NavigationBar(
        containerColor = MayraNavyCard,
        tonalElevation = 8.dp
    ) {
        MainDestination.values().forEach { destination ->
            val isSelected = currentDestination == destination
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectDestination(destination) },
                icon = {
                    Icon(
                        imageVector = destination.icon,
                        contentDescription = destination.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = destination.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MayraNavyDark,
                    selectedTextColor = MayraCyanAccent,
                    indicatorColor = MayraCyanAccent,
                    unselectedIconColor = MayraTextMuted,
                    unselectedTextColor = MayraTextMuted
                ),
                modifier = Modifier.testTag(destination.tag)
            )
        }
    }
}

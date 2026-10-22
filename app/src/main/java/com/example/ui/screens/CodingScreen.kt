package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MayraRepository
import com.example.model.CodeFile
import com.example.model.CodeProject
import com.example.model.CompanionMode
import com.example.network.GeminiClient
import com.example.ui.components.CodeSnippetCard
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
fun CodingScreen(
    repository: MayraRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val projects by repository.codeProjects.collectAsState()
    var selectedProject by remember { mutableStateOf(projects.firstOrNull()) }
    var selectedFileIndex by remember { mutableStateOf(0) }

    var activeTab by remember { mutableStateOf(0) } // 0 = Multi-file Workspace, 1 = AI Code Generator, 2 = Live Web Preview
    var codingPrompt by remember { mutableStateOf("") }
    var generatedCode by remember { mutableStateOf("") }
    var codeExplanation by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val currentFile = selectedProject?.files?.getOrNull(selectedFileIndex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MayraNavyDark)
    ) {
        // Coding Header
        Surface(color = MayraNavyCard, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = MayraCyanAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Mayra Multi-File Coding Lab", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Full-Stack Workspace • Live Web Preview • Project Builder", color = MayraTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MayraNavyCard,
                    contentColor = MayraCyanAccent,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("File Explorer", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("AI Generator", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Live Web Preview", fontSize = 12.sp) }
                    )
                }
            }
        }

        when (activeTab) {
            0 -> {
                // Multi-file Workspace with File Explorer
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Project Selector Chips
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = MayraAmberGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Active Project:", color = MayraTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(projects) { proj ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedProject?.id == proj.id) MayraPurplePrimary else MayraNavyCard,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selectedProject?.id == proj.id) MayraPurplePrimary else MayraNavyBorder
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedProject = proj
                                        selectedFileIndex = 0
                                    }
                                ) {
                                    Text(
                                        text = proj.title,
                                        color = if (selectedProject?.id == proj.id) MayraNavyDark else MayraTextPrimary,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Files in Project Tabs
                    selectedProject?.let { proj ->
                        item {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(proj.files.indices.toList()) { idx ->
                                    val f = proj.files[idx]
                                    val isSelected = selectedFileIndex == idx
                                    Surface(
                                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                                        color = if (isSelected) MayraNavyElevated else Color(0xFF0D1424),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) MayraCyanAccent else MayraNavyBorder
                                        ),
                                        modifier = Modifier.clickable { selectedFileIndex = idx }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = if (isSelected) MayraCyanAccent else MayraTextMuted, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = f.name,
                                                color = if (isSelected) MayraTextPrimary else MayraTextMuted,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Editor Content Card
                        currentFile?.let { file ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF090D1A)),
                                    shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp, topEnd = 12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
                                ) {
                                    Column {
                                        // File Action Header
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MayraNavyElevated)
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${file.name} (${file.language.uppercase()})",
                                                color = MayraCyanGlow,
                                                fontSize = 11.5.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(file.content))
                                                    Toast.makeText(context, "${file.name} copied!", Toast.LENGTH_SHORT).show()
                                                },
                                                modifier = Modifier.size(26.dp)
                                            ) {
                                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = MayraCyanAccent, modifier = Modifier.size(14.dp))
                                            }
                                        }

                                        // Source code view
                                        Box(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = file.content,
                                                color = Color(0xFF38BDF8),
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // AI Generator & Project Builder
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "AI Project & Code Generator",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Describe your desired website, Android feature, or script for Mayra to architect.",
                            color = MayraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Prompt Presets
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val presets = listOf(
                                "Build a responsive dark-mode portfolio",
                                "Create an Android Jetpack Compose Counter",
                                "Write a Python script for weather scraping",
                                "Fix NullPointerException in Kotlin Flow"
                            )
                            items(presets) { p ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MayraNavyElevated,
                                    modifier = Modifier.clickable { codingPrompt = p }
                                ) {
                                    Text(
                                        text = "⚡ $p",
                                        color = MayraCyanAccent,
                                        fontSize = 10.5.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = codingPrompt,
                            onValueChange = { codingPrompt = it },
                            label = { Text("What would you like to build or debug?") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MayraCyanAccent,
                                unfocusedBorderColor = MayraNavyBorder,
                                focusedTextColor = MayraTextPrimary,
                                unfocusedTextColor = MayraTextPrimary,
                                focusedContainerColor = MayraNavyElevated,
                                unfocusedContainerColor = MayraNavyCard
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                if (codingPrompt.isNotBlank()) {
                                    isLoading = true
                                    coroutineScope.launch {
                                        val reply = GeminiClient.askMayra(
                                            prompt = "Write clean, modular code with concise architectural explanation for: $codingPrompt",
                                            mode = CompanionMode.CODING,
                                            language = repository.activeLanguage.value,
                                            userCustomKey = repository.userCustomApiKey.value
                                        )
                                        generatedCode = reply
                                        codeExplanation = "Architected by Mayra Coding Expert with production best practices."
                                        isLoading = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MayraNavyDark)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Code & Architecture", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (generatedCode.isNotBlank()) {
                        item {
                            CodeSnippetCard(
                                title = "Mayra AI Generated Solution",
                                language = "code",
                                code = generatedCode,
                                explanation = codeExplanation,
                                onCopy = {
                                    repository.addNote("Code: ${codingPrompt.take(20)}", generatedCode, "Code")
                                    Toast.makeText(context, "Saved to Notes!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }

            2 -> {
                // Live Web Preview simulation
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, tint = MayraCyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Interactive Website Preview", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        Text(
                            text = "Live simulated rendering of HTML, CSS, and modern interactive elements from your project.",
                            color = MayraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Rendered Preview Window
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraCyanAccent.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Browser-like Mock Tab bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0A0F1D), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(shape = CircleShape, color = MayraErrorRed, modifier = Modifier.size(8.dp)) {}
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(shape = CircleShape, color = MayraAmberGold, modifier = Modifier.size(8.dp)) {}
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(shape = CircleShape, color = MayraSuccessGreen, modifier = Modifier.size(8.dp)) {}
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("localhost:3000/portfolio", color = MayraTextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Rendered Hero simulation
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MayraNavyCard, RoundedCornerShape(12.dp))
                                        .border(1.dp, MayraNavyBorder, RoundedCornerShape(12.dp))
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "🚀 Hello, I'm Developer",
                                            color = MayraTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Crafted with Mayra AI Full-Stack Companion",
                                            color = MayraCyanGlow,
                                            fontSize = 12.sp
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Button(
                                            onClick = {
                                                Toast.makeText(context, "JavaScript alert: Connected successfully! ✨", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Connect With Me", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
}

package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MayraRepository
import com.example.model.ActionProposal
import com.example.model.ActionType
import com.example.service.DeviceActionDispatcher
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

@Composable
fun ProductivityScreen(
    repository: MayraRepository,
    actionDispatcher: DeviceActionDispatcher,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf(0) } // 0 = Actions, 1 = Notes & Todos, 2 = Mayra Memory

    val notes by repository.notes.collectAsState()
    val todos by repository.todos.collectAsState()
    val memories by repository.memories.collectAsState()

    var showAddNoteDialog by remember { mutableStateOf(false) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }

    var showAddTodoDialog by remember { mutableStateOf(false) }
    var todoTitle by remember { mutableStateOf("") }

    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var memoryKey by remember { mutableStateOf("") }
    var memoryVal by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MayraNavyDark)
    ) {
        // Tab Header
        Surface(color = MayraNavyCard, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Mayra Productivity & Control",
                    color = MayraTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                Text(
                    text = "Android Actions • Notes • To-Dos • Privacy Memory",
                    color = MayraTextSecondary,
                    fontSize = 11.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MayraNavyCard,
                    contentColor = MayraCyanAccent,
                    divider = {}
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Actions", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("Notes & Tasks", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("AI Memory", fontSize = 12.sp) }
                    )
                }
            }
        }

        when (activeTab) {
            0 -> {
                // Android Actions Grid
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "Direct Android Hardware & Intents",
                            color = MayraCyanAccent,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Safe, allowlisted actions executable with one tap or via voice commands.",
                            color = MayraTextSecondary,
                            fontSize = 11.5.sp
                        )
                    }

                    item {
                        ActionCardItem(
                            icon = Icons.Default.FlashOn,
                            title = "Flashlight Toggle",
                            subtitle = "Hardware camera torch on / off",
                            actionColor = MayraAmberGold
                        ) {
                            actionDispatcher.executeAction(
                                ActionProposal(ActionType.TOGGLE_FLASHLIGHT, "Flashlight", "")
                            ) { res ->
                                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    item {
                        ActionCardItem(
                            icon = Icons.Default.Videocam,
                            title = "Launch Camera",
                            subtitle = "Capture photos or video recording",
                            actionColor = MayraPurplePrimary
                        ) {
                            actionDispatcher.executeAction(
                                ActionProposal(ActionType.OPEN_CAMERA, "Camera", "")
                            ) { res ->
                                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    item {
                        ActionCardItem(
                            icon = Icons.Default.PlayArrow,
                            title = "YouTube Search & Lecture",
                            subtitle = "Open educational video or favorite song",
                            actionColor = MayraCyanAccent
                        ) {
                            actionDispatcher.executeAction(
                                ActionProposal(ActionType.SEARCH_YOUTUBE, "YouTube", "Mayra AI Assistant Hindi")
                            ) { res ->
                                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    item {
                        ActionCardItem(
                            icon = Icons.Default.Settings,
                            title = "Android Settings",
                            subtitle = "Open system preferences & permissions",
                            actionColor = MayraTextSecondary
                        ) {
                            actionDispatcher.executeAction(
                                ActionProposal(ActionType.OPEN_SETTINGS, "Settings", "")
                            ) { res ->
                                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    item {
                        ActionCardItem(
                            icon = Icons.Default.Alarm,
                            title = "Set 5-Minute Productivity Timer",
                            subtitle = "Trigger Android Clock timer alarm",
                            actionColor = MayraSuccessGreen
                        ) {
                            actionDispatcher.executeAction(
                                ActionProposal(ActionType.SET_TIMER, "Mayra Focus Timer", "300")
                            ) { res ->
                                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    item {
                        ActionCardItem(
                            icon = Icons.Default.Language,
                            title = "Open Web Browser",
                            subtitle = "Browse articles, tutorials & research",
                            actionColor = MayraPurplePrimary
                        ) {
                            actionDispatcher.executeAction(
                                ActionProposal(ActionType.OPEN_BROWSER, "Browser", "https://www.google.com")
                            ) { res ->
                                Toast.makeText(context, res, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }

            1 -> {
                // Notes & To-Dos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Notes Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📝 Quick Notes (${notes.size})",
                                color = MayraTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { showAddNoteDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text("New Note", fontSize = 11.sp)
                            }
                        }
                    }

                    items(notes) { note ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = note.title,
                                        color = MayraCyanGlow,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    IconButton(
                                        onClick = { repository.deleteNote(note.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MayraTextMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = note.content,
                                    color = MayraTextPrimary,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // To-Dos Header
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "✅ Daily To-Do List (${todos.size})",
                                color = MayraTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Button(
                                onClick = { showAddTodoDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MayraCyanAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MayraNavyDark, modifier = Modifier.size(14.dp))
                                Text("Add Task", color = MayraNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    items(todos) { todo ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = todo.isCompleted,
                                    onCheckedChange = { repository.toggleTodo(todo.id) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = MayraSuccessGreen,
                                        uncheckedColor = MayraNavyBorder
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = todo.title,
                                    color = if (todo.isCompleted) MayraTextMuted else MayraTextPrimary,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { repository.deleteTodo(todo.id) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = MayraTextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // User Memory Management
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🧠 Mayra Memory & Privacy",
                                    color = MayraTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Facts Mayra remembers to personalize your companion experience.",
                                    color = MayraTextSecondary,
                                    fontSize = 11.5.sp
                                )
                            }
                            Button(
                                onClick = { showAddMemoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("+ Add Fact", fontSize = 11.sp)
                            }
                        }
                    }

                    items(memories) { mem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mem.key,
                                        color = MayraCyanGlow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = mem.value,
                                        color = MayraTextPrimary,
                                        fontSize = 12.sp
                                    )
                                }
                                IconButton(
                                    onClick = { repository.deleteMemory(mem.id) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Forget fact",
                                        tint = MayraTextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Note Dialog
    if (showAddNoteDialog) {
        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("New Note", color = MayraTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Content") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank()) {
                            repository.addNote(noteTitle, noteContent)
                            noteTitle = ""
                            noteContent = ""
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MayraNavyElevated
        )
    }

    // Add Todo Dialog
    if (showAddTodoDialog) {
        AlertDialog(
            onDismissRequest = { showAddTodoDialog = false },
            title = { Text("New Task", color = MayraTextPrimary) },
            text = {
                OutlinedTextField(
                    value = todoTitle,
                    onValueChange = { todoTitle = it },
                    label = { Text("Task description") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (todoTitle.isNotBlank()) {
                            repository.addTodo(todoTitle)
                            todoTitle = ""
                            showAddTodoDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MayraCyanAccent)
                ) {
                    Text("Add", color = MayraNavyDark)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTodoDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MayraNavyElevated
        )
    }

    // Add Memory Dialog
    if (showAddMemoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddMemoryDialog = false },
            title = { Text("Add Memory Fact", color = MayraTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = memoryKey,
                        onValueChange = { memoryKey = it },
                        label = { Text("Category (e.g. Favorite Subject, Career Goal)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = memoryVal,
                        onValueChange = { memoryVal = it },
                        label = { Text("Detail / Preference") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memoryKey.isNotBlank()) {
                            repository.addMemory(memoryKey, memoryVal)
                            memoryKey = ""
                            memoryVal = ""
                            showAddMemoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary)
                ) {
                    Text("Remember")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddMemoryDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = MayraNavyElevated
        )
    }
}

@Composable
private fun ActionCardItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    actionColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = actionColor.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = actionColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = MayraTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.5.sp)
                Text(text = subtitle, color = MayraTextSecondary, fontSize = 11.5.sp)
            }
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = MayraTextMuted, modifier = Modifier.size(16.dp))
        }
    }
}

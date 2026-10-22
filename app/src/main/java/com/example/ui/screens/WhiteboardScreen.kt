package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MayraRepository
import com.example.model.CompanionMode
import com.example.network.GeminiClient
import com.example.ui.theme.MayraAmberGold
import com.example.ui.theme.MayraCyanAccent
import com.example.ui.theme.MayraCyanGlow
import com.example.ui.theme.MayraNavyBorder
import com.example.ui.theme.MayraNavyCard
import com.example.ui.theme.MayraNavyDark
import com.example.ui.theme.MayraNavyElevated
import com.example.ui.theme.MayraPinkAccent
import com.example.ui.theme.MayraPurplePrimary
import com.example.ui.theme.MayraSuccessGreen
import com.example.ui.theme.MayraTextMuted
import com.example.ui.theme.MayraTextPrimary
import com.example.ui.theme.MayraTextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StrokeLine(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun WhiteboardScreen(
    repository: MayraRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val flashcards by repository.flashcards.collectAsState()

    // Canvas Stroke Management (Undo / Redo stacks)
    val strokes = remember { mutableStateListOf<StrokeLine>() }
    val redoStack = remember { mutableStateListOf<StrokeLine>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var selectedColor by remember { mutableStateOf(MayraCyanAccent) }
    var strokeWidth by remember { mutableStateOf(6f) }
    var isEraser by remember { mutableStateOf(false) }

    var activeTab by remember { mutableStateOf(0) } // 0 = Canvas, 1 = AI Tutor & STEM, 2 = Flashcards, 3 = OCR Reader
    val subjects = listOf("Mathematics", "Physics", "Chemistry", "Biology", "English Learning")
    var selectedSubject by remember { mutableStateOf("Mathematics") }

    var studyQuestion by remember { mutableStateOf("") }
    var studyExplanation by remember { mutableStateOf("") }
    var isLoadingExplanation by remember { mutableStateOf(false) }

    var ocrScreenText by remember { mutableStateOf("") }
    var ocrExplanation by remember { mutableStateOf("") }
    var isOcrLoading by remember { mutableStateOf(false) }

    // Flashcard flip states
    var revealedFlashcardId by remember { mutableStateOf<String?>(null) }

    val colors = listOf(
        MayraCyanAccent,
        MayraPurplePrimary,
        MayraPinkAccent,
        MayraAmberGold,
        Color.White,
        MayraSuccessGreen
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MayraNavyDark)
    ) {
        // Top Header
        Surface(
            color = MayraNavyCard,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mayra Study Lab & Whiteboard",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "STEM Tutor • Whiteboard (Undo/Redo) • OCR & Flashcards",
                            color = MayraTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Row {
                        IconButton(
                            onClick = {
                                if (strokes.isNotEmpty()) {
                                    repository.addNote(
                                        title = "Whiteboard Sketch (${strokes.size} strokes)",
                                        content = "Whiteboard session drawing saved on ${SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date())}",
                                        category = "Whiteboard"
                                    )
                                    Toast.makeText(context, "Whiteboard saved to Notes! 📝", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Draw on canvas first!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "Save Canvas", tint = MayraCyanAccent, modifier = Modifier.size(18.dp))
                        }

                        IconButton(
                            onClick = {
                                strokes.clear()
                                redoStack.clear()
                                currentPoints = emptyList()
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear Canvas", tint = MayraTextMuted, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    containerColor = MayraNavyCard,
                    contentColor = MayraPurplePrimary,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("Drawing Canvas", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("AI STEM Tutor", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Flashcards (${flashcards.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        text = { Text("Screen OCR", fontSize = 12.sp) }
                    )
                }
            }
        }

        when (activeTab) {
            0 -> {
                // Interactive Whiteboard with Undo / Redo
                Column(modifier = Modifier.fillMaxSize()) {
                    // Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MayraNavyElevated)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Color Palette
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(colors) { col ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            width = if (selectedColor == col && !isEraser) 2.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            selectedColor = col
                                            isEraser = false
                                            strokeWidth = 6f
                                        }
                                )
                            }
                        }

                        // Undo, Redo, Eraser controls
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Undo
                            IconButton(
                                onClick = {
                                    if (strokes.isNotEmpty()) {
                                        val last = strokes.removeAt(strokes.size - 1)
                                        redoStack.add(last)
                                    }
                                },
                                enabled = strokes.isNotEmpty(),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = "Undo",
                                    tint = if (strokes.isNotEmpty()) MayraTextPrimary else MayraTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Redo
                            IconButton(
                                onClick = {
                                    if (redoStack.isNotEmpty()) {
                                        val item = redoStack.removeAt(redoStack.size - 1)
                                        strokes.add(item)
                                    }
                                },
                                enabled = redoStack.isNotEmpty(),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Redo,
                                    contentDescription = "Redo",
                                    tint = if (redoStack.isNotEmpty()) MayraCyanAccent else MayraTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Eraser toggle
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isEraser) MayraPurplePrimary else Color.Transparent,
                                modifier = Modifier.clickable {
                                    isEraser = !isEraser
                                    strokeWidth = if (isEraser) 26f else 6f
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Eraser",
                                        tint = if (isEraser) Color.White else MayraTextSecondary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Eraser",
                                        color = if (isEraser) Color.White else MayraTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Interactive Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF090D18))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints = listOf(offset)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPoints = currentPoints + change.position
                                    },
                                    onDragEnd = {
                                        if (currentPoints.isNotEmpty()) {
                                            val drawColor = if (isEraser) Color(0xFF090D18) else selectedColor
                                            strokes.add(StrokeLine(currentPoints, drawColor, strokeWidth))
                                            redoStack.clear()
                                            currentPoints = emptyList()
                                        }
                                    }
                                )
                            }
                            .testTag("whiteboard_canvas")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw saved strokes
                            strokes.forEach { stroke ->
                                if (stroke.points.size > 1) {
                                    val path = Path().apply {
                                        moveTo(stroke.points.first().x, stroke.points.first().y)
                                        for (i in 1 until stroke.points.size) {
                                            lineTo(stroke.points[i].x, stroke.points[i].y)
                                        }
                                    }
                                    drawPath(
                                        path = path,
                                        color = stroke.color,
                                        style = Stroke(
                                            width = stroke.strokeWidth,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }

                            // Draw live active stroke
                            if (currentPoints.size > 1) {
                                val activeColor = if (isEraser) Color(0xFF090D18) else selectedColor
                                val path = Path().apply {
                                    moveTo(currentPoints.first().x, currentPoints.first().y)
                                    for (i in 1 until currentPoints.size) {
                                        lineTo(currentPoints[i].x, currentPoints[i].y)
                                    }
                                }
                                drawPath(
                                    path = path,
                                    color = activeColor,
                                    style = Stroke(
                                        width = strokeWidth,
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Watermark / hint
                        if (strokes.isEmpty() && currentPoints.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = null,
                                        tint = MayraTextMuted.copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Draw formulas, science diagrams & notes with Undo/Redo support",
                                        color = MayraTextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // Quick formula drawer chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MayraNavyCard)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickFormulas = listOf(
                            "E = mc²",
                            "x = (-b ± √D) / 2a",
                            "d/dx [sin x] = cos x",
                            "PV = nRT",
                            "C₆H₁₂O₆ + 6O₂"
                        )
                        quickFormulas.forEach { f ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MayraNavyElevated,
                                modifier = Modifier.clickable {
                                    studyQuestion = "Explain and derive step-by-step: $f"
                                    activeTab = 1
                                }
                            ) {
                                Text(
                                    text = f,
                                    color = MayraCyanGlow,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // AI STEM Tutor Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Mayra Multi-Subject AI Tutor",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Specialized tutoring across Physics, Chemistry, Biology, Mathematics and English.",
                            color = MayraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    // Subject Chips
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(subjects) { subj ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedSubject == subj) MayraPurplePrimary else MayraNavyCard,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (selectedSubject == subj) MayraPurplePrimary else MayraNavyBorder
                                    ),
                                    modifier = Modifier.clickable { selectedSubject = subj }
                                ) {
                                    Text(
                                        text = subj,
                                        color = if (selectedSubject == subj) MayraNavyDark else MayraTextSecondary,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selectedSubject == subj) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = studyQuestion,
                            onValueChange = { studyQuestion = it },
                            label = { Text("Ask a question in $selectedSubject...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MayraCyanAccent,
                                unfocusedBorderColor = MayraNavyBorder,
                                focusedTextColor = MayraTextPrimary,
                                unfocusedTextColor = MayraTextPrimary,
                                focusedContainerColor = MayraNavyElevated,
                                unfocusedContainerColor = MayraNavyCard
                            ),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (isLoadingExplanation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = MayraPurplePrimary,
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (studyQuestion.isNotBlank()) {
                                        isLoadingExplanation = true
                                        coroutineScope.launch {
                                            val answer = GeminiClient.askMayra(
                                                prompt = "[$selectedSubject] Solve step-by-step with clear explanations, formulas and intuitive concepts in Hinglish: $studyQuestion",
                                                mode = CompanionMode.STUDY,
                                                language = repository.activeLanguage.value,
                                                userCustomKey = repository.userCustomApiKey.value
                                            )
                                            studyExplanation = answer
                                            isLoadingExplanation = false
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Explain Step-by-Step", fontSize = 12.sp)
                            }

                            Button(
                                onClick = {
                                    val topic = studyQuestion.ifBlank { selectedSubject }
                                    isLoadingExplanation = true
                                    coroutineScope.launch {
                                        val quiz = GeminiClient.askMayra(
                                            prompt = "Generate 3 quick practice quiz questions with options and answers for: $topic in $selectedSubject",
                                            mode = CompanionMode.STUDY,
                                            language = repository.activeLanguage.value,
                                            userCustomKey = repository.userCustomApiKey.value
                                        )
                                        studyExplanation = quiz
                                        isLoadingExplanation = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MayraCyanAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = MayraNavyDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Quiz Me", color = MayraNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (studyExplanation.isNotBlank()) {
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
                                        Text(
                                            text = "📖 $selectedSubject Solution",
                                            color = MayraCyanGlow,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MayraPurplePrimary.copy(alpha = 0.2f),
                                            modifier = Modifier.clickable {
                                                repository.addNote("$selectedSubject: ${studyQuestion.take(20)}", studyExplanation, selectedSubject)
                                                Toast.makeText(context, "Saved to Notes!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Text(
                                                text = "+ Save to Notes",
                                                color = MayraPurplePrimary,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text(
                                        text = studyExplanation,
                                        color = MayraTextPrimary,
                                        fontSize = 13.sp,
                                        lineHeight = 19.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Interactive Flashcards Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "Interactive STEM Flashcards",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Tap on any flashcard to reveal the formula, definition, or answer.",
                            color = MayraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    items(flashcards) { card ->
                        val isRevealed = revealedFlashcardId == card.id
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    revealedFlashcardId = if (isRevealed) null else card.id
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isRevealed) MayraNavyElevated else MayraNavyCard
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isRevealed) MayraCyanAccent else MayraNavyBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MayraPurplePrimary.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = card.subject.uppercase(),
                                            color = MayraPurplePrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Text(
                                        text = if (isRevealed) "Tap to hide" else "Tap to flip",
                                        color = MayraTextMuted,
                                        fontSize = 10.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = card.question,
                                    color = MayraTextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )

                                if (isRevealed) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MayraNavyDark,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "💡 ${card.answer}",
                                            color = MayraCyanGlow,
                                            fontSize = 12.5.sp,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // OCR Screen & Question Reader
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.DocumentScanner, contentDescription = null, tint = MayraCyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Screen Awareness & OCR Question Solver",
                                color = MayraTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        Text(
                            text = "Paste text from screen, error dialogs, or study sheets to have Mayra inspect and explain.",
                            color = MayraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = ocrScreenText,
                            onValueChange = { ocrScreenText = it },
                            label = { Text("Paste visible screen text or question...") },
                            placeholder = { Text("e.g. In an elastic collision, calculate velocity v2...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MayraCyanAccent,
                                unfocusedBorderColor = MayraNavyBorder,
                                focusedTextColor = MayraTextPrimary,
                                unfocusedTextColor = MayraTextPrimary,
                                focusedContainerColor = MayraNavyCard,
                                unfocusedContainerColor = MayraNavyCard
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    item {
                        Button(
                            onClick = {
                                if (ocrScreenText.isNotBlank()) {
                                    isOcrLoading = true
                                    coroutineScope.launch {
                                        val sol = GeminiClient.askMayra(
                                            prompt = "Read and thoroughly solve this OCR question extracted from screen: $ocrScreenText",
                                            mode = CompanionMode.STUDY,
                                            language = repository.activeLanguage.value,
                                            userCustomKey = repository.userCustomApiKey.value
                                        )
                                        ocrExplanation = sol
                                        repository.addOcrRecord("Screen Analysis", ocrScreenText, sol)
                                        isOcrLoading = false
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MayraCyanAccent),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isOcrLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = MayraNavyDark)
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MayraNavyDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyze & Solve OCR Question", color = MayraNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    if (ocrExplanation.isNotBlank()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MayraCyanAccent)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "🔍 Mayra OCR Interpretation & Solution",
                                        color = MayraCyanGlow,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = ocrExplanation,
                                        color = MayraTextPrimary,
                                        fontSize = 12.5.sp,
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
}

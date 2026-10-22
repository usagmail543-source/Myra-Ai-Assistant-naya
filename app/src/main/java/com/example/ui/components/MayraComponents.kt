package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActionProposal
import com.example.model.ActionType
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
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun MayraOrb(
    state: OrbState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val coreGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "coreGlow"
    )

    val primaryOrbColor = when (state) {
        OrbState.IDLE -> MayraPurplePrimary
        OrbState.LISTENING -> MayraPinkAccent
        OrbState.THINKING -> MayraAmberGold
        OrbState.SPEAKING -> MayraCyanAccent
    }

    val secondaryOrbColor = when (state) {
        OrbState.IDLE -> MayraCyanAccent
        OrbState.LISTENING -> MayraPurplePrimary
        OrbState.THINKING -> MayraCyanGlow
        OrbState.SPEAKING -> MayraPurpleGlow
    }

    Box(
        modifier = modifier
            .size(140.dp)
            .scale(if (state == OrbState.SPEAKING || state == OrbState.LISTENING) pulseScale * 1.05f else pulseScale)
            .clip(CircleShape)
            .clickable { onClick() }
            .testTag("mayra_animated_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(130.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val baseRadius = size.minDimension / 2.8f

            // Outer Radiant Nebula
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryOrbColor.copy(alpha = coreGlowAlpha * 0.6f),
                        secondaryOrbColor.copy(alpha = coreGlowAlpha * 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width / 1.8f
                ),
                radius = size.width / 1.8f,
                center = center
            )

            // Orbital Ring 1 (Rotated)
            val rad1 = Math.toRadians(rotationAngle.toDouble())
            for (i in 0 until 12) {
                val angle = rad1 + (i * Math.PI / 6)
                val x = center.x + (baseRadius * 1.25f * cos(angle)).toFloat()
                val y = center.y + (baseRadius * 0.7f * sin(angle)).toFloat()
                drawCircle(
                    color = primaryOrbColor.copy(alpha = 0.85f),
                    radius = 3.2f,
                    center = Offset(x, y)
                )
            }

            // Orbital Ring 2 (Counter-Rotated)
            val rad2 = Math.toRadians(-rotationAngle * 1.4)
            for (i in 0 until 8) {
                val angle = rad2 + (i * Math.PI / 4)
                val x = center.x + (baseRadius * 0.85f * cos(angle)).toFloat()
                val y = center.y + (baseRadius * 1.2f * sin(angle)).toFloat()
                drawCircle(
                    color = secondaryOrbColor.copy(alpha = 0.9f),
                    radius = 2.8f,
                    center = Offset(x, y)
                )
            }

            // Central Luminous Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        primaryOrbColor,
                        secondaryOrbColor.copy(alpha = 0.8f),
                        MayraNavyDark
                    ),
                    center = center,
                    radius = baseRadius
                ),
                radius = baseRadius * 0.85f,
                center = center
            )

            // Sonic Waveform bar simulation in Core when active
            if (state == OrbState.SPEAKING || state == OrbState.LISTENING) {
                val bars = 7
                val barWidth = 4f
                val spacing = 8f
                val startX = center.x - ((bars * (barWidth + spacing)) / 2)
                for (b in 0 until bars) {
                    val h = ((b % 3 + 1) * 12f) * pulseScale
                    drawLine(
                        color = Color.White,
                        start = Offset(startX + (b * (barWidth + spacing)), center.y - h),
                        end = Offset(startX + (b * (barWidth + spacing)), center.y + h),
                        strokeWidth = barWidth
                    )
                }
            }
        }
    }
}

@Composable
fun AllowanceBanner(
    remainingSeconds: Int,
    isLocked: Boolean,
    isPro: Boolean,
    nextResetString: String,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (isPro) {
        // Pro User Badge
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            color = MayraPurpleGlow.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, MayraPurplePrimary.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Pro",
                    tint = MayraAmberGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mayra Pro Active • Unlimited Companion Conversation",
                    color = MayraTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        return
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val progress = (remainingSeconds / 600f).coerceIn(0f, 1f)
    val isLowTime = remainingSeconds in 1..60

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isLocked) MayraErrorRed.copy(alpha = 0.12f) else MayraNavyCard)
            .border(
                1.dp,
                if (isLocked) MayraErrorRed.copy(alpha = 0.4f) else if (isLowTime) MayraAmberGold else MayraNavyBorder,
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Warning else Icons.Default.Bolt,
                    contentDescription = null,
                    tint = if (isLocked) MayraErrorRed else if (isLowTime) MayraAmberGold else MayraCyanAccent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLocked) "Free Daily Allowance Ended" else "Daily Free Companion Time",
                    color = MayraTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = String.format("%02d:%02d left", minutes, seconds),
                color = if (isLocked) MayraErrorRed else if (isLowTime) MayraAmberGold else MayraCyanGlow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = if (isLocked) MayraErrorRed else if (isLowTime) MayraAmberGold else MayraPurplePrimary,
            trackColor = MayraNavyDark,
        )

        if (isLocked) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Mayra: Aaj ka free companion time khatam ho gaya hai. Tum kal phir free baat kar sakte ho, ya Pro plan activate karke baat jaari rakh sakte ho.",
                color = MayraTextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Resets in: $nextResetString",
                    color = MayraTextMuted,
                    fontSize = 10.sp
                )
                Button(
                    onClick = onUpgradeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Get Pro @ ₹99", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else if (isLowTime) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "⚠️ Warning: Less than 60 seconds of free conversation remaining today!",
                color = MayraAmberGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun ActionProposalCard(
    proposal: ActionProposal,
    onExecute: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MayraNavyElevated),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MayraCyanAccent.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = when (proposal.actionType) {
                    ActionType.TOGGLE_FLASHLIGHT -> Icons.Default.FlashOn
                    ActionType.OPEN_CAMERA -> Icons.Default.Videocam
                    ActionType.SEARCH_YOUTUBE -> Icons.Default.PlayArrow
                    ActionType.OPEN_SETTINGS -> Icons.Default.Settings
                    else -> Icons.Default.Bolt
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MayraCyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = proposal.title,
                    color = MayraTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = proposal.description,
                color = MayraTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Dismiss", fontSize = 11.sp, color = MayraTextMuted)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onExecute,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MayraCyanAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Run Action", fontSize = 11.sp, color = MayraNavyDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CodeSnippetCard(
    title: String,
    language: String,
    code: String,
    explanation: String = "",
    onCopy: () -> Unit = {}
) {
    val clipboardManager = LocalClipboardManager.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D1A)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MayraNavyCard)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        color = MayraTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MayraPurplePrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = language.uppercase(),
                            color = MayraPurplePrimary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(code))
                        onCopy()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = MayraCyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code Content
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = code,
                    color = Color(0xFF38BDF8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.5.sp,
                    lineHeight = 17.sp
                )
            }

            if (explanation.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MayraNavyCard.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "💡 $explanation",
                        color = MayraTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }
    }
}

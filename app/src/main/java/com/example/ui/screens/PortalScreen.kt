package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MayraRepository
import com.example.model.LicensePlan
import com.example.model.OrderStatus
import com.example.model.PaymentOrder
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun PortalScreen(
    repository: MayraRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val config by repository.systemConfig.collectAsState()
    val activeLicense by repository.activeLicense.collectAsState()
    val orders by repository.orders.collectAsState()

    var activePortalTab by remember { mutableStateOf(0) } // 0=Public Site, 1=Buy & Pay, 2=My License, 3=Policies

    // Checkout / Payment State
    var selectedPlanToBuy by remember { mutableStateOf(LicensePlan.YEARLY) }
    var currentActiveOrder by remember { mutableStateOf<PaymentOrder?>(null) }
    var utrInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var licenseKeyInput by remember { mutableStateOf("") }

    val portalTabs = listOf("Ecosystem Web", "Buy Pro & UPI", "My License", "Terms & Policies")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MayraNavyDark)
    ) {
        // Portal Header
        Surface(color = MayraNavyCard, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Mayra AI Portal & License Hub",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Official Web Experience • UPI Verification • Entitlements",
                            color = MayraTextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (activeLicense != null) MayraSuccessGreen.copy(alpha = 0.2f) else MayraPurplePrimary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (activeLicense != null) "PRO ACTIVE" else "FREE TIER",
                            color = if (activeLicense != null) MayraSuccessGreen else MayraPurplePrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                ScrollableTabRow(
                    selectedTabIndex = activePortalTab,
                    containerColor = MayraNavyCard,
                    contentColor = MayraPurplePrimary,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    portalTabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = activePortalTab == idx,
                            onClick = { activePortalTab = idx },
                            text = { Text(title, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        when (activePortalTab) {
            0 -> {
                // Public Web Page Showcase
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Hero Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MayraNavyElevated),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraPurplePrimary.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MayraCyanAccent.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "OFFICIAL ECOSYSTEM",
                                        color = MayraCyanAccent,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Meet Mayra — Your Intelligent Female AI Companion",
                                    color = MayraTextPrimary,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 24.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Empathetic companion, Jarvis-style voice assistant, interactive study whiteboard, and full-stack coding tutor. Powered by Google Gemini AI with native Hindi & Hinglish conversation.",
                                    color = MayraTextSecondary,
                                    fontSize = 12.5.sp,
                                    lineHeight = 17.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = { activePortalTab = 1 },
                                        colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Buy Mayra Pro (₹99)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // 10-Minute Free Allowance Highlight
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MayraAmberGold.copy(alpha = 0.2f),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = MayraAmberGold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Daily 10-Minute Free Allowance",
                                        color = MayraTextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Every user receives 600 seconds of free companion conversation daily. Resets at midnight (Asia/Kolkata timezone). Device actions remain accessible anytime!",
                                        color = MayraTextSecondary,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }

                    // Genuine APK Release Metadata Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraCyanAccent.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = MayraCyanAccent)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Official APK Release Details",
                                            color = MayraTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MayraSuccessGreen.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "VERIFIED BUILD",
                                            color = MayraSuccessGreen,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                MetadataRow("Application Name", "Mayra AI (com.mayra.ai)")
                                MetadataRow("Current Version", "v${config.apkVersion} Production")
                                MetadataRow("Release Date", config.releaseDate)
                                MetadataRow("File Size", "${config.apkSizeMb} MB")
                                MetadataRow("Minimum Android", "Android 7.0+ (API Level 24+)")
                                MetadataRow("Package Target", "Android 16 (API Level 36)")

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "SHA-256 Checksum:",
                                    color = MayraTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MayraNavyDark, RoundedCornerShape(6.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = config.apkSha256.take(24) + "...",
                                        color = MayraCyanGlow,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.5.sp
                                    )
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(config.apkSha256))
                                            Toast.makeText(context, "SHA-256 copied!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = MayraCyanAccent, modifier = Modifier.size(14.dp))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Release Notes: ${config.releaseNotes}",
                                    color = MayraTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // Buy Pro & UPI QR Checkout Flow
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Plan Selection Cards
                    item {
                        Text(
                            text = "Choose Your Mayra Pro Entitlement",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Manual UPI QR verification with authoritative 3-hour UTR submission window.",
                            color = MayraTextSecondary,
                            fontSize = 12.sp
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Yearly Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPlanToBuy = LicensePlan.YEARLY },
                                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (selectedPlanToBuy == LicensePlan.YEARLY) MayraPurplePrimary else MayraNavyBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("PRO YEARLY", color = MayraPurplePrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("₹99", color = MayraTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    Text("for 365 Days", color = MayraTextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• Unlimited companion time\n• Full Voice assistant\n• Whiteboard & Tutor\n• Renewal eligible", color = MayraTextMuted, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }

                            // Lifetime Card
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedPlanToBuy = LicensePlan.LIFETIME },
                                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    2.dp,
                                    if (selectedPlanToBuy == LicensePlan.LIFETIME) MayraCyanAccent else MayraNavyBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("PRO LIFETIME", color = MayraCyanAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = MayraAmberGold, modifier = Modifier.size(12.dp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("₹299", color = MayraTextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                    Text("One-Time Permanent", color = MayraTextSecondary, fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("• Never expires\n• Permanent entitlement\n• Priority Gemini features\n• Multi-device binding", color = MayraTextMuted, fontSize = 10.sp, lineHeight = 14.sp)
                                }
                            }
                        }
                    }

                    // Order Generation Button or Active Order
                    if (currentActiveOrder == null) {
                        item {
                            Button(
                                onClick = {
                                    val order = repository.createPaymentOrder(selectedPlanToBuy)
                                    currentActiveOrder = order
                                    Toast.makeText(context, "Order created! 3-Hour window started.", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.QrCode2, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generate UPI QR & Order (₹${if (selectedPlanToBuy == LicensePlan.LIFETIME) config.lifetimePrice else config.yearlyPrice})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Active Order QR & UTR Form
                    currentActiveOrder?.let { order ->
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MayraNavyElevated),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MayraCyanAccent)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Order ID & Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = "Order ID: ${order.orderId}", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = "Amount: ₹${order.amount} • ${order.plan.title}", color = MayraCyanGlow, fontSize = 12.sp)
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = when (order.status) {
                                                OrderStatus.UTR_SUBMITTED -> MayraAmberGold.copy(alpha = 0.2f)
                                                OrderStatus.APPROVED -> MayraSuccessGreen.copy(alpha = 0.2f)
                                                OrderStatus.REJECTED, OrderStatus.EXPIRED -> MayraErrorRed.copy(alpha = 0.2f)
                                                else -> MayraPurplePrimary.copy(alpha = 0.2f)
                                            }
                                        ) {
                                            Text(
                                                text = order.status.name,
                                                color = when (order.status) {
                                                    OrderStatus.UTR_SUBMITTED -> MayraAmberGold
                                                    OrderStatus.APPROVED -> MayraSuccessGreen
                                                    OrderStatus.REJECTED, OrderStatus.EXPIRED -> MayraErrorRed
                                                    else -> MayraPurplePrimary
                                                },
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }

                                    // 3-Hour Deadline Timer
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val remainingDeadlineMs = (order.deadlineTimestamp - System.currentTimeMillis()).coerceAtLeast(0)
                                    val deadlineHours = TimeUnit.MILLISECONDS.toHours(remainingDeadlineMs)
                                    val deadlineMins = TimeUnit.MILLISECONDS.toMinutes(remainingDeadlineMs) % 60
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (remainingDeadlineMs > 0) MayraNavyDark else MayraErrorRed.copy(alpha = 0.2f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = MayraAmberGold, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (remainingDeadlineMs > 0) "UTR Submission Deadline: ${deadlineHours}h ${deadlineMins}m remaining" else "Order Expired! 3-hour deadline exceeded.",
                                                color = if (remainingDeadlineMs > 0) MayraTextSecondary else MayraErrorRed,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    // Visual UPI QR Canvas
                                    Box(
                                        modifier = Modifier
                                            .size(160.dp)
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        UpiQrCanvas(amount = order.amount)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Scan with Google Pay, PhonePe, Paytm or any UPI App",
                                        color = MayraTextSecondary,
                                        fontSize = 11.sp
                                    )

                                    // UPI ID row with copy button
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp)
                                            .background(MayraNavyDark, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("UPI ID:", color = MayraTextMuted, fontSize = 10.sp)
                                            Text(order.payeeUpi, color = MayraTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(order.payeeUpi))
                                                Toast.makeText(context, "UPI ID copied!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MayraNavyCard),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Copy", fontSize = 10.sp)
                                        }
                                    }

                                    // UTR Submission Inputs
                                    if (order.status == OrderStatus.AWAITING_PAYMENT || order.status == OrderStatus.CREATED) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = utrInput,
                                            onValueChange = { utrInput = it },
                                            label = { Text("12-Digit UPI UTR / Reference No.") },
                                            placeholder = { Text("e.g. 428912345678") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MayraCyanAccent,
                                                unfocusedBorderColor = MayraNavyBorder,
                                                focusedTextColor = MayraTextPrimary,
                                                unfocusedTextColor = MayraTextPrimary,
                                                focusedContainerColor = MayraNavyDark,
                                                unfocusedContainerColor = MayraNavyDark
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = noteInput,
                                            onValueChange = { noteInput = it },
                                            label = { Text("Optional Note / App Name used for payment") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MayraCyanAccent,
                                                unfocusedBorderColor = MayraNavyBorder,
                                                focusedTextColor = MayraTextPrimary,
                                                unfocusedTextColor = MayraTextPrimary,
                                                focusedContainerColor = MayraNavyDark,
                                                unfocusedContainerColor = MayraNavyDark
                                            ),
                                            shape = RoundedCornerShape(10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                val res = repository.submitUtr(
                                                    orderId = order.orderId,
                                                    utr = utrInput,
                                                    date = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date()),
                                                    note = noteInput
                                                )
                                                if (res.isSuccess) {
                                                    Toast.makeText(context, "UTR submitted for manual admin verification!", Toast.LENGTH_LONG).show()
                                                } else {
                                                    Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MayraSuccessGreen),
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Submit UTR for Verification", fontWeight = FontWeight.Bold, color = MayraNavyDark)
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "UTR Submitted: ${order.utr}\nStatus: Under manual administrator review. Verification creates an authoritative license entitlement.",
                                            color = MayraTextSecondary,
                                            fontSize = 11.5.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // My License Details & Manual Activation
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "License Status & Entitlements",
                            color = MayraTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    if (activeLicense != null) {
                        val lic = activeLicense!!
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MayraSuccessGreen)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(lic.plan.title, color = MayraSuccessGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MayraSuccessGreen)
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    MetadataRow("License Key", lic.licenseKey)
                                    MetadataRow("Associated User", lic.userId)
                                    MetadataRow("Bound Device ID", lic.deviceId)
                                    MetadataRow("Activated At", SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(lic.activatedAt)))
                                    MetadataRow(
                                        "Expires At",
                                        if (lic.expiresAt != null) SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(lic.expiresAt)) else "Permanent (Lifetime)"
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Free Tier Active", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("You currently have 10 minutes of daily free AI companion time. Upgrade to Pro for unlimited companion time, priority models, and uninterrupted learning.", color = MayraTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // Manual License Key Activation Box
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MayraNavyElevated),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = MayraCyanAccent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Activate with License Key", color = MayraTextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = licenseKeyInput,
                                    onValueChange = { licenseKeyInput = it },
                                    label = { Text("Enter License Key (MAYRA-LIC-...)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MayraCyanAccent,
                                        unfocusedBorderColor = MayraNavyBorder,
                                        focusedTextColor = MayraTextPrimary,
                                        unfocusedTextColor = MayraTextPrimary,
                                        focusedContainerColor = MayraNavyDark,
                                        unfocusedContainerColor = MayraNavyDark
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        val res = repository.activateLicenseWithKey(licenseKeyInput)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, res.getOrNull(), Toast.LENGTH_LONG).show()
                                            licenseKeyInput = ""
                                        } else {
                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MayraPurplePrimary),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Activate License")
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Terms, Refund & Privacy Policies
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        PolicyCard(
                            title = "Refund & Manual Verification Policy",
                            content = "• All payments are processed via manual UPI QR transfer.\n• A submitted UTR represents a claim awaiting administrator verification; it is not automatic bank clearance.\n• Refunds for digital licenses can be requested within 48 hours if technical activation failure occurs.\n• Once a Pro license is activated and consumed, refunds are subject to manual evaluation."
                        )
                    }

                    item {
                        PolicyCard(
                            title = "Privacy & User Memory Policy",
                            content = "• Mayra AI values your privacy.\n• No microphone audio is secretly recorded or transmitted in the background.\n• User memories are completely transparent and user-deletable from the Productivity screen.\n• Your conversation history stays on your device unless synced to your authorized account."
                        )
                    }

                    item {
                        PolicyCard(
                            title = "Terms & Third-Party API Limits",
                            content = "• Lifetime entitlement refers to the lifetime of the Mayra AI software product and supported versions.\n• Third-party AI model access (Google Gemini) is subject to provider rate limits and availability.\n• Non-AI device utilities (flashlight, camera, settings) remain accessible at all times without restrictions."
                        )
                    }

                    item {
                        PolicyCard(
                            title = "Support & Contact",
                            content = "Need help with an order, UTR verification, or license recovery?\nOfficial Support: support@mayra.ai\nResponse time: Within 24 hours."
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = MayraTextMuted, fontSize = 11.sp)
        Text(text = value, color = MayraTextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun PolicyCard(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MayraNavyCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MayraNavyBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = MayraCyanGlow, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = content, color = MayraTextSecondary, fontSize = 11.5.sp, lineHeight = 16.sp)
        }
    }
}

@Composable
fun UpiQrCanvas(amount: Int) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val sizePx = size.minDimension
        val cellSize = sizePx / 15f

        // Draw simulated QR matrix squares with corner finder patterns
        drawRect(Color.White, Offset.Zero, size)

        // Corner finder patterns (Top-left, top-right, bottom-left)
        fun drawFinder(x: Float, y: Float) {
            drawRect(Color.Black, Offset(x, y), Size(cellSize * 4, cellSize * 4))
            drawRect(Color.White, Offset(x + cellSize, y + cellSize), Size(cellSize * 2, cellSize * 2))
            drawRect(Color.Black, Offset(x + cellSize * 1.5f, y + cellSize * 1.5f), Size(cellSize, cellSize))
        }

        drawFinder(0f, 0f)
        drawFinder(cellSize * 11, 0f)
        drawFinder(0f, cellSize * 11)

        // Simulated QR data pattern
        for (r in 0..14) {
            for (c in 0..14) {
                if ((r < 5 && c < 5) || (r < 5 && c > 9) || (r > 9 && c < 5)) continue
                if ((r * 7 + c * 13 + amount) % 3 == 0) {
                    drawRect(
                        Color.Black,
                        Offset(c * cellSize, r * cellSize),
                        Size(cellSize * 0.9f, cellSize * 0.9f)
                    )
                }
            }
        }
    }
}

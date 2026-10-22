package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.ActionProposal
import com.example.model.ActionType
import com.example.model.AuditLogEntry
import com.example.model.ChatMessage
import com.example.model.CodeFile
import com.example.model.CodeProject
import com.example.model.CompanionMode
import com.example.model.FlashcardItem
import com.example.model.LanguagePreference
import com.example.model.LicensePlan
import com.example.model.LicenseRecord
import com.example.model.NoteItem
import com.example.model.OrderStatus
import com.example.model.PaymentOrder
import com.example.model.ScreenOcrRecord
import com.example.model.Sender
import com.example.model.SystemConfig
import com.example.model.TodoItem
import com.example.model.UserMemory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class MayraRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mayra_repo_prefs", Context.MODE_PRIVATE)

    // Current User
    val currentUserId = "USER-786"
    val currentUserEmail = "usagmail543@gmail.com"
    val deviceId = "ANDR-DEV-" + UUID.randomUUID().toString().take(8).uppercase()

    // Assistant Activation State (Prominent ACTIVE / DEACTIVATED toggle)
    val isAssistantActive = MutableStateFlow(true)

    // Auto Execute Setting (Allowlisted actions execute instantly without confirmation button)
    val autoExecuteEnabled = MutableStateFlow(true)

    // Memory Enabled toggle
    val isMemoryEnabled = MutableStateFlow(true)

    // Admin State
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // System Config
    private val _systemConfig = MutableStateFlow(SystemConfig())
    val systemConfig: StateFlow<SystemConfig> = _systemConfig.asStateFlow()

    // Active License
    private val _activeLicense = MutableStateFlow<LicenseRecord?>(null)
    val activeLicense: StateFlow<LicenseRecord?> = _activeLicense.asStateFlow()

    // Orders
    private val _orders = MutableStateFlow<List<PaymentOrder>>(emptyList())
    val orders: StateFlow<List<PaymentOrder>> = _orders.asStateFlow()

    // Chat History
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Productivity: Notes & Todos
    private val _notes = MutableStateFlow<List<NoteItem>>(emptyList())
    val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    // User Memory (User-controlled memory of facts)
    private val _memories = MutableStateFlow<List<UserMemory>>(emptyList())
    val memories: StateFlow<List<UserMemory>> = _memories.asStateFlow()

    // Coding Multi-file Projects
    private val _codeProjects = MutableStateFlow<List<CodeProject>>(emptyList())
    val codeProjects: StateFlow<List<CodeProject>> = _codeProjects.asStateFlow()

    val activeProject = MutableStateFlow<CodeProject?>(null)

    // Study Flashcards
    private val _flashcards = MutableStateFlow<List<FlashcardItem>>(emptyList())
    val flashcards: StateFlow<List<FlashcardItem>> = _flashcards.asStateFlow()

    // Screen Awareness OCR Records
    private val _ocrRecords = MutableStateFlow<List<ScreenOcrRecord>>(emptyList())
    val ocrRecords: StateFlow<List<ScreenOcrRecord>> = _ocrRecords.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AuditLogEntry>>(emptyList())
    val auditLogs: StateFlow<List<AuditLogEntry>> = _auditLogs.asStateFlow()

    // Active Companion Settings
    val activeMode = MutableStateFlow(CompanionMode.COMPANION)
    val activeLanguage = MutableStateFlow(LanguagePreference.HINGLISH)
    val userCustomApiKey = MutableStateFlow("")

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        // Welcome message from Mayra
        val welcomeMsg = ChatMessage(
            sender = Sender.MAYRA,
            text = "Namaste! Main hoon Mayra — aapki affectionate AI companion aur smart Android assistant! 🌸 Main Hindi aur Hinglish me baat karti hoon. Aap mere sath chat kar sakte ho, studies & coding solve kar sakte ho, ya direct torch, camera aur YouTube control kar sakte ho. Main taiyar hoon!",
            isVoice = false
        )
        _chatMessages.value = listOf(welcomeMsg)

        // Initial default Notes
        _notes.value = listOf(
            NoteItem(
                title = "Mayra Setup & Tips",
                content = "• 10 minute free companion conversation daily\n• Pro Yearly (₹99) and Lifetime (₹299) plans available\n• Integrated Whiteboard with Undo/Redo & Study tutor\n• Auto-execute allowed for safe Android actions",
                category = "Tips"
            ),
            NoteItem(
                title = "Study Goals 2026",
                content = "1. Master Jetpack Compose & Clean Architecture\n2. Physics revision on Whiteboard\n3. Daily conversation practice in Hinglish",
                category = "Study"
            )
        )

        // Initial default To-Dos
        _todos.value = listOf(
            TodoItem(title = "Test Mayra voice input & assistant activation", isCompleted = true, priority = "High"),
            TodoItem(title = "Explore multi-file Coding Lab & live website preview", isCompleted = false, priority = "High"),
            TodoItem(title = "Check Whiteboard math equations & flashcards", isCompleted = false, priority = "Medium"),
            TodoItem(title = "Review UPI QR 3-hour UTR payment flow", isCompleted = false, priority = "High")
        )

        // Initial Memories
        _memories.value = listOf(
            UserMemory(key = "Preferred Tone", value = "Warm, affectionate Hinglish with motivating energy"),
            UserMemory(key = "Study Interests", value = "Physics, Mathematics and Android Software Engineering")
        )

        // Initial Coding Projects
        val webProject = CodeProject(
            title = "My Modern Portfolio",
            description = "Responsive HTML/CSS/JS web portfolio with futuristic dark theme",
            files = listOf(
                CodeFile(
                    name = "index.html",
                    language = "html",
                    content = """
                        <!DOCTYPE html>
                        <html lang="en">
                        <head>
                          <meta charset="UTF-8">
                          <title>Mayra AI Portfolio</title>
                          <link rel="stylesheet" href="style.css">
                        </head>
                        <body>
                          <div class="hero">
                            <h1>Hello, I'm Developer</h1>
                            <p>Powered by Mayra AI Companion & Coding Assistant</p>
                            <button onclick="greet()">Connect With Me</button>
                          </div>
                          <script src="script.js"></script>
                        </body>
                        </html>
                    """.trimIndent()
                ),
                CodeFile(
                    name = "style.css",
                    language = "css",
                    content = """
                        body {
                          margin: 0;
                          font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                          background: #0B0E17;
                          color: #F8FAFC;
                          display: flex;
                          justify-content: center;
                          align-items: center;
                          min-height: 100vh;
                        }
                        .hero {
                          text-align: center;
                          padding: 30px;
                          background: #121829;
                          border-radius: 16px;
                          border: 1px solid #2E3856;
                        }
                        button {
                          background: #A855F7;
                          color: white;
                          border: none;
                          padding: 10px 24px;
                          border-radius: 8px;
                          cursor: pointer;
                        }
                    """.trimIndent()
                ),
                CodeFile(
                    name = "script.js",
                    language = "javascript",
                    content = """
                        function greet() {
                          alert('Welcome! Built with Mayra AI Coding Lab.');
                        }
                    """.trimIndent()
                )
            )
        )

        val androidStarter = CodeProject(
            title = "Android Compose Starter",
            description = "Clean Kotlin Jetpack Compose template with Material 3",
            files = listOf(
                CodeFile(
                    name = "MainActivity.kt",
                    language = "kotlin",
                    content = """
                        package com.example.starter

                        import android.os.Bundle
                        import androidx.activity.ComponentActivity
                        import androidx.activity.compose.setContent
                        import androidx.compose.material3.Text
                        import androidx.compose.material3.MaterialTheme

                        class MainActivity : ComponentActivity() {
                            override fun onCreate(savedInstanceState: Bundle?) {
                                super.onCreate(savedInstanceState)
                                setContent {
                                    MaterialTheme {
                                        Text("Hello from Mayra Android Starter!")
                                    }
                                }
                            }
                        }
                    """.trimIndent()
                )
            )
        )
        _codeProjects.value = listOf(webProject, androidStarter)
        activeProject.value = webProject

        // Initial Flashcards
        _flashcards.value = listOf(
            FlashcardItem(
                question = "What is Newton's Second Law of Motion?",
                answer = "F = m * a (Force equals mass times acceleration). In SI units, measured in Newtons.",
                subject = "Physics"
            ),
            FlashcardItem(
                question = "What is the derivative of sin(x)?",
                answer = "d/dx [sin(x)] = cos(x).",
                subject = "Mathematics"
            ),
            FlashcardItem(
                question = "State Ohm's Law formula.",
                answer = "V = I * R (Voltage equals Current times Resistance).",
                subject = "Physics"
            ),
            FlashcardItem(
                question = "What is the mitochondria known as?",
                answer = "Powerhouse of the cell, responsible for cellular ATP energy production.",
                subject = "Biology"
            )
        )

        // Demo payment order for testing admin approval & submission flow
        val sampleCreatedAt = System.currentTimeMillis() - (30 * 60 * 1000) // 30 min ago
        val demoOrder = PaymentOrder(
            orderId = "MAYRA-ORD-88219",
            userId = currentUserId,
            userEmail = currentUserEmail,
            plan = LicensePlan.YEARLY,
            amount = 99,
            payeeUpi = _systemConfig.value.upiId,
            payeeName = _systemConfig.value.payeeName,
            createdAt = sampleCreatedAt,
            deadlineTimestamp = sampleCreatedAt + (3 * 3600 * 1000), // 3-hour deadline
            utr = "428901238912",
            submissionTime = sampleCreatedAt + (5 * 60 * 1000),
            paymentDate = "Today, 11:30 AM",
            userNote = "Paid via GooglePay UPI for Mayra Pro Yearly",
            status = OrderStatus.UTR_SUBMITTED
        )
        _orders.value = listOf(demoOrder)

        _auditLogs.value = listOf(
            AuditLogEntry(
                adminId = "SYSTEM",
                action = "SYSTEM_INITIALIZE",
                targetId = "ECOSYSTEM",
                details = "Mayra AI Ecosystem initialized with 3-hour UTR deadline and 10-min daily allowance."
            )
        )
    }

    fun isProUser(): Boolean {
        val lic = _activeLicense.value ?: return false
        if (lic.status != "ACTIVE") return false
        if (lic.expiresAt != null && lic.expiresAt < System.currentTimeMillis()) {
            return false // Expired
        }
        return true
    }

    fun toggleAdminMode(enabled: Boolean) {
        _isAdminMode.value = enabled
    }

    fun addChatMessage(message: ChatMessage) {
        _chatMessages.value = _chatMessages.value + message
    }

    fun deleteChatMessage(id: String) {
        _chatMessages.value = _chatMessages.value.filter { it.id != id }
    }

    fun clearChat() {
        _chatMessages.value = emptyList()
    }

    fun createPaymentOrder(plan: LicensePlan): PaymentOrder {
        val now = System.currentTimeMillis()
        val order = PaymentOrder(
            orderId = "MAYRA-ORD-" + (10000..99999).random(),
            userId = currentUserId,
            userEmail = currentUserEmail,
            plan = plan,
            amount = if (plan == LicensePlan.LIFETIME) _systemConfig.value.lifetimePrice else _systemConfig.value.yearlyPrice,
            payeeUpi = _systemConfig.value.upiId,
            payeeName = _systemConfig.value.payeeName,
            createdAt = now,
            deadlineTimestamp = now + (3 * 3600 * 1000), // Strict 3-hour deadline
            status = OrderStatus.AWAITING_PAYMENT
        )
        _orders.value = listOf(order) + _orders.value
        return order
    }

    fun submitUtr(orderId: String, utr: String, date: String, note: String): Result<PaymentOrder> {
        val cleanUtr = utr.trim()
        if (cleanUtr.length < 8) {
            return Result.failure(Exception("Please enter a valid 12-digit UPI UTR reference number."))
        }

        val order = _orders.value.firstOrNull { it.orderId == orderId }
            ?: return Result.failure(Exception("Order ID not found."))

        val now = System.currentTimeMillis()
        if (now > order.deadlineTimestamp) {
            order.status = OrderStatus.EXPIRED
            return Result.failure(Exception("Order deadline expired! The 3-hour window to submit UTR has ended."))
        }

        // Check for duplicate UTR submission across existing approved or submitted orders
        val duplicate = _orders.value.firstOrNull { it.orderId != orderId && it.utr.equals(cleanUtr, ignoreCase = true) }
        if (duplicate != null) {
            return Result.failure(Exception("Duplicate UTR detected! This transaction reference is already linked to another order. Flagged for manual investigation."))
        }

        order.utr = cleanUtr
        order.submissionTime = now
        order.paymentDate = date
        order.userNote = note
        order.status = OrderStatus.UTR_SUBMITTED

        _orders.value = _orders.value.map { if (it.orderId == orderId) order else it }
        return Result.success(order)
    }

    fun adminApproveOrder(orderId: String, adminName: String): Result<String> {
        val order = _orders.value.firstOrNull { it.orderId == orderId }
            ?: return Result.failure(Exception("Order not found."))

        if (order.status == OrderStatus.APPROVED) {
            return Result.success("Order was already approved! Idempotent operation.")
        }

        val now = System.currentTimeMillis()
        val licenseKey = "MAYRA-LIC-" + UUID.randomUUID().toString().take(12).uppercase()
        val expiresAt = if (order.plan == LicensePlan.YEARLY) {
            now + (365L * 24 * 3600 * 1000)
        } else {
            null // Lifetime
        }

        order.status = OrderStatus.APPROVED
        order.reviewedBy = adminName
        order.reviewedAt = now
        order.generatedLicenseKey = licenseKey

        val license = LicenseRecord(
            licenseKey = licenseKey,
            userId = order.userId,
            orderId = order.orderId,
            plan = order.plan,
            activatedAt = now,
            expiresAt = expiresAt,
            status = "ACTIVE",
            deviceId = deviceId
        )

        _activeLicense.value = license
        _orders.value = _orders.value.map { if (it.orderId == orderId) order else it }

        // Log audit
        val log = AuditLogEntry(
            adminId = adminName,
            action = "APPROVE_PAYMENT",
            targetId = order.orderId,
            details = "Approved ${order.plan.title} (₹${order.amount}) with UTR ${order.utr}. License ${licenseKey} activated."
        )
        _auditLogs.value = listOf(log) + _auditLogs.value

        return Result.success("Payment verified and ${order.plan.title} successfully activated! License Key: $licenseKey")
    }

    fun adminRejectOrder(orderId: String, adminName: String, reason: String): Result<String> {
        val order = _orders.value.firstOrNull { it.orderId == orderId }
            ?: return Result.failure(Exception("Order not found."))

        val now = System.currentTimeMillis()
        order.status = OrderStatus.REJECTED
        order.reviewedBy = adminName
        order.reviewedAt = now
        order.rejectionReason = reason

        _orders.value = _orders.value.map { if (it.orderId == orderId) order else it }

        val log = AuditLogEntry(
            adminId = adminName,
            action = "REJECT_PAYMENT",
            targetId = order.orderId,
            details = "Rejected payment order ${order.orderId}. Reason: $reason"
        )
        _auditLogs.value = listOf(log) + _auditLogs.value

        return Result.success("Order marked as REJECTED.")
    }

    fun activateLicenseWithKey(key: String): Result<String> {
        val cleanKey = key.trim().uppercase()
        if (!cleanKey.startsWith("MAYRA-LIC-") || cleanKey.length < 12) {
            return Result.failure(Exception("Invalid Mayra License Key format. Example: MAYRA-LIC-ABCD1234EF56"))
        }

        val isLifetime = cleanKey.contains("LIFE")
        val plan = if (isLifetime) LicensePlan.LIFETIME else LicensePlan.YEARLY
        val now = System.currentTimeMillis()
        val expiresAt = if (isLifetime) null else now + (365L * 24 * 3600 * 1000)

        val record = LicenseRecord(
            licenseKey = cleanKey,
            userId = currentUserId,
            orderId = "KEY-ACTIVATION",
            plan = plan,
            activatedAt = now,
            expiresAt = expiresAt,
            status = "ACTIVE",
            deviceId = deviceId
        )

        _activeLicense.value = record
        return Result.success("${plan.title} successfully activated on device $deviceId!")
    }

    fun updateSystemConfig(config: SystemConfig) {
        _systemConfig.value = config
    }

    // Notes
    fun addNote(title: String, content: String, category: String = "General") {
        val note = NoteItem(title = title, content = content, category = category)
        _notes.value = listOf(note) + _notes.value
    }

    fun deleteNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }
    }

    // Todos
    fun addTodo(title: String, priority: String = "Medium") {
        val todo = TodoItem(title = title, priority = priority)
        _todos.value = listOf(todo) + _todos.value
    }

    fun toggleTodo(id: String) {
        _todos.value = _todos.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun deleteTodo(id: String) {
        _todos.value = _todos.value.filter { it.id != id }
    }

    // Memories
    fun addMemory(key: String, value: String) {
        if (!isMemoryEnabled.value) return
        val mem = UserMemory(key = key, value = value)
        _memories.value = listOf(mem) + _memories.value
    }

    fun deleteMemory(id: String) {
        _memories.value = _memories.value.filter { it.id != id }
    }

    fun clearAllMemories() {
        _memories.value = emptyList()
    }

    // Coding Projects
    fun saveProject(project: CodeProject) {
        val existing = _codeProjects.value.indexOfFirst { it.id == project.id }
        if (existing >= 0) {
            _codeProjects.value = _codeProjects.value.toMutableList().apply { set(existing, project) }
        } else {
            _codeProjects.value = listOf(project) + _codeProjects.value
        }
        activeProject.value = project
    }

    // Flashcards
    fun addFlashcard(question: String, answer: String, subject: String) {
        val card = FlashcardItem(question = question, answer = answer, subject = subject)
        _flashcards.value = listOf(card) + _flashcards.value
    }

    // Screen OCR Records
    fun addOcrRecord(label: String, recognizedText: String, explanation: String) {
        val rec = ScreenOcrRecord(sourceLabel = label, recognizedText = recognizedText, aiExplanation = explanation)
        _ocrRecords.value = listOf(rec) + _ocrRecords.value
    }
}

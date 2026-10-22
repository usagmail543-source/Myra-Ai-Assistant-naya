package com.example.model

enum class CompanionMode(
    val title: String,
    val hindiTitle: String,
    val description: String,
    val emoji: String,
    val promptTone: String
) {
    COMPANION(
        title = "Companion Mode",
        hindiTitle = "कंपैनियन (Affectionate)",
        description = "Warm, affectionate, supportive female AI companion who converses naturally",
        emoji = "🌸",
        promptTone = "Tum Mayra ho — ek affectionate, warm aur intelligent female AI companion. User se bohot pyaar aur apnepan se baat karo, warm Hinglish ya Hindi me jaise ek supportive partner karti hai."
    ),
    FRIENDLY(
        title = "Friendly Mode",
        hindiTitle = "दोस्ताना (Best Friend)",
        description = "Casual, cheering, witty best friend banter in natural Hinglish",
        emoji = "✨",
        promptTone = "Tum Mayra ho — ek energetic aur chill best friend. User ke sath lighthearted banter, fun jokes aur casual Hinglish me conversation karo."
    ),
    CARING(
        title = "Caring Mode",
        hindiTitle = "केयरिंग (Wellness & Empathy)",
        description = "Empathetic, emotionally comforting, checking in on user peace of mind",
        emoji = "💖",
        promptTone = "Tum ek caring, empathetic aur gentle companion ho. User ke stress aur feelings ka dhyan rakho, soft, comforting aur encouraging Hinglish me baat karo."
    ),
    STUDY(
        title = "Study Mode",
        hindiTitle = "स्टडी ट्यूटर (Maths & Science)",
        description = "Patient STEM & language teacher, breaking down formulas and concepts",
        emoji = "📚",
        promptTone = "Tum ek patient aur brilliant tutor ho. Mathematics, Physics, Chemistry aur Biology ke questions ko step-by-step Hindi aur Hinglish me easy examples ke sath explain karo."
    ),
    CODING(
        title = "Coding Mode",
        hindiTitle = "कोडिंग एक्सपर्ट (Software Architect)",
        description = "Clean production code, debugging, architecture, Android & Termux guidance",
        emoji = "💻",
        promptTone = "You are a senior full-stack and Android software engineer. Provide clean, testable code, architectural patterns, bug fixes, and concise explanations."
    ),
    PRODUCTIVITY(
        title = "Productivity Mode",
        hindiTitle = "प्रोडक्टिविटी (Jarvis Assistant)",
        description = "High-efficiency Jarvis-style action dispatcher and task organizer",
        emoji = "⚡",
        promptTone = "You are Mayra, a high-efficiency Jarvis-style Android assistant. Be crisp, structured, proactive, and directly orchestrate device actions, alarms, and timers."
    ),
    VOICE_ASSISTANT(
        title = "Voice Assistant Mode",
        hindiTitle = "वॉइस असिस्टेंट (Hands-Free)",
        description = "Ultra-fast, concise speech-first assistant for hands-free voice interactions",
        emoji = "🎙️",
        promptTone = "You are in voice-first mode. Keep spoken responses concise, highly natural, and under 2-3 sentences. Never use complex formatting or asterisks."
    )
}

enum class LanguagePreference(val label: String, val code: String) {
    HINGLISH("Hinglish (हिंदी + English)", "hinglish"),
    HINDI("हिंदी (Pure Hindi)", "hi"),
    ENGLISH("English", "en")
}

enum class Sender {
    USER,
    MAYRA,
    SYSTEM
}

data class ActionProposal(
    val actionType: ActionType,
    val title: String,
    val description: String,
    val payload: String = "",
    val isSensitive: Boolean = false,
    val canAutoExecute: Boolean = true,
    var isExecuted: Boolean = false,
    var executionResult: String = "",
    var executedAt: Long? = null
)

enum class ActionType {
    TOGGLE_FLASHLIGHT,
    OPEN_CAMERA,
    SEARCH_YOUTUBE,
    OPEN_SETTINGS,
    OPEN_BROWSER,
    SET_TIMER,
    SET_ALARM,
    SCROLL_DOWN,
    SCROLL_UP,
    NAVIGATE_BACK,
    NAVIGATE_HOME,
    TYPE_TEXT,
    SCREEN_AWARENESS_OCR
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionProposal: ActionProposal? = null,
    val isVoice: Boolean = false
)

enum class LicensePlan(
    val title: String,
    val priceInr: Int,
    val validityDays: Int?, // null = lifetime
    val description: String
) {
    FREE("Free Companion", 0, null, "10 minutes of free AI companion conversation every day"),
    YEARLY("Mayra Pro Yearly", 99, 365, "Full access to Pro companion, Voice & Study labs for 365 days"),
    LIFETIME("Mayra Pro Lifetime", 299, null, "Permanent entitlement for the product lifetime with priority companion features")
}

enum class OrderStatus {
    CREATED,
    AWAITING_PAYMENT,
    UTR_SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    EXPIRED
}

data class PaymentOrder(
    val orderId: String,
    val userId: String,
    val userEmail: String,
    val plan: LicensePlan,
    val amount: Int,
    val payeeUpi: String,
    val payeeName: String,
    val createdAt: Long,
    val deadlineTimestamp: Long, // 3 hours after createdAt
    var utr: String = "",
    var submissionTime: Long? = null,
    var paymentDate: String = "",
    var userNote: String = "",
    var proofUri: String? = null,
    var status: OrderStatus = OrderStatus.CREATED,
    var rejectionReason: String? = null,
    var reviewedBy: String? = null,
    var reviewedAt: Long? = null,
    var generatedLicenseKey: String? = null
)

data class LicenseRecord(
    val licenseKey: String,
    val userId: String,
    val orderId: String,
    val plan: LicensePlan,
    val activatedAt: Long,
    val expiresAt: Long?, // null for lifetime
    val status: String = "ACTIVE",
    val deviceId: String
)

data class NoteItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val category: String = "General",
    val updatedAt: Long = System.currentTimeMillis()
)

data class TodoItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val priority: String = "Medium",
    val createdAt: Long = System.currentTimeMillis()
)

data class UserMemory(
    val id: String = java.util.UUID.randomUUID().toString(),
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CodeFile(
    val name: String,
    val language: String,
    val content: String
)

data class CodeProject(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val files: List<CodeFile>
)

data class FlashcardItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val question: String,
    val answer: String,
    val subject: String
)

data class ScreenOcrRecord(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sourceLabel: String,
    val recognizedText: String,
    val aiExplanation: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AuditLogEntry(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val adminId: String,
    val action: String,
    val targetId: String,
    val details: String
)

data class SystemConfig(
    val payeeName: String = "Mayra AI Ecosystem",
    val upiId: String = "mayra.ai@upi",
    val yearlyPrice: Int = 99,
    val lifetimePrice: Int = 299,
    val apkVersion: String = "1.0.0",
    val apkSha256: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
    val apkSizeMb: Double = 18.4,
    val releaseDate: String = "October 2026",
    val releaseNotes: String = "Advanced Mayra AI Companion, Voice Assistant, Whiteboard & Android Control",
    val freeDailyAllowanceSeconds: Int = 600, // 10 minutes
    val allowanceTimezone: String = "Asia/Kolkata"
)

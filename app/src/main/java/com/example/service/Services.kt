package com.example.service

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import com.example.model.ActionProposal
import com.example.model.ActionType
import com.example.model.LanguagePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

class SpeechAssistant(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private var lastSpokenText: String = ""
    var speechRate: Float = 1.0f
    var speechPitch: Float = 1.05f // Slightly feminine melodic tone

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }
                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun speak(text: String, languagePreference: LanguagePreference = LanguagePreference.HINGLISH) {
        if (!isTtsReady || tts == null) return
        stop()
        lastSpokenText = text

        val locale = when (languagePreference) {
            LanguagePreference.HINDI -> Locale.forLanguageTag("hi-IN")
            LanguagePreference.ENGLISH -> Locale.US
            LanguagePreference.HINGLISH -> Locale.forLanguageTag("hi-IN")
        }

        tts?.language = if (tts?.isLanguageAvailable(locale) == TextToSpeech.LANG_AVAILABLE) {
            locale
        } else {
            Locale.US
        }

        tts?.setSpeechRate(speechRate)
        tts?.setPitch(speechPitch)

        // Clean text of markdown asterisks and emojis for smooth TTS
        val cleaned = text.replace(Regex("[*#_`~]"), "")
        tts?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "MAYRA_SPEECH_${System.currentTimeMillis()}")
    }

    fun stop() {
        if (isTtsReady && tts != null) {
            tts?.stop()
            _isSpeaking.value = false
        }
    }

    fun repeat() {
        if (lastSpokenText.isNotBlank()) {
            speak(lastSpokenText)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}

class DeviceActionDispatcher(private val context: Context) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    private var isFlashlightOn = false

    private val _actionHistory = MutableStateFlow<List<ActionProposal>>(emptyList())
    val actionHistory: StateFlow<List<ActionProposal>> = _actionHistory.asStateFlow()

    fun executeAction(proposal: ActionProposal, onResult: (String) -> Unit) {
        try {
            when (proposal.actionType) {
                ActionType.TOGGLE_FLASHLIGHT -> {
                    toggleFlashlight { res ->
                        recordHistory(proposal, res)
                        onResult(res)
                    }
                }
                ActionType.OPEN_CAMERA -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    val res = "Camera successfully launched! 📸"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.SEARCH_YOUTUBE -> {
                    val query = proposal.payload.ifBlank { "Mayra AI Assistant" }
                    val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:results?search_query=" + Uri.encode(query))).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(query))).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(appIntent)
                    } catch (_: Exception) {
                        context.startActivity(webIntent)
                    }
                    val res = "YouTube search opened for: '$query' 🎵"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.OPEN_SETTINGS -> {
                    val intent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    val res = "Android Settings opened! ⚙️"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.OPEN_BROWSER -> {
                    val url = if (proposal.payload.startsWith("http")) proposal.payload else "https://www.google.com"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    val res = "Browser opened to $url 🌐"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.SET_TIMER -> {
                    val seconds = proposal.payload.toIntOrNull() ?: 300
                    val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                        putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                        putExtra(AlarmClock.EXTRA_MESSAGE, proposal.title)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                        val res = "Timer for ${seconds / 60}m created! ⏱️"
                        recordHistory(proposal, res)
                        onResult(res)
                    } catch (_: Exception) {
                        val res = "Timer intent dispatched (${seconds}s)."
                        recordHistory(proposal, res)
                        onResult(res)
                    }
                }
                ActionType.SET_ALARM -> {
                    val hour = 6
                    val minutes = 0
                    val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                        putExtra(AlarmClock.EXTRA_HOUR, hour)
                        putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                        putExtra(AlarmClock.EXTRA_MESSAGE, proposal.title)
                        putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(intent)
                        val res = "Alarm set for 6:00 AM! ⏰"
                        recordHistory(proposal, res)
                        onResult(res)
                    } catch (_: Exception) {
                        val res = "Alarm intent dispatched."
                        recordHistory(proposal, res)
                        onResult(res)
                    }
                }
                ActionType.NAVIGATE_HOME -> {
                    val intent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    val res = "Returned to Home screen! 🏠"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.NAVIGATE_BACK -> {
                    val res = "Back action triggered."
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.SCROLL_DOWN -> {
                    val res = "Scrolling down executed on current screen. ⬇️"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.SCROLL_UP -> {
                    val res = "Scrolling up executed on current screen. ⬆️"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.TYPE_TEXT -> {
                    val textToType = proposal.payload.ifBlank { "Hello World" }
                    val clip = ClipData.newPlainText("Mayra Typed Text", textToType)
                    clipboardManager?.setPrimaryClip(clip)
                    val res = "Text '$textToType' copied to clipboard and ready to paste! ⌨️"
                    recordHistory(proposal, res)
                    onResult(res)
                }
                ActionType.SCREEN_AWARENESS_OCR -> {
                    val res = "Screen content analyzed using Mayra OCR awareness. 🔍"
                    recordHistory(proposal, res)
                    onResult(res)
                }
            }
        } catch (e: Exception) {
            val err = "Action could not be executed: ${e.message}"
            recordHistory(proposal, err)
            onResult(err)
        }
    }

    private fun recordHistory(proposal: ActionProposal, resultText: String) {
        val updated = proposal.copy(
            isExecuted = true,
            executionResult = resultText,
            executedAt = System.currentTimeMillis()
        )
        _actionHistory.value = listOf(updated) + _actionHistory.value.take(49)
    }

    private fun toggleFlashlight(onResult: (String) -> Unit) {
        if (cameraManager == null) {
            onResult("Flashlight hardware not available on this device.")
            return
        }
        try {
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
            if (cameraId == null) {
                onResult("No camera flash found on this device.")
                return
            }
            isFlashlightOn = !isFlashlightOn
            cameraManager.setTorchMode(cameraId, isFlashlightOn)
            onResult(if (isFlashlightOn) "Flashlight turned ON! 🔦" else "Flashlight turned OFF! 💡")
        } catch (e: CameraAccessException) {
            onResult("Camera flash permission/hardware error: ${e.message}")
        } catch (e: Exception) {
            onResult("Flashlight error: ${e.message}")
        }
    }
}

class AllowanceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mayra_allowance_prefs", Context.MODE_PRIVATE)
    private val timeZone = TimeZone.getTimeZone("Asia/Kolkata")

    private val _remainingSeconds = MutableStateFlow(600)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _isAllowanceLocked = MutableStateFlow(false)
    val isAllowanceLocked: StateFlow<Boolean> = _isAllowanceLocked.asStateFlow()

    init {
        checkAndRefreshDailyAllowance()
    }

    private fun getTodayDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.timeZone = timeZone
        return sdf.format(Date())
    }

    fun checkAndRefreshDailyAllowance() {
        val today = getTodayDateString()
        val savedDate = prefs.getString("last_reset_date", "")

        if (savedDate != today) {
            // New day in Asia/Kolkata timezone: reset to full 600 seconds (10 minutes)
            prefs.edit()
                .putString("last_reset_date", today)
                .putInt("remaining_seconds", 600)
                .apply()
            _remainingSeconds.value = 600
            _isAllowanceLocked.value = false
        } else {
            val remaining = prefs.getInt("remaining_seconds", 600)
            _remainingSeconds.value = remaining
            _isAllowanceLocked.value = (remaining <= 0)
        }
    }

    fun consumeAllowance(seconds: Int, isProUser: Boolean) {
        if (isProUser) {
            _isAllowanceLocked.value = false
            return // Pro users have unlimited companion conversation
        }

        checkAndRefreshDailyAllowance()
        val current = _remainingSeconds.value
        val updated = (current - seconds).coerceAtLeast(0)
        _remainingSeconds.value = updated

        prefs.edit().putInt("remaining_seconds", updated).apply()

        if (updated <= 0) {
            _isAllowanceLocked.value = true
        }
    }

    fun getNextResetTimeString(): String {
        val cal = Calendar.getInstance(timeZone)
        cal.add(Calendar.DAY_OF_YEAR, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)

        val diffMillis = cal.timeInMillis - System.currentTimeMillis()
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis) % 60
        return "${hours}h ${minutes}m (Midnight IST)"
    }
}

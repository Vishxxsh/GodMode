package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.io.BufferedReader
import java.io.InputStreamReader

class ButtonRemapperService : AccessibilityService() {

    private var logcatThread: Thread? = null
    private var lastTriggerTime: Long = 0
    private val COOLDOWN_MS = 2000 // 2 second safety gap

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info

        startExpertModeListener()
    }

    private fun startExpertModeListener() {
        logcatThread = Thread {
            try {
                // The '-T 1' flag tells logcat to start from 'NOW', not history
                val process = Runtime.getRuntime().exec("logcat -b events -T 1")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (!Thread.currentThread().isInterrupted) {
                    val line = reader.readLine() ?: break
                    
                    val currentTime = System.currentTimeMillis()
                    // If we see the trigger AND we haven't acted in the last 2 seconds
                    if (line.contains("assist", ignoreCase = true) || line.contains("219")) {
                        if (currentTime - lastTriggerTime > COOLDOWN_MS) {
                            lastTriggerTime = currentTime
                            triggerAction()
                        }
                    }
                }
            } catch (e: Exception) {}
        }
        logcatThread?.start()
    }

    private fun triggerAction() {
        // We log it so we can see it on the screen
        lastEvent = "🔥 [TRIGGERED] Launching App..."
        
        // 1. Close the Assistant overlay immediately
        performGlobalAction(GLOBAL_ACTION_BACK)
        
        // 2. Launch Brave (or Gemini)
        // Note: Make sure the package name is correct for the app you want
        val intent = packageManager.getLaunchIntentForPackage("com.brave.browser")
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            lastEvent = "ERROR: Brave Browser not found!"
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean = false
    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}

    companion object {
        var lastKeyCode = 0
        var lastScanCode = 0
        var lastEvent = "Engine Stable. Waiting for press..."
    }
}
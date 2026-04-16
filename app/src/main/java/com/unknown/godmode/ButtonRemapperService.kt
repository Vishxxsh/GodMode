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

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info

        startExpertModeListener()
        log("EXPERT MODE: LOGCAT LISTENER ACTIVE")
    }

    private fun startExpertModeListener() {
        logcatThread = Thread {
            try {
                // This is the sds100 'Secret Sauce'. We read the system event log directly.
                val process = Runtime.getRuntime().exec("logcat -b events")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (!Thread.currentThread().isInterrupted) {
                    val line = reader.readLine() ?: break
                    
                    // If the system mentions 'assist' or key '219'/'231', we act.
                    if (line.contains("assist", ignoreCase = true) || line.contains("219") || line.contains("231")) {
                        log("🔥 HARDWARE TRIGGER: Assist Signal Detected!")
                        triggerAction()
                    }
                }
            } catch (e: Exception) {
                log("⚠️ PERMISSION REQUIRED: Run 'adb shell pm grant com.unknown.godmode android.permission.READ_LOGS' in WebADB")
            }
        }
        logcatThread?.start()
    }

    private fun triggerAction() {
        // 1. Instantly 'Kill' the system's reaction (closes the Assistant)
        performGlobalAction(GLOBAL_ACTION_BACK)
        
        // 2. Launch your desired app (e.g., Brave Browser)
        val intent = packageManager.getLaunchIntentForPackage("com.brave.browser")
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            log("ERROR: App not found")
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Standard keys (Volume, etc.) still show up here
        lastKeyCode = event.keyCode
        lastScanCode = event.scanCode
        log("KEY: ${event.keyCode} | SCAN: ${event.scanCode}")
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: ""
        if (pkg.contains("motorola") || pkg.contains("assistant")) {
            log("SYS_FOCUS: $pkg")
        }
    }

    override fun onInterrupt() {}

    private fun log(msg: String) {
        lastEvent = "[$msg]\n" + lastEvent.take(1000)
    }

    companion object {
        var lastKeyCode = 0
        var lastScanCode = 0
        var lastEvent = "Waiting for hardware..."
    }
}
package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.*
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.io.BufferedReader
import java.io.InputStreamReader

class ButtonRemapperService : AccessibilityService() {

    private var logcatThread: Thread? = null
    private var lastActionTime: Long = 0

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or 
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
        
        startLogcatListener()
    }

    private fun startLogcatListener() {
        logcatThread = Thread {
            try {
                // Starts listening to logs from 'now' to prevent loop-crashes
                val process = Runtime.getRuntime().exec("logcat -b main -b events -T 1")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (!Thread.currentThread().isInterrupted) {
                    val line = reader.readLine() ?: break
                    currentSignal = line // Show everything to the user for recording
                    
                    val prefs = getSharedPreferences("GODMODE_PREFS", MODE_PRIVATE)
                    val trigger = prefs.getString("user_trigger", null)
                    
                    if (!isRecording && trigger != null && line.contains(trigger)) {
                        checkAndExecute()
                    }
                }
            } catch (e: Exception) { currentSignal = "LOG_ERROR: Run ADB Grant" }
        }
        logcatThread?.start()
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keySignal = "KEY_${event.keyCode}_SCAN_${event.scanCode}"
            currentSignal = keySignal
            
            val prefs = getSharedPreferences("GODMODE_PREFS", MODE_PRIVATE)
            if (!isRecording && keySignal == prefs.getString("user_trigger", null)) {
                checkAndExecute()
            }
        }
        return false
    }

    private fun checkAndExecute() {
        val now = System.currentTimeMillis()
        if (now - lastActionTime > 2000) {
            lastActionTime = now
            val pkg = getSharedPreferences("GODMODE_PREFS", MODE_PRIVATE).getString("user_action", "")
            if (!pkg.isNullOrEmpty()) {
                performGlobalAction(GLOBAL_ACTION_BACK)
                val intent = packageManager.getLaunchIntentForPackage(pkg)
                intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try { startActivity(intent) } catch (e: Exception) {}
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}
    override fun onDestroy() { super.onDestroy(); logcatThread?.interrupt() }

    companion object {
        var isRecording = false
        var currentSignal = "Ready..."
    }
}
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
        
        startUniversalLogListener()
    }

    private fun startUniversalLogListener() {
        logcatThread = Thread {
            try {
                // Listen to main and event logs, starting from 'now' to avoid loops
                val process = Runtime.getRuntime().exec("logcat -b main -b events -T 1")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (!Thread.currentThread().isInterrupted) {
                    val line = reader.readLine() ?: break
                    
                    // Update the live feed for the UI
                    currentSignal = line
                    
                    val prefs = getSharedPreferences("GODMODE_PREFS", MODE_PRIVATE)
                    val savedTrigger = prefs.getString("user_trigger", null)
                    val savedAction = prefs.getString("user_action", null)

                    // If NOT recording and the log line contains our saved trigger...
                    if (!isRecording && savedTrigger != null && line.contains(savedTrigger)) {
                        val now = System.currentTimeMillis()
                        if (now - lastActionTime > 2000) { // 2s Cooldown
                            lastActionTime = now
                            executeUserAction(savedAction)
                        }
                    }
                }
            } catch (e: Exception) {
                currentSignal = "LOG_ERROR: Grant READ_LOGS permission"
            }
        }
        logcatThread?.start()
    }

    private fun executeUserAction(packageName: String?) {
        if (packageName.isNullOrEmpty()) return
        
        // Step 1: Close any system overlay (like Assistant)
        performGlobalAction(GLOBAL_ACTION_BACK)
        
        // Step 2: Launch the user's chosen app
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            currentSignal = "ERROR: Could not launch $packageName"
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val signal = "HARDWARE_KEY_${event.keyCode}_SCAN_${event.scanCode}"
            currentSignal = signal
            
            // Check for saved hardware trigger
            val prefs = getSharedPreferences("GODMODE_PREFS", MODE_PRIVATE)
            if (!isRecording && signal == prefs.getString("user_trigger", null)) {
                executeUserAction(prefs.getString("user_action", null))
            }
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: ""
            // Allow recording of window transitions (like the Motorola UX Core)
            currentSignal = "WINDOW_CHANGE_$pkg"
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        logcatThread?.interrupt()
    }

    companion object {
        var isRecording = false
        var currentSignal = "System Ready..."
        var lastKeyCode = 0   // Put back for compatibility
        var lastScanCode = 0  // Put back for compatibility
    }
}
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

    private val motoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            lastEvent = "MOTO_SIGNAL: ${intent?.action}"
        }
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                     AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        this.serviceInfo = info

        // Register for EVERY known Motorola hardware broadcast
        val filter = IntentFilter().apply {
            addAction("com.motorola.intent.action.STK_KEY_PRESS")
            addAction("com.motorola.actions.key_press")
            addAction("com.motorola.smartkey.KEY_PRESS")
            addAction("android.intent.action.ASSIST")
            addAction("android.intent.action.VOICE_COMMAND")
        }
        registerReceiver(motoReceiver, filter)
        
        startLogcatHunter() // Start the high-sensitivity log scanner
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        lastKeyCode = event.keyCode
        lastScanCode = event.scanCode
        lastEvent = "HARDWARE_KEY: ${event.keyCode} | SCAN: ${event.scanCode}"
        
        // If it's an Assist-type key, we try to block the system from seeing it
        if (event.keyCode == KeyEvent.KEYCODE_ASSIST || event.keyCode == 219 || event.keyCode == 231) {
            lastEvent = "SUCCESS: Assist Key Captured!"
            return true // Hijack it
        }
        return false
    }

    private fun startLogcatHunter() {
        logcatThread = Thread {
            try {
                // This is how sds100 "Expert Mode" finds hidden buttons
                val process = Runtime.getRuntime().exec("logcat -b events -v brief")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (!Thread.currentThread().isInterrupted) {
                    val line = reader.readLine() ?: break
                    if (line.contains("assist", ignoreCase = true) || line.contains("key", ignoreCase = true)) {
                        lastEvent = "LOGCAT_DETECTION: ${line.takeLast(30)}"
                    }
                }
            } catch (e: Exception) {}
        }
        logcatThread?.start()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: ""
            if (pkg.contains("assistant") || pkg.contains("motorola")) {
                lastEvent = "WINDOW_HIJACK: $pkg"
            }
        }
    }

    override fun onInterrupt() {}
    override fun onDestroy() { 
        super.onDestroy()
        logcatThread?.interrupt()
        try { unregisterReceiver(motoReceiver) } catch (e: Exception) {}
    }

    companion object {
        var lastKeyCode: Int = 0
        var lastScanCode: Int = 0
        var lastEvent: String = "Engine Online: Press any button"
    }
}
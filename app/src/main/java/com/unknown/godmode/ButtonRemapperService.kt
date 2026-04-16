package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.content.*
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import java.io.BufferedReader
import java.io.InputStreamReader

class ButtonRemapperService : AccessibilityService() {

    override fun onServiceConnected() {
        val info = serviceInfo
        info.flags = info.flags or 32 // flagRequestFilterKeyEvents
        this.serviceInfo = info
        startUniversalListener()
    }

    private fun startUniversalListener() {
        Thread {
            try {
                // Stabilized logcat: only looks for events and main input logs
                val process = Runtime.getRuntime().exec("logcat -b events -b main -T 1")
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                while (!Thread.currentThread().isInterrupted) {
                    val line = reader.readLine() ?: break
                    
                    // Always update the live feed so user can see what's happening
                    currentSignal = line
                    
                    val prefs = getSharedPreferences("REMAP_SETTINGS", MODE_PRIVATE)
                    val savedTrigger = prefs.getString("trigger", null)
                    val savedAction = prefs.getString("action", "com.android.chrome")

                    // If we aren't recording and the line matches our saved trigger...
                    if (!isRecording && savedTrigger != null && line.contains(savedTrigger)) {
                        executeAction(savedAction!!)
                    }
                }
            } catch (e: Exception) { currentSignal = "Logcat Error: Run ADB Command" }
        }.start()
    }

    private fun executeAction(pkg: String) {
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
            performGlobalAction(GLOBAL_ACTION_BACK) // Close default assistant
        } catch (e: Exception) { currentSignal = "FAILED TO LAUNCH $pkg" }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            currentSignal = "KEYCODE_${event.keyCode}"
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}

    companion object {
        var isRecording = false
        var currentSignal = "Waiting..."
        var lastEvent = ""
    }
}
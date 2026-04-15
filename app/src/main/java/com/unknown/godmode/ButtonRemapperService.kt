package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class ButtonRemapperService : AccessibilityService() {

    override fun onServiceConnected() {
        Log.d("GodMode", "Service Connected and Listening")
    }

    // 1. Trigger: Physical Buttons
    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"
        updateUI("KEY: $keyCode ($action)")
        
        // Dynamic Rule Check (Example: Button 135 -> Gemini)
        if (keyCode == 135 && event.action == KeyEvent.ACTION_DOWN) {
            launchApp("com.google.android.googlequicksearchbox")
            return true
        }
        return false 
    }

    // 2. Trigger: App Opens / UI Changes
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: ""
        val type = AccessibilityEvent.eventTypeToString(event.eventType)
        
        // Update the UI with the latest "Event Stream"
        updateUI("APP: $pkg\nEVENT: $type")

        // Rule: YouTube Redirect
        if (pkg == "com.google.android.youtube" && event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            launchApp("com.brave.browser")
        }
    }

    private fun launchApp(packageName: String) {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        intent?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
        }
    }

    private fun updateUI(message: String) {
        lastEvent = message
    }

    override fun onInterrupt() {}

    companion object {
        var lastEvent: String = "Waiting for events..."
    }
}
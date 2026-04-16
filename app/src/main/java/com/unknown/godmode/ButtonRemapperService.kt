package com.unknown.godmode
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ButtonRemapperService : AccessibilityService() {

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        // We are using every available flag to force the system to share keys
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                     AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        
        this.serviceInfo = info
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Log the KeyCode, ScanCode, and the hardware Source (e.g. 257 for keyboard)
        val keyCode = event.keyCode
        val scanCode = event.scanCode
        val source = event.source
        
        // We log both DOWN and UP actions. Sometimes 'Assist' only fires on UP.
        val actionName = if (event.action == KeyEvent.ACTION_DOWN) "DOWN" else "UP"
        
        lastKeyCode = keyCode
        lastScanCode = scanCode
        lastEvent = "[$actionName] ID: $keyCode | SCAN: $scanCode | SRC: $source"

        // Keep this false. If we set it to true, we 'kill' the button.
        return false 
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // If the button is 'swallowed' by the system, it will still trigger a window change.
        // We will catch the 'ClassName' of what the button is trying to open.
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: "None"
            val cls = event.className?.toString() ?: "None"
            // If the key is hidden, this will show us what 'App' or 'Overlay' just woke up.
            lastEvent = "WIN_CHANGE | PKG: $pkg | CLS: $cls"
        }
    }

    override fun onInterrupt() {}

    companion object {
        var lastKeyCode: Int = 0
        var lastScanCode: Int = 0
        var lastEvent: String = "Monitoring Hardware..."
    }
}
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
        
        // FLAG_REQUEST_FILTER_KEY_EVENTS is what allows us to "Record" hardware
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        
        this.serviceInfo = info
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // This is the "Expert Mode" discovery logic
        val keyCode = event.keyCode
        val scanCode = event.scanCode // The raw hardware ID
        
        if (event.action == KeyEvent.ACTION_DOWN) {
            lastKeyCode = keyCode
            lastScanCode = scanCode
            lastEvent = "HARDWARE DETECTED | Key: $keyCode | Scan: $scanCode"
        }

        // Return false so we don't block the button while we are just "Recording"
        return false 
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Watch for package changes to identify which "System App" the button opens
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            lastPackage = event.packageName?.toString() ?: "Unknown"
        }
    }

    override fun onInterrupt() {}

    companion object {
        var lastKeyCode: Int = 0
        var lastScanCode: Int = 0
        var lastPackage: String = "None"
        var lastEvent: String = "Press any hardware button..."
    }
}
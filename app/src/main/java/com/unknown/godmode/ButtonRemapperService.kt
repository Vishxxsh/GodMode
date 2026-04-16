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
        
        // This flag is the 'Expert Mode' secret—it tells the phone to let us 
        // see the keys BEFORE the system consumes them.
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        
        this.serviceInfo = info
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val scanCode = event.scanCode

        // Log the press immediately
        if (event.action == KeyEvent.ACTION_DOWN) {
            lastKeyCode = keyCode
            lastScanCode = scanCode
            
            // Identifying the "Assist" button by its system ID
            val keyName = when (keyCode) {
                KeyEvent.KEYCODE_ASSIST -> "ASSIST_KEY (219)"
                KeyEvent.KEYCODE_VOICE_ASSIST -> "VOICE_ASSIST (231)"
                0 -> "RAW_HARDWARE"
                else -> "KEY_$keyCode"
            }
            
            lastEvent = "DETECTED: $keyName | SCAN: $scanCode"
        }

        // IMPORTANT: We return 'false' so the button still works for now.
        // Once we want to remap it, we change this to 'true' to 'kill' the original action.
        return false 
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}

    companion object {
        var lastKeyCode: Int = 0
        var lastScanCode: Int = 0
        var lastEvent: String = "Press the Assist button..."
    }
}
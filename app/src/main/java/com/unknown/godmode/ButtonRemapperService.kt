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
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        this.serviceInfo = info
    }
    override fun onKeyEvent(event: KeyEvent): Boolean {
        lastKeyCode = event.keyCode
        return false 
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        lastEvent = event.packageName?.toString() ?: "Unknown"
    }
    override fun onInterrupt() {}
    companion object {
        var lastKeyCode: Int = 0
        var lastEvent: String = "Monitoring..."
    }
}

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
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        this.serviceInfo = info
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val scanCode = event.scanCode

        if (event.action == KeyEvent.ACTION_DOWN) {
            lastKeyCode = keyCode
            lastScanCode = scanCode
            
            if (keyCode == KeyEvent.KEYCODE_ASSIST || keyCode == 219 || keyCode == 231) {
                lastEvent = "SERVICE_CATCH: ASSIST BUTTON ($keyCode)"
            } else {
                lastEvent = "KEY: $keyCode | SCAN: $scanCode"
            }
        }
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
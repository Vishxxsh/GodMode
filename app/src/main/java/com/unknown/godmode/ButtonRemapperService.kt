package com.unknown.godmode
import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ButtonRemapperService : AccessibilityService() {

    private val motoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // This catches the 'secret' signals Motorola sends for its Smart Keys
            lastEvent = "BROADCAST DETECTED: ${intent?.action}"
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

        // Register for common Motorola secret button signals
        val filter = IntentFilter()
        filter.addAction("com.motorola.intent.action.KEY_PRESS")
        filter.addAction("com.motorola.actions.key_press")
        filter.addAction("com.motorola.smartkey.KEY_PRESS")
        registerReceiver(motoReceiver, filter)
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        // Log EVERYTHING, even if the KeyCode is 0 or "Unknown"
        lastKeyCode = event.keyCode
        lastScanCode = event.scanCode
        lastEvent = "KEY: ${event.keyCode} | SCAN: ${event.scanCode} | DEV: ${event.device?.name}"
        return false 
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Log every window change. If the button triggers a hidden app, we'll see it here.
        val pkg = event.packageName?.toString() ?: "None"
        val type = AccessibilityEvent.eventTypeToString(event.eventType)
        lastEvent = "EVENT: $type | PKG: $pkg"
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(motoReceiver) } catch (e: Exception) {}
    }

    companion object {
        var lastKeyCode: Int = 0
        var lastScanCode: Int = 0
        var lastEvent: String = "Listening for everything..."
    }
}
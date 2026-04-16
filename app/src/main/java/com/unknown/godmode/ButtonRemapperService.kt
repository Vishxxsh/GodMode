package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.*
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ButtonRemapperService : AccessibilityService() {

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            log("BROADCAST: ${intent?.action}")
        }
    }

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo()
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        // Adding every flag sds100 uses for expert mode
        info.flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                     AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                     AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                     AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        this.serviceInfo = info

        val filter = IntentFilter().apply {
            addAction("com.motorola.intent.action.STK_KEY_PRESS")
            addAction("com.motorola.actions.key_press")
            addAction("android.intent.action.ASSIST")
            addAction("android.intent.action.VOICE_COMMAND")
        }
        registerReceiver(receiver, filter)
        log("Engine: Online & Listening")
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val code = event.keyCode
        val scan = event.scanCode
        lastKeyCode = code
        lastScanCode = scan
        
        log("KEY: $code | SCAN: $scan | ACTION: ${if(event.action == 0) "DOWN" else "UP"}")

        // Catch the 'Assist' key (219/231) or any ScanCode that isn't 0
        if (code == 219 || code == 231 || (code == 0 && scan > 0)) {
            log("🎯 TRIGGER CAPTURED: $code / $scan")
            return true // Hijack it
        }
        return false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val pkg = event.packageName?.toString() ?: ""
        val type = AccessibilityEvent.eventTypeToString(event.eventType)
        if (pkg.contains("motorola") || pkg.contains("assistant")) {
            log("WINDOW: $pkg | TYPE: $type")
        }
    }

    override fun onInterrupt() {}

    private fun log(msg: String) {
        lastEvent = msg + "\n" + lastEvent.take(500)
    }

    companion object {
        var lastKeyCode = 0
        var lastScanCode = 0
        var lastEvent = "Starting..."
    }
}
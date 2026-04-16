package com.unknown.godmode

import android.accessibilityservice.*
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

        // 1. Force the banner to show immediately
        showOverlay("MONITORING: Searching...")

        val root = rootInActiveWindow ?: return

        // 2. CHECK: Are we at the code popup?
        val code = find6DigitCode(root)
        if (code != null) {
            showOverlay("CODE DETECTED: $code")
            File("/data/local/tmp/p_code.txt").writeText(code)
            isIgniting = false
            return
        }

        // 3. CHECK: Are we in the sub-menu?
        val pairNode = findNode(root, "Pair device with pairing code")
        if (pairNode != null) {
            showOverlay("STAGE: Inside Sub-Menu. Clicking Pair...")
            clickNode(pairNode)
            return
        }

        // 4. CHECK: Is Wireless Debugging visible in the main list?
        val wirelessNode = findNode(root, "Wireless debugging")
        if (wirelessNode != null) {
            if (wirelessNode.isVisibleToUser) {
                showOverlay("STAGE: Target Visible. Clicking...")
                clickNode(wirelessNode)
            } else {
                showOverlay("STAGE: Target Hidden. Swiping...")
                powerFlick()
            }
        } else {
            // Only flick if we are in the settings app
            if (root.packageName?.contains("settings") == true) {
                showOverlay("STAGE: Searching list... (Swiping)")
                powerFlick()
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
        // In Moto Settings, the text itself isn't clickable, the ROW is. 
        // We climb the tree until we find the clickable row.
        while (target != null && !target.isClickable) {
            target = target.parent
        }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun powerFlick() {
        val swipePath = Path().apply {
            moveTo(540f, 1500f) 
            lineTo(540f, 500f)  
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(swipePath, 0, 300))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xDD000000.toInt())
                setTextColor(0xFF00FF00.toInt())
                setPadding(40, 20, 40, 20)
                textSize = 15f
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

    override fun onInterrupt() {}
    companion object { var isIgniting = false; var currentSignal = "Ready..." }
}
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

        val root = rootInActiveWindow ?: return
        val currentPkg = root.packageName?.toString() ?: "Unknown"
        
        // --- RE-SCAN THE BRAIN ---
        val wirelessNode = findNode(root, "Wireless debugging")
        val pairButtonNode = findNode(root, "Pair device with pairing code")
        val codePopupNode = findNode(root, "Pairing code") // Looking for the popup title

        when {
            // STAGE 4: THE POPUP IS OPEN
            codePopupNode != null || find6DigitCode(root) != null -> {
                val code = find6DigitCode(root)
                if (code != null) {
                    showOverlay("CODE CAPTURED: $code")
                    File("/data/local/tmp/p_code.txt").writeText(code)
                    isIgniting = false // Mission Accomplished
                } else {
                    showOverlay("STAGE: Popup detected, waiting for code...")
                }
            }

            // STAGE 3: INSIDE THE SUB-MENU
            pairButtonNode != null -> {
                showOverlay("STAGE: Inside Wireless Debugging. Clicking Pair...")
                clickNode(pairButtonNode)
            }

            // STAGE 2: IN DEVELOPER OPTIONS
            wirelessNode != null -> {
                if (wirelessNode.isVisibleToUser) {
                    showOverlay("STAGE: Target Found. Clicking...")
                    clickNode(wirelessNode)
                } else {
                    showOverlay("STAGE: Target hidden below. Flicking...")
                    powerFlick()
                }
            }

            // STAGE 1: SEARCHING
            else -> {
                showOverlay("STAGE: Searching... (Last PKG: $currentPkg)")
                // If we are in Settings, we must scroll to find the target
                if (currentPkg.contains("settings")) {
                    powerFlick()
                }
            }
        }
    }

    private fun find6DigitCode(node: AccessibilityNodeInfo): String? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val text = child.text?.toString() ?: ""
            if (text.matches(Regex("\\d{6}"))) return text
            val found = find6DigitCode(child)
            if (found != null) return found
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
        val swipePath = Path().apply {
            moveTo(500f, 1500f) // Start lower
            lineTo(500f, 300f)  // Swipe much higher
        }
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 200)) // Faster swipe
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xEE000000.toInt())
                setTextColor(0xFF00FF00.toInt()) // Neon Green
                setPadding(40, 30, 40, 30)
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
        statusView?.text = "GODMODE MONITOR: $text"
    }

    private fun removeOverlay() {
        statusView?.let { windowManager?.removeView(it) }
        statusView = null
    }

    override fun onInterrupt() {}
    companion object {
        var isIgniting = false
        var currentSignal = "Ready..."
    }
}
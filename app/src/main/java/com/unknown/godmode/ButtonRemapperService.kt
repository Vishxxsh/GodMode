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

        // --- STEP 1: NAVIGATION LOGIC ---
        val wirelessNode = findNode(root, "Wireless debugging")
        val pairButtonNode = findNode(root, "Pair device with pairing code")
        val possibleCode = findPairingCode(root)

        when {
            // Priority 1: We see a 6-digit code (The Popup is open)
            possibleCode != null -> {
                showOverlay("STAGE: POPUP OPEN! Code Caught: $possibleCode")
                File("/data/local/tmp/p_code.txt").writeText(possibleCode)
                isIgniting = false 
            }
            
            // Priority 2: We are in the sub-menu, but haven't clicked 'Pair' yet
            pairButtonNode != null -> {
                showOverlay("STAGE: Sub-Menu Found. Clicking 'Pair'...")
                clickNode(pairButtonNode)
            }
            
            // Priority 3: We are in Developer Options, looking for 'Wireless debugging'
            wirelessNode != null -> {
                if (wirelessNode.isVisibleToUser) {
                    showOverlay("STAGE: Target Found. Entering Menu...")
                    clickNode(wirelessNode)
                } else {
                    showOverlay("STAGE: Target Hidden. Flicking... (PKG: $currentPkg)")
                    powerFlick()
                }
            }
            
            // Priority 4: Searching...
            else -> {
                showOverlay("STAGE: Searching Settings... (PKG: $currentPkg)")
                if (currentPkg.contains("settings")) {
                    powerFlick()
                }
            }
        }
    }

    private fun findPairingCode(node: AccessibilityNodeInfo): String? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val txt = child.text?.toString() ?: ""
            if (txt.matches(Regex("\\d{6}"))) return txt
            val result = findPairingCode(child)
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
        val swipePath = Path().apply {
            moveTo(500f, 1600f)
            lineTo(500f, 400f)
        }
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 350))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xEE000000.toInt())
                setTextColor(0xFF00FF00.toInt())
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
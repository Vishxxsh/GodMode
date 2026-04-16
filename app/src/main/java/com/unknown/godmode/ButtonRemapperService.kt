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
    private var lastPackage: String = "None"

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (!isIgniting) {
            removeOverlay()
            return
        }

        // Keep track of where we are for debugging
        if (event.packageName != null) {
            lastPackage = event.packageName.toString()
        }

        val root = rootInActiveWindow
        if (root == null) {
            showOverlay("ERROR: Cannot see screen. Last PKG: $lastPackage")
            return
        }

        // --- STEP 1: FIND TARGETS ---
        val wirelessNode = findNode(root, "Wireless debugging")
        val pairNode = findNode(root, "Pair device with pairing code")
        val pairingPopupActive = findNode(root, "Pairing code") != null

        // --- STEP 2: LOGIC & FEEDBACK ---
        when {
            pairingPopupActive -> {
                showOverlay("STAGE: POPUP DETECTED. Scraping...")
                scrapeData(root)
            }
            pairNode != null -> {
                showOverlay("STAGE: INSIDE MENU. Clicking Pair...")
                clickNode(pairNode)
            }
            wirelessNode != null -> {
                if (wirelessNode.isVisibleToUser) {
                    showOverlay("STAGE: TARGET SEEN. Clicking...")
                    clickNode(wirelessNode)
                } else {
                    showOverlay("STAGE: TARGET OFF-SCREEN. Flicking...")
                    powerFlick()
                }
            }
            else -> {
                showOverlay("STAGE: SEARCHING... PKG: $lastPackage")
                if (lastPackage.contains("settings")) powerFlick()
            }
        }
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
            moveTo(500f, 1600f) // Start lower
            lineTo(500f, 200f)  // Swipe higher for bigger scroll
        }
        val gestureBuilder = GestureDescription.Builder()
        gestureBuilder.addStroke(GestureDescription.StrokeDescription(swipePath, 0, 400))
        dispatchGesture(gestureBuilder.build(), null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xDD000000.toInt())
                setTextColor(0xFF00FF00.toInt()) // High-vis Green
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
            
            // Log everything found to the banner so we know what it's seeing
            if (txt.length >= 4) {
                // If it looks like a code, save it
                if (txt.matches(Regex("\\d{6}"))) {
                    File("/data/local/tmp/p_code.txt").writeText(txt)
                    showOverlay("SUCCESS! CAUGHT CODE: $txt")
                    isIgniting = false // Stop only when code is found
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
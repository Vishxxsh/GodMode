package com.unknown.godmode

import android.accessibilityservice.*
import android.graphics.Path
import android.graphics.PixelFormat
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

    private val pulseRunnable = object : Runnable {
        override fun run() {
            if (isIgniting) {
                performSearchAndAction()
                handler.postDelayed(this, 300) // Scan every 300ms
            } else {
                removeOverlay()
            }
        }
    }

    override fun onServiceConnected() {
        handler.post(pulseRunnable)
    }

    private fun performSearchAndAction() {
        val root = rootInActiveWindow ?: return
        
        val pairNode = findNode(root, "Pair device with pairing code")
        val code = find6DigitCode(root)
        val wirelessNode = findNode(root, "Wireless debugging")

        when {
            code != null -> {
                showOverlay("SUCCESS! CODE: $code")
                File("/data/local/tmp/p_code.txt").writeText(code)
                isIgniting = false 
            }
            pairNode != null -> {
                showOverlay("STAGE: Inside Sub-Menu. Clicking Pair...")
                clickNode(pairNode)
            }
            wirelessNode != null -> {
                if (wirelessNode.isVisibleToUser) {
                    showOverlay("STAGE: Target Visible. Clicking...")
                    clickNode(wirelessNode)
                } else {
                    showOverlay("STAGE: Target Below. Swiping...")
                    powerFlick()
                }
            }
            else -> {
                showOverlay("STAGE: Searching settings list...")
                powerFlick()
            }
        }
    }

    private fun find6DigitCode(node: AccessibilityNodeInfo): String? {
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val txt = child.text?.toString() ?: ""
            if (txt.matches(Regex("\\d{6}"))) return txt
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
            moveTo(540f, 1500f) // Center-bottom
            lineTo(540f, 400f)  // Center-top
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(swipePath, 0, 250))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xEE000000.toInt())
                setTextColor(0xFF00FF00.toInt())
                setPadding(40, 20, 40, 20)
                textSize = 15f
                gravity = Gravity.CENTER
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
            ).apply { gravity = Gravity.TOP }
            windowManager?.addView(statusView, params)
        }
        statusView?.text = "DEBUG: $text"
    }

    private fun removeOverlay() {
        statusView?.let { windowManager?.removeView(it) }
        statusView = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {}
    override fun onInterrupt() {}
    companion object { var isIgniting = false; var currentSignal = "Ready..." }
}
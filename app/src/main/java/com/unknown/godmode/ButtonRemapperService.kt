package com.unknown.godmode

import android.accessibilityservice.*
import android.content.*
import android.graphics.*
import android.os.*
import android.view.*
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import java.io.File

class ButtonRemapperService : AccessibilityService() {
    private var statusView: TextView? = null
    private var windowManager: WindowManager? = null
    private val handler = Handler(Looper.getMainLooper())

    private val radar = object : Runnable {
        override fun run() {
            if (isIgniting) {
                showOverlay("RADAR ACTIVE: Scanning...")
                runAutomation()
                handler.postDelayed(this, 350)
            } else {
                removeOverlay()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "START_IGNITION") {
            isIgniting = true
            handler.post(radar)
        }
        return START_STICKY
    }

    private fun runAutomation() {
        val root = rootInActiveWindow ?: return
        
        // 1. Check for Code (6 Digits)
        val code = find6DigitCode(root)
        if (code != null) {
            showOverlay("✅ SUCCESS! CODE: $code")
            File("/data/local/tmp/p_code.txt").writeText(code)
            isIgniting = false
            // Auto-return to app
            val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            return
        }

        // 2. Check for Pair Button
        val pairNode = findNode(root, "Pair device with pairing code")
        if (pairNode != null && pairNode.isVisibleToUser) {
            showOverlay("👇 Clicking Pair Button...")
            clickNode(pairNode)
            return
        }

        // 3. Check for Wireless Debugging
        val wirelessNode = findNode(root, "Wireless debugging")
        if (wirelessNode != null) {
            if (wirelessNode.isVisibleToUser) {
                showOverlay("👆 Opening Wireless Debugging...")
                clickNode(wirelessNode)
            } else {
                showOverlay("📜 Swiping down to target...")
                powerSwipe()
            }
        } else if (root.packageName?.contains("settings") == true) {
            showOverlay("🔎 Searching for Wireless Debugging...")
            powerSwipe()
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
        var target: AccessibilityNodeInfo? = node
        while (target != null && !target.isClickable) { target = target.parent }
        target?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun powerSwipe() {
        val path = Path().apply {
            moveTo(540f, 1500f)
            lineTo(540f, 300f)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 250))
            .build()
        dispatchGesture(gesture, null, null)
    }

    private fun showOverlay(text: String) {
        if (statusView == null) {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            statusView = TextView(this).apply {
                setBackgroundColor(0xCC000000.toInt())
                setTextColor(Color.GREEN)
                setPadding(50, 20, 50, 20)
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
            try { windowManager?.addView(statusView, params) } catch (e: Exception) {}
        }
        statusView?.text = text
    }

    private fun removeOverlay() {
        try { statusView?.let { windowManager?.removeView(it) }; statusView = null } catch (e: Exception) {}
    }

    override fun onAccessibilityEvent(event: android.view.accessibility.AccessibilityEvent?) {}
    override fun onInterrupt() {}
    companion object { var isIgniting = false; var currentSignal = "Ready..." }
}
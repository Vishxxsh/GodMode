package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.content.Context
import android.os.Vibrator
import android.os.VibrationEffect

class ButtonRemapperService : AccessibilityService() {

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            val keyCode = event.keyCode
            lastKeyCode = keyCode // Send to UI for debugging
            
            // 135 is the common Moto Assistant key, 131 is another common one.
            if (keyCode == 135 || keyCode == KeyEvent.KEYCODE_ASSIST || keyCode == 131) {
                vibrate()
                launchGemini()
                return true // Stop the system from opening the default assistant
            }
        }
        return super.onKeyEvent(event)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // FEATURE: YouTube to Brave Redirect
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString()
            if (pkg == "com.google.android.youtube") {
                launchBrave()
            }
        }
    }

    private fun launchGemini() {
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            setPackage("com.google.android.googlequicksearchbox")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { startActivity(intent) } catch (e: Exception) {}
    }

    private fun launchBrave() {
        val intent = packageManager.getLaunchIntentForPackage("com.brave.browser")
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onInterrupt() {}

    companion object {
        var lastKeyCode: Int = 0
    }
}

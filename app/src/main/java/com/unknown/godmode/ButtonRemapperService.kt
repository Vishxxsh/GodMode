package com.unknown.godmode

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ButtonRemapperService : AccessibilityService() {
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            // Moto's dedicated AI button (Keycode 135)
            if (event.keyCode == 135 || event.keyCode == KeyEvent.KEYCODE_ASSIST) {
                launchGemini()
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    private fun launchGemini() {
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            setPackage("com.google.android.googlequicksearchbox")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try { startActivity(intent) } catch (e: Exception) {}
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}

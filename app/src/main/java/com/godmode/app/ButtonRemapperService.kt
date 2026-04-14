package com.godmode.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

class ButtonRemapperService : AccessibilityService() {

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val action = event.action

        // Check for the AI/Assistant button (Commonly KeyCode 135 or 119 on Moto)
        // We only trigger on 'Action Down' (the moment you press it)
        if (action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_ASSIST || keyCode == 135) {
                launchGemini()
                return true // This stops the original action from happening
            }
        }
        return super.onKeyEvent(event)
    }

    private fun launchGemini() {
        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
            setPackage("com.google.android.googlequicksearchbox") // Google App (Gemini)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Fallback if the direct intent fails
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}
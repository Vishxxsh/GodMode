package com.unknown.godmode

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent

class UnknownKeyboard : InputMethodService() {
    // This is the VIP lane for hardware keys
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            ButtonRemapperService.lastKeyCode = keyCode
            ButtonRemapperService.lastScanCode = event.scanCode
            ButtonRemapperService.lastEvent = "KEYBOARD_CATCH: $keyCode (Assist?)"
        }
        // Return false so the button still works for now
        return false
    }
}
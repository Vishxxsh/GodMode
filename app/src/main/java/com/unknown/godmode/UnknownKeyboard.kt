package com.unknown.godmode

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent

class UnknownKeyboard : InputMethodService() {
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            // Updated reference to the Service companion object
            ButtonRemapperService.currentSignal = "KEYBOARD_CODE_${keyCode}_SCAN_${event.scanCode}"
        }
        return false
    }
}
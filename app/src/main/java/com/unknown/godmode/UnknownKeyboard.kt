package com.unknown.godmode

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent

class UnknownKeyboard : InputMethodService() {
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null) {
            // Feed the keyboard press into the Universal Engine
            ButtonRemapperService.currentSignal = "KEYBOARD_CODE_${keyCode}_SCAN_${event.scanCode}"
        }
        return false
    }
}
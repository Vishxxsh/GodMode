override fun onKeyEvent(event: KeyEvent): Boolean {
    val keyCode = event.keyCode
    val scanCode = event.scanCode

    if (event.action == KeyEvent.ACTION_DOWN) {
        lastKeyCode = keyCode
        lastScanCode = scanCode
        
        if (keyCode == KeyEvent.KEYCODE_ASSIST || keyCode == 219 || keyCode == 231) {
            lastEvent = "SERVICE_CATCH: ASSIST BUTTON ($keyCode)"
            // return true // Un-comment this later to block the default action
        } else {
            lastEvent = "KEY: $keyCode | SCAN: $scanCode"
        }
    }
    return false
}
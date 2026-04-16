package com.unknown.godmode
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startUIUpdater()
    }

    // This is what your friend was talking about! 
    // It catches the key while you are looking at the app.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        val scanCode = event.scanCode
        
        if (event.action == KeyEvent.ACTION_DOWN) {
            // Update the shared variables so the UI shows it immediately
            ButtonRemapperService.lastKeyCode = keyCode
            ButtonRemapperService.lastScanCode = scanCode
            ButtonRemapperService.lastEvent = "ACTIVITY_CATCH: $keyCode | SCAN: $scanCode"
            
            // If it's the Assist key, we might want to "consume" it (return true)
            // so the Google Assistant doesn't pop up and annoy you.
            if (keyCode == KeyEvent.KEYCODE_ASSIST || keyCode == 219 || keyCode == 231) {
                return true 
            }
        }
        return super.dispatchKeyEvent(event)
    }

    private fun startUIUpdater() {
        val tvKeyCode = findViewById<TextView>(R.id.tvKeyCode)
        val tvAppEvent = findViewById<TextView>(R.id.tvAppEvent)
        val mainHandler = Handler(Looper.getMainLooper())
        
        mainHandler.post(object : Runnable {
            override fun run() {
                tvKeyCode.text = "CODE: ${ButtonRemapperService.lastKeyCode}\nSCAN: ${ButtonRemapperService.lastScanCode}"
                tvAppEvent.text = ButtonRemapperService.lastEvent
                mainHandler.postDelayed(this, 100)
            }
        })
    }
}
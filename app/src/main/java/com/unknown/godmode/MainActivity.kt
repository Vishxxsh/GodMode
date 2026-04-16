package com.unknown.godmode

import android.content.Intent
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
        handleIntent(intent)
        startUIUpdater()
    }

    // This catches the 'Assist' trigger when the app is already open
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_ASSIST) {
            ButtonRemapperService.lastEvent = "INTENT_CATCH: Assist Button Pressed!"
            // Here is where you can trigger your custom action!
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            ButtonRemapperService.lastKeyCode = event.keyCode
            ButtonRemapperService.lastScanCode = event.scanCode
            ButtonRemapperService.lastEvent = "KEY_CATCH: ${event.keyCode}"
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
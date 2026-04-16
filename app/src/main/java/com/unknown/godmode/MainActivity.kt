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

    // Direct foreground override
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        ButtonRemapperService.lastKeyCode = keyCode
        ButtonRemapperService.lastScanCode = event?.scanCode ?: 0
        ButtonRemapperService.lastEvent = "ACTIVITY_DOWN: $keyCode"
        return super.onKeyDown(keyCode, event)
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
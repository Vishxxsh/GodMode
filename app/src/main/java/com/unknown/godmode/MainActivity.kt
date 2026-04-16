package com.unknown.godmode
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvKeyCode = findViewById<TextView>(R.id.tvKeyCode)
        val tvAppEvent = findViewById<TextView>(R.id.tvAppEvent)

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                // Show the Raw Hardware ScanCode (The Expert Mode secret)
                tvKeyCode.text = "SCAN: ${ButtonRemapperService.lastScanCode}"
                tvAppEvent.text = ButtonRemapperService.lastEvent
                mainHandler.postDelayed(this, 100)
            }
        })
    }
}
package com.unknown.godmode
import android.os.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val tvKeyCode = findViewById<TextView>(R.id.tvKeyCode)
        val tvAppEvent = findViewById<TextView>(R.id.tvAppEvent)
        val handler = Handler(Looper.getMainLooper())

        handler.post(object : Runnable {
            override fun run() {
                tvKeyCode.text = "CODE: ${ButtonRemapperService.lastKeyCode}\nSCAN: ${ButtonRemapperService.lastScanCode}"
                tvAppEvent.text = ButtonRemapperService.lastEvent
                handler.postDelayed(this, 100)
            }
        })
    }
}

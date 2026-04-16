package com.unknown.godmode
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val tvKeyCode = findViewById<TextView>(R.id.tvKeyCode)
        val tvAppEvent = findViewById<TextView>(R.id.tvAppEvent)
        val btnEnable = findViewById<Button>(R.id.btnEnable)
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                tvKeyCode.text = ButtonRemapperService.lastKeyCode.toString()
                tvAppEvent.text = ButtonRemapperService.lastEvent
                mainHandler.postDelayed(this, 100) 
            }
        })
        btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}

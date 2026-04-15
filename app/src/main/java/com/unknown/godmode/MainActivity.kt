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

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnEnable = findViewById<Button>(R.id.btnEnable)

        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                val lastKey = ButtonRemapperService.lastKeyCode
                tvStatus.text = "GOD MODE ACTIVE\n\nLast Key Pressed: $lastKey\n(If you press your side button and this number changes, we can remap it!)"
                mainHandler.postDelayed(this, 500)
            }
        })

        btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }
}

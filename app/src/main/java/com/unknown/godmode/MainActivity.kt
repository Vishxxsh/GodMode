package com.unknown.godmode

import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnRecord = findViewById<Button>(R.id.btnRecord)
        val tvSaved = findViewById<TextView>(R.id.tvSavedTrigger)
        val tvLive = findViewById<TextView>(R.id.tvLiveLog)
        val etAction = findViewById<EditText>(R.id.etAction)
        val prefs = getSharedPreferences("REMAP_SETTINGS", MODE_PRIVATE)

        btnRecord.setOnClickListener {
            if (!ButtonRemapperService.isRecording) {
                ButtonRemapperService.isRecording = true
                btnRecord.text = "PRESS YOUR BUTTON NOW..."
            } else {
                // Save the current signal as the trigger
                val trigger = ButtonRemapperService.currentSignal
                prefs.edit().putString("trigger", trigger).apply()
                prefs.edit().putString("action", etAction.text.toString()).apply()
                
                ButtonRemapperService.isRecording = false
                btnRecord.text = "START RECORDING TRIGGER"
                tvSaved.text = "Saved Trigger: $trigger"
            }
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                tvLive.text = ButtonRemapperService.currentSignal
                handler.postDelayed(this, 100)
            }
        })
    }
}
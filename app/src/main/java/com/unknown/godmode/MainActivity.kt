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
        val prefs = getSharedPreferences("GODMODE_PREFS", MODE_PRIVATE)

        // Load existing settings
        tvSaved.text = "Saved Trigger: \${prefs.getString("user_trigger", "NONE")}"
        etAction.setText(prefs.getString("user_action", "com.brave.browser"))

        btnRecord.setOnClickListener {
            if (!ButtonRemapperService.isRecording) {
                ButtonRemapperService.isRecording = true
                btnRecord.text = "RECORDING... PRESS YOUR BUTTON"
            } else {
                val trigger = ButtonRemapperService.currentSignal
                prefs.edit().putString("user_trigger", trigger).apply()
                prefs.edit().putString("user_action", etAction.text.toString()).apply()
                
                ButtonRemapperService.isRecording = false
                btnRecord.text = "START RECORDING TRIGGER"
                tvSaved.text = "Saved Trigger: \$trigger"
                Toast.makeText(this, "Trigger and Action Saved!", Toast.LENGTH_SHORT).show()
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
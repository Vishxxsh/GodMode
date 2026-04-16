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

        // Load your previous choice
        val saved = prefs.getString("user_trigger", "NONE")
        tvSaved.text = "Active Trigger: " + saved
        etAction.setText(prefs.getString("user_action", "com.brave.browser"))

        btnRecord.setOnClickListener {
            if (!ButtonRemapperService.isRecording) {
                ButtonRemapperService.isRecording = true
                btnRecord.text = "RECORDING... PRESS ANY BUTTON"
            } else {
                val foundTrigger = ButtonRemapperService.currentSignal
                val chosenAction = etAction.text.toString()
                
                prefs.edit().putString("user_trigger", foundTrigger).apply()
                prefs.edit().putString("user_action", chosenAction).apply()
                
                ButtonRemapperService.isRecording = false
                btnRecord.text = "START RECORDING TRIGGER"
                tvSaved.text = "Active Trigger: " + foundTrigger
                Toast.makeText(this, "Success! Trigger Mapped.", Toast.LENGTH_SHORT).show()
            }
        }

        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                tvLive.text = "LIVE SIGNAL:\n" + ButtonRemapperService.currentSignal
                handler.postDelayed(this, 100)
            }
        })
    }
}
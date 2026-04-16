package com.unknown.godmode

import android.content.Intent
import android.os.*
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnIgnite = findViewById<Button>(R.id.btnIgnite)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        btnIgnite.setOnClickListener {
            ButtonRemapperService.isIgniting = true
            // Go to Developer Options
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            Toast.makeText(this, "Searching for Wireless Debugging...", Toast.LENGTH_SHORT).show()
            watchForSuccess()
        }
    }

    private fun watchForSuccess() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val code = File("/data/local/tmp/p_code.txt")
                if (code.exists()) {
                    ButtonRemapperService.isIgniting = false
                    // Auto-return to home
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    
                    Toast.makeText(this@MainActivity, "GODMODE ACTIVATED", Toast.LENGTH_LONG).show()
                    code.delete()
                } else {
                    handler.postDelayed(this, 500)
                }
            }
        })
    }
}
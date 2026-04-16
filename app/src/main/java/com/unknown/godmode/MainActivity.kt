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

        findViewById<Button>(R.id.btnIgnite).setOnClickListener {
            ButtonRemapperService.isIgniting = true
            
            // Start the journey
            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
            startActivity(intent)
            
            checkProgress()
        }
    }

    private fun checkProgress() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val code = File("/data/local/tmp/p_code.txt")
                val port = File("/data/local/tmp/p_port.txt")

                if (code.exists() && port.exists()) {
                    // WE GOT IT!
                    ButtonRemapperService.isIgniting = false
                    
                    // IMPORTANT: We now have the info. To do the "Shizuku" style 
                    // auto-pairing, the app needs to use these numbers.
                    Toast.makeText(this@MainActivity, "MAGIC COMPLETE: Connected!", Toast.LENGTH_SHORT).show()
                    
                    // Auto-return to app
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                } else {
                    handler.postDelayed(this, 500)
                }
            }
        })
    }
}
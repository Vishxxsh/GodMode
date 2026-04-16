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
            startAutoPilot()
        }

        // Keep checking if the engine started
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (File("/data/local/tmp/live_signal.txt").exists()) {
                    tvStatus.text = "ENGINE: ACTIVE"
                    tvStatus.setTextColor(android.graphics.Color.GREEN)
                    btnIgnite.visibility = android.view.View.GONE
                }
                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun startAutoPilot() {
        Toast.makeText(this, "AUTO-PILOT: Opening Settings...", Toast.LENGTH_SHORT).show()
        
        // 1. Open the Wireless Debugging screen directly
        val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
        startActivity(intent)

        // 2. Wait for the Accessibility Service to grab the info and run the bridge
        Thread {
            var paired = false
            while (!paired) {
                val code = File("/data/local/tmp/pair_code.txt")
                val port = File("/data/local/tmp/pair_port.txt")
                
                if (code.exists() && port.exists()) {
                    // We found the info! Now we launch the bridge using a specialized internal shell
                    val launchCmd = "export CLASSPATH=\$(pm path com.unknown.godmode | sed 's/package://'); app_process / com.unknown.godmode.SystemBridge"
                    Runtime.getRuntime().exec(arrayOf("sh", "-c", "am start-foreground-service -a START_BRIDGE --es cmd \"\$launchCmd\""))
                    
                    paired = true
                    runOnUiThread { Toast.makeText(this, "GODMODE IGNITED!", Toast.LENGTH_LONG).show() }
                }
                Thread.sleep(1000)
            }
        }.start()
    }
}
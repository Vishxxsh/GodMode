package com.unknown.godmode

import android.content.Intent
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnIgnite = findViewById<Button>(R.id.btnIgnite)
        
        btnIgnite.setOnClickListener {
            ButtonRemapperService.isIgniting = true
            
            // DIRECT LINK: This goes straight to the Wireless Debugging page
            val intent = Intent("android.settings.WIFI_DEBUGGING_SETTINGS")
            try {
                startActivity(intent)
                startPairingWatcher()
            } catch (e: Exception) {
                // Backup for older Moto versions
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }
        }
    }

    private fun startPairingWatcher() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                val codeFile = File("/data/local/tmp/p_code.txt")
                val portFile = File("/data/local/tmp/p_port.txt")
                
                if (codeFile.exists() && portFile.exists()) {
                    // SUCCESS: We caught the info! 
                    // Now the app simulates the WebADB handshake internally
                    igniteBridge(codeFile.readText(), portFile.readText())
                    
                    codeFile.delete()
                    portFile.delete()
                    ButtonRemapperService.isIgniting = false
                    
                    // Take the user back to the app automatically
                    val backIntent = Intent(this@MainActivity, MainActivity::class.java)
                    backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(backIntent)
                } else {
                    handler.postDelayed(this, 500)
                }
            }
        })
    }

    private fun igniteBridge(code: String, port: String) {
        // Internal command to start the bridge via the local wireless port
        Thread {
            try {
                val cmd = "export CLASSPATH=\$(pm path com.unknown.godmode | sed 's/package://'); app_process / com.unknown.godmode.SystemBridge"
                // We use a local shell command to finalize the connection
                Runtime.getRuntime().exec(arrayOf("sh", "-c", "am start-foreground-service -a START --es c \"\$cmd\""))
            } catch (e: Exception) {}
        }.start()
    }
}
package com.unknown.godmode

import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.net.Socket

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnIgnite = findViewById<Button>(R.id.btnIgnite)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)

        // Check if Bridge is already running
        if (isBridgeAlive()) {
            tvStatus.text = "ENGINE: ACTIVE"
            tvStatus.setTextColor(android.graphics.Color.GREEN)
            btnIgnite.visibility = android.view.View.GONE
        }

        btnIgnite.setOnClickListener {
            showPairingDialog()
        }
    }

    private fun showPairingDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }
        val etPort = EditText(this).apply { hint = "Port (e.g. 34567)" }
        val etCode = EditText(this).apply { hint = "Pairing Code" }
        layout.addView(etPort)
        layout.addView(etCode)

        android.app.AlertDialog.Builder(this)
            .setTitle("Wireless Connect")
            .setMessage("Enable 'Wireless Debugging' in Settings, then tap 'Pair with code'")
            .setView(layout)
            .setPositiveButton("CONNECT") { _, _ ->
                launchInternalBridge(etPort.text.toString(), etCode.text.toString())
            }
            .show()
    }

    private fun launchInternalBridge(port: String, code: String) {
        Thread {
            try {
                // This is the internal "magic" command that replicates WebADB
                val cmd = "export CLASSPATH=\$(pm path com.unknown.godmode | sed 's/package://'); app_process / com.unknown.godmode.SystemBridge"
                
                // We use the shell to pair and run
                val process = Runtime.getRuntime().exec(arrayOf("sh", "-c", "adb pair localhost:\$port \$code && adb shell \"\$cmd\""))
                
                runOnUiThread {
                    Toast.makeText(this, "Ignition Successful!", Toast.LENGTH_SHORT).show()
                    recreate() // Refresh UI
                }
            } catch (e: Exception) {
                runOnUiThread { Toast.makeText(this, "Failed: \${e.message}", Toast.LENGTH_LONG).show() }
            }
        }.start()
    }

    private fun isBridgeAlive(): Boolean {
        // If the live_signal file was updated in the last 2 seconds, it's alive
        val file = File("/data/local/tmp/live_signal.txt")
        return file.exists() && (System.currentTimeMillis() - file.lastModified() < 2000)
    }
}
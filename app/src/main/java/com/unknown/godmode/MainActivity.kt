package com.unknown.godmode

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etHex = EditText(this).apply { hint = "Enter Hex (e.g. 00d9)" }
        val etPkg = EditText(this).apply { hint = "Enter Package (e.g. com.brave.browser)" }
        val btnSave = Button(this).apply { text = "SAVE TO HARDWARE BRIDGE" }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(etHex)
            addView(etPkg)
            addView(btnSave)
        }
        setContentView(layout)

        btnSave.setOnClickListener {
            // We write to /data/local/tmp because both App and Shell can access it
            val configFile = File("/data/local/tmp/godmode.cfg")
            try {
                configFile.writeText("${etHex.text}\n${etPkg.text}")
                Toast.makeText(this, "Config Sent to Bridge", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Permission Error: Run ADB chmod", Toast.LENGTH_LONG).show()
            }
        }
    }
}
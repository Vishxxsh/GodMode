package com.unknown.godmode

import android.content.Intent
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnIgnite).setOnClickListener {
            // Check Overlay Permission
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                return@setOnClickListener
            }

            // Ignite the Radar
            ButtonRemapperService.isIgniting = true
            
            // Jump to Settings
            try {
                startActivity(Intent("android.settings.WIFI_DEBUGGING_SETTINGS"))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset the engine if we come back to the app manually
        if (ButtonRemapperService.isIgniting) {
            Toast.makeText(this, "Radar Active...", Toast.LENGTH_SHORT).show()
        }
    }
}
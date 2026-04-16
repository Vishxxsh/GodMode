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
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                startActivity(intent)
                Toast.makeText(this, "Enable 'Display over other apps' first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            ButtonRemapperService.isIgniting = true
            
            // Try to jump DIRECTLY to Wireless Debugging
            try {
                val intent = Intent("android.settings.WIFI_DEBUGGING_SETTINGS")
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }
        }
    }
}
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
            // 1. Request Overlay Permission
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
                return@setOnClickListener
            }

            // 2. Start Service Radar via Intent
            val serviceIntent = Intent(this, ButtonRemapperService::class.java).apply {
                action = "START_IGNITION"
            }
            startService(serviceIntent)

            // 3. Jump to Settings
            try {
                startActivity(Intent("android.settings.WIFI_DEBUGGING_SETTINGS"))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }
        }
    }
}
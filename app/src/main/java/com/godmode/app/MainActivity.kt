package com.godmode.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val btnEnable = findViewById<Button>(R.id.btnEnable)
        val btnCheck = findViewById<Button>(R.id.btnCheck)

        fun runDiagnostic() {
            val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            
            // 1. Check if the service is REGISTERED in the system
            val installedServices = am.getInstalledAccessibilityServiceList()
            val isPresent = installedServices.any { it.resolveInfo.serviceInfo.packageName == packageName }

            // 2. Check if it is actually ENABLED
            val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            val isEnabled = enabledServices?.contains(packageName) ?: false

            val report = StringBuilder()
            report.append("Package: $packageName\n")
            report.append("System Sees App: ${if (isPresent) "✅ YES" else "❌ NO"}\n")
            report.append("Service Enabled: ${if (isEnabled) "✅ YES" else "❌ NO"}\n")
            
            if (!isPresent) {
                report.append("\nCRITICAL: The OS is ignoring the service metadata. Check Manifest/XML.")
            } else if (!isEnabled) {
                report.append("\nACTION: Service found but needs manual activation in settings.")
            }

            tvStatus.text = report.toString()
        }

        btnEnable.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        btnCheck.setOnClickListener { runDiagnostic() }

        runDiagnostic() // Run immediately on open
    }
}
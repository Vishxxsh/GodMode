package com.unknown.godmode

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
            val installedServices = am.getInstalledAccessibilityServiceList()
            val isPresent = installedServices.any { it.resolveInfo.serviceInfo.packageName == packageName }

            val report = StringBuilder()
            report.append("ID: $packageName\n")
            report.append("System Sees App: ${if (isPresent) "✅ YES" else "❌ NO"}\n\n")
            report.append("RECOGNIZED:\n")
            installedServices.forEach { report.append("- ${it.resolveInfo.serviceInfo.packageName}\n") }
            tvStatus.text = report.toString()
        }

        btnEnable.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        btnCheck.setOnClickListener { runDiagnostic() }
        runDiagnostic()
    }
}

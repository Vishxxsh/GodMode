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
            val pkg = packageName // This should be com.godmode.app
            
            val installedServices = am.getInstalledAccessibilityServiceList()
            val isPresent = installedServices.any { it.resolveInfo.serviceInfo.packageName == pkg }

            val report = StringBuilder()
            report.append("Checking for: $pkg\n")
            report.append("System Sees Service: ${if (isPresent) "✅ YES" else "❌ NO"}\n")
            
            if (!isPresent) {
                report.append("\nDEBUG: Total Accessibility Services found: ${installedServices.size}\n")
                installedServices.take(3).forEach { 
                    report.append("- ${it.resolveInfo.serviceInfo.packageName}\n")
                }
            }

            tvStatus.text = report.toString()
        }

        btnEnable.setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        btnCheck.setOnClickListener { runDiagnostic() }
        runDiagnostic()
    }
}